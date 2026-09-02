package tss.vista.componentes;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import tss.vista.diseno.Medidas;
import tss.vista.diseno.Paleta;
import tss.vista.diseno.Pinceles;
import tss.vista.diseno.Tipografia;

/**
 * Barra de estado al pie de cada panel: avance de la corrida, texto de estado y
 * boton de cancelar. Tambien muestra los avisos temporales de exportacion.
 */
@SuppressWarnings({ "serial", "this-escape" })
public final class BarraProgreso extends JPanel {

    private final Riel riel = new Riel();
    private final JLabel estado = new JLabel("Sin corridas.");
    private final BotonAccion cancelar = BotonAccion.secundaria("Cancelar");
    private Runnable alCancelar;
    private Timer temporal;
    private String estadoPrevio = "Sin corridas.";

    public BarraProgreso() {
        setLayout(new BorderLayout(Medidas.E4, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(Medidas.E3, 0, 0, 0));

        estado.setFont(Tipografia.interfaz(Tipografia.PEQUENA));
        estado.setForeground(Paleta.TINTA_SUAVE);

        cancelar.setEnabled(false);
        cancelar.addActionListener(e -> {
            if (alCancelar != null) {
                alCancelar.run();
            }
        });

        JPanel centro = new JPanel(new BorderLayout(Medidas.E4, 0));
        centro.setOpaque(false);
        centro.add(riel, BorderLayout.CENTER);
        centro.add(estado, BorderLayout.WEST);

        add(centro, BorderLayout.CENTER);
        add(cancelar, BorderLayout.EAST);
    }

    public void alCancelar(Runnable accion) {
        this.alCancelar = accion;
    }

    /** Dispara la cancelacion igual que si se pulsara el boton. */
    public void pedirCancelacion() {
        if (cancelar.isEnabled() && alCancelar != null) {
            alCancelar.run();
        }
    }

    public void iniciar(String mensaje) {
        detenerTemporal();
        riel.activo = true;
        riel.fraccion = 0.0;
        cancelar.setEnabled(true);
        establecerEstado(mensaje);
    }

    public void avanzar(int porcentaje, String mensaje) {
        riel.activo = true;
        riel.fraccion = Math.min(Math.max(porcentaje / 100.0, 0.0), 1.0);
        establecerEstado(mensaje);
    }

    public void terminar(String mensaje) {
        riel.activo = false;
        riel.fraccion = 0.0;
        cancelar.setEnabled(false);
        establecerEstado(mensaje);
    }

    /** Aviso de cuatro segundos, sin dialogos modales. */
    public void avisoTemporal(String mensaje) {
        detenerTemporal();
        estadoPrevio = estado.getText();
        estado.setText(mensaje);
        estado.setForeground(Paleta.VERDE);
        temporal = new Timer(4000, e -> {
            estado.setText(estadoPrevio);
            estado.setForeground(Paleta.TINTA_SUAVE);
            detenerTemporal();
        });
        temporal.setRepeats(false);
        temporal.start();
    }

    public void error(String mensaje) {
        detenerTemporal();
        riel.activo = false;
        cancelar.setEnabled(false);
        estado.setText(mensaje);
        estado.setForeground(Paleta.ROJO);
        repaint();
    }

    private void establecerEstado(String mensaje) {
        estado.setText(mensaje);
        estado.setForeground(Paleta.TINTA_SUAVE);
        repaint();
    }

    private void detenerTemporal() {
        if (temporal != null) {
            temporal.stop();
            temporal = null;
        }
    }

    /** Riel de avance: canal en reticula y relleno ambar sin redondeos. */
    private static final class Riel extends JComponent {

        private boolean activo;
        private double fraccion;

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(200, Medidas.ALTURA_BOTON);
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (!activo) {
                return;
            }
            Graphics2D g2 = Pinceles.preparar((Graphics2D) g.create());
            int alto = 4;
            int y = (getHeight() - alto) / 2;
            Pinceles.rellenar(g2, 0, y, getWidth(), alto, Medidas.RADIO_PLANO, Paleta.RETICULA);
            Pinceles.rellenar(g2, 0, y, (int) (getWidth() * fraccion), alto,
                    Medidas.RADIO_PLANO, Paleta.AMBAR);
            g2.dispose();
        }
    }
}
