package tss.modelo.rechazo;

import java.util.List;

/**
 * Densidad definida por tramos sobre [a, b], con la cota M del metodo de rechazo.
 * Una sola clase cubre los ejercicios 1 y 2, que solo difieren en sus tramos.
 * Informe: secciones 5.1 y 5.2.
 */
public final class DensidadPorTramos {

    private final String nombre;
    private final List<Tramo> tramos;
    private final double limiteInferior;
    private final double limiteSuperior;
    private final double maximo;

    public DensidadPorTramos(String nombre, List<Tramo> tramos) {
        if (tramos == null || tramos.isEmpty()) {
            throw new IllegalArgumentException("La densidad necesita al menos un tramo");
        }
        this.nombre = nombre;
        this.tramos = List.copyOf(tramos);
        this.limiteInferior = this.tramos.get(0).inicio();
        this.limiteSuperior = this.tramos.get(this.tramos.size() - 1).fin();
        double mayor = Double.NEGATIVE_INFINITY;
        for (Tramo t : this.tramos) {
            mayor = Math.max(mayor, t.maximo());
        }
        this.maximo = mayor;
    }

    /** Ejercicio 1: f(x) = 3/4 en [0,1] y f(x) = 1/4 en (1,2]. */
    public static DensidadPorTramos escalonada() {
        return new DensidadPorTramos("Densidad escalonada", List.of(
                Tramo.constante(0.0, 1.0, 3.0 / 4.0, "3/4"),
                Tramo.constante(1.0, 2.0, 1.0 / 4.0, "1/4")));
    }

    /** Ejercicio 2: f(x) = 3/4 en [0,1] y rampa -(9/8)x + 15/8 en (1, 5/3]. */
    public static DensidadPorTramos escalonConRampa() {
        return new DensidadPorTramos("Escalon y rampa descendente", List.of(
                Tramo.constante(0.0, 1.0, 3.0 / 4.0, "3/4"),
                Tramo.lineal(1.0, 5.0 / 3.0, -9.0 / 8.0, 15.0 / 8.0, "-(9/8)x + 15/8")));
    }

    // ═══ CLAVE 5.1b · Evaluacion de la densidad por tramos ═══════════════════
    // El primer tramo incluye ambos extremos y los siguientes excluyen su inicio,
    // de modo que x = 1 se evalua con el primer tramo, como hace el informe.
    // ═════════════════════════════════════════════════════════════════════════
    public double evaluar(double x) {
        int indice = indiceDeTramo(x);
        return indice < 0 ? 0.0 : tramos.get(indice).evaluar(x);
    }

    public int indiceDeTramo(double x) {
        for (int i = 0; i < tramos.size(); i++) {
            Tramo t = tramos.get(i);
            boolean dentro = (i == 0)
                    ? (x >= t.inicio() && x <= t.fin())
                    : (x > t.inicio() && x <= t.fin());
            if (dentro) {
                return i;
            }
        }
        return -1;
    }

    /** Eficiencia teorica de aceptacion del metodo: 1 / (M * (b - a)). */
    public double eficienciaTeorica() {
        return 1.0 / (maximo * (limiteSuperior - limiteInferior));
    }

    /** Integral de la densidad; debe valer 1 y se muestra como control. */
    public double integral() {
        double area = 0.0;
        for (Tramo t : tramos) {
            double ancho = t.fin() - t.inicio();
            area += ancho * (t.evaluar(t.inicio()) + t.evaluar(t.fin())) / 2.0;
        }
        return area;
    }

    public String nombre() {
        return nombre;
    }

    public List<Tramo> tramos() {
        return tramos;
    }

    public double limiteInferior() {
        return limiteInferior;
    }

    public double limiteSuperior() {
        return limiteSuperior;
    }

    public double maximo() {
        return maximo;
    }
}
