package tss.vista.graficas;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import tss.vista.diseno.Formatos;
import tss.vista.diseno.Medidas;
import tss.vista.diseno.Paleta;
import tss.vista.diseno.Pinceles;
import tss.vista.diseno.Tipografia;

/**
 * Convergencia de la probabilidad estimada contra el numero de ensambles, con eje
 * logaritmico, referencia analitica punteada, banda de error y marca en n.
 * Informe: seccion 5.6.
 */
@SuppressWarnings({ "serial", "this-escape" })
public final class GraficaConvergencia extends Grafica {

    private int[] ensambles = new int[0];
    private double[] probabilidades = new double[0];
    private double referencia;
    private double banda;
    private int marcaVertical;

    public GraficaConvergencia() {
        rotular("Convergencia de la probabilidad estimada", "Ensambles (escala logarítmica)", "p");
        ejeXLogaritmico = true;
        decimalesX = 0;
        decimalesY = 3;
    }

    public void mostrar(int[] ensambles, double[] probabilidades, double referencia,
            double banda, int marcaVertical) {
        this.ensambles = ensambles.clone();
        this.probabilidades = probabilidades.clone();
        this.referencia = referencia;
        this.banda = banda;
        this.marcaVertical = marcaVertical;
        if (ensambles.length == 0) {
            reiniciar();
            return;
        }
        minimoX = Math.max(1, ensambles[0]);
        maximoX = Math.max(ensambles[ensambles.length - 1], minimoX * 10);
        // El eje se fija alrededor de la referencia: las primeras estimaciones
        // oscilan entre 0 y 1 y aplastarian la banda de error si mandaran ellas.
        minimoY = Math.max(0.0, referencia - 15 * banda);
        maximoY = Math.min(1.0, referencia + 15 * banda);
        limpiarLeyenda();
        agregarLeyenda("p estimada", Paleta.AMBAR);
        agregarLeyenda("Valor analítico", Paleta.TINTA_SUAVE);
        marcarConDatos();
        repaint();
    }

    @Override
    protected void dibujarSerie(Graphics2D g2, Rectangle area) {
        int arriba = py(area, referencia + banda);
        int abajo = py(area, referencia - banda);
        g2.setColor(Paleta.conAlfa(Paleta.VERDE, 30));
        g2.fillRect(area.x, arriba, area.width, Math.max(abajo - arriba, 1));

        int yReferencia = py(area, referencia);
        g2.setColor(Paleta.TINTA_SUAVE);
        g2.setStroke(Pinceles.PUNTEADO_MEDIO);
        g2.drawLine(area.x, yReferencia, area.x + area.width, yReferencia);

        g2.setFont(Tipografia.cifras(Tipografia.MENOR));
        g2.drawString(Formatos.probabilidad(referencia), area.x + Medidas.E2, yReferencia - 5);

        if (marcaVertical > 0 && marcaVertical <= maximoX) {
            int x = px(area, marcaVertical);
            g2.setColor(Paleta.conAlfa(Paleta.AZUL, 190));
            g2.setStroke(Pinceles.PUNTEADO);
            g2.drawLine(x, area.y, x, area.y + area.height);
            g2.setFont(Tipografia.interfaz(Tipografia.MENOR));
            String etiqueta = "n = " + Formatos.entero(marcaVertical);
            FontMetrics fm = g2.getFontMetrics();
            int ex = Math.min(x + 5, area.x + area.width - fm.stringWidth(etiqueta) - 2);
            g2.drawString(etiqueta, ex, area.y + 12);
        }

        g2.setColor(Paleta.AMBAR);
        g2.setStroke(Pinceles.MEDIO);
        for (int i = 1; i < ensambles.length; i++) {
            g2.drawLine(px(area, ensambles[i - 1]), py(area, probabilidades[i - 1]),
                    px(area, ensambles[i]), py(area, probabilidades[i]));
        }
    }
}
