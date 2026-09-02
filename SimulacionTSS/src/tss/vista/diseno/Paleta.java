package tss.vista.diseno;

import java.awt.Color;

/**
 * Paleta unica de la aplicacion. El color codifica un estado del modelo y nunca
 * decora: ambar para lo generado y las acciones, verde para aceptado y ahorro,
 * rojo para rechazado y costo, neutros para todo lo demas.
 */
public final class Paleta {

    public static final Color FONDO = new Color(0x15, 0x2A, 0x2E);
    public static final Color SUPERFICIE = new Color(0x1D, 0x38, 0x3D);
    public static final Color SUPERFICIE_ALTA = new Color(0x26, 0x47, 0x4D);
    public static final Color RETICULA = new Color(0x2F, 0x56, 0x5D);
    public static final Color TINTA = new Color(0xEA, 0xF2, 0xEE);
    public static final Color TINTA_SUAVE = new Color(0x9C, 0xB5, 0xB4);
    public static final Color TINTA_TENUE = new Color(0x64, 0x7F, 0x80);
    public static final Color AMBAR = new Color(0xF2, 0xB1, 0x34);
    public static final Color AMBAR_OSCURO = new Color(0xC9, 0x8F, 0x1F);
    public static final Color VERDE = new Color(0x5F, 0xC2, 0xA0);
    public static final Color ROJO = new Color(0xE2, 0x66, 0x4F);
    public static final Color AZUL = new Color(0x6E, 0xA8, 0xD8);

    private Paleta() {
    }

    public static Color conAlfa(Color color, int alfa) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alfa);
    }

    /** Mezcla lineal entre dos colores; f = 0 devuelve el primero. */
    public static Color mezclar(Color a, Color b, double f) {
        double g = Math.min(Math.max(f, 0.0), 1.0);
        return new Color(
                (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * g),
                (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * g),
                (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * g));
    }
}
