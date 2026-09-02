package tss.modelo.interferencia;

import tss.modelo.aleatorio.MuestraNormal;

/**
 * Un ensamble con el detalle que pide la experimentacion de primera etapa: los
 * doce uniformes de cada pieza, sus z, los diametros y el veredicto.
 * Informe: seccion 5.6.
 */
public record EnsambleSimulado(
        int numero,
        MuestraNormal cojinete,
        MuestraNormal flecha,
        double holgura,
        boolean interferencia) {

    public double x1() {
        return cojinete.valor();
    }

    public double x2() {
        return flecha.valor();
    }
}
