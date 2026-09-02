package tss.vista.componentes;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import tss.vista.diseno.Medidas;
import tss.vista.diseno.Paleta;
import tss.vista.diseno.Pinceles;
import tss.vista.diseno.Tipografia;

/**
 * Columna izquierda de cada ejercicio: titulo, pila vertical de controles y
 * zona de acciones al pie. Todos los paneles la usan igual, de modo que los
 * seis ejercicios comparten la misma anatomia.
 */
@SuppressWarnings({ "serial", "this-escape" })
public final class PanelParametros extends JPanel {

    private final JPanel contenido = new JPanel();
    private final JPanel acciones = new JPanel();

    public PanelParametros(String titulo) {
        setLayout(new BorderLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(Medidas.ANCHO_PARAMETROS, 100));
        setMinimumSize(new Dimension(248, 100));
        setBorder(BorderFactory.createEmptyBorder(Medidas.E4, Medidas.E4, Medidas.E4, Medidas.E4));

        JLabel etiqueta = new JLabel(titulo);
        etiqueta.setFont(Tipografia.interfazFuerte(Tipografia.CUERPO));
        etiqueta.setForeground(Paleta.TINTA);
        etiqueta.setBorder(BorderFactory.createEmptyBorder(0, 0, Medidas.E3, 0));

        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setAlignmentX(Component.LEFT_ALIGNMENT);

        acciones.setOpaque(false);
        acciones.setLayout(new BoxLayout(acciones, BoxLayout.Y_AXIS));
        acciones.setBorder(BorderFactory.createEmptyBorder(Medidas.E4, 0, 0, 0));

        Columna envoltura = new Columna();
        envoltura.add(contenido, BorderLayout.NORTH);

        JScrollPane desplazable = Desplazamiento.envolver(envoltura);
        desplazable.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(etiqueta, BorderLayout.NORTH);
        add(desplazable, BorderLayout.CENTER);
        add(acciones, BorderLayout.SOUTH);
    }

    public void agregar(JComponent componente) {
        componente.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenido.add(componente);
        contenido.add(Box.createVerticalStrut(Medidas.E3));
    }

    public void agregarSubtitulo(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(Tipografia.interfazFuerte(Tipografia.PEQUENA));
        etiqueta.setForeground(Paleta.TINTA_SUAVE);
        etiqueta.setAlignmentX(Component.LEFT_ALIGNMENT);
        etiqueta.setBorder(BorderFactory.createEmptyBorder(Medidas.E2, 0, Medidas.E1, 0));
        contenido.add(etiqueta);
        contenido.add(separador());
        contenido.add(Box.createVerticalStrut(Medidas.E3));
    }

    /** Coloca dos o mas controles en una sola linea de anchos iguales. */
    public void agregarFila(JComponent... componentes) {
        JPanel fila = new JPanel(new GridLayout(1, componentes.length, Medidas.E2, 0));
        fila.setOpaque(false);
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (JComponent c : componentes) {
            fila.add(c);
        }
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, fila.getPreferredSize().height));
        contenido.add(fila);
        contenido.add(Box.createVerticalStrut(Medidas.E3));
    }

    /** Renglon de solo lectura: nombre a la izquierda y cifra a la derecha. */
    public void agregarLectura(String clave, String valor) {
        JPanel fila = new JPanel(new BorderLayout());
        fila.setOpaque(false);
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        JLabel etiqueta = new JLabel(clave);
        etiqueta.setFont(Tipografia.interfaz(Tipografia.MENOR));
        etiqueta.setForeground(Paleta.TINTA_TENUE);

        JLabel cifra = new JLabel(valor);
        cifra.setFont(Tipografia.cifras(Tipografia.MENOR));
        cifra.setForeground(Paleta.TINTA_SUAVE);

        fila.add(etiqueta, BorderLayout.WEST);
        fila.add(cifra, BorderLayout.EAST);
        contenido.add(fila);
        contenido.add(Box.createVerticalStrut(Medidas.E1));
    }

    /** Botones pequenos que rellenan un campo con un valor de uso frecuente. */
    public void agregarAccesosRapidos(String etiqueta, String[] rotulos,
            java.util.function.IntConsumer accion) {
        JLabel titulo = new JLabel(etiqueta);
        titulo.setFont(Tipografia.interfaz(Tipografia.MENOR));
        titulo.setForeground(Paleta.TINTA_TENUE);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenido.add(titulo);
        contenido.add(Box.createVerticalStrut(Medidas.E1));

        JPanel fila = new JPanel(new GridLayout(1, rotulos.length, Medidas.E1, 0));
        fila.setOpaque(false);
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (int i = 0; i < rotulos.length; i++) {
            final int indice = i;
            BotonAccion boton = BotonAccion.secundaria(rotulos[i]);
            boton.setFont(Tipografia.interfaz(Tipografia.MENOR));
            boton.addActionListener(e -> accion.accept(indice));
            fila.add(boton);
        }
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, Medidas.ALTURA_BOTON));
        contenido.add(fila);
        contenido.add(Box.createVerticalStrut(Medidas.E3));
    }

    public void agregarNota(String texto) {
        JLabel nota = new JLabel(texto);
        nota.setFont(Tipografia.interfaz(Tipografia.MENOR));
        nota.setForeground(Paleta.TINTA_TENUE);
        nota.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenido.add(nota);
        contenido.add(Box.createVerticalStrut(Medidas.E1));
    }

    public void agregarAccion(JComponent componente) {
        componente.setAlignmentX(Component.LEFT_ALIGNMENT);
        componente.setMaximumSize(new Dimension(Integer.MAX_VALUE, Medidas.ALTURA_BOTON));
        acciones.add(componente);
        acciones.add(Box.createVerticalStrut(Medidas.E2));
    }

    public void agregarFilaAccion(JComponent... componentes) {
        JPanel fila = new JPanel(new GridLayout(1, componentes.length, Medidas.E2, 0));
        fila.setOpaque(false);
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (JComponent c : componentes) {
            fila.add(c);
        }
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, Medidas.ALTURA_BOTON));
        acciones.add(fila);
        acciones.add(Box.createVerticalStrut(Medidas.E2));
    }

    /** Contenedor que se ajusta al ancho del visor: nada de recorte lateral. */
    private static final class Columna extends JPanel implements javax.swing.Scrollable {

        private Columna() {
            super(new BorderLayout());
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(java.awt.Rectangle visible, int orientacion,
                int direccion) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(java.awt.Rectangle visible, int orientacion,
                int direccion) {
            return visible.height;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private JComponent separador() {
        JPanel linea = new JPanel();
        linea.setOpaque(true);
        linea.setBackground(Paleta.RETICULA);
        linea.setAlignmentX(Component.LEFT_ALIGNMENT);
        linea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        linea.setPreferredSize(new Dimension(10, 1));
        return linea;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Pinceles.preparar((Graphics2D) g.create());
        Pinceles.rellenar(g2, 0, 0, getWidth(), getHeight(),
                Medidas.RADIO_PLANO, Paleta.SUPERFICIE);
        Pinceles.bordear(g2, 0, 0, getWidth(), getHeight(),
                Medidas.RADIO_PLANO, Paleta.RETICULA);
        g2.dispose();
        super.paintComponent(g);
    }
}
