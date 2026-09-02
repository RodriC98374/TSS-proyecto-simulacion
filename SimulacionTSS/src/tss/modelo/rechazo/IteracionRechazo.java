package tss.modelo.rechazo;

/**
 * Una iteracion del metodo de rechazo con todas las columnas que el informe
 * lleva a la tabla de la prueba de escritorio.
 * Informe: secciones 5.1 y 5.2.
 */
public record IteracionRechazo(
        int numero,
        double r1,
        double r2,
        double x,
        int indiceTramo,
        double fx,
        double razon,
        boolean aceptado) {

    public String textoTramo() {
        return indiceTramo < 0 ? "-" : (indiceTramo + 1) + ".º";
    }

    public String textoResultado() {
        return aceptado
                ? String.format(java.util.Locale.forLanguageTag("es-BO"), "Aceptado, x = %.4f", x)
                : "Rechazado";
    }
}
