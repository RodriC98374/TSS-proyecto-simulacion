package tss.modelo.renta;

/**
 * Traza de un dia de operacion de la flota, con los valores que la corrida
 * visual muestra en pantalla.
 * Informe: seccion 5.5.
 */
public record DiaRenta(
        int dia,
        int disponiblesInicio,
        int retornos,
        double r1,
        int demanda,
        int rentados,
        int faltantes,
        int ociosos,
        double ingresoDia) {
}
