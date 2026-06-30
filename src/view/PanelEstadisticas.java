package view;

import model.ResultadoTrimestre;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class PanelEstadisticas extends JPanel {

    private static final String[] COLS_RESUMEN = {
        "Trimestre", "Precio (Bs/L)", "Total Veh.", "Migrantes",
        "% Migracion", "Espera Prom. (min)", "Costo Prom. Sub. (Bs)"
    };

    private final DefaultTableModel modeloResumen = new DefaultTableModel(COLS_RESUMEN, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tablaResumen = new JTable(modeloResumen);
    private final JTextArea txtObservaciones = new JTextArea();

    public PanelEstadisticas() {
        setLayout(new BorderLayout(0, 10));
        setBackground(UMSSTheme.GRIS_FONDO);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        add(buildResumenPanel(), BorderLayout.CENTER);
        add(buildObservacionesPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildResumenPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(UMSSTheme.BLANCO);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UMSSTheme.GRIS_BORDE),
                new EmptyBorder(10, 12, 10, 12)));

        JLabel titulo = new JLabel("Resumen por Trimestre");
        titulo.setFont(UMSSTheme.SUBTITULO);
        titulo.setForeground(UMSSTheme.AZUL);

        tablaResumen.setFont(UMSSTheme.NORMAL);
        tablaResumen.setRowHeight(26);
        tablaResumen.setGridColor(UMSSTheme.GRIS_BORDE);
        tablaResumen.setShowGrid(true);
        tablaResumen.setIntercellSpacing(new Dimension(1, 1));
        tablaResumen.setSelectionBackground(UMSSTheme.AZUL_CLARO);
        tablaResumen.setRowSelectionAllowed(true);
        tablaResumen.getTableHeader().setFont(UMSSTheme.BOLD);
        tablaResumen.getTableHeader().setBackground(UMSSTheme.AZUL);
        tablaResumen.getTableHeader().setForeground(UMSSTheme.BLANCO);
        tablaResumen.getTableHeader().setReorderingAllowed(false);
        tablaResumen.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tablaResumen.setDefaultRenderer(Object.class, new ResumenRenderer());

        JScrollPane scroll = new JScrollPane(tablaResumen);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        p.add(titulo, BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildObservacionesPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(UMSSTheme.BLANCO);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UMSSTheme.GRIS_BORDE),
                new EmptyBorder(10, 12, 10, 12)));
        p.setPreferredSize(new Dimension(0, 180));

        JLabel titulo = new JLabel("Observaciones del Sistema");
        titulo.setFont(UMSSTheme.SUBTITULO);
        titulo.setForeground(UMSSTheme.AZUL);

        txtObservaciones.setFont(UMSSTheme.NORMAL);
        txtObservaciones.setForeground(UMSSTheme.GRIS_TEXTO);
        txtObservaciones.setBackground(UMSSTheme.GRIS_FONDO);
        txtObservaciones.setEditable(false);
        txtObservaciones.setLineWrap(true);
        txtObservaciones.setWrapStyleWord(true);
        txtObservaciones.setBorder(new EmptyBorder(6, 8, 6, 8));
        txtObservaciones.setText("Ejecute la simulacion para ver el analisis del sistema.");

        JScrollPane scroll = new JScrollPane(txtObservaciones);
        scroll.setBorder(BorderFactory.createLineBorder(UMSSTheme.GRIS_BORDE));

        p.add(titulo, BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    public void cargarResultados(List<ResultadoTrimestre> resultados) {
        modeloResumen.setRowCount(0);
        for (ResultadoTrimestre t : resultados) {
            modeloResumen.addRow(new Object[]{
                "Trimestre " + t.numeroTrimestre,
                String.format("Bs %.2f", t.precioSubvencionado),
                t.getTotalVehiculos(),
                t.getMigrantes(),
                String.format("%.1f%%", t.getPorcentajeMigracion()),
                String.format("%.2f", t.getEsperaPromedioSubvencionados()),
                String.format("Bs %.2f", t.getCostoPromedioSubvencionados())
            });
        }
        txtObservaciones.setText(generarObservaciones(resultados));
    }

    private String generarObservaciones(List<ResultadoTrimestre> resultados) {
        if (resultados == null || resultados.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();

        // Trimestre de primera migracion
        int primerT = -1;
        for (ResultadoTrimestre t : resultados) {
            if (t.getMigrantes() > 0 && primerT < 0) {
                primerT = t.numeroTrimestre;
                sb.append("► Primera migracion al surtidor internacional: Trimestre ")
                  .append(t.numeroTrimestre)
                  .append(" (precio = Bs ").append(String.format("%.2f", t.precioSubvencionado)).append("/L).\n\n");
            }
        }
        if (primerT < 0) {
            sb.append("► No se registraron migraciones al surtidor internacional en ningun trimestre.\n\n");
        }

        // Trimestre de mayor migracion
        ResultadoTrimestre mayorMig = resultados.get(0);
        for (ResultadoTrimestre t : resultados) {
            if (t.getPorcentajeMigracion() > mayorMig.getPorcentajeMigracion()) mayorMig = t;
        }
        sb.append("► Mayor porcentaje de migracion: Trimestre ").append(mayorMig.numeroTrimestre)
          .append(" con ").append(String.format("%.1f%%", mayorMig.getPorcentajeMigracion()))
          .append(" de los vehiculos derivados al precio internacional ")
          .append("(precio subvencionado = Bs ").append(String.format("%.2f", mayorMig.precioSubvencionado)).append("/L).\n\n");

        // Evolucion espera
        ResultadoTrimestre t1 = resultados.get(0);
        ResultadoTrimestre tUlt = resultados.get(resultados.size() - 1);
        double espIni = t1.getEsperaPromedioSubvencionados();
        double espFin = tUlt.getEsperaPromedioSubvencionados();
        sb.append("► Espera promedio: de ").append(String.format("%.2f", espIni))
          .append(" min (T").append(t1.numeroTrimestre).append(") a ")
          .append(String.format("%.2f", espFin))
          .append(" min (T").append(tUlt.numeroTrimestre).append(").\n\n");

        sb.append("► Mecanismo central: conforme el precio subvencionado sube trimestre a trimestre, ")
          .append("el costo de la espera supera el ahorro en combustible para los agentes con mayor ")
          .append("costo de oportunidad (transporte pesado), que migran al surtidor internacional. ")
          .append("Esa salida alivia la congestion para los vehiculos que permanecen en la red subvencionada.");

        return sb.toString();
    }

    // Row renderer: highlight high-migration rows
    private static class ResumenRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, val, sel, focus, row, col);
            setFont(UMSSTheme.NORMAL);
            setHorizontalAlignment(col == 0 ? SwingConstants.LEFT : SwingConstants.CENTER);

            // Parse migration pct from column 4
            String pctStr = t.getValueAt(row, 4).toString().replace("%", "").trim();
            double pct = 0;
            try { pct = Double.parseDouble(pctStr); } catch (NumberFormatException ignored) {}

            if (sel) {
                setBackground(UMSSTheme.AZUL_CLARO);
                setForeground(UMSSTheme.AZUL_OSCURO);
            } else if (pct > 30) {
                setBackground(new Color(0xFF, 0xF0, 0xF0));
                setForeground(col == 4 ? UMSSTheme.ROJO : UMSSTheme.GRIS_TEXTO);
            } else if (pct > 0) {
                setBackground(new Color(0xFF, 0xF8, 0xE8));
                setForeground(UMSSTheme.GRIS_TEXTO);
            } else {
                setBackground(row % 2 == 0 ? UMSSTheme.BLANCO : UMSSTheme.GRIS_FONDO);
                setForeground(UMSSTheme.GRIS_TEXTO);
            }

            if (col == 4 && pct > 0) setFont(UMSSTheme.BOLD);
            return this;
        }
    }
}
