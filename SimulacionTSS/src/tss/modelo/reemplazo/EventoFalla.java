package tss.modelo.reemplazo;

/**
 * Una intervencion de mantenimiento: instante en que ocurre, componente que
 * falla, vida generada para el repuesto y costo acumulado hasta ese momento.
 * Informe: seccion 5.8.
 */
public record EventoFalla(
        int numero,
        double instante,
        int componente,
        double vidaGenerada,
        double horasParo,
        int componentesAcumulados,
        double costoAcumulado) {
}
