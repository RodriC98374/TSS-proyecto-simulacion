package tss.vista.diseno;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Dos familias resueltas en tiempo de ejecucion contra las fuentes instaladas:
 * una sans para la interfaz y una monoespaciada reservada a las cifras, donde
 * alinear columnas de numeros justifica su uso.
 */
public final class Tipografia {

    public static final int MENOR = 11;
    public static final int PEQUENA = 13;
    public static final int CUERPO = 15;
    public static final int SUBTITULO = 19;
    public static final int TITULO = 26;
    public static final int CIFRA_GRANDE = 34;

    private static final String FAMILIA_INTERFAZ = elegir(
            "Inter", "Segoe UI Variable", "Segoe UI", "SF Pro Text",
            "Helvetica Neue", "Ubuntu", "DejaVu Sans", Font.SANS_SERIF);

    private static final String FAMILIA_CIFRAS = elegir(
            "JetBrains Mono", "Cascadia Code", "Consolas", "SF Mono",
            "Menlo", "DejaVu Sans Mono", Font.MONOSPACED);

    private Tipografia() {
    }

    public static Font interfaz(int tamanio) {
        return new Font(FAMILIA_INTERFAZ, Font.PLAIN, tamanio);
    }

    public static Font interfazFuerte(int tamanio) {
        return new Font(FAMILIA_INTERFAZ, Font.BOLD, tamanio);
    }

    public static Font cifras(int tamanio) {
        return new Font(FAMILIA_CIFRAS, Font.PLAIN, tamanio);
    }

    public static Font cifrasFuerte(int tamanio) {
        return new Font(FAMILIA_CIFRAS, Font.BOLD, tamanio);
    }

    /** El unico texto con espaciado ampliado de toda la aplicacion. */
    public static Font tituloAplicacion() {
        Map<TextAttribute, Object> atributos = new HashMap<>();
        atributos.put(TextAttribute.TRACKING, 0.06);
        return interfazFuerte(CUERPO).deriveFont(atributos);
    }

    public static String familiaInterfaz() {
        return FAMILIA_INTERFAZ;
    }

    public static String familiaCifras() {
        return FAMILIA_CIFRAS;
    }

    private static String elegir(String... candidatas) {
        Set<String> disponibles = new HashSet<>();
        for (String f : GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames()) {
            disponibles.add(f);
        }
        for (String candidata : candidatas) {
            if (disponibles.contains(candidata)) {
                return candidata;
            }
        }
        return candidatas[candidatas.length - 1];
    }
}
