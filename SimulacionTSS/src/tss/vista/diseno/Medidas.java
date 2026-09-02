package tss.vista.diseno;

/**
 * Espaciados en multiplos de cuatro, radios permitidos y medidas fijas de la
 * ventana. Concentrarlos aqui evita numeros sueltos repartidos por la vista.
 */
public final class Medidas {

    public static final int E1 = 4;
    public static final int E2 = 8;
    public static final int E3 = 12;
    public static final int E4 = 16;
    public static final int E6 = 24;
    public static final int E8 = 32;
    public static final int E12 = 48;

    /** Radio 0 en tablas y campos; 4 en botones y tarjetas. No hay un tercero. */
    public static final int RADIO_PLANO = 0;
    public static final int RADIO_SUAVE = 4;

    public static final int ANCHO_RAIL = 232;
    public static final int ANCHO_PARAMETROS = 300;
    public static final int ALTURA_FILA_TABLA = 26;
    public static final int ALTURA_CONTROL = 32;
    public static final int ALTURA_BOTON = 34;

    public static final int VENTANA_MINIMA_ANCHO = 1180;
    public static final int VENTANA_MINIMA_ALTO = 760;
    public static final int VENTANA_INICIAL_ANCHO = 1360;
    public static final int VENTANA_INICIAL_ALTO = 860;

    private Medidas() {
    }
}
