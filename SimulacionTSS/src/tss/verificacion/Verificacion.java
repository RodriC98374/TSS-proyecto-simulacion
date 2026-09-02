package tss.verificacion;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import tss.modelo.aleatorio.FuenteSecuenciaFija;
import tss.modelo.aleatorio.GeneradorCongruencialMixto;
import tss.modelo.aleatorio.GeneradorVariables;
import tss.modelo.comun.ProgresoSimulacion;
import tss.modelo.comun.ResumenEstadistico;
import tss.modelo.interferencia.ParametrosInterferencia;
import tss.modelo.interferencia.ResultadoInterferencia;
import tss.modelo.interferencia.SimuladorInterferencia;
import tss.modelo.inventario.ParametrosInventario;
import tss.modelo.inventario.PoliticaInventario;
import tss.modelo.inventario.ResumenPolitica;
import tss.modelo.inventario.SimuladorInventario;
import tss.modelo.rechazo.DensidadPorTramos;
import tss.modelo.rechazo.IteracionRechazo;
import tss.modelo.rechazo.ResultadoRechazo;
import tss.modelo.rechazo.SimuladorRechazo;
import tss.modelo.reemplazo.ParametrosReemplazo;
import tss.modelo.reemplazo.PoliticaReemplazo;
import tss.modelo.reemplazo.ResumenReemplazo;
import tss.modelo.reemplazo.SimuladorReemplazo;
import tss.modelo.renta.ParametrosRenta;
import tss.modelo.renta.ResumenFlota;
import tss.modelo.renta.SimuladorRenta;

/**
 * Autocomprobacion sin interfaz grafica: reproduce las pruebas de escritorio y
 * los valores de referencia publicados en el informe. Devuelve codigo de salida
 * distinto de cero si alguna comprobacion falla.
 */
public final class Verificacion {

    private static final long SEMILLA = 12345L;
    private static final PrintStream SALIDA =
            new PrintStream(System.out, true, StandardCharsets.UTF_8);

    private final List<Comprobacion> comprobaciones = new ArrayList<>();

    public static void main(String[] args) {
        Verificacion v = new Verificacion();
        long inicio = System.currentTimeMillis();

        v.pruebaGenerador();
        v.pruebaEscritorioRechazo();
        v.pruebaTasasRechazo();
        v.pruebaRenta();
        v.pruebaInterferencia();
        v.pruebaInventario();
        v.pruebaReemplazo();

        boolean todoBien = v.imprimir(System.currentTimeMillis() - inicio);
        System.exit(todoBien ? 0 : 1);
    }

    // ── Prueba 0: calidad del generador ────────────────────────────────────

    private void pruebaGenerador() {
        GeneradorCongruencialMixto g = new GeneradorCongruencialMixto(SEMILLA);
        ResumenEstadistico uniformes = new ResumenEstadistico();
        for (int i = 0; i < 100000; i++) {
            uniformes.agregar(g.siguiente());
        }
        agregar("Generador: media de 100.000 uniformes", 0.5, uniformes.media(), 0.005);
        agregar("Generador: varianza de 100.000 uniformes", 1.0 / 12.0, uniformes.varianza(), 0.002);

        GeneradorVariables gv = new GeneradorVariables(new GeneradorCongruencialMixto(SEMILLA));
        ResumenEstadistico normales = new ResumenEstadistico();
        for (int i = 0; i < 100000; i++) {
            normales.agregar(gv.normalPorTeoremaLimiteCentral(0.0, 1.0));
        }
        agregar("Generador: media de 100.000 normales TLC", 0.0, normales.media(), 0.01);
        agregar("Generador: desviacion de 100.000 normales TLC", 1.0, normales.desviacion(), 0.02);

        GeneradorCongruencialMixto a = new GeneradorCongruencialMixto(SEMILLA);
        GeneradorCongruencialMixto b = new GeneradorCongruencialMixto(SEMILLA);
        boolean iguales = true;
        for (int i = 0; i < 10000 && iguales; i++) {
            iguales = a.siguiente() == b.siguiente();
        }
        agregarBooleano("Generador: misma semilla, misma secuencia", "reproducible", iguales);
    }

    // ── Prueba 1: pruebas de escritorio de los ejercicios 1 y 2 ────────────

    private void pruebaEscritorioRechazo() {
        double[][] esperadoUno = {
                { 1.2, 2, 0.25, 0.3333, 0 },
                { 0.6, 1, 0.75, 1.0000, 1 },
                { 1.5, 2, 0.25, 0.3333, 0 },
                { 1.1, 2, 0.25, 0.3333, 0 },
                { 1.7, 2, 0.25, 0.3333, 1 } };
        double[] secuenciaUno = { 0.60, 0.50, 0.30, 0.50, 0.75, 0.70, 0.55, 0.60, 0.85, 0.20 };
        compararEscritorio("Ejercicio 1", DensidadPorTramos.escalonada(), secuenciaUno, esperadoUno);

        double[][] esperadoDos = {
                { 1.00, 1, 0.7500, 1.00, 1 },
                { 1.50, 2, 0.1875, 0.25, 0 },
                { 1.30, 2, 0.4125, 0.55, 0 },
                { 1.10, 2, 0.6375, 0.85, 0 },
                { 1.20, 2, 0.5250, 0.70, 1 } };
        double[] secuenciaDos = { 0.60, 0.50, 0.90, 0.30, 0.78, 0.70, 0.66, 0.90, 0.72, 0.50 };
        compararEscritorio("Ejercicio 2", DensidadPorTramos.escalonConRampa(), secuenciaDos, esperadoDos);
    }

