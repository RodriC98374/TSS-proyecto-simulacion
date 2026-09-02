package tss.vista.componentes;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import tss.vista.diseno.Medidas;
import tss.vista.diseno.Paleta;
import tss.vista.diseno.Pinceles;
import tss.vista.diseno.Tipografia;

/**
 * Selector segmentado de dos a cuatro opciones excluyentes, pintado a mano.
 * La opcion activa se marca con fondo elevado y una barra ambar inferior.
 */
@SuppressWarnings({ "serial", "this-escape" })
public final class SelectorOpcion extends JComponent {

    private final String[] opciones;
    private final List<Runnable> escuchas = new ArrayList<>();
    private int seleccion;
    private int bajoCursor = -1;

    public SelectorOpcion(String... opciones) {
        this.opciones = opciones.clone();
        setFont(Tipografia.interfaz(Tipografia.PEQUENA));
        setFocusable(true);
        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                seleccionar(indiceEn(e.getX()));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                bajoCursor = -1;
                repaint();
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                bajoCursor = indiceEn(e.getX());
                repaint();
            }
        });
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    seleccionar(Math.max(0, seleccion - 1));
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    seleccionar(Math.min(opciones.length - 1, seleccion + 1));
                }
            }
        });
        addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                repaint();
            }
        });
    }

    public int seleccionado() {
        return seleccion;
    }

    public String textoSeleccionado() {
        return opciones[seleccion];
    }

    public void seleccionar(int indice) {
        if (indice < 0 || indice >= opciones.length || indice == seleccion) {
            return;
        }
        seleccion = indice;
        repaint();
        escuchas.forEach(Runnable::run);
    }

    public void alCambiar(Runnable escucha) {
        escuchas.add(escucha);
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        int ancho = 0;
        for (String o : opciones) {
            ancho = Math.max(ancho, fm.stringWidth(o) + Medidas.E4);
        }
        return new Dimension(ancho * opciones.length, Medidas.ALTURA_CONTROL);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, Medidas.ALTURA_CONTROL);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Pinceles.preparar((Graphics2D) g.create());
        int alto = getHeight();
        Pinceles.rellenar(g2, 0, 0, getWidth(), alto, Medidas.RADIO_PLANO, Paleta.SUPERFICIE);
        Pinceles.bordear(g2, 0, 0, getWidth(), alto, Medidas.RADIO_PLANO, Paleta.RETICULA);

        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        for (int i = 0; i < opciones.length; i++) {
            int x = inicioDe(i);
            int ancho = finDe(i) - x;
            if (i == seleccion) {
                Pinceles.rellenar(g2, x, 1, ancho, alto - 2, Medidas.RADIO_PLANO,
                        Paleta.SUPERFICIE_ALTA);
                g2.setColor(Paleta.AMBAR);
                g2.fillRect(x, alto - 3, ancho, 2);
            } else if (i == bajoCursor && isEnabled()) {
                Pinceles.rellenar(g2, x, 1, ancho, alto - 2, Medidas.RADIO_PLANO,
                        Paleta.conAlfa(Paleta.SUPERFICIE_ALTA, 120));
            }
            if (i > 0) {
                Pinceles.separadorVertical(g2, x, 1, alto - 1);
            }
            g2.setColor(i == seleccion ? Paleta.TINTA : Paleta.TINTA_SUAVE);
            int tx = x + (ancho - fm.stringWidth(opciones[i])) / 2;
            g2.drawString(opciones[i], tx, (alto - fm.getHeight()) / 2 + fm.getAscent());
        }
        if (isFocusOwner()) {
            Pinceles.anilloFoco(g2, 1, 1, getWidth() - 2, alto - 2, Medidas.RADIO_PLANO);
        }
        g2.dispose();
    }

    private int inicioDe(int indice) {
        return getWidth() * indice / opciones.length;
    }

    private int finDe(int indice) {
        return getWidth() * (indice + 1) / opciones.length;
    }

    private int indiceEn(int x) {
        int indice = x * opciones.length / Math.max(getWidth(), 1);
        return Math.min(Math.max(indice, 0), opciones.length - 1);
    }
}
