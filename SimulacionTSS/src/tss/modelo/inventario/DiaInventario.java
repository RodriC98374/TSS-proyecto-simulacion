package tss.modelo.inventario;

/**
 * Traza de un dia del sistema de inventario, con las columnas de la corrida
 * visual: llegadas, demanda, faltante, posicion y la orden colocada si la hubo.
 * Informe: seccion 5.7.
 */
public record DiaInventario(
        int dia,
        int inventarioInicial,
        int llegadas,
        double[] uniformesDemanda,
        int demanda,
        int faltante,
        int inventarioFinal,
        int posicion,
        boolean ordena,
        int cantidad,
        int tiempoEntrega,
        int diaLlegada) {
}
