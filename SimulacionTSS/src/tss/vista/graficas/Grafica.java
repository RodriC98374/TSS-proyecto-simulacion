package tss.vista.graficas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import tss.vista.diseno.Formatos;
import tss.vista.diseno.Medidas;
import tss.vista.diseno.Paleta;
import tss.vista.diseno.Pinceles;
import tss.vista.diseno.Tipografia;

/**
 * Base de todas las graficas: margenes, ejes con marcas legibles, reticula
 * tenue, leyenda y exportacion a PNG al doble de resolucion para que las
 * capturas del informe salgan nitidas.
 */
@SuppressWarnings({ "serial", "this-escape" })
public abstract class Grafica extends JComponent {

    protected static final int MARGEN_IZQUIERDO = 60;
    protected static final int MARGEN_DERECHO = 20;
    protected static final int MARGEN_SUPERIOR = 50;
    protected static final int MARGEN_INFERIOR = 46;

    private final List<Entrada> leyenda = new ArrayList<>();

    protected double minimoX;
    protected double maximoX = 1.0;
    protected double minimoY;
    protected double maximoY = 1.0;
    protected boolean ejeXLogaritmico;
    /** Las graficas de categorias rotulan bajo cada barra y no usan eje numerico. */
    protected boolean sinMarcasX;
    protected int decimalesX = 2;
    protected int decimalesY = 2;

    private String titulo = "";
    private String rotuloX = "";
    private String rotuloY = "";
    private boolean conDatos;

    protected Grafica() {
        setOpaque(false);
    }

    public void rotular(String titulo, String rotuloX, String rotuloY) {
        this.titulo = titulo;
        this.rotuloX = rotuloX;
        this.rotuloY = rotuloY;
    }

    public void limpiarLeyenda() {
        leyenda.clear();
    }

    public void agregarLeyenda(String texto, Color color) {
        leyenda.add(new Entrada(texto, color));
    }

    protected void marcarConDatos() {
        conDatos = true;
    }

    public boolean tieneDatos() {
        return conDatos;
    }

    public void reiniciar() {
        conDatos = false;
        leyenda.clear();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(420, 280);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(260, 180);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Pinceles.preparar((Graphics2D) g.create());
        pintarTodo(g2, getWidth(), getHeight());
        g2.dispose();
    }

