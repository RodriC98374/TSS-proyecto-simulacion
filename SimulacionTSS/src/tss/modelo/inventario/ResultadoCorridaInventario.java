package tss.modelo.inventario;

import java.util.List;

/**
 * Costos de una corrida completa del horizonte para una politica, junto con la
 * traza dia a dia cuando se pidio corrida visual.
 * Informe: seccion 5.7.
 */
public record ResultadoCorridaInventario(
        PoliticaInventario politica,
        double costoMantener,
        double costoFaltante,
        double costoOrdenar,
        double costoTotal,
        int ordenes,
        int unidadesFaltantes,
        List<DiaInventario> traza) {
}
