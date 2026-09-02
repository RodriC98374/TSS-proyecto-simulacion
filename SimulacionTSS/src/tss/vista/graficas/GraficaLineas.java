package tss.vista.graficas;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import tss.vista.diseno.Formatos;
import tss.vista.diseno.Medidas;
import tss.vista.diseno.Paleta;
import tss.vista.diseno.Pinceles;
import tss.vista.diseno.Tipografia;

/**
 * Grafica de lineas con marcadores opcionales, marca sobre el maximo, linea de
 * referencia horizontal y sombreado de la zona negativa. La usan el barrido de
 * flotas del ejercicio 3 y el inventario dia a dia del ejercicio 5.
 */
@SuppressWarnings({ "serial", "this-escape" })
public final class GraficaLineas extends Grafica {

    private final List<Serie> series = new ArrayList<>();
    private boolean marcarMaximo;
    private boolean sombrearNegativo;
    private double referenciaY = Double.NaN;
    private String textoReferencia = "";
    private Color colorReferencia = Paleta.TINTA_TENUE;
    private String formatoEtiqueta = "dinero";

    public void limpiarSeries() {
        series.clear();
        marcarMaximo = false;
        sombrearNegativo = false;
        referenciaY = Double.NaN;
        limpiarLeyenda();
        reiniciar();
    }

    public void agregarSerie(String nombre, double[] x, double[] y, Color color, boolean conPuntos) {
        series.add(new Serie(nombre, x.clone(), y.clone(), color, conPuntos));
        if (!nombre.isEmpty()) {
            agregarLeyenda(nombre, color);
        }
    }

    public void marcarMaximo(boolean activo, String formato) {
        this.marcarMaximo = activo;
        this.formatoEtiqueta = formato;
    }

    public void sombrearNegativo(boolean activo) {
        this.sombrearNegativo = activo;
    }

    public void lineaReferencia(double valor, String texto, Color color) {
        this.referenciaY = valor;
        this.textoReferencia = texto;
        this.colorReferencia = color;
    }

    /** Calcula los rangos de ambos ejes a partir de las series cargadas. */
    public void ajustar(int decimalesEjeX, int decimalesEjeY) {
        double menorX = Double.POSITIVE_INFINITY;
        double mayorX = Double.NEGATIVE_INFINITY;
        double menorY = Double.POSITIVE_INFINITY;
        double mayorY = Double.NEGATIVE_INFINITY;
        for (Serie s : series) {
            for (double v : s.x) {
                menorX = Math.min(menorX, v);
                mayorX = Math.max(mayorX, v);
            }
            for (double v : s.y) {
                menorY = Math.min(menorY, v);
                mayorY = Math.max(mayorY, v);
            }
        }
        if (!Double.isNaN(referenciaY)) {
            menorY = Math.min(menorY, referenciaY);
            mayorY = Math.max(mayorY, referenciaY);
        }
        if (series.isEmpty()) {
            return;
        }
        if (series.size() < 2) {
            // Con una sola serie la leyenda no informa nada: el titulo ya la nombra.
            limpiarLeyenda();
        }
        double[] rangoY = rangoAmable(menorY, mayorY);
        minimoX = menorX;
        maximoX = mayorX == menorX ? menorX + 1 : mayorX;
        minimoY = rangoY[0];
        maximoY = rangoY[1];
        decimalesX = decimalesEjeX;
        decimalesY = decimalesEjeY;
        marcarConDatos();
        repaint();
    }

    @Override
    protected void dibujarSerie(Graphics2D g2, Rectangle area) {
        if (sombrearNegativo && minimoY < 0) {
            int cero = py(area, 0);
            int abajo = area.y + area.height;
            g2.setColor(Paleta.conAlfa(Paleta.ROJO, 34));
            g2.fillRect(area.x, cero, area.width, Math.max(abajo - cero, 0));
            g2.setColor(Paleta.conAlfa(Paleta.ROJO, 130));
            g2.setStroke(Pinceles.FINO);
            g2.drawLine(area.x, cero, area.x + area.width, cero);
        }
        if (!Double.isNaN(referenciaY)) {
            int y = py(area, referenciaY);
            g2.setColor(colorReferencia);
            g2.setStroke(Pinceles.PUNTEADO);
            g2.drawLine(area.x, y, area.x + area.width, y);
            if (!textoReferencia.isEmpty()) {
                g2.setFont(Tipografia.interfaz(Tipografia.MENOR));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(textoReferencia,
                        area.x + area.width - fm.stringWidth(textoReferencia) - Medidas.E2, y - 4);
            }
        }
        for (Serie s : series) {
            dibujarTrazo(g2, area, s);
        }
        if (marcarMaximo && !series.isEmpty()) {
            marcarPuntoMaximo(g2, area, series.get(0));
        }
    }

    private void dibujarTrazo(Graphics2D g2, Rectangle area, Serie s) {
        g2.setColor(s.color);
        g2.setStroke(Pinceles.MEDIO);
        for (int i = 1; i < s.x.length; i++) {
            g2.drawLine(px(area, s.x[i - 1]), py(area, s.y[i - 1]),
                    px(area, s.x[i]), py(area, s.y[i]));
        }
        if (s.conPuntos && s.x.length <= 80) {
            for (int i = 0; i < s.x.length; i++) {
                int x = px(area, s.x[i]);
                int y = py(area, s.y[i]);
                g2.setColor(Paleta.SUPERFICIE);
                g2.fillOval(x - 3, y - 3, 7, 7);
                g2.setColor(s.color);
                g2.drawOval(x - 3, y - 3, 6, 6);
            }
        }
    }

    private void marcarPuntoMaximo(Graphics2D g2, Rectangle area, Serie s) {
        int mejor = 0;
        for (int i = 1; i < s.y.length; i++) {
            if (s.y[i] > s.y[mejor]) {
                mejor = i;
            }
        }
        int x = px(area, s.x[mejor]);
        int y = py(area, s.y[mejor]);
        g2.setColor(Paleta.AMBAR);
        g2.setStroke(Pinceles.MEDIO);
        g2.drawOval(x - 6, y - 6, 12, 12);
        g2.fillOval(x - 3, y - 3, 6, 6);

        String etiqueta = "dinero".equals(formatoEtiqueta)
                ? Formatos.dinero(s.y[mejor])
                : Formatos.decimal(s.y[mejor]);
        g2.setFont(Tipografia.cifrasFuerte(Tipografia.MENOR));
        FontMetrics fm = g2.getFontMetrics();
        int ex = Math.min(x + 12, area.x + area.width - fm.stringWidth(etiqueta) - 2);
        g2.setColor(Paleta.AMBAR);
        g2.drawString(etiqueta, ex, y - 10);
    }

    private record Serie(String nombre, double[] x, double[] y, Color color, boolean conPuntos) {
    }
}
