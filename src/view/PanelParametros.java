package view;

import model.Parametros;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class PanelParametros extends JPanel {

    private JTextField tfTasaArribo;
    private JTextField tfVehiculos;
    private JTextField tfTrimestres;
    private JTextField tfProporcionPesado;
    private JTextField tfSemilla;
    private JTextField tfPrecioInicial;
    private JTextField tfIncremento;
    private JTextField tfCostoOptPesado;
    private JTextField tfCostoOptParticular;
    private JTextField tfValidacion;
    private JTextField tfCaudal;
    private JButton btnSimular;
    private JButton btnRestaurar;

    public PanelParametros() {
        setLayout(new BorderLayout());
        setBackground(UMSSTheme.BLANCO);
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UMSSTheme.GRIS_BORDE));
        setPreferredSize(new Dimension(260, 0));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(UMSSTheme.AZUL);
        h.setBorder(new EmptyBorder(10, 14, 10, 14));
        JLabel lbl = new JLabel("Parametros de Simulacion");
        lbl.setFont(UMSSTheme.SUBTITULO);
        lbl.setForeground(UMSSTheme.BLANCO);
        h.add(lbl, BorderLayout.CENTER);
        return h;
    }

    private JScrollPane buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UMSSTheme.BLANCO);
        form.setBorder(new EmptyBorder(8, 14, 8, 14));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(3, 0, 3, 0);

        int row = 0;

        row = addSectionLabel(form, gc, row, "Dinamica de Arribo");
        tfTasaArribo = addField(form, gc, row++, "Tasa arribo (veh/min):", "1.0");
        tfVehiculos = addField(form, gc, row++, "Vehiculos por trimestre:", "100");
        tfProporcionPesado = addField(form, gc, row++, "Proporcion pesado (0-1):", "0.35");
        tfSemilla = addField(form, gc, row++, "Semilla aleatoria:", "12345");

        row = addSectionLabel(form, gc, row, "Politica de Precios");
        tfPrecioInicial = addField(form, gc, row++, "Precio inicial (Bs/L):", "6.96");
        tfIncremento = addField(form, gc, row++, "Incremento trimestral:", "1.35");
        tfTrimestres = addField(form, gc, row++, "Num. trimestres:", "6");

        row = addSectionLabel(form, gc, row, "Costos de Oportunidad");
        tfCostoOptPesado = addField(form, gc, row++, "C.Op. Pesado (Bs/h):", "150");
        tfCostoOptParticular = addField(form, gc, row++, "C.Op. Particular (Bs/h):", "30");

        row = addSectionLabel(form, gc, row, "Servicio");
        tfValidacion = addField(form, gc, row++, "T. validacion B-SISA (min):", "2.0");
        tfCaudal = addField(form, gc, row++, "Caudal surtidor (L/min):", "100");

        // Padding
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2; gc.weighty = 1.0;
        form.add(Box.createVerticalGlue(), gc);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private int addSectionLabel(JPanel form, GridBagConstraints gc, int row, String text) {
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2; gc.weightx = 1.0; gc.weighty = 0;
        JLabel lbl = new JLabel(text);
        lbl.setFont(UMSSTheme.BOLD);
        lbl.setForeground(UMSSTheme.AZUL);
        lbl.setBorder(new EmptyBorder(8, 0, 2, 0));
        form.add(lbl, gc);
        gc.gridwidth = 1;
        return row + 1;
    }

    private JTextField addField(JPanel form, GridBagConstraints gc, int row, String label, String def) {
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0.55;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UMSSTheme.PEQUENA);
        lbl.setForeground(UMSSTheme.GRIS_TEXTO);
        form.add(lbl, gc);

        gc.gridx = 1; gc.weightx = 0.45;
        JTextField tf = new JTextField(def, 7);
        tf.setFont(UMSSTheme.MONO);
        tf.setHorizontalAlignment(JTextField.RIGHT);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UMSSTheme.GRIS_BORDE),
                new EmptyBorder(2, 5, 2, 5)));
        form.add(tf, gc);
        return tf;
    }

    private JPanel buildButtons() {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 6));
        p.setBackground(UMSSTheme.BLANCO);
        p.setBorder(new EmptyBorder(10, 14, 14, 14));

        btnSimular = new JButton("SIMULAR");
        btnSimular.setFont(UMSSTheme.BOLD);
        btnSimular.setBackground(UMSSTheme.AZUL);
        btnSimular.setForeground(UMSSTheme.BLANCO);
        btnSimular.setFocusPainted(false);
        btnSimular.setBorderPainted(false);
        btnSimular.setOpaque(true);
        btnSimular.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnRestaurar = new JButton("Restaurar defaults");
        btnRestaurar.setFont(UMSSTheme.PEQUENA);
        btnRestaurar.setBackground(UMSSTheme.GRIS_FONDO);
        btnRestaurar.setForeground(UMSSTheme.GRIS_TEXTO);
        btnRestaurar.setFocusPainted(false);
        btnRestaurar.setBorder(BorderFactory.createLineBorder(UMSSTheme.GRIS_BORDE));
        btnRestaurar.setOpaque(true);
        btnRestaurar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        p.add(btnSimular);
        p.add(btnRestaurar);
        return p;
    }

    public Parametros leerParametros() throws IllegalArgumentException {
        Parametros params = new Parametros();
        try {
            params.tasaArribo = Double.parseDouble(tfTasaArribo.getText().trim());
            params.vehiculosPorTrimestre = Integer.parseInt(tfVehiculos.getText().trim());
            params.proporcionPesado = Double.parseDouble(tfProporcionPesado.getText().trim());
            params.semilla = Long.parseLong(tfSemilla.getText().trim());
            params.precioInicialSubvencionado = Double.parseDouble(tfPrecioInicial.getText().trim());
            params.incrementoTrimestral = Double.parseDouble(tfIncremento.getText().trim());
            params.trimestres = Integer.parseInt(tfTrimestres.getText().trim());
            params.costoOptPesado = Double.parseDouble(tfCostoOptPesado.getText().trim());
            params.costoOptParticular = Double.parseDouble(tfCostoOptParticular.getText().trim());
            params.tiempoValidacion = Double.parseDouble(tfValidacion.getText().trim());
            params.caudal = Double.parseDouble(tfCaudal.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Verifique que todos los parametros sean numericos validos.");
        }

        if (params.tasaArribo <= 0) throw new IllegalArgumentException("La tasa de arribo debe ser positiva.");
        if (params.vehiculosPorTrimestre < 1) throw new IllegalArgumentException("Se necesita al menos 1 vehiculo.");
        if (params.trimestres < 1) throw new IllegalArgumentException("Se necesita al menos 1 trimestre.");
        if (params.proporcionPesado < 0 || params.proporcionPesado > 1)
            throw new IllegalArgumentException("La proporcion pesado debe estar entre 0 y 1.");

        return params;
    }

    public void restaurarDefaults() {
        tfTasaArribo.setText("1.0");
        tfVehiculos.setText("100");
        tfProporcionPesado.setText("0.35");
        tfSemilla.setText("12345");
        tfPrecioInicial.setText("6.96");
        tfIncremento.setText("1.35");
        tfTrimestres.setText("6");
        tfCostoOptPesado.setText("150");
        tfCostoOptParticular.setText("30");
        tfValidacion.setText("2.0");
        tfCaudal.setText("100");
    }

    public void addSimularListener(ActionListener l) { btnSimular.addActionListener(l); }
    public void addRestaurarListener(ActionListener l) { btnRestaurar.addActionListener(l); }
    public void setSimularHabilitado(boolean b) { btnSimular.setEnabled(b); }
}
