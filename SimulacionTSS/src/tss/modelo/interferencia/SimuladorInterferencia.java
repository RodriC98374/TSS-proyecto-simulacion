package tss.modelo.interferencia;

import java.util.ArrayList;
import java.util.List;
import tss.modelo.aleatorio.GeneradorCongruencialMixto;
import tss.modelo.aleatorio.GeneradorVariables;
import tss.modelo.aleatorio.MuestraNormal;
import tss.modelo.comun.ProgresoSimulacion;

/**
 * Genera ensambles de cojinete y flecha por el teorema del limite central, estima
 * la probabilidad de interferencia y el tamano de muestra para un error dado.
 * Informe: seccion 5.6 (Aplicacion 2 - Interferencia eje-cojinete).
 */
public final class SimuladorInterferencia {

    /** Tope de ensambles cuyo detalle de doce uniformes se conserva. */
    public static final int MAXIMO_TRAZA = 500;

    private static final int CLASES_HOLGURA = 40;

    private final ParametrosInterferencia parametros;
    private final long semilla;

    private long uniformesConsumidos;

    public SimuladorInterferencia(ParametrosInterferencia parametros, long semilla) {
        this.parametros = parametros;
        this.semilla = semilla;
    }

    public long uniformesConsumidos() {
        return uniformesConsumidos;
    }

    // ═══ CLAVE 5.6 · Algoritmo de resolucion — Interferencia eje-cojinete ════
    // Cada ensamble genera x1 y x2 con doce uniformes cada uno y compara la
    // holgura x1 - x2 contra cero.
    // ═════════════════════════════════════════════════════════════════════════
    public ResultadoInterferencia simular(int ensambles, ProgresoSimulacion.Monitor monitor) {
        GeneradorVariables generador = new GeneradorVariables(new GeneradorCongruencialMixto(semilla));
        boolean guardarTraza = ensambles <= MAXIMO_TRAZA;
        List<EnsambleSimulado> traza = guardarTraza ? new ArrayList<>(ensambles) : List.of();

        double[] bordes = bordesHolgura();
        int[] conteos = new int[CLASES_HOLGURA];
        List<Integer> marcasX = new ArrayList<>();
        List<Double> marcasY = new ArrayList<>();

        int conInterferencia = 0;
        double sumaX1 = 0.0;
        double sumaX2 = 0.0;
        int proximaMarca = 1;
        int hechos = 0;

        for (int i = 1; i <= ensambles; i++) {
            if (monitor.cancelado()) {
                break;
            }
            MuestraNormal cojinete = generador.generarNormalDetallada(
                    parametros.mediaCojinete(), parametros.desviacionCojinete());
            MuestraNormal flecha = generador.generarNormalDetallada(
                    parametros.mediaFlecha(), parametros.desviacionFlecha());
            double holgura = cojinete.valor() - flecha.valor();
            boolean interfiere = holgura < 0.0;

            hechos = i;
            sumaX1 += cojinete.valor();
            sumaX2 += flecha.valor();
            if (interfiere) {
                conInterferencia++;
            }
            conteos[claseDe(holgura, bordes)]++;
            if (guardarTraza) {
                traza.add(new EnsambleSimulado(i, cojinete, flecha, holgura, interfiere));
            }
            if (i >= proximaMarca) {
                marcasX.add(i);
                marcasY.add((double) conInterferencia / i);
                proximaMarca = Math.max(i + 1, (int) Math.ceil(i * 1.06));
            }
            if (i % 2000 == 0 || i == ensambles) {
                monitor.reportar(new ProgresoSimulacion(i, ensambles, "Ensamble " + i));
            }
        }

        uniformesConsumidos = generador.fuente().generados();
        double probabilidad = hechos == 0 ? 0.0 : (double) conInterferencia / hechos;
        return armarResultado(hechos, conInterferencia, probabilidad, sumaX1, sumaX2,
                traza, marcasX, marcasY, bordes, conteos);
    }

    // ═══ CLAVE 5.6b · Tamano de muestra para un error dado ═══════════════════
    // n >= z^2 * p * (1 - p) / e^2, redondeado hacia arriba.
    // ═════════════════════════════════════════════════════════════════════════
    public static int calcularTamanioMuestra(double p, double error, double z) {
        double n = (z * z * p * (1.0 - p)) / (error * error);
        return (int) Math.ceil(n);
    }

    private ResultadoInterferencia armarResultado(int ensambles, int conInterferencia,
            double probabilidad, double sumaX1, double sumaX2, List<EnsambleSimulado> traza,
            List<Integer> marcasX, List<Double> marcasY, double[] bordes, int[] conteos) {
        int[] ejeX = new int[marcasX.size()];
        double[] ejeY = new double[marcasY.size()];
        for (int i = 0; i < ejeX.length; i++) {
            ejeX[i] = marcasX.get(i);
            ejeY[i] = marcasY.get(i);
        }
        double referencia = ParametrosInterferencia.PROBABILIDAD_REFERENCIA;
        int requerido = calcularTamanioMuestra(referencia,
                ParametrosInterferencia.ERROR_OBJETIVO, ParametrosInterferencia.Z_95);
        return new ResultadoInterferencia(parametros, ensambles, conInterferencia, probabilidad,
                referencia, Math.abs(probabilidad - referencia),
                ensambles == 0 ? 0.0 : sumaX1 / ensambles,
                ensambles == 0 ? 0.0 : sumaX2 / ensambles,
                requerido, List.copyOf(traza), ejeX, ejeY, bordes, conteos);
    }

    /** Clases centradas en la holgura teorica; el cero cae siempre en un borde. */
    private double[] bordesHolgura() {
        double sigma = parametros.desviacionHolgura();
        double ancho = 8.0 * sigma / CLASES_HOLGURA;
        double inferior = ancho * Math.floor((parametros.mediaHolgura() - 4.0 * sigma) / ancho);
        double[] bordes = new double[CLASES_HOLGURA + 1];
        for (int i = 0; i <= CLASES_HOLGURA; i++) {
            bordes[i] = inferior + ancho * i;
        }
        return bordes;
    }

    private static int claseDe(double holgura, double[] bordes) {
        double ancho = bordes[1] - bordes[0];
        int clase = (int) Math.floor((holgura - bordes[0]) / ancho);
        return Math.min(Math.max(clase, 0), bordes.length - 2);
    }
}
