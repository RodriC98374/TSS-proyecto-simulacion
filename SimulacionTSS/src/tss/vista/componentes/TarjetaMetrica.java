package tss.vista.componentes;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.function.DoubleFunction;
import javax.swing.JComponent;
import javax.swing.Timer;
import tss.vista.diseno.Medidas;
import tss.vista.diseno.Paleta;
import tss.vista.diseno.Pinceles;
import tss.vista.diseno.Tipografia;

/**
 * Tarjeta de metrica: etiqueta menor arriba, cifra grande en monoespaciada y
 * una linea inferior de dos pixeles cuyo color codifica el estado del valor.
 */
@SuppressWarnings({ "serial", "this-escape" })
public final class TarjetaMetrica extends JComponent {

    private static final int DURACION_MS = 350;
    private static final int PASO_MS = 16;

    private final String etiqueta;
    private String texto = "—";
    private String detalle = "";
    private Color estado = Paleta.RETICULA;
    private Timer animacion;

    public TarjetaMetrica(String etiqueta) {
        this.etiqueta = etiqueta;
        setOpaque(false);
    }

    public void mostrarTexto(String valor, Color color) {
        detenerAnimacion();
        this.texto = valor;
        this.estado = color;
        repaint();
    }

    public void mostrarDetalle(String detalle) {
        this.detalle = detalle == null ? "" : detalle;
        repaint();
    }

    /** Unica animacion de la aplicacion: la cifra interpola durante 350 ms. */
    public void mostrarNumeroAnimado(double valor, DoubleFunction<String> formato, Color color) {
        detenerAnimacion();
        this.estado = color;
        final double desde = 0.0;
        final long inicio = System.currentTimeMillis();
        animacion = new Timer(PASO_MS, e -> {
            double avance = Math.min(1.0, (System.currentTimeMillis() - inicio) / (double) DURACION_MS);
            double suave = 1.0 - Math.pow(1.0 - avance, 3.0);
            texto = formato.apply(desde + (valor - desde) * suave);
            repaint();
            if (avance >= 1.0) {
                texto = formato.apply(valor);
                detenerAnimacion();
                repaint();
            }
        });
        animacion.start();
    }

    public void limpiar() {
        detenerAnimacion();
        texto = "—";
        detalle = "";
        estado = Paleta.RETICULA;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(180, 92);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(120, 92);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, 92);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Pinceles.preparar((Graphics2D) g.create());
        int ancho = getWidth();
        int alto = getHeight();
        Pinceles.rellenar(g2, 0, 0, ancho, alto, Medidas.RADIO_SUAVE, Paleta.SUPERFICIE);
        Pinceles.bordear(g2, 0, 0, ancho, alto, Medidas.RADIO_SUAVE, Paleta.RETICULA);

        g2.setFont(Tipografia.interfaz(Tipografia.MENOR));
        g2.setColor(Paleta.TINTA_SUAVE);
        g2.drawString(etiqueta, Medidas.E3, Medidas.E3 + g2.getFontMetrics().getAscent());

        g2.setFont(Tipografia.cifrasFuerte(cuerpoQueCabe(g2, ancho)));
        g2.setColor(Paleta.TINTA);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(texto, Medidas.E3, Medidas.E3 + 18 + fm.getAscent());

        if (!detalle.isEmpty()) {
            g2.setFont(Tipografia.interfaz(Tipografia.MENOR));
            g2.setColor(Paleta.TINTA_TENUE);
            g2.drawString(detalle, Medidas.E3, alto - Medidas.E3);
        }

        g2.setColor(estado);
        g2.fillRect(0, alto - 2, ancho, 2);
        g2.dispose();
    }

    /** Reduce la cifra si no cabe, para que la ventana minima siga siendo legible. */
    private int cuerpoQueCabe(Graphics2D g2, int ancho) {
        int[] escala = { Tipografia.CIFRA_GRANDE, Tipografia.TITULO, Tipografia.SUBTITULO };
        for (int tamanio : escala) {
            g2.setFont(Tipografia.cifrasFuerte(tamanio));
            if (g2.getFontMetrics().stringWidth(texto) <= ancho - Medidas.E3 * 2) {
                return tamanio;
            }
        }
        return Tipografia.CUERPO;
    }

    private void detenerAnimacion() {
        if (animacion != null) {
            animacion.stop();
            animacion = null;
        }
    }
}
