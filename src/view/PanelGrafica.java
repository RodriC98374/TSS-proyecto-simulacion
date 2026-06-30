package view;

import model.ResultadoTrimestre;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.*;
import java.util.List;

public class PanelGrafica extends JPanel {

    private List<ResultadoTrimestre> datos;
    private final GraficaBarras graficaBarras = new GraficaBarras();
    private final GraficaLinea graficaLinea = new GraficaLinea();

    public PanelGrafica() {
        setLayout(new GridLayout(2, 1, 0, 8));
        setBackground(UMSSTheme.GRIS_FONDO);
        setBorder(new EmptyBorder(12, 12, 12, 12));
        add(wrapChart(graficaBarras, "Porcentaje de Migracion al Surtidor Internacional por Trimestre"));
        add(wrapChart(graficaLinea, "Tiempo de Espera Promedio en Red Subvencionada (min)"));
    }

    private JPanel wrapChart(JPanel chart, String titulo) {
        JPanel wrap = new JPanel(new BorderLayout(0, 4));
        wrap.setBackground(UMSSTheme.BLANCO);
        wrap.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UMSSTheme.GRIS_BORDE),
                new EmptyBorder(10, 12, 10, 12)));

        JLabel lbl = new JLabel(titulo);
        lbl.setFont(UMSSTheme.SUBTITULO);
        lbl.setForeground(UMSSTheme.AZUL);
        wrap.add(lbl, BorderLayout.NORTH);
        wrap.add(chart, BorderLayout.CENTER);
        return wrap;
    }

    public void cargarResultados(List<ResultadoTrimestre> resultados) {
        this.datos = resultados;
        graficaBarras.setDatos(resultados);
        graficaLinea.setDatos(resultados);
        repaint();
    }

    // ── Grafica de barras: % migracion ─────────────────────────────────────
    static class GraficaBarras extends JPanel {
        private List<ResultadoTrimestre> datos;
        private static final int PAD_IZQ = 55, PAD_DER = 20, PAD_SUP = 15, PAD_INF = 50;

        GraficaBarras() {
            setBackground(UMSSTheme.BLANCO);
        }

        void setDatos(List<ResultadoTrimestre> d) { this.datos = d; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (datos == null || datos.isEmpty()) {
                drawPlaceholder(g);
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int plotW = w - PAD_IZQ - PAD_DER;
            int plotH = h - PAD_SUP - PAD_INF;

            drawGrid(g2, PAD_IZQ, PAD_SUP, plotW, plotH, 5, "%");

            int n = datos.size();
            float barW = (float) plotW / (n * 1.6f);
            float gap = (float) plotW / n;

            for (int i = 0; i < n; i++) {
                ResultadoTrimestre t = datos.get(i);
                double pct = t.getPorcentajeMigracion();
                int barH = (int) (pct / 100.0 * plotH);
                int x = PAD_IZQ + (int) (i * gap + (gap - barW) / 2);
                int y = PAD_SUP + plotH - barH;

                // Color: red if > 30%, blue otherwise
                Color barColor = pct > 30 ? UMSSTheme.ROJO : UMSSTheme.AZUL;
                g2.setColor(barColor);
                g2.fillRoundRect(x, y, (int) barW, Math.max(barH, 2), 4, 4);

                // Value label above bar
                g2.setColor(barColor);
                g2.setFont(UMSSTheme.BOLD);
                String val = String.format("%.1f%%", pct);
                FontMetrics fm = g2.getFontMetrics();
                int tx = x + (int) barW / 2 - fm.stringWidth(val) / 2;
                g2.drawString(val, tx, Math.max(y - 4, PAD_SUP + 12));

                // X axis label
                g2.setColor(UMSSTheme.GRIS_TEXTO);
                g2.setFont(UMSSTheme.PEQUENA);
                String lbl = "T" + t.numeroTrimestre;
                String precio = "Bs " + String.format("%.2f", t.precioSubvencionado);
                int lx = x + (int) barW / 2;
                g2.drawString(lbl, lx - fm.stringWidth(lbl) / 2, h - PAD_INF + 16);
                FontMetrics fmP = g2.getFontMetrics();
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                g2.drawString(precio, lx - g2.getFontMetrics().stringWidth(precio) / 2, h - PAD_INF + 28);
            }

            drawAxes(g2, PAD_IZQ, PAD_SUP, plotW, plotH);
            drawYLabels(g2, PAD_IZQ, PAD_SUP, plotH, 5, 100, "%");
            g2.dispose();
        }

        private void drawPlaceholder(Graphics g) {
            g.setColor(UMSSTheme.GRIS_BORDE);
            g.setFont(UMSSTheme.NORMAL);
            String msg = "Ejecute la simulacion para ver resultados";
            FontMetrics fm = g.getFontMetrics();
            g.drawString(msg, getWidth() / 2 - fm.stringWidth(msg) / 2, getHeight() / 2);
        }

        private void drawGrid(Graphics2D g2, int x0, int y0, int w, int h, int divs, String unit) {
            g2.setColor(new Color(0xE8, 0xEC, 0xF2));
            g2.setStroke(new BasicStroke(0.8f));
            for (int i = 0; i <= divs; i++) {
                int y = y0 + h - (int) (i * h / (float) divs);
                g2.drawLine(x0, y, x0 + w, y);
            }
        }

        private void drawAxes(Graphics2D g2, int x0, int y0, int w, int h) {
            g2.setColor(UMSSTheme.GRIS_TEXTO);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(x0, y0, x0, y0 + h);
            g2.drawLine(x0, y0 + h, x0 + w, y0 + h);
        }

        private void drawYLabels(Graphics2D g2, int x0, int y0, int h, int divs, double maxVal, String unit) {
            g2.setColor(UMSSTheme.GRIS_TEXTO);
            g2.setFont(UMSSTheme.PEQUENA);
            FontMetrics fm = g2.getFontMetrics();
            for (int i = 0; i <= divs; i++) {
                int y = y0 + h - (int) (i * h / (float) divs);
                double val = i * maxVal / divs;
                String s = String.format("%.0f%s", val, unit);
                g2.drawString(s, x0 - fm.stringWidth(s) - 4, y + fm.getAscent() / 2);
            }
        }
    }

    // ── Grafica de linea: espera promedio ───────────────────────────────────
    static class GraficaLinea extends JPanel {
        private List<ResultadoTrimestre> datos;
        private static final int PAD_IZQ = 65, PAD_DER = 20, PAD_SUP = 15, PAD_INF = 50;

        GraficaLinea() {
            setBackground(UMSSTheme.BLANCO);
        }

        void setDatos(List<ResultadoTrimestre> d) { this.datos = d; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (datos == null || datos.isEmpty()) {
                g.setColor(UMSSTheme.GRIS_BORDE);
                g.setFont(UMSSTheme.NORMAL);
                String msg = "Ejecute la simulacion para ver resultados";
                FontMetrics fm = g.getFontMetrics();
                g.drawString(msg, getWidth() / 2 - fm.stringWidth(msg) / 2, getHeight() / 2);
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int plotW = w - PAD_IZQ - PAD_DER;
            int plotH = h - PAD_SUP - PAD_INF;
            int n = datos.size();

            // Find max espera
            double maxEspera = 1.0;
            for (ResultadoTrimestre t : datos) {
                maxEspera = Math.max(maxEspera, t.getEsperaPromedioSubvencionados());
            }
            maxEspera = Math.ceil(maxEspera * 1.1);

            drawGrid(g2, PAD_IZQ, PAD_SUP, plotW, plotH, 5);

            // Points
            int[] px = new int[n];
            int[] py = new int[n];
            float step = n > 1 ? (float) plotW / (n - 1) : plotW / 2;
            for (int i = 0; i < n; i++) {
                double esp = datos.get(i).getEsperaPromedioSubvencionados();
                px[i] = PAD_IZQ + (n == 1 ? plotW / 2 : (int) (i * step));
                py[i] = PAD_SUP + plotH - (int) (esp / maxEspera * plotH);
            }

            // Shaded area
            Polygon area = new Polygon();
            area.addPoint(px[0], PAD_SUP + plotH);
            for (int i = 0; i < n; i++) area.addPoint(px[i], py[i]);
            area.addPoint(px[n - 1], PAD_SUP + plotH);
            g2.setColor(new Color(0x00, 0x37, 0x70, 30));
            g2.fillPolygon(area);

            // Line
            g2.setColor(UMSSTheme.AZUL);
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < n - 1; i++) {
                g2.drawLine(px[i], py[i], px[i + 1], py[i + 1]);
            }

            // Points and labels
            for (int i = 0; i < n; i++) {
                ResultadoTrimestre t = datos.get(i);
                double esp = t.getEsperaPromedioSubvencionados();

                g2.setColor(UMSSTheme.BLANCO);
                g2.fillOval(px[i] - 5, py[i] - 5, 10, 10);
                g2.setColor(UMSSTheme.AZUL);
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(px[i] - 5, py[i] - 5, 10, 10);

                g2.setFont(UMSSTheme.BOLD);
                String val = String.format("%.1f", esp);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(val, px[i] - fm.stringWidth(val) / 2, py[i] - 9);

                // X label
                g2.setColor(UMSSTheme.GRIS_TEXTO);
                g2.setFont(UMSSTheme.PEQUENA);
                String lbl = "T" + t.numeroTrimestre;
                g2.drawString(lbl, px[i] - fm.stringWidth(lbl) / 2, h - PAD_INF + 16);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                String precio = "Bs " + String.format("%.2f", t.precioSubvencionado);
                g2.drawString(precio, px[i] - g2.getFontMetrics().stringWidth(precio) / 2, h - PAD_INF + 28);
            }

            drawAxes(g2, PAD_IZQ, PAD_SUP, plotW, plotH);
            drawYLabels(g2, PAD_IZQ, PAD_SUP, plotH, 5, maxEspera);
            g2.dispose();
        }

        private void drawGrid(Graphics2D g2, int x0, int y0, int w, int h, int divs) {
            g2.setColor(new Color(0xE8, 0xEC, 0xF2));
            g2.setStroke(new BasicStroke(0.8f));
            for (int i = 0; i <= divs; i++) {
                int y = y0 + h - (int) (i * h / (float) divs);
                g2.drawLine(x0, y, x0 + w, y);
            }
        }

        private void drawAxes(Graphics2D g2, int x0, int y0, int w, int h) {
            g2.setColor(UMSSTheme.GRIS_TEXTO);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(x0, y0, x0, y0 + h);
            g2.drawLine(x0, y0 + h, x0 + w, y0 + h);
        }

        private void drawYLabels(Graphics2D g2, int x0, int y0, int h, int divs, double maxVal) {
            g2.setColor(UMSSTheme.GRIS_TEXTO);
            g2.setFont(UMSSTheme.PEQUENA);
            FontMetrics fm = g2.getFontMetrics();
            for (int i = 0; i <= divs; i++) {
                int y = y0 + h - (int) (i * h / (float) divs);
                double val = i * maxVal / divs;
                String s = String.format("%.1f min", val);
                g2.drawString(s, x0 - fm.stringWidth(s) - 4, y + fm.getAscent() / 2);
            }
        }
    }
}
