package tss.modelo.rechazo;

import java.util.ArrayList;
import java.util.List;
import tss.modelo.aleatorio.FuenteAleatoria;
import tss.modelo.comun.ProgresoSimulacion;

/**
 * Aplica el metodo de rechazo de Coss Bu sobre una densidad definida por tramos.
 * Sirve por igual a los ejercicios 1 y 2: solo cambia la densidad recibida.
 * Informe: secciones 5.1 y 5.2.
 */
public final class SimuladorRechazo {

    /** Tope de iteraciones cuya traza se conserva; evita listas enormes con 100000 corridas. */
    public static final int MAXIMO_TRAZA = 20000;

    private static final int CLASES_HISTOGRAMA = 20;

    private final DensidadPorTramos densidad;
    private final FuenteAleatoria fuente;

    public SimuladorRechazo(DensidadPorTramos densidad, FuenteAleatoria fuente) {
        this.densidad = densidad;
        this.fuente = fuente;
    }

    public ResultadoRechazo simular(int iteraciones) {
        return simular(iteraciones, ProgresoSimulacion.SILENCIOSO);
    }

    // ═══ CLAVE 5.1 · Algoritmo de resolucion — metodo de rechazo ═════════════
    // Genera R1 y R2, forma x = a + (b - a)*R1 y acepta cuando R2 <= f(x)/M.
    // ═════════════════════════════════════════════════════════════════════════
    public ResultadoRechazo simular(int iteraciones, ProgresoSimulacion.Monitor monitor) {
        double a = densidad.limiteInferior();
        double b = densidad.limiteSuperior();
        double m = densidad.maximo();

        List<IteracionRechazo> traza = new ArrayList<>(Math.min(iteraciones, MAXIMO_TRAZA));
        int[] conteos = new int[CLASES_HISTOGRAMA];
        int aceptados = 0;
        double sumaAceptados = 0.0;

        for (int i = 1; i <= iteraciones; i++) {
            if (monitor.cancelado()) {
                break;
            }
            double r1 = fuente.siguiente();
            double r2 = fuente.siguiente();
            double x = a + (b - a) * r1;
            int indiceTramo = densidad.indiceDeTramo(x);
            double fx = densidad.evaluar(x);
            double razon = fx / m;
            boolean aceptado = r2 <= razon;

            if (aceptado) {
                aceptados++;
                sumaAceptados += x;
                conteos[claseDe(x, a, b)]++;
            }
            if (traza.size() < MAXIMO_TRAZA) {
                traza.add(new IteracionRechazo(i, r1, r2, x, indiceTramo, fx, razon, aceptado));
            }
            if (i % 2000 == 0 || i == iteraciones) {
                monitor.reportar(new ProgresoSimulacion(i, iteraciones, "Iteracion " + i));
            }
        }

        int rechazados = iteraciones - aceptados;
        double tasa = iteraciones == 0 ? 0.0 : (double) aceptados / iteraciones;
        double media = aceptados == 0 ? 0.0 : sumaAceptados / aceptados;
        return new ResultadoRechazo(densidad, iteraciones, List.copyOf(traza),
                iteraciones > MAXIMO_TRAZA, aceptados, rechazados, tasa,
                densidad.eficienciaTeorica(), bordes(a, b), conteos, media);
    }

    private static int claseDe(double x, double a, double b) {
        int clase = (int) ((x - a) / (b - a) * CLASES_HISTOGRAMA);
        return Math.min(Math.max(clase, 0), CLASES_HISTOGRAMA - 1);
    }

    private static double[] bordes(double a, double b) {
        double[] bordes = new double[CLASES_HISTOGRAMA + 1];
        for (int i = 0; i <= CLASES_HISTOGRAMA; i++) {
            bordes[i] = a + (b - a) * i / CLASES_HISTOGRAMA;
        }
        return bordes;
    }
}
