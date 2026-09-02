package tss.vista.componentes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import tss.vista.diseno.Medidas;
import tss.vista.diseno.Paleta;
import tss.vista.diseno.Pinceles;
import tss.vista.diseno.Tipografia;

/**
 * Consola de corrida visual: texto monoespaciado con estilos por estado del
 * modelo, autodesplazamiento al final y tope de lineas para no crecer sin fin.
 */
@SuppressWarnings({ "serial", "this-escape" })
public final class Bitacora extends JPanel {

    public enum Estilo {
        NEUTRO, TENUE, ACENTO, ACEPTADO, RECHAZADO, TITULO
    }

    private static final int MAXIMO_LINEAS = 4000;

    private final JTextPane texto = new JTextPane();
    private final Map<Estilo, Style> estilos = new EnumMap<>(Estilo.class);
    private int lineas;

    public Bitacora() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        texto.setEditable(false);
        texto.setBackground(Paleta.SUPERFICIE);
        texto.setForeground(Paleta.TINTA);
        texto.setCaretColor(Paleta.SUPERFICIE);
        texto.setFont(Tipografia.cifras(Tipografia.PEQUENA));
        texto.setBorder(BorderFactory.createEmptyBorder(Medidas.E3, Medidas.E3,
                Medidas.E3, Medidas.E3));
        texto.setSelectionColor(Paleta.conAlfa(Paleta.AMBAR, 80));

        crearEstilo(Estilo.NEUTRO, Paleta.TINTA, false);
        crearEstilo(Estilo.TENUE, Paleta.TINTA_TENUE, false);
        crearEstilo(Estilo.ACENTO, Paleta.AMBAR, false);
        crearEstilo(Estilo.ACEPTADO, Paleta.VERDE, false);
        crearEstilo(Estilo.RECHAZADO, Paleta.ROJO, false);
        crearEstilo(Estilo.TITULO, Paleta.TINTA_SUAVE, true);

        // Envolver el JTextPane en un BorderLayout impide el ajuste de linea:
        // las filas de cifras se leen enteras y se desplazan en horizontal.
        JPanel sinAjuste = new JPanel(new BorderLayout());
        sinAjuste.setOpaque(false);
        sinAjuste.add(texto, BorderLayout.CENTER);

        JScrollPane desplazable = Desplazamiento.envolver(sinAjuste);
        desplazable.getViewport().setBackground(Paleta.SUPERFICIE);
        desplazable.getViewport().setOpaque(true);
        add(desplazable, BorderLayout.CENTER);
    }

    public void limpiar() {
        texto.setText("");
        lineas = 0;
    }

    /** Deja una linea que dice como llenar la bitacora cuando aun no hay corrida. */
    public void invitar(String texto) {
        limpiar();
        escribirLinea(texto, Estilo.TENUE);
    }

    public void escribir(String contenido, Estilo estilo) {
        StyledDocument documento = texto.getStyledDocument();
        try {
            documento.insertString(documento.getLength(), contenido, estilos.get(estilo));
        } catch (BadLocationException e) {
            return;
        }
        texto.setCaretPosition(documento.getLength());
    }

    public void escribirLinea(String contenido, Estilo estilo) {
        escribir(contenido + "\n", estilo);
        lineas++;
        if (lineas > MAXIMO_LINEAS) {
            recortar();
        }
    }

    public void escribirLinea(String contenido) {
        escribirLinea(contenido, Estilo.NEUTRO);
    }

    public void separar() {
        escribirLinea("", Estilo.TENUE);
    }

    public String contenido() {
        return texto.getText();
    }

    /** Tras volcar una corrida completa conviene dejar la vista al principio. */
    public void irAlInicio() {
        texto.setCaretPosition(0);
        javax.swing.SwingUtilities.invokeLater(() -> texto.scrollRectToVisible(
                new java.awt.Rectangle(0, 0, 1, 1)));
    }

    private void crearEstilo(Estilo clave, Color color, boolean negrita) {
        Style estilo = texto.addStyle(clave.name(), null);
        StyleConstants.setForeground(estilo, color);
        StyleConstants.setFontFamily(estilo, Tipografia.familiaCifras());
        StyleConstants.setFontSize(estilo, Tipografia.PEQUENA);
        StyleConstants.setBold(estilo, negrita);
        estilos.put(clave, estilo);
    }

    private void recortar() {
        StyledDocument documento = texto.getStyledDocument();
        try {
            String todo = documento.getText(0, documento.getLength());
            int corte = todo.indexOf('\n', todo.length() / 3);
            if (corte > 0) {
                documento.remove(0, corte + 1);
                lineas -= contarLineas(todo.substring(0, corte + 1));
            }
        } catch (BadLocationException e) {
            limpiar();
        }
    }

    private static int contarLineas(String fragmento) {
        int total = 0;
        for (int i = 0; i < fragmento.length(); i++) {
            if (fragmento.charAt(i) == '\n') {
                total++;
            }
        }
        return total;
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
