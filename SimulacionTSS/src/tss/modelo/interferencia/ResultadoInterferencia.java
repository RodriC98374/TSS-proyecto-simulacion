package tss.modelo.interferencia;

import java.util.List;

/**
 * Resultado del experimento: probabilidad estimada, su distancia al valor
 * analitico, la serie de convergencia y el histograma de la holgura.
 * Informe: seccion 5.6.
 */
public record ResultadoInterferencia(
        ParametrosInterferencia parametros,
        int ensambles,
        int conInterferencia,
        double probabilidad,
        double referencia,
        double diferenciaAbsoluta,
        double mediaCojineteGenerada,
        double mediaFlechaGenerada,
        int tamanioMuestraRequerido,
        List<EnsambleSimulado> traza,
        int[] convergenciaEnsambles,
        double[] convergenciaProbabilidad,
        double[] bordesHolgura,
        int[] conteosHolgura) {
}
