package tss.modelo.reemplazo;

import java.util.List;

/**
 * Resultado de una corrida del horizonte completo bajo una politica de
 * reemplazo, con la traza de eventos para la corrida visual.
 * Informe: seccion 5.8.
 */
public record ResultadoCorridaReemplazo(
        PoliticaReemplazo politica,
        int intervenciones,
        int componentesConsumidos,
        double horasParo,
        double costoComponentes,
        double costoParo,
        double costoTotal,
        List<EventoFalla> traza) {
}