    private void compararEscritorio(String titulo, DensidadPorTramos densidad,
            double[] secuencia, double[][] esperado) {
        SimuladorRechazo sim = new SimuladorRechazo(densidad, new FuenteSecuenciaFija(secuencia));
        ResultadoRechazo r = sim.simular(esperado.length);
        for (int i = 0; i < esperado.length; i++) {
            IteracionRechazo it = r.traza().get(i);
            String rotulo = titulo + " · escritorio it. " + (i + 1);
            agregar(rotulo + " · x", esperado[i][0], it.x(), 1e-6);
            agregar(rotulo + " · tramo", esperado[i][1], it.indiceTramo() + 1, 1e-9);
            agregar(rotulo + " · f(x)", esperado[i][2], it.fx(), 1e-6);
            agregar(rotulo + " · f(x)/M", esperado[i][3], it.razon(), 5e-4);
            agregarBooleano(rotulo + " · veredicto",
                    esperado[i][4] == 1 ? "Aceptado" : "Rechazado",
                    it.aceptado() == (esperado[i][4] == 1));
        }
    }

    // ── Prueba 2: tasas de aceptacion con 100.000 iteraciones ──────────────

    private void pruebaTasasRechazo() {
        ResultadoRechazo uno = new SimuladorRechazo(DensidadPorTramos.escalonada(),
                new GeneradorCongruencialMixto(SEMILLA)).simular(100000);
        agregar("Ejercicio 1 · tasa de aceptación (100.000 it.)",
                2.0 / 3.0, uno.tasaAceptacion(), 0.005);

        ResultadoRechazo dos = new SimuladorRechazo(DensidadPorTramos.escalonConRampa(),
                new GeneradorCongruencialMixto(SEMILLA)).simular(100000);
        agregar("Ejercicio 2 · tasa de aceptación (100.000 it.)",
                0.80, dos.tasaAceptacion(), 0.005);
    }

    // ── Prueba 3: renta de autos ───────────────────────────────────────────

    private void pruebaRenta() {
        SimuladorRenta sim = new SimuladorRenta(ParametrosRenta.porDefecto(), SEMILLA);
        List<ResumenFlota> resumenes = sim.evaluarFlotas(1, 8, 5000, ProgresoSimulacion.SILENCIOSO);
        ResumenFlota mejor = SimuladorRenta.mejorFlota(resumenes);

        agregarBooleano("Ejercicio 3 · flota óptima", "N = 5", mejor != null && mejor.n() == 5);
        agregarRelativo("Ejercicio 3 · utilidad de N = 5", 93455.04,
                mejor == null ? 0.0 : mejor.utilidadPromedio(), 0.02);
        ResumenFlota cinco = resumenes.get(4);
        agregarRelativo("Ejercicio 3 · ingreso de N = 5", 516971.0, cinco.ingresoPromedio(), 0.02);
        agregarRelativo("Ejercicio 3 · faltantes de N = 5", 154.89, cinco.faltantesPromedio(), 0.02);
        agregarRelativo("Ejercicio 3 · ociosos de N = 5", 350.78, cinco.ociososPromedio(), 0.02);
    }

    // ── Prueba 4: interferencia eje-cojinete ───────────────────────────────

    private void pruebaInterferencia() {
        agregar("Ejercicio 4 · tamaño de muestra n", 8677,
                SimuladorInterferencia.calcularTamanioMuestra(0.34458, 0.01, 1.96), 0.0);
        agregar("Ejercicio 4 · tamaño de muestra conservador", 9604,
                SimuladorInterferencia.calcularTamanioMuestra(0.5, 0.01, 1.96), 0.0);

        SimuladorInterferencia sim =
                new SimuladorInterferencia(ParametrosInterferencia.porDefecto(), SEMILLA);
        ResultadoInterferencia r = sim.simular(8677, ProgresoSimulacion.SILENCIOSO);
        agregar("Ejercicio 4 · p estimada con 8.677 ensambles",
                ParametrosInterferencia.PROBABILIDAD_REFERENCIA, r.probabilidad(), 0.01);
        agregar("Ejercicio 4 · media generada de x1", 1.50, r.mediaCojineteGenerada(), 0.005);
        agregar("Ejercicio 4 · media generada de x2", 1.48, r.mediaFlechaGenerada(), 0.005);
    }

    // ── Prueba 5: politica de inventario ───────────────────────────────────

