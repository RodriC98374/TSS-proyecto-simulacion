package tss.vista;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import tss.vista.diseno.Medidas;
import tss.vista.diseno.Paleta;
import tss.vista.diseno.Pinceles;
import tss.vista.diseno.Tipografia;

/**
 * Ventana de la aplicacion: barra de titulo propia, rail de navegacion a la
 * izquierda y los seis paneles en un CardLayout. Sin decoracion del sistema
 * para que toda la superficie mantenga el mismo material.
 */
@SuppressWarnings({ "serial", "this-escape" })
public final class VentanaPrincipal extends JFrame {

    private static final int BORDE_REDIMENSION = 6;
    private static final int ALTO_BARRA = 44;

    private final CardLayout cartas = new CardLayout();
    private final JPanel contenedor = new JPanel(cartas);
    private final RailNavegacion rail;

    public VentanaPrincipal(String[] titulos, String[] nombres) {
        super("Simulación de sistemas · Actividad 1");
        setUndecorated(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(Medidas.VENTANA_MINIMA_ANCHO, Medidas.VENTANA_MINIMA_ALTO));
        // El tamano inicial no debe salirse del area util de la pantalla.
        Rectangle util = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getMaximumWindowBounds();
        setSize(Math.min(Medidas.VENTANA_INICIAL_ANCHO, util.width),
                Math.min(Medidas.VENTANA_INICIAL_ALTO, util.height));
        setLocationRelativeTo(null);

        rail = new RailNavegacion(titulos, nombres);
        contenedor.setOpaque(true);
        contenedor.setBackground(Paleta.FONDO);

        JPanel cuerpo = new JPanel(new BorderLayout());
        cuerpo.setOpaque(true);
        cuerpo.setBackground(Paleta.FONDO);
        cuerpo.add(rail, BorderLayout.WEST);
        cuerpo.add(contenedor, BorderLayout.CENTER);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setOpaque(true);
        raiz.setBackground(Paleta.FONDO);
        raiz.setBorder(BorderFactory.createLineBorder(Paleta.RETICULA, 1));
        raiz.add(new BarraTitulo(), BorderLayout.NORTH);
        raiz.add(cuerpo, BorderLayout.CENTER);
        setContentPane(raiz);

        Redimensionador redimensionador = new Redimensionador();
        raiz.addMouseListener(redimensionador);
        raiz.addMouseMotionListener(redimensionador);
    }

    public RailNavegacion rail() {
        return rail;
    }

    public void agregarPanel(String clave, PanelEjercicio panel) {
        contenedor.add(panel, clave);
    }

    public void mostrarPanel(String clave) {
        cartas.show(contenedor, clave);
    }

    /** Barra de titulo propia: nombre de la aplicacion y controles de ventana. */
    private final class BarraTitulo extends JPanel {

        private Point origen;

