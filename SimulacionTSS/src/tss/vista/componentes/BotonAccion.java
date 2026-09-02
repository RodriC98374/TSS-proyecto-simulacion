package tss.vista.componentes;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JButton;
import tss.vista.diseno.Medidas;
import tss.vista.diseno.Paleta;
import tss.vista.diseno.Pinceles;
import tss.vista.diseno.Tipografia;

/**
 * Boton pintado a mano en dos variantes: primaria en ambar para la accion
 * principal de cada panel y secundaria de contorno para el resto.
 */
@SuppressWarnings({ "serial", "this-escape" })
public final class BotonAccion extends JButton {

    public enum Variante {
        PRIMARIA, SECUNDARIA
    }

    private final Variante variante;

    public BotonAccion(String texto, Variante variante) {
        super(texto);
        this.variante = variante;
        setFont(Tipografia.interfazFuerte(Tipografia.PEQUENA));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    }

    public static BotonAccion primaria(String texto) {
        return new BotonAccion(texto, Variante.PRIMARIA);
    }

    public static BotonAccion secundaria(String texto) {
        return new BotonAccion(texto, Variante.SECUNDARIA);
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        int ancho = fm.stringWidth(getText()) + Medidas.E6 * 2;
        return new Dimension(Math.max(ancho, 96), Medidas.ALTURA_BOTON);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, Medidas.ALTURA_BOTON);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Pinceles.preparar((Graphics2D) g.create());
        int ancho = getWidth();
        int alto = getHeight();
        boolean activo = isEnabled();
        boolean presionado = getModel().isArmed() && getModel().isPressed();
        boolean encima = getModel().isRollover();

        if (variante == Variante.PRIMARIA) {
            Color fondo = !activo ? Paleta.SUPERFICIE_ALTA
                    : presionado ? Paleta.AMBAR_OSCURO
                    : encima ? Paleta.mezclar(Paleta.AMBAR, Color.WHITE, 0.12)
                    : Paleta.AMBAR;
            Pinceles.rellenar(g2, 0, 0, ancho, alto, Medidas.RADIO_SUAVE, fondo);
            g2.setColor(activo ? Paleta.FONDO : Paleta.TINTA_TENUE);
        } else {
            if (encima && activo) {
                Pinceles.rellenar(g2, 0, 0, ancho, alto, Medidas.RADIO_SUAVE,
                        Paleta.SUPERFICIE_ALTA);
            }
            Pinceles.bordear(g2, 0, 0, ancho, alto, Medidas.RADIO_SUAVE,
                    activo ? Paleta.RETICULA : Paleta.conAlfa(Paleta.RETICULA, 120));
            g2.setColor(!activo ? Paleta.TINTA_TENUE
                    : presionado ? Paleta.AMBAR : Paleta.TINTA);
        }

        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int x = (ancho - fm.stringWidth(getText())) / 2;
        int y = (alto - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(getText(), x, y);

        if (isFocusOwner() && activo) {
            Pinceles.anilloFoco(g2, 1, 1, ancho - 2, alto - 2, Medidas.RADIO_SUAVE);
        }
        g2.dispose();
    }
}
