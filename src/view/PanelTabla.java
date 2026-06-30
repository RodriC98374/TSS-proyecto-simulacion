package view;

import model.ResultadoTrimestre;
import model.ResultadoVehiculo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PanelTabla extends JPanel {

    private static final String[] COLUMNAS = {
        "ID", "Arribo (min)", "Perfil", "Vol (L)",
        "Espera Est. (min)", "Costo Sub. (Bs)", "Costo Int. (Bs)",
        "Surtidor", "Fin Servicio", "Decision"
    };

    private final DefaultTableModel modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabla = new JTable(modeloTabla);
    private JComboBox<String> cbTrimestre;
    private List<ResultadoTrimestre> todosLosResultados = new ArrayList<>();
    private JLabel lblConteo;

    public PanelTabla() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UMSSTheme.GRIS_FONDO);
        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTabla(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bar.setBackground(UMSSTheme.BLANCO);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UMSSTheme.GRIS_BORDE));

        JLabel lbl = new JLabel("Filtrar trimestre:");
        lbl.setFont(UMSSTheme.NORMAL);
        lbl.setForeground(UMSSTheme.GRIS_TEXTO);

        cbTrimestre = new JComboBox<>(new String[]{"Todos"});
        cbTrimestre.setFont(UMSSTheme.NORMAL);
        cbTrimestre.setBackground(UMSSTheme.BLANCO);
        cbTrimestre.addActionListener(e -> filtrarPorTrimestre());

        lblConteo = new JLabel("Sin datos");
        lblConteo.setFont(UMSSTheme.PEQUENA);
        lblConteo.setForeground(UMSSTheme.GRIS_TEXTO);

        bar.add(lbl);
        bar.add(cbTrimestre);
        bar.add(Box.createHorizontalStrut(20));
        bar.add(lblConteo);
        return bar;
    }

    private JScrollPane buildTabla() {
        tabla.setFont(UMSSTheme.PEQUENA);
        tabla.setRowHeight(22);
        tabla.setGridColor(UMSSTheme.GRIS_BORDE);
        tabla.setShowGrid(true);
        tabla.setIntercellSpacing(new Dimension(1, 1));
        tabla.setSelectionBackground(UMSSTheme.AZUL_CLARO);
        tabla.setSelectionForeground(UMSSTheme.AZUL_OSCURO);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JTableHeader header = tabla.getTableHeader();
        header.setFont(UMSSTheme.BOLD);
        header.setBackground(UMSSTheme.AZUL);
        header.setForeground(UMSSTheme.BLANCO);
        header.setReorderingAllowed(false);
        ((DefaultTableCellRenderer) header.getDefaultRenderer())
                .setHorizontalAlignment(SwingConstants.CENTER);

        // Column widths
        int[] widths = {45, 95, 145, 70, 120, 120, 120, 75, 105, 115};
        for (int i = 0; i < widths.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        tabla.setDefaultRenderer(Object.class, new FilaRenderer());

        JScrollPane sp = new JScrollPane(tabla);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(UMSSTheme.BLANCO);
        return sp;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        footer.setBackground(UMSSTheme.BLANCO);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UMSSTheme.GRIS_BORDE));

        JLabel leyenda1 = new JLabel("  ");
        leyenda1.setOpaque(true);
        leyenda1.setBackground(UMSSTheme.ROJO_SUAVE);
        leyenda1.setBorder(BorderFactory.createLineBorder(UMSSTheme.ROJO));

        JLabel txt1 = new JLabel("Migro a surtidor internacional");
        txt1.setFont(UMSSTheme.PEQUENA);
        txt1.setForeground(UMSSTheme.GRIS_TEXTO);

        JLabel leyenda2 = new JLabel("  ");
        leyenda2.setOpaque(true);
        leyenda2.setBackground(UMSSTheme.AZUL_CLARO);
        leyenda2.setBorder(BorderFactory.createLineBorder(UMSSTheme.AZUL));

        JLabel txt2 = new JLabel("Permanecio en red subvencionada");
        txt2.setFont(UMSSTheme.PEQUENA);
        txt2.setForeground(UMSSTheme.GRIS_TEXTO);

        footer.add(leyenda1); footer.add(txt1);
        footer.add(Box.createHorizontalStrut(10));
        footer.add(leyenda2); footer.add(txt2);
        return footer;
    }

    public void cargarResultados(List<ResultadoTrimestre> resultados) {
        this.todosLosResultados = resultados;

        // Rebuild combo
        cbTrimestre.removeAllItems();
        cbTrimestre.addItem("Todos");
        for (ResultadoTrimestre t : resultados) {
            cbTrimestre.addItem("T" + t.numeroTrimestre + " - Bs "
                    + String.format("%.2f", t.precioSubvencionado) + "/L");
        }
        cbTrimestre.setSelectedIndex(0);
        filtrarPorTrimestre();
    }

    private void filtrarPorTrimestre() {
        modeloTabla.setRowCount(0);
        int sel = cbTrimestre.getSelectedIndex();
        List<ResultadoTrimestre> mostrar = (sel <= 0)
                ? todosLosResultados
                : List.of(todosLosResultados.get(sel - 1));

        int total = 0;
        int migrantes = 0;
        for (ResultadoTrimestre t : mostrar) {
            for (ResultadoVehiculo v : t.vehiculos) {
                modeloTabla.addRow(new Object[]{
                    "V-" + String.format("%02d", v.id),
                    String.format("%.2f", v.minutoArribo),
                    v.perfil.nombre,
                    String.format("%.1f", v.volumen),
                    String.format("%.2f", v.esperaEstimada),
                    String.format("%.2f", v.costoSubvencionado),
                    String.format("%.2f", v.costoInternacional),
                    v.surtidorAsignado,
                    String.format("%.2f", v.finServicio),
                    v.migroInternacional ? "Internacional" : "Subvencionado"
                });
                total++;
                if (v.migroInternacional) migrantes++;
            }
        }

        double pct = total == 0 ? 0 : migrantes * 100.0 / total;
        lblConteo.setText(String.format("Total: %d  |  Migrantes: %d  |  Migracion: %.1f%%",
                total, migrantes, pct));
    }

    // Row renderer: red tint for migrants, alternating blue tint otherwise
    private static class FilaRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, val, sel, focus, row, col);
            setFont(UMSSTheme.PEQUENA);
            setHorizontalAlignment(SwingConstants.CENTER);

            boolean migro = "Internacional".equals(t.getValueAt(row, 9));

            if (sel) {
                setBackground(UMSSTheme.AZUL_CLARO);
                setForeground(UMSSTheme.AZUL_OSCURO);
            } else if (migro) {
                setBackground(UMSSTheme.ROJO_SUAVE);
                setForeground(UMSSTheme.ROJO);
            } else {
                setBackground(row % 2 == 0 ? UMSSTheme.BLANCO : new Color(0xF7, 0xF9, 0xFF));
                setForeground(UMSSTheme.GRIS_TEXTO);
            }

            // Decision column: bold
            if (col == 9) {
                setFont(UMSSTheme.BOLD);
                setForeground(migro ? UMSSTheme.ROJO : UMSSTheme.VERDE);
            }

            return this;
        }
    }
}
