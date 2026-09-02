package tss.vista.diseno;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;

/**
 * Utilidades de dibujo compartidas: calidad de trazo, bordes de un pixel,
 * anillo de foco y trazos punteados. Centralizarlas mantiene identico el
 * acabado de todos los componentes pintados a mano.
 */
public final class Pinceles {

    public static final Stroke FINO = new BasicStroke(1f);
    public static final Stroke MEDIO = new BasicStroke(2f);
    public static final Stroke GRUESO = new BasicStroke(3f);

    public static final Stroke PUNTEADO = new BasicStroke(1f, BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER, 10f, new float[] { 3f, 4f }, 0f);

    public static final Stroke PUNTEADO_MEDIO = new BasicStroke(1.6f, BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER, 10f, new float[] { 5f, 5f }, 0f);

    private Pinceles() {
    }

    public static Graphics2D preparar(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);
        return g2;
    }

    public static void rellenar(Graphics2D g2, int x, int y, int ancho, int alto,
            int radio, Color color) {
        g2.setColor(color);
        if (radio <= 0) {
            g2.fillRect(x, y, ancho, alto);
        } else {
            g2.fillRoundRect(x, y, ancho, alto, radio * 2, radio * 2);
        }
    }

    public static void bordear(Graphics2D g2, int x, int y, int ancho, int alto,
            int radio, Color color) {
        g2.setColor(color);
        g2.setStroke(FINO);
        if (radio <= 0) {
            g2.drawRect(x, y, ancho - 1, alto - 1);
        } else {
            g2.drawRoundRect(x, y, ancho - 1, alto - 1, radio * 2, radio * 2);
        }
    }

    /** Anillo ambar de un pixel que marca el control con el foco de teclado. */
    public static void anilloFoco(Graphics2D g2, int x, int y, int ancho, int alto, int radio) {
        bordear(g2, x - 2, y - 2, ancho + 4, alto + 4, radio + 2, Paleta.AMBAR);
    }

    /** Linea horizontal de un pixel usada como separador de bloques. */
    public static void separador(Graphics2D g2, int x1, int x2, int y, Color color) {
        g2.setColor(color);
        g2.setStroke(FINO);
        g2.drawLine(x1, y, x2, y);
    }

    public static void separadorVertical(Graphics2D g2, int x, int y1, int y2) {
        g2.setColor(Paleta.RETICULA);
        g2.setStroke(FINO);
        g2.drawLine(x, y1, x, y2);
    }
}
