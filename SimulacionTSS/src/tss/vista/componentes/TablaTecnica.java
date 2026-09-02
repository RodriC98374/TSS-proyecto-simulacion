package tss.vista.componentes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import tss.vista.diseno.Medidas;
import tss.vista.diseno.Paleta;
import tss.vista.diseno.Tipografia;

/**
 * Tabla de resultados con el acabado del proyecto: sin lineas verticales, una
 * linea horizontal por fila, cabecera elevada y numeros monoespaciados a la
 * derecha. El color se aplica a celdas sueltas, nunca a la fila entera.
 */
@SuppressWarnings({ "serial", "this-escape" })
public final class TablaTecnica extends JPanel {

    /** Devuelve el color de una celda concreta, o null para dejarla en neutro. */
    public interface Coloreador {
        Color color(int fila, int columna, Object valor);
    }

    private final ModeloTabla modelo = new ModeloTabla();
    private final JTable tabla = new JTable(modelo);
    private final JScrollPane desplazable;
    private Coloreador coloreador;
    private int filaResaltada = -1;
    private int columnaResaltada = -1;

    public TablaTecnica() {
        setLayout(new BorderLayout());
        setOpaque(false);

        tabla.setRowHeight(Medidas.ALTURA_FILA_TABLA);
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.setFillsViewportHeight(true);
        tabla.setBackground(Paleta.SUPERFICIE);
        tabla.setForeground(Paleta.TINTA);
        tabla.setSelectionBackground(Paleta.SUPERFICIE_ALTA);
        tabla.setSelectionForeground(Paleta.TINTA);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabla.setFocusable(false);
        tabla.setDefaultRenderer(Object.class, new PintorCelda());

        JTableHeader cabecera = tabla.getTableHeader();
        cabecera.setReorderingAllowed(false);
        cabecera.setResizingAllowed(true);
        cabecera.setDefaultRenderer(new PintorCabecera());
        cabecera.setPreferredSize(new Dimension(10, 30));
        cabecera.setBackground(Paleta.SUPERFICIE_ALTA);

        desplazable = Desplazamiento.envolver(tabla);
        desplazable.getViewport().setBackground(Paleta.SUPERFICIE);
        desplazable.getViewport().setOpaque(true);
        desplazable.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                ajustarModoDeAncho();
            }
        });
        add(desplazable, BorderLayout.CENTER);
    }

    public void definirColumnas(String[] titulos, boolean[] numericas) {
        modelo.definir(titulos, numericas);
        ajustarAnchos();
    }

    public void establecerFilas(List<Object[]> filas) {
        modelo.establecer(filas);
        ajustarAnchos();
    }

    public void limpiar() {
        filaResaltada = -1;
        columnaResaltada = -1;
        modelo.establecer(List.of());
    }

    public void resaltarFila(int fila) {
        filaResaltada = fila;
        tabla.repaint();
    }

    /** Marca una columna entera con una guia ambar sobre su borde izquierdo. */
    public void resaltarColumna(int columna) {
        columnaResaltada = columna;
        tabla.repaint();
    }

    public void setColoreador(Coloreador coloreador) {
        this.coloreador = coloreador;
        tabla.repaint();
    }

    public String[] titulos() {
        return modelo.titulos.clone();
    }

    public List<Object[]> filas() {
        return List.copyOf(modelo.filas);
    }

    public boolean tieneDatos() {
        return !modelo.filas.isEmpty();
    }

    public void irAlFinal() {
        if (modelo.getRowCount() > 0) {
            tabla.scrollRectToVisible(tabla.getCellRect(modelo.getRowCount() - 1, 0, true));
        }
    }

    private void ajustarAnchos() {
        for (int c = 0; c < modelo.getColumnCount(); c++) {
            TableColumn columna = tabla.getColumnModel().getColumn(c);
            int ancho = anchoDeTexto(modelo.titulos[c], Tipografia.interfaz(Tipografia.MENOR));
            for (int f = 0; f < Math.min(modelo.getRowCount(), 200); f++) {
                Object valor = modelo.getValueAt(f, c);
                ancho = Math.max(ancho, anchoDeTexto(String.valueOf(valor),
                        Tipografia.cifras(Tipografia.PEQUENA)));
            }
            // El relleno interno de la celda es E3 a cada lado; hay que sumarlo.
            columna.setPreferredWidth(ancho + Medidas.E3 * 2 + Medidas.E1);
        }
        ajustarModoDeAncho();
    }

    /** Si las columnas caben, se reparten el ancho; si no, aparece el desplazamiento. */
    private void ajustarModoDeAncho() {
        int suma = 0;
        for (int c = 0; c < modelo.getColumnCount(); c++) {
            suma += tabla.getColumnModel().getColumn(c).getPreferredWidth();
        }
        int disponible = desplazable.getViewport().getWidth();
        tabla.setAutoResizeMode(disponible > 0 && suma <= disponible
                ? JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS
                : JTable.AUTO_RESIZE_OFF);
    }

    private int anchoDeTexto(String texto, java.awt.Font fuente) {
        return getFontMetrics(fuente).stringWidth(texto == null ? "" : texto);
    }

    private final class PintorCelda extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor,
                boolean seleccionada, boolean conFoco, int fila, int columna) {
            JLabel etiqueta = (JLabel) super.getTableCellRendererComponent(
                    tabla, valor, false, false, fila, columna);
            boolean numerica = modelo.esNumerica(columna);
            etiqueta.setFont(numerica
                    ? Tipografia.cifras(Tipografia.PEQUENA)
                    : Tipografia.interfaz(Tipografia.PEQUENA));
            etiqueta.setHorizontalAlignment(numerica ? SwingConstants.RIGHT : SwingConstants.LEFT);
            boolean marcada = columna == columnaResaltada;
            etiqueta.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, marcada ? 2 : 0, 1, 0,
                            marcada ? Paleta.AMBAR : Paleta.RETICULA),
                    BorderFactory.createEmptyBorder(0, marcada ? Medidas.E3 - 2 : Medidas.E3,
                            0, Medidas.E3)));
            etiqueta.setOpaque(true);
            etiqueta.setBackground(fila == filaResaltada
                    ? Paleta.SUPERFICIE_ALTA : Paleta.SUPERFICIE);
            Color propio = coloreador == null ? null : coloreador.color(fila, columna, valor);
            etiqueta.setForeground(propio != null ? propio : Paleta.TINTA);
            return etiqueta;
        }
    }

    private static final class PintorCabecera implements TableCellRenderer {

        private final JLabel etiqueta = new JLabel();

        private PintorCabecera() {
            etiqueta.setOpaque(true);
            etiqueta.setBackground(Paleta.SUPERFICIE_ALTA);
            etiqueta.setForeground(Paleta.TINTA_SUAVE);
            etiqueta.setFont(Tipografia.interfaz(Tipografia.MENOR));
            etiqueta.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Paleta.RETICULA),
                    BorderFactory.createEmptyBorder(0, Medidas.E3, 0, Medidas.E3)));
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor,
                boolean seleccionada, boolean conFoco, int fila, int columna) {
            etiqueta.setText(String.valueOf(valor));
            return etiqueta;
        }
    }

    private static final class ModeloTabla extends AbstractTableModel {

        private String[] titulos = new String[0];
        private boolean[] numericas = new boolean[0];
        private List<Object[]> filas = new ArrayList<>();

        private void definir(String[] titulos, boolean[] numericas) {
            this.titulos = titulos.clone();
            this.numericas = numericas.clone();
            this.filas = new ArrayList<>();
            fireTableStructureChanged();
        }

        private void establecer(List<Object[]> nuevas) {
            this.filas = new ArrayList<>(nuevas);
            fireTableDataChanged();
        }

        private boolean esNumerica(int columna) {
            return columna < numericas.length && numericas[columna];
        }

        @Override
        public int getRowCount() {
            return filas.size();
        }

        @Override
        public int getColumnCount() {
            return titulos.length;
        }

        @Override
        public String getColumnName(int columna) {
            return titulos[columna];
        }

        @Override
        public Object getValueAt(int fila, int columna) {
            Object[] datos = filas.get(fila);
            return columna < datos.length ? datos[columna] : "";
        }

        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    }
}
