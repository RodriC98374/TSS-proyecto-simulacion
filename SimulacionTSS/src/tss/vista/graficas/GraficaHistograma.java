package tss.vista.graficas;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.function.IntFunction;
import tss.modelo.rechazo.DensidadPorTramos;
import tss.vista.diseno.Paleta;
import tss.vista.diseno.Pinceles;

/**
 * Histograma de barras contiguas con color por clase, util tanto para los
 * valores aceptados del metodo de rechazo como para la holgura del ejercicio 4,
 * donde el area negativa es la probabilidad buscada.
 */
@SuppressWarnings({ "serial", "this-escape" })
public final class GraficaHistograma extends Grafica {

    private double[] bordes = new double[0];
    private double[] alturas = new double[0];
    private IntFunction<Color> colorClase = clase -> Paleta.AMBAR;
    private DensidadPorTramos superpuesta;

    public void mostrar(double[] bordes, double[] alturas) {
        this.bordes = bordes.clone();
        this.alturas = alturas.clone();
        double mayor = 0.0;
        for (double a : alturas) {
            mayor = Math.max(mayor, a);
        }
        minimoX = bordes[0];
        maximoX = bordes[bordes.length - 1];
        minimoY = 0.0;
        maximoY = mayor <= 0 ? 1.0 : mayor * 1.2;
        marcarConDatos();
        repaint();
    }

    public void colorearPor(IntFunction<Color> colorClase) {
        this.colorClase = colorClase;
        repaint();
    }

    public void superponer(DensidadPorTramos densidad) {
        this.superpuesta = densidad;
        repaint();
    }

    @Override
    protected void dibujarSerie(Graphics2D g2, Rectangle area) {
        for (int i = 0; i < alturas.length; i++) {
            int x1 = px(area, bordes[i]);
            int x2 = px(area, bordes[i + 1]);
            int y = py(area, alturas[i]);
            int base = py(area, 0);
            int ancho = Math.max(x2 - x1 - 1, 1);
            g2.setColor(Paleta.conAlfa(colorClase.apply(i), 190));
            g2.fillRect(x1, y, ancho, base - y);
            g2.setColor(colorClase.apply(i));
            g2.setStroke(Pinceles.FINO);
            g2.drawLine(x1, y, x1 + ancho, y);
        }
        if (superpuesta != null) {
            dibujarDensidadTeorica(g2, area);
        }
    }

    private void dibujarDensidadTeorica(Graphics2D g2, Rectangle area) {
        g2.setColor(Paleta.TINTA);
        g2.setStroke(Pinceles.MEDIO);
        int anterior = -1;
        int anteriorY = 0;
        for (int px = area.x; px <= area.x + area.width; px++) {
            double x = minimoX + (maximoX - minimoX) * (px - area.x) / (double) area.width;
            int py = py(area, superpuesta.evaluar(x));
            if (anterior >= 0) {
                g2.drawLine(anterior, anteriorY, px, py);
            }
            anterior = px;
            anteriorY = py;
        }
    }
}