        private BarraTitulo() {
            setLayout(new BorderLayout());
            setOpaque(false);
            setPreferredSize(new Dimension(100, ALTO_BARRA));
            setBorder(BorderFactory.createEmptyBorder(0, Medidas.E6, 0, Medidas.E2));

            JLabel titulo = new JLabel("Simulación de sistemas · Actividad 1");
            titulo.setFont(Tipografia.tituloAplicacion());
            titulo.setForeground(Paleta.TINTA);

            JPanel botones = new JPanel();
            botones.setOpaque(false);
            botones.setLayout(new javax.swing.BoxLayout(botones, javax.swing.BoxLayout.X_AXIS));
            botones.add(new BotonVentana(BotonVentana.Tipo.MINIMIZAR));
            botones.add(Box.createHorizontalStrut(Medidas.E1));
            botones.add(new BotonVentana(BotonVentana.Tipo.MAXIMIZAR));
            botones.add(Box.createHorizontalStrut(Medidas.E1));
            botones.add(new BotonVentana(BotonVentana.Tipo.CERRAR));

            add(titulo, BorderLayout.WEST);
            add(botones, BorderLayout.EAST);

            MouseAdapter arrastre = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    origen = e.getPoint();
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (origen == null || getExtendedState() == Frame.MAXIMIZED_BOTH) {
                        return;
                    }
                    Point pantalla = e.getLocationOnScreen();
                    setLocation(pantalla.x - origen.x, pantalla.y - origen.y);
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        alternarMaximizado();
                    }
                }
            };
            addMouseListener(arrastre);
            addMouseMotionListener(arrastre);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Pinceles.preparar((Graphics2D) g.create());
            Pinceles.rellenar(g2, 0, 0, getWidth(), getHeight(),
                    Medidas.RADIO_PLANO, Paleta.FONDO);
            Pinceles.separador(g2, 0, getWidth(), getHeight() - 1, Paleta.RETICULA);
            g2.dispose();
        }
    }

    private void alternarMaximizado() {
        setExtendedState(getExtendedState() == Frame.MAXIMIZED_BOTH
                ? Frame.NORMAL : Frame.MAXIMIZED_BOTH);
    }

    /** Boton de ventana dibujado a mano: sin iconos externos ni relieve. */
    private final class BotonVentana extends JComponent {

        private enum Tipo {
            MINIMIZAR, MAXIMIZAR, CERRAR
        }

        private final Tipo tipo;
        private boolean bajoCursor;

        private BotonVentana(Tipo tipo) {
            this.tipo = tipo;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
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

                @Override
                public void mousePressed(MouseEvent e) {
                    switch (tipo) {
                        case MINIMIZAR -> setExtendedState(Frame.ICONIFIED);
                        case MAXIMIZAR -> alternarMaximizado();
                        case CERRAR -> dispose();
                    }
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(34, 28);
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Pinceles.preparar((Graphics2D) g.create());
            if (bajoCursor) {
                Pinceles.rellenar(g2, 0, 0, getWidth(), getHeight(), Medidas.RADIO_SUAVE,
                        tipo == Tipo.CERRAR ? Paleta.ROJO : Paleta.SUPERFICIE_ALTA);
            }
            g2.setColor(bajoCursor && tipo == Tipo.CERRAR ? Paleta.FONDO : Paleta.TINTA_SUAVE);
            g2.setStroke(Pinceles.FINO);
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            switch (tipo) {
                case MINIMIZAR -> g2.drawLine(cx - 5, cy, cx + 5, cy);
                case MAXIMIZAR -> g2.drawRect(cx - 5, cy - 5, 10, 10);
                case CERRAR -> {
                    g2.drawLine(cx - 5, cy - 5, cx + 5, cy + 5);
                    g2.drawLine(cx + 5, cy - 5, cx - 5, cy + 5);
                }
            }
            g2.dispose();
        }
    }

    /** Devuelve el redimensionado por los bordes que se pierde al quitar la decoracion. */
    private final class Redimensionador extends MouseAdapter {

        private int zona;
        private Rectangle limitesIniciales;
        private Point origenPantalla;

        @Override
        public void mouseMoved(MouseEvent e) {
            setCursor(Cursor.getPredefinedCursor(cursorDe(zonaDe(e.getPoint()))));
        }

        @Override
        public void mouseExited(MouseEvent e) {
            setCursor(Cursor.getDefaultCursor());
        }

        @Override
        public void mousePressed(MouseEvent e) {
            zona = zonaDe(e.getPoint());
            limitesIniciales = getBounds();
            origenPantalla = e.getLocationOnScreen();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (zona == 0 || getExtendedState() == Frame.MAXIMIZED_BOTH) {
                return;
            }
            Point ahora = e.getLocationOnScreen();
            int dx = ahora.x - origenPantalla.x;
            int dy = ahora.y - origenPantalla.y;
            Rectangle nuevo = new Rectangle(limitesIniciales);
            if ((zona & 1) != 0) {
                nuevo.width = limitesIniciales.width + dx;
            }
            if ((zona & 2) != 0) {
                nuevo.height = limitesIniciales.height + dy;
            }
            if ((zona & 4) != 0) {
                nuevo.x = limitesIniciales.x + dx;
                nuevo.width = limitesIniciales.width - dx;
            }
            nuevo.width = Math.max(nuevo.width, Medidas.VENTANA_MINIMA_ANCHO);
            nuevo.height = Math.max(nuevo.height, Medidas.VENTANA_MINIMA_ALTO);
            setBounds(nuevo);
            validate();
        }

        /** Bits: 1 derecha, 2 abajo, 4 izquierda. */
        private int zonaDe(Point punto) {
            int zonas = 0;
            if (punto.x >= getWidth() - BORDE_REDIMENSION) {
                zonas |= 1;
            }
            if (punto.y >= getHeight() - BORDE_REDIMENSION) {
                zonas |= 2;
            }
            if (punto.x <= BORDE_REDIMENSION) {
                zonas |= 4;
            }
            return zonas;
        }

        private int cursorDe(int zona) {
            return switch (zona) {
                case 1 -> Cursor.E_RESIZE_CURSOR;
                case 2 -> Cursor.S_RESIZE_CURSOR;
                case 3 -> Cursor.SE_RESIZE_CURSOR;
                case 4 -> Cursor.W_RESIZE_CURSOR;
                case 6 -> Cursor.SW_RESIZE_CURSOR;
                default -> Cursor.DEFAULT_CURSOR;
            };
        }
    }
}
