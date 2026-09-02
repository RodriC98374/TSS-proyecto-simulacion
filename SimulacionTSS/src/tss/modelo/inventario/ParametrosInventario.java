package tss.modelo.inventario;

/**
 * Parametros del sistema de inventario y tabla de tiempo de entrega Poisson
 * (lambda = 3) tal como aparece tabulada y redondeada en el informe.
 * Informe: seccion 5.7 (Aplicacion 3 - Politica de inventario).
 */
public record ParametrosInventario(
        int horizonte,
        int inventarioInicial,
        int nivelObjetivo,
        int puntoReorden,
        int periodoRevision,
        double costoMantener,
        double costoFaltante,
        double costoOrdenar,
        int ensayosDemanda,
        double thetaDemanda) {

    /** Acumuladas de la tabla del informe; el ultimo renglon "8 o mas" se trata como 8. */
    public static final double[] ENTREGA_ACUMULADAS = {
            0.0498, 0.1991, 0.4232, 0.6472, 0.8153, 0.9161, 0.9665, 0.9881, 1.0000 };

    public static final double[] ENTREGA_PROBABILIDADES = {
            0.0498, 0.1494, 0.2240, 0.2240, 0.1680, 0.1008, 0.0504, 0.0216, 0.0119 };

    public static final int ENTREGA_MAXIMA = 8;

    public static ParametrosInventario porDefecto() {
        return new ParametrosInventario(365, 30, 30, 10, 8, 1.0, 10.0, 50.0, 6, 0.5);
    }
}
