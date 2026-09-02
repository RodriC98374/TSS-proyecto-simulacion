package tss.modelo.aleatorio;

/**
 * Generador congruencial mixto X(n+1) = (a*X(n) + c) mod m, con R = X/m.
 * Las constantes por defecto cumplen Hull-Dobell: el periodo es maximo, 2^32.
 * Informe: Parte 1 (generacion de numeros pseudoaleatorios).
 */
public final class GeneradorCongruencialMixto implements FuenteAleatoria {

    public static final long A_POR_DEFECTO = 1664525L;
    public static final long C_POR_DEFECTO = 1013904223L;
    public static final long M_POR_DEFECTO = 4294967296L; // 2^32
    public static final long SEMILLA_POR_DEFECTO = 12345L;

    private final long a;
    private final long c;
    private final long m;
    private final long semillaInicial;

    private long estado;
    private long generados;

    public GeneradorCongruencialMixto() {
        this(SEMILLA_POR_DEFECTO);
    }

    public GeneradorCongruencialMixto(long semilla) {
        this(semilla, A_POR_DEFECTO, C_POR_DEFECTO, M_POR_DEFECTO);
    }

    public GeneradorCongruencialMixto(long semilla, long a, long c, long m) {
        this.semillaInicial = Math.floorMod(semilla, m);
        this.a = a;
        this.c = c;
        this.m = m;
        this.estado = this.semillaInicial;
    }

    // ═══ CLAVE 6.1 · Generador congruencial mixto ════════════════════════════
    // Recurrencia X(n+1) = (a*X(n) + c) mod m con R(n+1) = X(n+1)/m.
    // ═════════════════════════════════════════════════════════════════════════
    @Override
    public double siguiente() {
        // La mascara equivale a "mod 2^32" y evita el desbordamiento de long.
        estado = (m == M_POR_DEFECTO)
                ? ((a * estado + c) & 0xFFFFFFFFL)
                : Math.floorMod(a * estado + c, m);
        generados++;
        // Los bits bajos de un congruencial tienen periodo corto: por eso siempre
        // se compara el valor real en [0,1) y nunca un bit individual de X.
        return (double) estado / (double) m;
    }

    public void reiniciar() {
        estado = semillaInicial;
        generados = 0;
    }

    @Override
    public long semillaInicial() {
        return semillaInicial;
    }

    @Override
    public long generados() {
        return generados;
    }

    public long a() {
        return a;
    }

    public long c() {
        return c;
    }

    public long m() {
        return m;
    }

    public long estadoActual() {
        return estado;
    }

    /** Semilla derivada determinista para la corrida indicada de un experimento. */
    public static long semillaDerivada(long semillaBase, int indiceCorrida) {
        return semillaBase + (long) indiceCorrida * 7919L;
    }
}
