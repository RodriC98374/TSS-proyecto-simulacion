package tss.modelo.aleatorio;

/**
 * Detalle de una normal generada por el teorema del limite central: los doce
 * uniformes empleados, el valor tipificado z y el valor final de la variable.
 * Informe: seccion 5.6 (experimentacion de primera etapa).
 */
public record MuestraNormal(double[] uniformes, double z, double valor) {

    public double suma() {
        double s = 0.0;
        for (double u : uniformes) {
            s += u;
        }
        return s;
    }
}
