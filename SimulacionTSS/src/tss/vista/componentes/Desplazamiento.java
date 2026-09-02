package tss.vista.componentes;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.plaf.basic.BasicScrollBarUI;
import tss.vista.diseno.Paleta;
import tss.vista.diseno.Pinceles;

/**
 * Barras de desplazamiento discretas, del mismo material que la mesa: sin
 * flechas, sin relieve y con el pulgar en un neutro apenas mas claro.
 */
public final class Desplazamiento {

    private Desplazamiento() {
    }

    public static JScrollPane envolver(java.awt.Component contenido) {
        JScrollPane panel = new JScrollPane(contenido);
        aplicar(panel);
        return panel;
    }

    public static void aplicar(JScrollPane panel) {
        panel.setBorder(BorderFactory.createEmptyBorder());
        panel.setOpaque(false);
        panel.getViewport().setOpaque(false);
        panel.getVerticalScrollBar().setUnitIncrement(18);
        panel.getHorizontalScrollBar().setUnitIncrement(18);
        estilizar(panel.getVerticalScrollBar());
        estilizar(panel.getHorizontalScrollBar());
        panel.setCorner(JScrollPane.LOWER_RIGHT_CORNER, transparente());
    }

    private static void estilizar(JScrollBar barra) {
        barra.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                trackColor = Paleta.FONDO;
                thumbColor = Paleta.RETICULA;
            }

            @Override
            protected JButton createDecreaseButton(int orientacion) {
                return botonNulo();
            }

            @Override
            protected JButton createIncreaseButton(int orientacion) {
                return botonNulo();
            }

            @Override
            protected void paintTrack(Graphics g, javax.swing.JComponent c,
                    java.awt.Rectangle limites) {
                g.setColor(Paleta.conAlfa(Paleta.FONDO, 140));
                g.fillRect(limites.x, limites.y, limites.width, limites.height);
            }

            @Override
            protected void paintThumb(Graphics g, javax.swing.JComponent c,
                    java.awt.Rectangle limites) {
                Graphics2D g2 = Pinceles.preparar((Graphics2D) g.create());
                Color color = isDragging ? Paleta.TINTA_TENUE : Paleta.RETICULA;
                Pinceles.rellenar(g2, limites.x + 2, limites.y + 2,
                        limites.width - 4, limites.height - 4, 3, color);
                g2.dispose();
            }
        });
        barra.setPreferredSize(new Dimension(10, 10));
        barra.setOpaque(false);
    }

    private static JButton botonNulo() {
        JButton boton = new JButton();
        boton.setPreferredSize(new Dimension(0, 0));
        boton.setMinimumSize(new Dimension(0, 0));
        boton.setMaximumSize(new Dimension(0, 0));
        return boton;
    }

    private static javax.swing.JComponent transparente() {
        javax.swing.JPanel panel = new javax.swing.JPanel();
        panel.setOpaque(false);
        return panel;
    }
}
