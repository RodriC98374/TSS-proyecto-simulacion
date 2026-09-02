package tss.modelo.reemplazo;

/**
 * Parametros del equipo de cuatro componentes: vida normal, horizonte y costos
 * de componente y de hora de desconexion.
 * Informe: seccion 5.8 (Aplicacion 4 - Reemplazo de componentes).
 */
public record ParametrosReemplazo(
        int componentes,
        double horizonte,
        double mediaVida,
        double desviacionVida,
        double costoComponente,
        double costoHoraParo,
        double horasParoIndividual,
        double horasParoTotal) {

    public static ParametrosReemplazo porDefecto() {
        return new ParametrosReemplazo(4, 20000.0, 600.0, 100.0, 200.0, 100.0, 1.0, 2.0);
    }
}
