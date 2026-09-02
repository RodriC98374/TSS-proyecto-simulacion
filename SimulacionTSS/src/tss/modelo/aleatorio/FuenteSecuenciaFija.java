package tss.modelo.aleatorio;

/**
 * Fuente que devuelve una lista predeterminada de uniformes, en orden y de forma
 * ciclica. Sirve para reproducir las pruebas de escritorio del informe y para el
 * boton "cargar secuencia de la prueba" de los paneles de rechazo.
 */
public final class FuenteSecuenciaFija implements FuenteAleatoria {

    private final double[] valores;
    private int indice;
    private long generados;

    public FuenteSecuenciaFija(double... valores) {
        if (valores == null || valores.length == 0) {
            throw new IllegalArgumentException("La secuencia fija no puede estar vacia");
        }
        this.valores = valores.clone();
    }

    @Override
    public double siguiente() {
        double r = valores[indice];
        indice = (indice + 1) % valores.length;
        generados++;
        return r;
    }

    @Override
    public long semillaInicial() {
        return 0L;
    }

    @Override
    public long generados() {
        return generados;
    }

    public int cantidadValores() {
        return valores.length;
    }
}
