package tss.modelo.comun;

/**
 * Acumulador de conteo, media, varianza, minimo y maximo de una serie de
 * observaciones, calculado en una sola pasada con el metodo de Welford.
 * Lo usan los experimentos para promediar corridas independientes.
 */
public final class ResumenEstadistico {

    private long n;
    private double media;
    private double sumaCuadrados;
    private double minimo = Double.POSITIVE_INFINITY;
    private double maximo = Double.NEGATIVE_INFINITY;

    public void agregar(double valor) {
        n++;
        double delta = valor - media;
        media += delta / n;
        sumaCuadrados += delta * (valor - media);
        minimo = Math.min(minimo, valor);
        maximo = Math.max(maximo, valor);
    }

    public long n() {
        return n;
    }

    public double media() {
        return n == 0 ? 0.0 : media;
    }

    public double varianza() {
        return n < 2 ? 0.0 : sumaCuadrados / (n - 1);
    }

    public double desviacion() {
        return Math.sqrt(varianza());
    }

    public double minimo() {
        return n == 0 ? 0.0 : minimo;
    }

    public double maximo() {
        return n == 0 ? 0.0 : maximo;
    }
}
