package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import model.ResultadoTrimestre;

public class VentanaPrincipal extends JFrame {

    private final PanelParametros panelParametros = new PanelParametros();
    private final PanelTabla panelTabla = new PanelTabla();
    private final PanelGrafica panelGrafica = new PanelGrafica();
    private final PanelEstadisticas panelEstadisticas = new PanelEstadisticas();
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final JLabel lblStatus = new JLabel("Listo. Configure los parametros y presione SIMULAR.");
    private final JProgressBar progressBar = new JProgressBar();

    public VentanaPrincipal() {
        super("Simulacion de Demanda de Combustibles - UMSS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 680));
        setSize(1280, 760);
        setLocationRelativeTo(null);

        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UMSSTheme.AZUL_OSCURO);
        header.setPreferredSize(new Dimension(0, 72));
        header.setBorder(new EmptyBorder(0, 16, 0, 16));

        // Left: institution info
        JPanel left = new JPanel(new GridLayout(3, 1, 0, 1));
        left.setOpaque(false);
        left.setBorder(new EmptyBorder(8, 0, 8, 0));

        JLabel lblUniv = new JLabel("UNIVERSIDAD MAYOR DE SAN SIMON");
        lblUniv.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblUniv.setForeground(new Color(0xAA, 0xC4, 0xE0));

        JLabel lblFac = new JLabel("Facultad de Ciencias y Tecnologia  |  Ing. en Sistemas");
        lblFac.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        lblFac.setForeground(new Color(0x88, 0xA8, 0xCC));

        JLabel lblTitulo = new JLabel("Simulacion de Demanda en Surtidores de Combustible");
        lblTitulo.setFont(UMSSTheme.TITULO);
        lblTitulo.setForeground(UMSSTheme.BLANCO);

        left.add(lblUniv);
        left.add(lblTitulo);
        left.add(lblFac);

        // Right: accent bar
        JPanel right = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(UMSSTheme.ROJO);
                g2.fillRect(0, 0, 5, getHeight());
                g2.setColor(new Color(0x00, 0x50, 0xAA));
                g2.fillRect(5, 0, getWidth() - 5, getHeight());

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 30));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 36));
                g2.drawString("TSS", 20, 52);
            }
        };
        right.setPreferredSize(new Dimension(120, 0));
        right.setOpaque(false);

        // Red accent left border
        JPanel accentLine = new JPanel();
        accentLine.setBackground(UMSSTheme.ROJO);
        accentLine.setPreferredSize(new Dimension(4, 0));

        header.add(accentLine, BorderLayout.WEST);
        header.add(left, BorderLayout.CENTER);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JSplitPane buildContent() {
        // Tabs
        tabbedPane.setFont(UMSSTheme.NORMAL);
        tabbedPane.setBackground(UMSSTheme.GRIS_FONDO);
        customizeTabs();

        tabbedPane.addTab("  Tabla de Simulacion  ", panelTabla);
        tabbedPane.addTab("  Graficas  ", panelGrafica);
        tabbedPane.addTab("  Estadisticas  ", panelEstadisticas);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelParametros, tabbedPane);
        split.setDividerLocation(260);
        split.setDividerSize(1);
        split.setBorder(null);
        split.setOneTouchExpandable(false);
        return split;
    }

    private void customizeTabs() {
        UIManager.put("TabbedPane.selected", UMSSTheme.BLANCO);
        UIManager.put("TabbedPane.background", UMSSTheme.GRIS_FONDO);
        UIManager.put("TabbedPane.focus", UMSSTheme.AZUL);
        UIManager.put("TabbedPane.contentAreaColor", UMSSTheme.BLANCO);
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setBackground(new Color(0x1A, 0x2A, 0x3A));
        bar.setPreferredSize(new Dimension(0, 26));
        bar.setBorder(new EmptyBorder(4, 12, 4, 12));

        lblStatus.setFont(UMSSTheme.PEQUENA);
        lblStatus.setForeground(new Color(0x99, 0xBB, 0xCC));

        progressBar.setVisible(false);
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(140, 14));
        progressBar.setBackground(new Color(0x2A, 0x3A, 0x4A));
        progressBar.setForeground(UMSSTheme.AZUL);
        progressBar.setBorderPainted(false);

        bar.add(lblStatus, BorderLayout.CENTER);
        bar.add(progressBar, BorderLayout.EAST);
        return bar;
    }

    // ── Public API for controller ──────────────────────────────────────────

    public void setStatus(String msg) {
        SwingUtilities.invokeLater(() -> lblStatus.setText(msg));
    }

    public void setSimulando(boolean simulando) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setVisible(simulando);
            panelParametros.setSimularHabilitado(!simulando);
        });
    }

    public void mostrarResultados(List<ResultadoTrimestre> resultados) {
        SwingUtilities.invokeLater(() -> {
            panelTabla.cargarResultados(resultados);
            panelGrafica.cargarResultados(resultados);
            panelEstadisticas.cargarResultados(resultados);
        });
    }

    public void mostrarError(String mensaje) {
        SwingUtilities.invokeLater(() ->
            JOptionPane.showMessageDialog(this, mensaje, "Error de parametros",
                    JOptionPane.ERROR_MESSAGE));
    }

    public PanelParametros getPanelParametros() { return panelParametros; }
}
