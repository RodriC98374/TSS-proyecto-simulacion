package tss.modelo.reemplazo;

/**
 * Las dos politicas de mantenimiento que compara el ejercicio: reemplazar solo
 * el componente que falla, o reemplazar los cuatro en cada intervencion.
 * Informe: seccion 5.8 (Aplicacion 4 - Reemplazo de componentes).
 */
public enum PoliticaReemplazo {

    INDIVIDUAL("Política A", "Reemplazar solo el componente que falla"),
    TOTAL("Política B", "Reemplazar los cuatro componentes en cada falla");

    private final String nombreCorto;
    private final String descripcion;

    PoliticaReemplazo(String nombreCorto, String descripcion) {
        this.nombreCorto = nombreCorto;
        this.descripcion = descripcion;
    }

    public String nombreCorto() {
        return nombreCorto;
    }

    public String descripcion() {
        return descripcion;
    }
}
