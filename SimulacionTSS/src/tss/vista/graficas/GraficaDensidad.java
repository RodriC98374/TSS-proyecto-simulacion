package tss.vista.graficas;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;
import tss.modelo.rechazo.DensidadPorTramos;
import tss.modelo.rechazo.IteracionRechazo;
import tss.modelo.rechazo.ResultadoRechazo;
import tss.modelo.rechazo.Tramo;
import tss.vista.diseno.Paleta;
import tss.vista.diseno.Pinceles;

/**
 * Retrato del metodo de rechazo: la densidad f(x), el rectangulo envolvente en
 * punteado y cada iteracion como un punto verde si se acepto y rojo si no.
 * Informe: secciones 5.1 y 5.2.
 */
@SuppressWarnings({ "serial", "this-escape" })
public final class GraficaDensidad extends Grafica {

    private static final int MAXIMO_PUNTOS = 4000;

    private DensidadPorTramos densidad;
    private List<IteracionRechazo> puntos = List.of();

    public GraficaDensidad() {
        rotular("Densidad y puntos generados", "x", "f(x)");
        decimalesX = 2;
        decimalesY = 2;
    }

    public void mostrar(ResultadoRechazo resultado) {
        this.densidad = resultado.densidad();
        this.puntos = resultado.traza();
        minimoX = densidad.limiteInferior();
        maximoX = densidad.limiteSuperior();
        minimoY = 0.0;
        maximoY = densidad.maximo() * 1.25;
        limpiarLeyenda();
        agregarLeyenda("f(x)", Paleta.AMBAR);
        agregarLeyenda("Aceptado", Paleta.VERDE);
        agregarLeyenda("Rechazado", Paleta.ROJO);
        marcarConDatos();
        repaint();
    }

    @Override
    protected void dibujarSerie(Graphics2D g2, Rectangle area) {
        double m = densidad.maximo();

        g2.setColor(Paleta.TINTA_TENUE);
        g2.setStroke(Pinceles.PUNTEADO_MEDIO);
        int yTope = py(area, m);
        g2.drawLine(px(area, minimoX), yTope, px(area, maximoX), yTope);
        g2.drawLine(px(area, maximoX), yTope, px(area, maximoX), py(area, 0));

        int dibujados = 0;
        int salto = Math.max(1, puntos.size() / MAXIMO_PUNTOS);
        int alfa = puntos.size() > 500 ? 130 : 220;
        for (int i = 0; i < puntos.size(); i += salto) {
            IteracionRechazo it = puntos.get(i);
            int x = px(area, it.x());
            int y = py(area, it.r2() * m);
            g2.setColor(Paleta.conAlfa(it.aceptado() ? Paleta.VERDE : Paleta.ROJO, alfa));
            g2.fillOval(x - 2, y - 2, 5, 5);
            if (++dibujados > MAXIMO_PUNTOS) {
                break;
            }
        }

        g2.setColor(Paleta.AMBAR);
        g2.setStroke(Pinceles.GRUESO);
        for (Tramo tramo : densidad.tramos()) {
            g2.drawLine(px(area, tramo.inicio()), py(area, tramo.evaluar(tramo.inicio())),
                    px(area, tramo.fin()), py(area, tramo.evaluar(tramo.fin())));
        }
        // El escalon necesita el salto vertical entre tramos contiguos.
        List<Tramo> tramos = densidad.tramos();
        for (int i = 1; i < tramos.size(); i++) {
            double frontera = tramos.get(i).inicio();
            g2.drawLine(px(area, frontera), py(area, tramos.get(i - 1).evaluar(frontera)),
                    px(area, frontera), py(area, tramos.get(i).evaluar(frontera)));
        }
    }
}
