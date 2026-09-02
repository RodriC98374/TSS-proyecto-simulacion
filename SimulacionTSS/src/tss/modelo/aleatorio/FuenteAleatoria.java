package tss.modelo.aleatorio;

/**
 * Contrato de cualquier fuente de numeros uniformes en [0, 1). Permite cambiar el
 * generador congruencial por una secuencia fija en las pruebas de escritorio.
 * Informe: Parte 1 (generacion de numeros pseudoaleatorios).
 */
public interface FuenteAleatoria {

    double siguiente();

    long semillaInicial();

    long generados();
}
