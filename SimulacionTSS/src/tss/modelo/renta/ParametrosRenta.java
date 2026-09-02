package tss.modelo.renta;

/**
 * Parametros economicos y de horizonte del problema de renta de autos, mas las
 * dos tablas discretas del enunciado con sus acumuladas.
 * Informe: seccion 5.5 (Aplicacion 1 - Renta de autos).
 */
public record ParametrosRenta(
        double rentaPorDia,
        double costoFaltante,
        double costoOcioso,
        double costoAnualPorAuto,
        int diasOperacion) {

    public static final double[] DEMANDA_ACUMULADAS = { 0.10, 0.20, 0.45, 0.75, 1.00 };
    public static final int[] DEMANDA_VALORES = { 0, 1, 2, 3, 4 };
    public static final double[] DEMANDA_PROBABILIDADES = { 0.10, 0.10, 0.25, 0.30, 0.25 };

    public static final double[] DURACION_ACUMULADAS = { 0.40, 0.75, 0.90, 1.00 };
    public static final int[] DURACION_VALORES = { 1, 2, 3, 4 };
    public static final double[] DURACION_PROBABILIDADES = { 0.40, 0.35, 0.15, 0.10 };

    public static ParametrosRenta porDefecto() {
        return new ParametrosRenta(350.0, 200.0, 50.0, 75000.0, 365);
    }
}
