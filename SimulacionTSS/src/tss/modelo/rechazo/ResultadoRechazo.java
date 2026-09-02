package tss.modelo.rechazo;

import java.util.List;

/**
 * Resultado inmutable de una corrida del metodo de rechazo: traza de iteraciones,
 * conteos, tasa observada frente a la teorica e histograma de los aceptados.
 * Informe: secciones 5.1 y 5.2.
 */
public record ResultadoRechazo(
        DensidadPorTramos densidad,
        int iteraciones,
        List<IteracionRechazo> traza,
        boolean trazaTruncada,
        int aceptados,
        int rechazados,
        double tasaAceptacion,
        double tasaTeorica,
        double[] bordesHistograma,
        int[] conteosHistograma,
        double mediaAceptados) {

    public double anchoClase() {
        return bordesHistograma.length < 2 ? 1.0 : bordesHistograma[1] - bordesHistograma[0];
    }

    /** Frecuencia relativa por unidad de x, comparable directamente con f(x). */
    public double densidadEmpirica(int clase) {
        if (aceptados == 0) {
            return 0.0;
        }
        return conteosHistograma[clase] / (aceptados * anchoClase());
    }
}
