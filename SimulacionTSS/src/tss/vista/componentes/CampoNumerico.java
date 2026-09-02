package tss.vista.componentes;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import tss.vista.diseno.Medidas;
import tss.vista.diseno.Paleta;
import tss.vista.diseno.Pinceles;
import tss.vista.diseno.Tipografia;

/**
 * Campo de parametro numerico con etiqueta, validacion de rango y mensaje de
 * error bajo el campo. Solo admite digitos, signo y un separador decimal.
 */
@SuppressWarnings({ "serial", "this-escape" })
public final class CampoNumerico extends JPanel {

    private final JTextField campo;
    private final JLabel mensaje;
    private final double minimo;
    private final double maximo;
    private final boolean entero;
    private final String sustantivo;
    private final List<Runnable> escuchas = new ArrayList<>();

    private boolean valido = true;

    public CampoNumerico(String etiqueta, double minimo, double maximo, boolean entero,
            String sustantivo, String valorInicial) {
        this.minimo = minimo;
        this.maximo = maximo;
        this.entero = entero;
        this.sustantivo = sustantivo;

        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titulo = new JLabel(etiqueta);
        titulo.setFont(Tipografia.interfaz(Tipografia.PEQUENA));
        titulo.setForeground(Paleta.TINTA_SUAVE);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 0, Medidas.E1, 0));

        mensaje = new JLabel(" ");
        mensaje.setFont(Tipografia.interfaz(Tipografia.MENOR));
        mensaje.setForeground(Paleta.ROJO);
        mensaje.setAlignmentX(Component.LEFT_ALIGNMENT);
        mensaje.setBorder(BorderFactory.createEmptyBorder(Medidas.E1, 0, 0, 0));

        // El campo se crea despues del mensaje: su validador lo escribe al teclear.
        campo = new CampoInterno();
        campo.setText(valorInicial);

        add(titulo);
        add(campo);
        add(mensaje);
        revalidar();
    }

    public static CampoNumerico deEntero(String etiqueta, int minimo, int maximo,
            String sustantivo, int valor) {
        return new CampoNumerico(etiqueta, minimo, maximo, true, sustantivo,
                Integer.toString(valor));
    }

    public static CampoNumerico deDecimal(String etiqueta, double minimo, double maximo,
            String sustantivo, double valor) {
        return new CampoNumerico(etiqueta, minimo, maximo, false, sustantivo,
                textoDe(valor));
    }

    public int getEntero() {
        return (int) Math.round(getDecimal());
    }

    public long getEnteroLargo() {
        return Math.round(getDecimal());
    }

    public double getDecimal() {
        Double valor = interpretar(campo.getText());
        return valor == null ? minimo : valor;
    }

    public void setValor(double valor) {
        campo.setText(entero ? Long.toString(Math.round(valor)) : textoDe(valor));
    }

    public boolean esValido() {
        return valido;
    }

    public void alCambiar(Runnable escucha) {
        escuchas.add(escucha);
    }

    public void alConfirmar(ActionListener accion) {
        campo.addActionListener(accion);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }

    private void revalidar() {
        Double valor = interpretar(campo.getText());
        boolean antes = valido;
        valido = valor != null && valor >= minimo && valor <= maximo;
        mensaje.setText(valido ? " " : "Entre " + textoLimite(minimo) + " y "
                + textoLimite(maximo) + " " + sustantivo);
        campo.repaint();
        if (antes != valido || valor != null) {
            escuchas.forEach(Runnable::run);
        }
    }

    private Double interpretar(String texto) {
        String limpio = texto == null ? "" : texto.trim().replace(',', '.');
        if (limpio.isEmpty() || limpio.equals("-") || limpio.equals(".")) {
            return null;
        }
        try {
            return Double.valueOf(limpio);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String textoLimite(double valor) {
        return entero || valor == Math.rint(valor)
                ? Long.toString(Math.round(valor))
                : textoDe(valor);
    }

    private static String textoDe(double valor) {
        String texto = String.valueOf(valor);
        return texto.endsWith(".0") ? texto.substring(0, texto.length() - 2) : texto;
    }

    /** Campo de texto con borde propio: reticula, ambar con foco, rojo si es invalido. */
    private final class CampoInterno extends JTextField {

        private CampoInterno() {
            setFont(Tipografia.cifras(Tipografia.CUERPO));
            setForeground(Paleta.TINTA);
            setCaretColor(Paleta.AMBAR);
            setSelectionColor(Paleta.conAlfa(Paleta.AMBAR, 90));
            setSelectedTextColor(Paleta.TINTA);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(0, Medidas.E2, 0, Medidas.E2));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            ((PlainDocument) getDocument()).setDocumentFilter(new FiltroNumerico());
            getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    revalidar();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    revalidar();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    revalidar();
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(120, Medidas.ALTURA_CONTROL);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, Medidas.ALTURA_CONTROL);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Pinceles.preparar((Graphics2D) g.create());
            Pinceles.rellenar(g2, 0, 0, getWidth(), getHeight(),
                    Medidas.RADIO_PLANO, Paleta.SUPERFICIE_ALTA);
            Pinceles.bordear(g2, 0, 0, getWidth(), getHeight(), Medidas.RADIO_PLANO,
                    !valido ? Paleta.ROJO : isFocusOwner() ? Paleta.AMBAR : Paleta.RETICULA);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Admite signo inicial, digitos y un unico separador decimal. */
    private final class FiltroNumerico extends DocumentFilter {

        @Override
        public void insertString(FilterBypass fb, int desplazamiento, String texto,
                AttributeSet atributos) throws BadLocationException {
            reemplazar(fb, desplazamiento, 0, texto, atributos);
        }

        @Override
        public void replace(FilterBypass fb, int desplazamiento, int largo, String texto,
                AttributeSet atributos) throws BadLocationException {
            reemplazar(fb, desplazamiento, largo, texto, atributos);
        }

        private void reemplazar(FilterBypass fb, int desplazamiento, int largo, String texto,
                AttributeSet atributos) throws BadLocationException {
            String actual = fb.getDocument().getText(0, fb.getDocument().getLength());
            String propuesto = actual.substring(0, desplazamiento)
                    + (texto == null ? "" : texto)
                    + actual.substring(desplazamiento + largo);
            if (aceptable(propuesto)) {
                fb.replace(desplazamiento, largo, texto, atributos);
            }
        }

        private boolean aceptable(String texto) {
            if (texto.isEmpty()) {
                return true;
            }
            String signo = minimo < 0 ? "-?" : "";
            String patron = entero ? signo + "\\d*" : signo + "\\d*([.,]\\d*)?";
            return texto.matches(patron);
        }
    }
}
