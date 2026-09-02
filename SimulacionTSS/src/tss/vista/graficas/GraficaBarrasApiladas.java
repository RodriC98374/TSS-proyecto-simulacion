package tss.vista.graficas;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import tss.vista.diseno.Formatos;
import tss.vista.diseno.Medidas;
import tss.vista.diseno.Paleta;
import tss.vista.diseno.Tipografia;

/**
 * Comparacion de dos politicas mediante barras apiladas por componente de
 * costo. La barra ganadora lleva una marca ambar en su borde izquierdo.
 * Informe: secciones 5.7 y 5.8.
 */
@SuppressWarnings({ "serial", "this-escape" })
public final class GraficaBarrasApiladas extends Grafica {

    private String[] categorias = new String[0];
    private double[][] valores = new double[0][0];
    private Color[] colores = new Color[0];
    private int ganadora = -1;

    public GraficaBarrasApiladas() {
        sinMarcasX = true;
    }

    public void mostrar(String[] categorias, String[] componentes, double[][] valores,
            Color[] colores, int ganadora) {
        this.categorias = categorias.clone();
        this.valores = valores.clone();
        this.colores = colores.clone();
        this.ganadora = ganadora;

        double mayor = 0.0;
        for (double[] fila : valores) {
            double suma = 0.0;
            for (double v : fila) {
                suma += v;
            }
            mayor = Math.max(mayor, suma);
        }
        minimoX = 0;
        maximoX = categorias.length;
        minimoY = 0;
        maximoY = mayor * 1.20;
        decimalesY = 0;
        limpiarLeyenda();
        for (int i = 0; i < componentes.length; i++) {
            agregarLeyenda(componentes[i], colores[i]);
        }
        marcarConDatos();
        repaint();
    }

    @Override
    protected void dibujarSerie(Graphics2D g2, Rectangle area) {
        int columnas = categorias.length;
        int paso = area.width / Math.max(columnas, 1);
        int ancho = Math.min(paso - Medidas.E12, 132);
        ancho = Math.max(ancho, 40);

        for (int c = 0; c < columnas; c++) {
            int centro = area.x + paso * c + paso / 2;
            int x = centro - ancho / 2;
            double acumulado = 0.0;
            for (int k = 0; k < valores[c].length; k++) {
                int yInferior = py(area, acumulado);
                acumulado += valores[c][k];
                int ySuperior = py(area, acumulado);
                g2.setColor(colores[k]);
                g2.fillRect(x, ySuperior, ancho, yInferior - ySuperior);
                dibujarValorInterno(g2, x, ancho, ySuperior, yInferior, valores[c][k]);
            }
            if (c == ganadora) {
                g2.setColor(Paleta.AMBAR);
                g2.fillRect(x - 4, py(area, acumulado), 3, py(area, 0) - py(area, acumulado));
            }
            dibujarPie(g2, area, centro, acumulado, categorias[c]);
        }
    }

    private void dibujarValorInterno(Graphics2D g2, int x, int ancho, int ySuperior,
            int yInferior, double valor) {
        int alto = yInferior - ySuperior;
        if (alto < 18) {
            return;
        }
        g2.setFont(Tipografia.cifras(Tipografia.MENOR));
        g2.setColor(Paleta.FONDO);
        String texto = Formatos.entero(valor);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(texto, x + (ancho - fm.stringWidth(texto)) / 2,
                ySuperior + alto / 2 + 4);
    }

    private void dibujarPie(Graphics2D g2, Rectangle area, int centro, double total,
            String categoria) {
        g2.setFont(Tipografia.cifrasFuerte(Tipografia.PEQUENA));
        g2.setColor(Paleta.TINTA);
        FontMetrics fm = g2.getFontMetrics();
        String texto = Formatos.dinero(total);
        int y = Math.max(py(area, total) - 8, area.y + fm.getAscent());
        g2.drawString(texto, centro - fm.stringWidth(texto) / 2, y);

        g2.setFont(Tipografia.interfaz(Tipografia.MENOR));
        g2.setColor(Paleta.TINTA_SUAVE);
        fm = g2.getFontMetrics();
        g2.drawString(categoria, centro - fm.stringWidth(categoria) / 2,
                area.y + area.height + 18);
    }

}
