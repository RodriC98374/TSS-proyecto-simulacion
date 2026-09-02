package tss.modelo.inventario;

/**
 * Las dos politicas de reabastecimiento que compara el ejercicio: revision
 * periodica cada ocho dias y punto de reorden por nivel.
 * Informe: seccion 5.7 (Aplicacion 3 - Politica de inventario).
 */
public enum PoliticaInventario {

    PERIODICA("Política 1", "Ordenar cada 8 días hasta 30 artículos"),
    PUNTO_REORDEN("Política 2", "Ordenar hasta 30 cuando el inventario baje a 10");

    private final String nombreCorto;
    private final String descripcion;

    PoliticaInventario(String nombreCorto, String descripcion) {
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
