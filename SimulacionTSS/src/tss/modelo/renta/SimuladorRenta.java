package tss.modelo.renta;

import java.util.ArrayList;
import java.util.List;
import tss.modelo.aleatorio.GeneradorCongruencialMixto;
import tss.modelo.aleatorio.GeneradorVariables;
import tss.modelo.comun.ProgresoSimulacion;
import tss.modelo.comun.ResumenEstadistico;

/**
 * Simula un ano de operacion de la flota de renta y agrega los resultados
 * de multiples corridas independientes para elegir el tamano optimo.
 * Informe: seccion 5.5 (Aplicacion 1 - Renta de autos).
 */
public final class SimuladorRenta {

    private final ParametrosRenta parametros;
    private final long semillaBase;

    private GeneradorVariables generador;
    private boolean guardarTraza;
    private long consumidosPrevios;

    public SimuladorRenta(ParametrosRenta parametros, long semillaBase) {
        this.parametros = parametros;
        this.semillaBase = semillaBase;
        this.generador = new GeneradorVariables(new GeneradorCongruencialMixto(semillaBase));
        this.guardarTraza = true;
    }

    public ParametrosRenta parametros() {
        return parametros;
    }

    public long uniformesConsumidos() {
        return consumidosPrevios + generador.fuente().generados();
    }

    private void reiniciarGenerador(long semilla) {
        consumidosPrevios += generador.fuente().generados();
        generador = new GeneradorVariables(new GeneradorCongruencialMixto(semilla));
    }

    // ═══ CLAVE 5.5 · Algoritmo de resolucion — Renta de autos ════════════════
    // Implementa el pseudocodigo del informe para un ano de 365 dias.
    // ═════════════════════════════════════════════════════════════════════════
    public ResultadoAnioRenta simularAnio(int flota) {
        int dias = parametros.diasOperacion();
        // Se dimensiona a dias+5 (370) para tolerar t + duracion con t=365 y duracion=4.
        int[] retornos = new int[dias + 5];
        List<DiaRenta> traza = guardarTraza ? new ArrayList<>(dias) : List.of();

        int disponibles = flota;
        double ingreso = 0.0;
        int faltantes = 0;
        long ociosos = 0L;

        for (int t = 1; t <= dias; t++) {
            int inicio = disponibles;
            disponibles += retornos[t];
            double r1 = generador.uniforme();
            int demanda = GeneradorVariables.valorDiscreto(
                    r1, ParametrosRenta.DEMANDA_ACUMULADAS, ParametrosRenta.DEMANDA_VALORES);
            int rentados = Math.min(demanda, disponibles);
            int faltanteDia = demanda - rentados;
            faltantes += faltanteDia;

            double ingresoDia = programarRetornos(t, rentados, retornos);
            ingreso += ingresoDia;
            disponibles -= rentados;
            ociosos += disponibles;

            if (guardarTraza) {
                traza.add(new DiaRenta(t, inicio, retornos[t], r1, demanda, rentados,
                        faltanteDia, disponibles, ingresoDia));
            }
        }

        double costoFlota = parametros.costoAnualPorAuto() * flota;
        double utilidad = ingreso
                - parametros.costoFaltante() * faltantes
                - parametros.costoOcioso() * ociosos
                - costoFlota;
        return new ResultadoAnioRenta(flota, ingreso, faltantes, ociosos, costoFlota, utilidad, traza);
    }

    // ═══ CLAVE 5.5c · Programacion de retornos y cobro de la renta ═══════════
    // Cada auto rentado sortea su duracion y se anota el dia en que vuelve.
    // El ingreso se contabiliza el dia en que se firma, no prorrateado.
    // ═════════════════════════════════════════════════════════════════════════
    private double programarRetornos(int dia, int rentados, int[] retornos) {
        double ingresoDia = 0.0;
        for (int j = 1; j <= rentados; j++) {
            int duracion = generador.discretaPorTransformadaInversa(
                    ParametrosRenta.DURACION_ACUMULADAS, ParametrosRenta.DURACION_VALORES);
            retornos[dia + duracion]++;
            ingresoDia += parametros.rentaPorDia() * duracion;
        }
        return ingresoDia;
    }

    // ═══ CLAVE 5.5b · Barrido del tamano de flota ════════════════════════════
    // Repite el ano completo para cada N y promedia las corridas independientes.
    // ═════════════════════════════════════════════════════════════════════════
    public List<ResumenFlota> evaluarFlotas(int minimo, int maximo, int corridas,
            ProgresoSimulacion.Monitor monitor) {
        List<ResumenFlota> resumenes = new ArrayList<>();
        int totalPasos = (maximo - minimo + 1) * corridas;
        int paso = 0;
        guardarTraza = false;

        for (int n = minimo; n <= maximo; n++) {
            ResumenEstadistico ingreso = new ResumenEstadistico();
            ResumenEstadistico faltantes = new ResumenEstadistico();
            ResumenEstadistico ociosos = new ResumenEstadistico();
            ResumenEstadistico utilidad = new ResumenEstadistico();

            for (int c = 0; c < corridas; c++) {
                if (monitor.cancelado()) {
                    guardarTraza = true;
                    return resumenes;
                }
                reiniciarGenerador(GeneradorCongruencialMixto.semillaDerivada(semillaBase, c));
                ResultadoAnioRenta r = simularAnio(n);
                ingreso.agregar(r.ingreso());
                faltantes.agregar(r.faltantes());
                ociosos.agregar(r.ociosos());
                utilidad.agregar(r.utilidad());
                paso++;
                if (paso % 200 == 0 || paso == totalPasos) {
                    monitor.reportar(new ProgresoSimulacion(paso, totalPasos, "Flota de " + n + " autos"));
                }
            }
            resumenes.add(new ResumenFlota(n, corridas, ingreso.media(), faltantes.media(),
                    ociosos.media(), parametros.costoAnualPorAuto() * n, utilidad.media()));
        }
        guardarTraza = true;
        return resumenes;
    }

    /** Corrida visual: un solo ano con traza dia a dia y semilla propia. */
    public ResultadoAnioRenta corridaVisual(int flota) {
        reiniciarGenerador(semillaBase);
        guardarTraza = true;
        return simularAnio(flota);
    }

    public static ResumenFlota mejorFlota(List<ResumenFlota> resumenes) {
        ResumenFlota mejor = null;
        for (ResumenFlota r : resumenes) {
            if (mejor == null || r.utilidadPromedio() > mejor.utilidadPromedio()) {
                mejor = r;
            }
        }
        return mejor;
    }
}