    /** Escribe la grafica en un PNG al doble de escala. */
    public void exportarPNG(File destino) throws IOException {
        int ancho = Math.max(getWidth(), 480);
        int alto = Math.max(getHeight(), 320);
        BufferedImage imagen = new BufferedImage(ancho * 2, alto * 2, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = Pinceles.preparar(imagen.createGraphics());
        g2.scale(2.0, 2.0);
        pintarTodo(g2, ancho, alto);
        g2.dispose();
        ImageIO.write(imagen, "png", destino);
    }

    private void pintarTodo(Graphics2D g2, int ancho, int alto) {
        Pinceles.rellenar(g2, 0, 0, ancho, alto, Medidas.RADIO_PLANO, Paleta.SUPERFICIE);
        Pinceles.bordear(g2, 0, 0, ancho, alto, Medidas.RADIO_PLANO, Paleta.RETICULA);

        if (!titulo.isEmpty()) {
            g2.setFont(Tipografia.interfazFuerte(Tipografia.PEQUENA));
            g2.setColor(Paleta.TINTA);
            g2.drawString(titulo, MARGEN_IZQUIERDO, 20);
        }
        Rectangle area = areaDe(ancho, alto);
        if (area.width <= 10 || area.height <= 10) {
            return;
        }
        if (!conDatos) {
            dibujarVacio(g2, area);
            return;
        }
        dibujarLeyenda(g2, area);
        dibujarReticula(g2, area);
        // La serie se recorta al area de trazado: ningun punto invade los ejes.
        Graphics2D recortado = (Graphics2D) g2.create();
        recortado.clipRect(area.x, area.y, area.width + 1, area.height + 1);
        dibujarSerie(recortado, area);
        recortado.dispose();
        dibujarMarcos(g2, area);
    }

    protected abstract void dibujarSerie(Graphics2D g2, Rectangle area);

    protected Rectangle areaDe(int ancho, int alto) {
        return new Rectangle(MARGEN_IZQUIERDO, MARGEN_SUPERIOR,
                ancho - MARGEN_IZQUIERDO - MARGEN_DERECHO,
                alto - MARGEN_SUPERIOR - MARGEN_INFERIOR);
    }

    protected Rectangle area() {
        return areaDe(getWidth(), getHeight());
    }

    protected int px(Rectangle area, double x) {
        double f = ejeXLogaritmico
                ? (Math.log10(Math.max(x, minimoX)) - Math.log10(minimoX))
                        / (Math.log10(maximoX) - Math.log10(minimoX))
                : (x - minimoX) / (maximoX - minimoX);
        return area.x + (int) Math.round(f * area.width);
    }

    protected int py(Rectangle area, double y) {
        double f = (y - minimoY) / (maximoY - minimoY);
        return area.y + area.height - (int) Math.round(f * area.height);
    }

    private void dibujarVacio(Graphics2D g2, Rectangle area) {
        g2.setFont(Tipografia.interfaz(Tipografia.PEQUENA));
        g2.setColor(Paleta.TINTA_TENUE);
        String aviso = "Sin datos todavía.";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(aviso, area.x + (area.width - fm.stringWidth(aviso)) / 2,
                area.y + area.height / 2);
    }

    private void dibujarReticula(Graphics2D g2, Rectangle area) {
        g2.setFont(Tipografia.cifras(Tipografia.MENOR));
        FontMetrics fm = g2.getFontMetrics();

        // Menos marcas cuando la grafica es baja: las cifras no deben tocarse.
        for (double y : marcas(minimoY, maximoY, Math.max(2, area.height / 46))) {
            int py = py(area, y);
            if (py < area.y - 1 || py > area.y + area.height + 1) {
                continue;
            }
            g2.setColor(Paleta.conAlfa(Paleta.RETICULA, 110));
            g2.setStroke(Pinceles.FINO);
            g2.drawLine(area.x, py, area.x + area.width, py);
            g2.setColor(Paleta.TINTA_TENUE);
            String texto = Formatos.conDecimales(y, decimalesY);
            g2.drawString(texto, area.x - Medidas.E2 - fm.stringWidth(texto), py + 4);
        }

        for (double x : sinMarcasX ? new double[0] : marcasHorizontales()) {
            int px = px(area, x);
            if (px < area.x - 1 || px > area.x + area.width + 1) {
                continue;
            }
            g2.setColor(Paleta.conAlfa(Paleta.RETICULA, 70));
            g2.setStroke(Pinceles.FINO);
            g2.drawLine(px, area.y, px, area.y + area.height);
            g2.setColor(Paleta.TINTA_TENUE);
            g2.drawLine(px, area.y + area.height, px, area.y + area.height + 4);
            String texto = Formatos.conDecimales(x, decimalesX);
            g2.drawString(texto, px - fm.stringWidth(texto) / 2, area.y + area.height + 18);
        }
    }

    private void dibujarMarcos(Graphics2D g2, Rectangle area) {
        g2.setColor(Paleta.RETICULA);
        g2.setStroke(Pinceles.FINO);
        g2.drawLine(area.x, area.y, area.x, area.y + area.height);
        g2.drawLine(area.x, area.y + area.height, area.x + area.width, area.y + area.height);

        g2.setFont(Tipografia.interfaz(Tipografia.MENOR));
        g2.setColor(Paleta.TINTA_SUAVE);
        FontMetrics fm = g2.getFontMetrics();
        if (!rotuloX.isEmpty()) {
            g2.drawString(rotuloX, area.x + (area.width - fm.stringWidth(rotuloX)) / 2,
                    area.y + area.height + 36);
        }
        if (!rotuloY.isEmpty()) {
            Graphics2D girado = (Graphics2D) g2.create();
            girado.rotate(-Math.PI / 2, 14, area.y + area.height / 2.0);
            girado.drawString(rotuloY, 14 - fm.stringWidth(rotuloY) / 2,
                    (int) (area.y + area.height / 2.0) + 4);
            girado.dispose();
        }
    }

    /** La leyenda vive fuera del area de datos: banda del titulo o pie. */
    private void dibujarLeyenda(Graphics2D g2, Rectangle area) {
        if (leyenda.isEmpty()) {
            return;
        }
        g2.setFont(Tipografia.interfaz(Tipografia.MENOR));
        FontMetrics fm = g2.getFontMetrics();
        int total = 0;
        for (Entrada e : leyenda) {
            total += fm.stringWidth(e.texto) + 14 + Medidas.E3;
        }
        // Si la leyenda no cabe junto al titulo, baja a un segundo renglon.
        int anchoTitulo = titulo.isEmpty() ? 0
                : g2.getFontMetrics(Tipografia.interfazFuerte(Tipografia.PEQUENA))
                        .stringWidth(titulo) + Medidas.E6;
        boolean segundoRenglon = MARGEN_IZQUIERDO + anchoTitulo + total
                > area.x + area.width - Medidas.E3;
        int x = area.x + area.width - total + Medidas.E3;
        int y = segundoRenglon ? 38 : 20;
        for (Entrada e : leyenda) {
            g2.setColor(e.color);
            g2.fillRect(x, y - 6, 10, 3);
            g2.setColor(Paleta.TINTA_SUAVE);
            g2.drawString(e.texto, x + 14, y);
            x += fm.stringWidth(e.texto) + 14 + Medidas.E3;
        }
    }

    private double[] marcasHorizontales() {
        if (!ejeXLogaritmico) {
            return marcas(minimoX, maximoX, 6);
        }
        List<Double> valores = new ArrayList<>();
        for (double v = Math.pow(10, Math.floor(Math.log10(minimoX)));
                v <= maximoX * 1.0001; v *= 10) {
            if (v >= minimoX) {
                valores.add(v);
            }
        }
        double[] salida = new double[valores.size()];
        for (int i = 0; i < salida.length; i++) {
            salida[i] = valores.get(i);
        }
        return salida;
    }

    /** Marcas en pasos redondos (1, 2, 2.5 o 5 por decada). */
    protected static double[] marcas(double minimo, double maximo, int objetivo) {
        double rango = maximo - minimo;
        if (rango <= 0 || objetivo <= 0) {
            return new double[] { minimo };
        }
        double bruto = rango / objetivo;
        double magnitud = Math.pow(10, Math.floor(Math.log10(bruto)));
        double normalizado = bruto / magnitud;
        double paso = (normalizado <= 1 ? 1 : normalizado <= 2 ? 2
                : normalizado <= 2.5 ? 2.5 : normalizado <= 5 ? 5 : 10) * magnitud;
        double inicio = Math.ceil(minimo / paso) * paso;
        List<Double> valores = new ArrayList<>();
        for (double v = inicio; v <= maximo + paso * 1e-6; v += paso) {
            valores.add(v);
        }
        double[] salida = new double[valores.size()];
        for (int i = 0; i < salida.length; i++) {
            salida[i] = valores.get(i);
        }
        return salida;
    }

    /** Redondea un extremo hacia afuera para que el eje termine en cifra limpia. */
    protected static double[] rangoAmable(double minimo, double maximo) {
        if (minimo == maximo) {
            return new double[] { minimo - 1, maximo + 1 };
        }
        double margen = (maximo - minimo) * 0.08;
        return new double[] { minimo - margen, maximo + margen };
    }

    private record Entrada(String texto, Color color) {
    }
}
