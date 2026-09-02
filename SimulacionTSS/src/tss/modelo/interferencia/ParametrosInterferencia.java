package tss.modelo.interferencia;

/**
 * Medias y desviaciones del diametro interior del cojinete y del diametro de la
 * flecha, mas la probabilidad analitica de referencia del informe.
 * Informe: seccion 5.6 (Aplicacion 2 - Interferencia eje-cojinete).
 */
public record ParametrosInterferencia(
        double mediaCojinete,
        double desviacionCojinete,
        double mediaFlecha,
        double desviacionFlecha) {

    /** P(interferencia) = P(Z < -0,40) para la diferencia D = x1 - x2. */
    public static final double PROBABILIDAD_REFERENCIA = 0.34458;

    public static final double Z_95 = 1.96;
    public static final double ERROR_OBJETIVO = 0.01;

    public static ParametrosInterferencia porDefecto() {
        return new ParametrosInterferencia(1.50, 0.04, 1.48, 0.03);
    }

    public double mediaHolgura() {
        return mediaCojinete - mediaFlecha;
    }

    public double desviacionHolgura() {
        return Math.sqrt(desviacionCojinete * desviacionCojinete
                + desviacionFlecha * desviacionFlecha);
    }
}
