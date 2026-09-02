package tss.modelo.inventario;

/**
 * Promedios de multiples corridas para una politica de inventario. Son las
 * siete filas de la tabla comparativa del informe.
 * Informe: seccion 5.7.
 */
public record ResumenPolitica(
        PoliticaInventario politica,
        int corridas,
        double costoMantener,
        double costoFaltante,
        double costoOrdenar,
        double costoTotal,
        double costoPorDia,
        double ordenesPorAnio,
        double unidadesFaltantesPorAnio) {
}
