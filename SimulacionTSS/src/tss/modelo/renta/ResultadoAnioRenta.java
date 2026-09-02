package tss.modelo.renta;

import java.util.List;

/**
 * Resultado de un ano de operacion con un tamano de flota fijo.
 * Informe: seccion 5.5.
 */
public record ResultadoAnioRenta(
        int flota,
        double ingreso,
        int faltantes,
        long ociosos,
        double costoFlota,
        double utilidad,
        List<DiaRenta> traza) {
}
