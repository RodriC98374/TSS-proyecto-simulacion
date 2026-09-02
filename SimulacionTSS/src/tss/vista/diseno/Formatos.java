package tss.vista.diseno;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Formato de cifras en notacion boliviana: punto para los miles y coma para los
 * decimales. Todas las cantidades visibles pasan por aqui para que coincidan
 * con la notacion del informe.
 */
public final class Formatos {

    private static final Locale ES_BO = Locale.forLanguageTag("es-BO");
    private static final DecimalFormatSymbols SIMBOLOS = simbolos();

    private static final DecimalFormat DINERO = crear("#,##0.00");
    private static final DecimalFormat ENTERO = crear("#,##0");
    private static final DecimalFormat DOS = crear("#,##0.00");
    private static final DecimalFormat CUATRO = crear("0.0000");
    private static final DecimalFormat PORCENTAJE = crear("0.00");

    private Formatos() {
    }

    public static String dinero(double valor) {
        return DINERO.format(valor);
    }

    public static String entero(double valor) {
        return ENTERO.format(valor);
    }

    public static String decimal(double valor) {
        return DOS.format(valor);
    }

    public static String probabilidad(double valor) {
        return CUATRO.format(valor);
    }

    public static String porcentaje(double fraccion) {
        return PORCENTAJE.format(fraccion * 100.0) + " %";
    }

    public static String conDecimales(double valor, int decimales) {
        StringBuilder patron = new StringBuilder("#,##0");
        if (decimales > 0) {
            patron.append('.');
            patron.append("0".repeat(decimales));
        }
        return crear(patron.toString()).format(valor);
    }

    public static Locale locale() {
        return ES_BO;
    }

    private static DecimalFormat crear(String patron) {
        return new DecimalFormat(patron, SIMBOLOS);
    }

    private static DecimalFormatSymbols simbolos() {
        DecimalFormatSymbols s = new DecimalFormatSymbols(ES_BO);
        s.setDecimalSeparator(',');
        s.setGroupingSeparator('.');
        return s;
    }
}
