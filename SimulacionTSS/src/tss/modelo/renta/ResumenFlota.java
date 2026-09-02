package tss.modelo.renta;

/**
 * Promedios de multiples corridas independientes para un tamano de flota.
 * Es la fila que el informe lleva a la tabla de resultados del ejercicio 3.
 * Informe: seccion 5.5.
 */
public record ResumenFlota(
        int n,
        int corridas,
        double ingresoPromedio,
        double faltantesPromedio,
        double ociososPromedio,
        double costoFlota,
        double utilidadPromedio) {
}
