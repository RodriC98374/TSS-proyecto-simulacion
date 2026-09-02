package tss.modelo.reemplazo;

/**
 * Promedios de multiples corridas para una politica de reemplazo. Son las siete
 * filas de la tabla comparativa del informe.
 * Informe: seccion 5.8.
 */
public record ResumenReemplazo(
        PoliticaReemplazo politica,
        int corridas,
        double intervenciones,
        double componentesConsumidos,
        double horasParo,
        double costoComponentes,
        double costoParo,
        double costoTotal,
        double costoPorHora) {
}
