package tss.modelo.rechazo;

/**
 * Tramo de una densidad definida por partes: f(x) = pendiente*x + intercepto
 * sobre el intervalo [inicio, fin]. Con pendiente cero representa un escalon.
 * Informe: secciones 5.1 y 5.2 (metodo de rechazo).
 */
public record Tramo(double inicio, double fin, double pendiente, double intercepto, String etiqueta) {

    public static Tramo constante(double inicio, double fin, double valor, String etiqueta) {
        return new Tramo(inicio, fin, 0.0, valor, etiqueta);
    }

    public static Tramo lineal(double inicio, double fin, double pendiente, double intercepto, String etiqueta) {
        return new Tramo(inicio, fin, pendiente, intercepto, etiqueta);
    }

    public double evaluar(double x) {
        return pendiente * x + intercepto;
    }

    public double maximo() {
        return Math.max(evaluar(inicio), evaluar(fin));
    }

    public boolean esConstante() {
        return pendiente == 0.0;
    }
}
