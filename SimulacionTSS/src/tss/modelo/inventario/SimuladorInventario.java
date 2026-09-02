package tss.modelo.inventario;

import java.util.ArrayList;
import java.util.List;
import tss.modelo.aleatorio.GeneradorCongruencialMixto;
import tss.modelo.aleatorio.GeneradorVariables;
import tss.modelo.comun.ProgresoSimulacion;
import tss.modelo.comun.ResumenEstadistico;

/**
 * Simula el horizonte completo del sistema de inventario bajo una politica de
 * reabastecimiento y acumula los tres componentes del costo anual.
 * Informe: seccion 5.7 (Aplicacion 3 - Politica de inventario).
 */
public final class SimuladorInventario {

    private final ParametrosInventario parametros;
    private final long semillaBase;

    private GeneradorVariables generador;
    private boolean guardarTraza;
    private long consumidosPrevios;

    public SimuladorInventario(ParametrosInventario parametros, long semillaBase) {
        this.parametros = parametros;
        this.semillaBase = semillaBase;
        this.generador = new GeneradorVariables(new GeneradorCongruencialMixto(semillaBase));
        this.guardarTraza = true;
    }

    public ParametrosInventario parametros() {
        return parametros;
    }

    public long uniformesConsumidos() {
        return consumidosPrevios + generador.fuente().generados();
    }

    private void reiniciarGenerador(long semilla) {
        consumidosPrevios += generador.fuente().generados();
        generador = new GeneradorVariables(new GeneradorCongruencialMixto(semilla));
    }

    // ═══ CLAVE 5.7 · Algoritmo de resolucion — Politica de inventario ════════
    // Recorre el horizonte dia a dia: recibe llegadas, atiende la demanda,
    // cobra los costos y evalua si corresponde colocar una orden.
    // ═════════════════════════════════════════════════════════════════════════
    public ResultadoCorridaInventario simularCorrida(PoliticaInventario politica) {
        int horizonte = parametros.horizonte();
        // Dimensionado a horizonte+35 para tolerar t + 1 + L con L maximo de 8.
        int[] llegadas = new int[horizonte + 35];
        List<DiaInventario> traza = guardarTraza ? new ArrayList<>(horizonte) : List.of();

        int inventario = parametros.inventarioInicial();
        int pendientes = 0;
        double costoMantener = 0.0;
        double costoFaltante = 0.0;
        double costoOrdenar = 0.0;
        int ordenes = 0;
        int unidadesFaltantes = 0;

        for (int t = 1; t <= horizonte; t++) {
            int llegadaHoy = llegadas[t];
            int inicio = inventario;
            inventario += llegadaHoy;
            pendientes -= llegadaHoy;

            double[] uniformes = guardarTraza ? new double[parametros.ensayosDemanda()] : null;
            int demanda = generarDemandaDiaria(uniformes);
            int disponible = Math.max(inventario, 0);
            int faltante = Math.max(demanda - disponible, 0);
            costoFaltante += parametros.costoFaltante() * faltante;
            unidadesFaltantes += faltante;

            // El inventario puede quedar negativo: son las unidades por surtir,
            // que se cubren solas al sumar la siguiente llegada.
            inventario -= demanda;
            if (inventario > 0) {
                costoMantener += parametros.costoMantener() * inventario;
            }

            int posicion = inventario + pendientes;
            int[] orden = evaluarReabastecimiento(politica, t, posicion, pendientes, llegadas);
            if (orden != null) {
                pendientes += orden[0];
                costoOrdenar += parametros.costoOrdenar();
                ordenes++;
            }
            if (guardarTraza) {
                traza.add(new DiaInventario(t, inicio, llegadaHoy, uniformes, demanda, faltante,
                        inventario, posicion, orden != null, orden == null ? 0 : orden[0],
                        orden == null ? -1 : orden[1], orden == null ? -1 : orden[2]));
            }
        }

        double total = costoMantener + costoFaltante + costoOrdenar;
        return new ResultadoCorridaInventario(politica, costoMantener, costoFaltante,
                costoOrdenar, total, ordenes, unidadesFaltantes, traza);
    }

    // ═══ CLAVE 5.7b · Decision de reabastecimiento ═══════════════════════════
    // Politica 1: por calendario. Politica 2: por punto de reorden y sin orden
    // pendiente. Devuelve {cantidad, tiempo de entrega, dia de llegada} o null.
    // ═════════════════════════════════════════════════════════════════════════
    private int[] evaluarReabastecimiento(PoliticaInventario politica, int dia, int posicion,
            int pendientes, int[] llegadas) {
        boolean ordenar = (politica == PoliticaInventario.PERIODICA)
                ? (dia % parametros.periodoRevision() == 0)
                : (posicion <= parametros.puntoReorden() && pendientes == 0);
        if (!ordenar || posicion >= parametros.nivelObjetivo()) {
            return null;
        }
        int cantidad = parametros.nivelObjetivo() - posicion;
        double r = generador.uniforme();
        int entrega = GeneradorVariables.indiceDeIntervalo(r, ParametrosInventario.ENTREGA_ACUMULADAS);
        int diaLlegada = dia + 1 + entrega;
        llegadas[diaLlegada] += cantidad;
        return new int[] { cantidad, entrega, diaLlegada };
    }

    /** Demanda diaria binomial(n, theta) por ensayos de Bernoulli. */
    private int generarDemandaDiaria(double[] uniformes) {
        if (uniformes == null) {
            return generador.binomialPorEnsayosBernoulli(
                    parametros.ensayosDemanda(), parametros.thetaDemanda());
        }
        int demanda = 0;
        for (int j = 0; j < uniformes.length; j++) {
            uniformes[j] = generador.uniforme();
            if (uniformes[j] < parametros.thetaDemanda()) {
                demanda++;
            }
        }
        return demanda;
    }

    public ResumenPolitica evaluarPolitica(PoliticaInventario politica, int corridas,
            ProgresoSimulacion.Monitor monitor) {
        ResumenEstadistico mantener = new ResumenEstadistico();
        ResumenEstadistico faltante = new ResumenEstadistico();
        ResumenEstadistico ordenar = new ResumenEstadistico();
        ResumenEstadistico total = new ResumenEstadistico();
        ResumenEstadistico ordenes = new ResumenEstadistico();
        ResumenEstadistico unidades = new ResumenEstadistico();
        guardarTraza = false;

        for (int c = 0; c < corridas; c++) {
            if (monitor.cancelado()) {
                break;
            }
            reiniciarGenerador(GeneradorCongruencialMixto.semillaDerivada(semillaBase, c));
            ResultadoCorridaInventario r = simularCorrida(politica);
            mantener.agregar(r.costoMantener());
            faltante.agregar(r.costoFaltante());
            ordenar.agregar(r.costoOrdenar());
            total.agregar(r.costoTotal());
            ordenes.agregar(r.ordenes());
            unidades.agregar(r.unidadesFaltantes());
            if (c % 200 == 0 || c == corridas - 1) {
                monitor.reportar(new ProgresoSimulacion(c + 1, corridas,
                        politica.nombreCorto() + ", corrida " + (c + 1)));
            }
        }
        guardarTraza = true;
        return new ResumenPolitica(politica, corridas, mantener.media(), faltante.media(),
                ordenar.media(), total.media(), total.media() / parametros.horizonte(),
                ordenes.media(), unidades.media());
    }

    /** Corrida visual: horizonte corto con traza dia a dia y semilla base. */
    public ResultadoCorridaInventario corridaVisual(PoliticaInventario politica) {
        reiniciarGenerador(semillaBase);
        guardarTraza = true;
        return simularCorrida(politica);
    }
}
