package tss.modelo.aleatorio;

/**
 * Envuelve una FuenteAleatoria y produce las variables no uniformes del informe:
 * discreta inversa, normal por el limite central, binomial y Poisson tabulada.
 * Informe: Parte 2 (generacion de variables aleatorias no uniformes).
 */
public final class GeneradorVariables {

    /** Cantidad de uniformes que exige el teorema del limite central en el informe. */
    public static final int UNIFORMES_POR_NORMAL = 12;

    private final FuenteAleatoria fuente;

    public GeneradorVariables(FuenteAleatoria fuente) {
        this.fuente = fuente;
    }

    public FuenteAleatoria fuente() {
        return fuente;
    }

    public double uniforme() {
        return fuente.siguiente();
    }

    // ═══ CLAVE 6.2a · Transformada inversa para variables discretas ══════════
    // Convencion de intervalos del informe: acum[i-1] <= R < acum[i].
    // ═════════════════════════════════════════════════════════════════════════
    public int discretaPorTransformadaInversa(double[] acumuladas, int[] valores) {
        double r = fuente.siguiente();
        for (int i = 0; i < acumuladas.length; i++) {
            if (r < acumuladas[i]) {
                return valores[i];
            }
        }
        return valores[valores.length - 1];
    }

    // ═══ CLAVE 6.2b · Normal por teorema del limite central ══════════════════
    // z = (R1 + R2 + ... + R12) - 6; x = media + desviacion * z.
    // ═════════════════════════════════════════════════════════════════════════
    public double normalPorTeoremaLimiteCentral(double media, double desviacion) {
        double suma = 0.0;
        for (int i = 0; i < UNIFORMES_POR_NORMAL; i++) {
            suma += fuente.siguiente();
        }
        double z = suma - 6.0;
        return media + desviacion * z;
    }

    /** Igual que la anterior, pero conserva los doce uniformes y el z intermedio. */
    public MuestraNormal generarNormalDetallada(double media, double desviacion) {
        double[] uniformes = new double[UNIFORMES_POR_NORMAL];
        double suma = 0.0;
        for (int i = 0; i < UNIFORMES_POR_NORMAL; i++) {
            uniformes[i] = fuente.siguiente();
            suma += uniformes[i];
        }
        double z = suma - 6.0;
        return new MuestraNormal(uniformes, z, media + desviacion * z);
    }

    // ═══ CLAVE 6.2c · Binomial por ensayos de Bernoulli ══════════════════════
    // Genera exactamente n uniformes y cuenta cuantos resultan menores que theta.
    // ═════════════════════════════════════════════════════════════════════════
    public int binomialPorEnsayosBernoulli(int n, double theta) {
        int exitos = 0;
        for (int i = 0; i < n; i++) {
            if (fuente.siguiente() < theta) {
                exitos++;
            }
        }
        return exitos;
    }

    /** Igual que la anterior, pero conserva los n uniformes para la corrida visual. */
    public double[] uniformesDeBernoulli(int n) {
        double[] valores = new double[n];
        for (int i = 0; i < n; i++) {
            valores[i] = fuente.siguiente();
        }
        return valores;
    }

    /** Poisson tabulada: devuelve el indice del intervalo, que es el valor 0, 1, 2, ... */
    public int poissonPorTransformadaInversa(double[] acumuladas) {
        double r = fuente.siguiente();
        for (int i = 0; i < acumuladas.length; i++) {
            if (r < acumuladas[i]) {
                return i;
            }
        }
        return acumuladas.length - 1;
    }

    /** Busqueda del intervalo para un uniforme ya generado, cuando la traza lo necesita. */
    public static int indiceDeIntervalo(double r, double[] acumuladas) {
        for (int i = 0; i < acumuladas.length; i++) {
            if (r < acumuladas[i]) {
                return i;
            }
        }
        return acumuladas.length - 1;
    }

    /** Valor discreto para un uniforme ya generado, cuando la traza lo necesita. */
    public static int valorDiscreto(double r, double[] acumuladas, int[] valores) {
        return valores[indiceDeIntervalo(r, acumuladas)];
    }
}
