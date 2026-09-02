package tss.vista;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import tss.modelo.comun.ProgresoSimulacion;
import tss.vista.componentes.BarraProgreso;
import tss.vista.componentes.PanelParametros;
import tss.vista.diseno.Medidas;
import tss.vista.diseno.Paleta;
import tss.vista.diseno.Tipografia;

/**
 * Anatomia comun de los seis ejercicios: encabezado con titulo y pregunta,
 * columna de parametros a la izquierda, zona de resultados a la derecha y barra
 * de estado al pie. La consistencia hace que se lean como un solo instrumento.
 */
@SuppressWarnings({ "serial", "this-escape" })
public abstract class PanelEjercicio extends JPanel {

    /** Acciones que un panel puede pedir a su controlador. */
    public interface EscuchaPanel {
        void alPedirAccion(String accion);
    }

    public static final String ACCION_VISUAL = "visual";
    public static final String ACCION_EXPERIMENTO = "experimento";
    public static final String ACCION_EXPORTAR = "exportar";
    public static final String ACCION_LIMPIAR = "limpiar";

    protected final PanelParametros parametros = new PanelParametros("Parámetros");
    protected final JPanel resultados = new JPanel();
    protected final BarraProgreso barra = new BarraProgreso();

    private EscuchaPanel escucha;

    protected PanelEjercicio(String titulo, String pregunta) {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(Paleta.FONDO);
        setBorder(BorderFactory.createEmptyBorder(Medidas.E6, Medidas.E8, Medidas.E6, Medidas.E8));

        add(encabezado(titulo, pregunta), BorderLayout.NORTH);

        resultados.setOpaque(false);
        resultados.setLayout(new BorderLayout(0, Medidas.E4));

        JPanel cuerpo = new JPanel(new BorderLayout(Medidas.E4, 0));
        cuerpo.setOpaque(false);
        cuerpo.add(parametros, BorderLayout.WEST);
        cuerpo.add(resultados, BorderLayout.CENTER);

        add(cuerpo, BorderLayout.CENTER);
        add(barra, BorderLayout.SOUTH);
    }

    public void setEscucha(EscuchaPanel escucha) {
        this.escucha = escucha;
    }

    protected void pedir(String accion) {
        if (escucha != null) {
            escucha.alPedirAccion(accion);
        }
    }

    public BarraProgreso barra() {
        return barra;
    }

    public void mostrarProgreso(ProgresoSimulacion progreso) {
        barra.avanzar(progreso.porcentaje(), progreso.mensaje());
    }

    /** Nombre base de los archivos exportados, del estilo "ejercicio3". */
    public abstract String claveArchivo();

    public abstract void limpiar();

    /** Fila de tarjetas de metrica de anchos iguales. */
    protected static JComponent filaMetricas(JComponent... tarjetas) {
        JPanel fila = new JPanel(new java.awt.GridLayout(1, tarjetas.length, Medidas.E3, 0));
        fila.setOpaque(false);
        for (JComponent t : tarjetas) {
            fila.add(t);
        }
        fila.setPreferredSize(new Dimension(100, 92));
        return fila;
    }

    /** Division ajustable con separador de una linea, sin relieve ni flechas. */
    protected static javax.swing.JSplitPane division(JComponent izquierda, JComponent derecha,
            double peso) {
        javax.swing.JSplitPane division = new javax.swing.JSplitPane(
                javax.swing.JSplitPane.HORIZONTAL_SPLIT, izquierda, derecha);
        division.setResizeWeight(peso);
        division.setBorder(null);
        division.setDividerSize(9);
        division.setContinuousLayout(true);
        division.setOpaque(false);
        // La proporcion se fija en el primer dimensionado real, no en el preferido.
        division.addComponentListener(new java.awt.event.ComponentAdapter() {
            private boolean colocado;

            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                if (!colocado && division.getWidth() > 0) {
                    colocado = true;
                    division.setDividerLocation(peso);
                }
            }
        });
        division.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI() {
            @Override
            public javax.swing.plaf.basic.BasicSplitPaneDivider createDefaultDivider() {
                return new javax.swing.plaf.basic.BasicSplitPaneDivider(this) {
                    @Override
                    public void paint(java.awt.Graphics g) {
                        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                        g2.setColor(Paleta.FONDO);
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        g2.setColor(Paleta.RETICULA);
                        g2.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight());
                        g2.dispose();
                    }

                    @Override
                    protected javax.swing.JButton createLeftOneTouchButton() {
                        return null;
                    }

                    @Override
                    protected javax.swing.JButton createRightOneTouchButton() {
                        return null;
                    }
                };
            }
        });
        return division;
    }

    private JComponent encabezado(String titulo, String pregunta) {
        JPanel caja = new JPanel();
        caja.setOpaque(false);
        caja.setLayout(new BoxLayout(caja, BoxLayout.Y_AXIS));
        caja.setBorder(BorderFactory.createEmptyBorder(0, 0, Medidas.E4, 0));

        JLabel etiquetaTitulo = new JLabel(titulo);
        etiquetaTitulo.setFont(Tipografia.interfazFuerte(Tipografia.SUBTITULO));
        etiquetaTitulo.setForeground(Paleta.TINTA);
        etiquetaTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel etiquetaPregunta = new JLabel(pregunta);
        etiquetaPregunta.setFont(Tipografia.interfaz(Tipografia.PEQUENA));
        etiquetaPregunta.setForeground(Paleta.TINTA_SUAVE);
        etiquetaPregunta.setAlignmentX(Component.LEFT_ALIGNMENT);
        etiquetaPregunta.setBorder(BorderFactory.createEmptyBorder(Medidas.E1, 0, Medidas.E3, 0));

        JPanel linea = new JPanel();
        linea.setBackground(Paleta.RETICULA);
        linea.setAlignmentX(Component.LEFT_ALIGNMENT);
        linea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        linea.setPreferredSize(new Dimension(10, 1));

        caja.add(etiquetaTitulo);
        caja.add(etiquetaPregunta);
        caja.add(linea);
        caja.add(Box.createVerticalStrut(0));
        return caja;
    }
}