    private void pruebaInventario() {
        SimuladorInventario sim = new SimuladorInventario(ParametrosInventario.porDefecto(), SEMILLA);
        ResumenPolitica p1 = sim.evaluarPolitica(
                PoliticaInventario.PERIODICA, 5000, ProgresoSimulacion.SILENCIOSO);
        ResumenPolitica p2 = sim.evaluarPolitica(
                PoliticaInventario.PUNTO_REORDEN, 5000, ProgresoSimulacion.SILENCIOSO);

        agregarRelativo("Ejercicio 5 · costo total política 1", 7409.04, p1.costoTotal(), 0.01);
        agregarRelativo("Ejercicio 5 · costo total política 2", 7370.91, p2.costoTotal(), 0.01);
        agregarRelativo("Ejercicio 5 · órdenes por año política 1", 45.00, p1.ordenesPorAnio(), 0.01);
        agregarRelativo("Ejercicio 5 · órdenes por año política 2", 50.56, p2.ordenesPorAnio(), 0.01);
        agregarBooleano("Ejercicio 5 · política más económica", "Política 2",
                p2.costoTotal() < p1.costoTotal());
    }

    // ── Prueba 6: reemplazo de componentes ─────────────────────────────────

    private void pruebaReemplazo() {
        SimuladorReemplazo sim = new SimuladorReemplazo(ParametrosReemplazo.porDefecto(), SEMILLA);
        ResumenReemplazo a = sim.evaluarPolitica(
                PoliticaReemplazo.INDIVIDUAL, 5000, ProgresoSimulacion.SILENCIOSO);
        ResumenReemplazo b = sim.evaluarPolitica(
                PoliticaReemplazo.TOTAL, 5000, ProgresoSimulacion.SILENCIOSO);

        agregarRelativo("Ejercicio 6 · costo total política A", 39156.60, a.costoTotal(), 0.01);
        agregarRelativo("Ejercicio 6 · costo total política B", 39591.00, b.costoTotal(), 0.01);
        agregarRelativo("Ejercicio 6 · intervenciones política A", 130.52, a.intervenciones(), 0.01);
        agregarRelativo("Ejercicio 6 · intervenciones política B", 39.59, b.intervenciones(), 0.01);
        agregarBooleano("Ejercicio 6 · política más económica", "Política A",
                a.costoTotal() < b.costoTotal());
    }

    // ── Infraestructura de comprobacion ────────────────────────────────────

    private void agregar(String nombre, double esperado, double obtenido, double tolerancia) {
        double desviacion = obtenido - esperado;
        comprobaciones.add(new Comprobacion(nombre, formato(esperado), formato(obtenido),
                formatoDesviacion(desviacion), Math.abs(desviacion) <= tolerancia));
    }

    private void agregarRelativo(String nombre, double esperado, double obtenido, double relativa) {
        double desviacion = esperado == 0.0 ? obtenido - esperado : (obtenido - esperado) / esperado;
        comprobaciones.add(new Comprobacion(nombre, formato(esperado), formato(obtenido),
                String.format(java.util.Locale.ROOT, "%+.2f %%", desviacion * 100.0),
                Math.abs(desviacion) <= relativa));
    }

    private void agregarBooleano(String nombre, String esperado, boolean correcto) {
        comprobaciones.add(new Comprobacion(nombre, esperado,
                correcto ? esperado : "distinto", "—", correcto));
    }

    private static String formato(double valor) {
        double absoluto = Math.abs(valor);
        if (absoluto >= 1000.0) {
            return String.format(java.util.Locale.ROOT, "%.2f", valor);
        }
        return String.format(java.util.Locale.ROOT, "%.4f", valor);
    }

    private static String formatoDesviacion(double valor) {
        return String.format(java.util.Locale.ROOT, "%+.5f", valor);
    }

    private boolean imprimir(long milisegundos) {
        int anchoNombre = 10;
        for (Comprobacion c : comprobaciones) {
            anchoNombre = Math.max(anchoNombre, c.nombre.length());
        }
        String formato = "%-" + anchoNombre + "s │ %14s │ %14s │ %12s │ %s%n";
        SALIDA.println();
        SALIDA.printf(formato, "PRUEBA", "ESPERADO", "OBTENIDO", "DESVIACIÓN", "ESTADO");
        SALIDA.println(linea(anchoNombre));

        int fallidas = 0;
        for (Comprobacion c : comprobaciones) {
            if (!c.correcto) {
                fallidas++;
            }
            SALIDA.printf(formato, c.nombre, c.esperado, c.obtenido, c.desviacion,
                    c.correcto ? "OK" : "FALLA");
        }
        SALIDA.println(linea(anchoNombre));
        SALIDA.printf("%d comprobaciones · %d correctas · %d fallidas · %,d ms%n",
                comprobaciones.size(), comprobaciones.size() - fallidas, fallidas, milisegundos);
        SALIDA.println(fallidas == 0
                ? "RESULTADO: todas las comprobaciones pasan."
                : "RESULTADO: hay comprobaciones fallidas.");
        return fallidas == 0;
    }

    private static String linea(int anchoNombre) {
        return "─".repeat(anchoNombre) + "─┼────────────────┼────────────────┼──────────────┼───────";
    }

    private record Comprobacion(String nombre, String esperado, String obtenido,
            String desviacion, boolean correcto) {
    }
}
