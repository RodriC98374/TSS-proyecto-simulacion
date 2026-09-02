package tss.vista;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import tss.vista.diseno.Formatos;
import tss.vista.diseno.Medidas;
import tss.vista.diseno.Paleta;
import tss.vista.diseno.Pinceles;
import tss.vista.diseno.Tipografia;

/**
 * Rail vertical de navegacion con las seis entradas y, al pie, el estado del
 * generador congruencial compartido por todos los ejercicios. Ese lector es lo
 * que sostiene la idea de un unico instrumento con una sola fuente aleatoria.
 */
@SuppressWarnings({ "serial", "this-escape" })
public final class RailNavegacion extends JPanel {

    private final List<Entrada> entradas = new ArrayList<>();
    private final JLabel lecturaSemilla = new JLabel("—");
    private final JLabel lecturaConsumidos = new JLabel("—");
    private IntConsumer alSeleccionar;
    private int seleccion;

    public RailNavegacion(String[] titulos, String[] nombres) {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(Paleta.FONDO);
        setPreferredSize(new Dimension(Medidas.ANCHO_RAIL, 100));
        setMinimumSize(new Dimension(Medidas.ANCHO_RAIL, 100));

        JPanel lista = new JPanel();
        lista.setOpaque(false);
        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
        lista.setBorder(BorderFactory.createEmptyBorder(Medidas.E3, 0, Medidas.E3, 0));
        for (int i = 0; i < titulos.length; i++) {
            Entrada entrada = new Entrada(i, titulos[i], nombres[i]);
            entradas.add(entrada);
            lista.add(entrada);
        }
        lista.add(Box.createVerticalGlue());

        add(lista, BorderLayout.CENTER);
        add(lectorGenerador(), BorderLayout.SOUTH);
    }

    public void alSeleccionar(IntConsumer accion) {
        this.alSeleccionar = accion;
    }

    public void seleccionar(int indice) {
        if (indice < 0 || indice >= entradas.size()) {
            return;
        }
        seleccion = indice;
        entradas.forEach(JComponent::repaint);
        if (alSeleccionar != null) {
            alSeleccionar.accept(indice);
        }
    }

    public int seleccionado() {
        return seleccion;
    }

    /** Refresca el lector del generador tras cada corrida. */
    public void actualizarGenerador(long semilla, long consumidos) {
        lecturaSemilla.setText(Formatos.entero(semilla));
        lecturaConsumidos.setText(Formatos.entero(consumidos));
    }

    private JComponent lectorGenerador() {
        JPanel caja = new JPanel();
        caja.setOpaque(false);
        caja.setLayout(new BoxLayout(caja, BoxLayout.Y_AXIS));
        caja.setBorder(BorderFactory.createEmptyBorder(Medidas.E4, Medidas.E4,
                Medidas.E4, Medidas.E4));

        JPanel linea = new JPanel();
        linea.setBackground(Paleta.RETICULA);
        linea.setAlignmentX(Component.LEFT_ALIGNMENT);
        linea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        linea.setPreferredSize(new Dimension(10, 1));

        JLabel titulo = new JLabel("Generador congruencial mixto");
        titulo.setFont(Tipografia.interfaz(Tipografia.MENOR));
        titulo.setForeground(Paleta.TINTA_SUAVE);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        titulo.setBorder(BorderFactory.createEmptyBorder(Medidas.E3, 0, Medidas.E2, 0));

        caja.add(linea);
        caja.add(titulo);
        caja.add(fila("a", Formatos.entero(1664525)));
        caja.add(fila("c", Formatos.entero(1013904223)));
        caja.add(fila("m", "2³²"));
        caja.add(fila("semilla", lecturaSemilla));
        caja.add(fila("consumidos", lecturaConsumidos));
        return caja;
    }

    private JComponent fila(String clave, String valor) {
        JLabel etiqueta = new JLabel(valor);
        return fila(clave, etiqueta);
    }

    private JComponent fila(String clave, JLabel valor) {
        JPanel fila = new JPanel(new BorderLayout());
        fila.setOpaque(false);
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));

        JLabel etiqueta = new JLabel(clave);
        etiqueta.setFont(Tipografia.interfaz(Tipografia.MENOR));
        etiqueta.setForeground(Paleta.TINTA_TENUE);

        valor.setFont(Tipografia.cifras(Tipografia.MENOR));
        valor.setForeground(Paleta.TINTA_SUAVE);
        valor.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        fila.add(etiqueta, BorderLayout.WEST);
        fila.add(valor, BorderLayout.EAST);
        return fila;
    }

    /** Entrada del rail: barra ambar de 3 px y fondo elevado cuando esta activa. */
    private final class Entrada extends JComponent {

        private final int indice;
        private final String titulo;
        private final String nombre;
        private boolean bajoCursor;

        private Entrada(int indice, String titulo, String nombre) {
            this.indice = indice;
            this.titulo = titulo;
            this.nombre = nombre;
            setFocusable(true);
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            setAlignmentX(Component.LEFT_ALIGNMENT);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    requestFocusInWindow();
                    seleccionar(indice);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    bajoCursor = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    bajoCursor = false;
                    repaint();
                }
            });
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER) {
                        seleccionar(indice);
                    } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        seleccionar(Math.min(entradas.size() - 1, indice + 1));
                        entradas.get(seleccion).requestFocusInWindow();
                    } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                        seleccionar(Math.max(0, indice - 1));
                        entradas.get(seleccion).requestFocusInWindow();
                    }
                }
            });
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    repaint();
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(Medidas.ANCHO_RAIL, 54);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, 54);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Pinceles.preparar((Graphics2D) g.create());
            boolean activa = indice == seleccion;
            if (activa) {
                Pinceles.rellenar(g2, 0, 0, getWidth(), getHeight(),
                        Medidas.RADIO_PLANO, Paleta.SUPERFICIE_ALTA);
                g2.setColor(Paleta.AMBAR);
                g2.fillRect(0, 0, 3, getHeight());
            } else if (bajoCursor) {
                Pinceles.rellenar(g2, 0, 0, getWidth(), getHeight(),
                        Medidas.RADIO_PLANO, Paleta.SUPERFICIE);
            }

            g2.setFont(Tipografia.interfaz(Tipografia.MENOR));
            g2.setColor(activa ? Paleta.AMBAR : Paleta.TINTA_TENUE);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(titulo, Medidas.E4, 20);

            g2.setFont(activa ? Tipografia.interfazFuerte(Tipografia.CUERPO)
                    : Tipografia.interfaz(Tipografia.CUERPO));
            g2.setColor(activa ? Paleta.TINTA : Paleta.TINTA_SUAVE);
            fm = g2.getFontMetrics();
            g2.drawString(nombre, Medidas.E4, 20 + fm.getAscent() + 2);

            if (isFocusOwner()) {
                Pinceles.bordear(g2, 2, 2, getWidth() - 4, getHeight() - 4,
                        Medidas.RADIO_PLANO, Paleta.AMBAR);
            }
            g2.dispose();
        }
    }
}
