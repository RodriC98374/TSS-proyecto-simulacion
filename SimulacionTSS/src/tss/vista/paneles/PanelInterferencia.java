package tss.vista.paneles;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import tss.modelo.interferencia.EnsambleSimulado;
import tss.modelo.interferencia.ParametrosInterferencia;
import tss.modelo.interferencia.ResultadoInterferencia;
import tss.vista.PanelEjercicio;
import tss.vista.componentes.Bitacora;
import tss.vista.componentes.BotonAccion;
import tss.vista.componentes.CampoNumerico;
import tss.vista.componentes.TablaTecnica;
import tss.vista.componentes.TarjetaMetrica;
import tss.vista.diseno.Formatos;
import tss.vista.diseno.Medidas;
import tss.vista.diseno.Paleta;
import tss.vista.graficas.GraficaConvergencia;
import tss.vista.graficas.GraficaHistograma;

/**
 * Panel de la interferencia eje-cojinete: estima la probabilidad de que la
 * flecha no entre en el cojinete y muestra su convergencia al valor analitico.
 * Informe: seccion 5.6.
 */
@SuppressWarnings({ "serial", "this-escape" })
public final class PanelInterferencia extends PanelEjercicio {

    private static final int[] TAMANIOS = { 100, 1000, 5000, 8677, 20000, 100000 };

    private final CampoNumerico mediaCojinete =
            CampoNumerico.deDecimal("Media cojinete", 0.01, 100.0, "milímetros", 1.50);
    private final CampoNumerico desviacionCojinete =
            CampoNumerico.deDecimal("Desv. cojinete", 0.0001, 10.0, "milímetros", 0.04);
    private final CampoNumerico mediaFlecha =
            CampoNumerico.deDecimal("Media flecha", 0.01, 100.0, "milímetros", 1.48);
    private final CampoNumerico desviacionFlecha =
            CampoNumerico.deDecimal("Desv. flecha", 0.0001, 10.0, "milímetros", 0.03);
    private final CampoNumerico ensambles =
            CampoNumerico.deEntero("Ensambles", 10, 1000000, "ensambles", 8677);
    private final CampoNumerico ensamblesVisuales =
            CampoNumerico.deEntero("Ensambles de la corrida visual", 5, 50, "ensambles", 12);
    private final CampoNumerico semilla =
            CampoNumerico.deEntero("Semilla", 1, 999999999, "para la semilla", 12345);

    private final TarjetaMetrica probabilidad = new TarjetaMetrica("Probabilidad estimada");
    private final TarjetaMetrica referencia = new TarjetaMetrica("Valor analítico");
    private final TarjetaMetrica diferencia = new TarjetaMetrica("Diferencia absoluta");
    private final TarjetaMetrica muestra = new TarjetaMetrica("Ensambles requeridos");

    private final TablaTecnica tabla = new TablaTecnica();
    private final Bitacora bitacora = new Bitacora();
    private final GraficaConvergencia convergencia = new GraficaConvergencia();
    private final GraficaHistograma holgura = new GraficaHistograma();

    private final BotonAccion experimento = BotonAccion.primaria("Ejecutar experimento");
    private final BotonAccion visual = BotonAccion.secundaria("Corrida visual");
    private final BotonAccion exportar = BotonAccion.secundaria("Exportar");

    public PanelInterferencia() {
        super("Ejercicio 4 — Interferencia eje-cojinete",
                "¿Con qué probabilidad la flecha no entra en el cojinete, y cuántas "
                        + "repeticiones hacen falta para un error menor que 0,01?");
        armarParametros();
        armarResultados();
    }

    public ParametrosInterferencia parametros() {
        return new ParametrosInterferencia(mediaCojinete.getDecimal(),
                desviacionCojinete.getDecimal(), mediaFlecha.getDecimal(),
                desviacionFlecha.getDecimal());
    }

    public int ensambles() {
        return ensambles.getEntero();
    }

    public int ensamblesVisuales() {
        return ensamblesVisuales.getEntero();
    }

    public long semilla() {
        return semilla.getEnteroLargo();
    }

    public boolean parametrosValidos() {
        return mediaCojinete.esValido() && desviacionCojinete.esValido()
                && mediaFlecha.esValido() && desviacionFlecha.esValido()
                && ensambles.esValido() && ensamblesVisuales.esValido() && semilla.esValido();
    }

    public TablaTecnica tabla() {
        return tabla;
    }

    public GraficaConvergencia convergencia() {
        return convergencia;
    }

    public GraficaHistograma holgura() {
        return holgura;
    }

    public void habilitarAcciones(boolean activo) {
        experimento.setEnabled(activo && parametrosValidos());
        visual.setEnabled(activo);
        exportar.setEnabled(activo);
    }

    @Override
    public String claveArchivo() {
        return "ejercicio4";
    }

    @Override
    public void limpiar() {
        tabla.limpiar();
        bitacora.limpiar();
        convergencia.reiniciar();
        holgura.reiniciar();
        probabilidad.limpiar();
        referencia.limpiar();
        diferencia.limpiar();
        muestra.limpiar();
        barra.terminar("Sin corridas.");
    }

    public void mostrarResultado(ResultadoInterferencia r) {
        probabilidad.mostrarNumeroAnimado(r.probabilidad(), Formatos::probabilidad, Paleta.AMBAR);
        probabilidad.mostrarDetalle(Formatos.entero(r.conInterferencia()) + " de "
                + Formatos.entero(r.ensambles()) + " ensambles");
        referencia.mostrarTexto(Formatos.probabilidad(r.referencia()), Paleta.RETICULA);
        referencia.mostrarDetalle("P(Z < −0,40)");
        boolean dentro = r.diferenciaAbsoluta() < ParametrosInterferencia.ERROR_OBJETIVO;
        diferencia.mostrarTexto(Formatos.probabilidad(r.diferenciaAbsoluta()),
                dentro ? Paleta.VERDE : Paleta.ROJO);
        diferencia.mostrarDetalle(dentro ? "dentro del error de 0,01" : "sobre el error de 0,01");
        muestra.mostrarTexto(Formatos.entero(r.tamanioMuestraRequerido()), Paleta.AZUL);
        muestra.mostrarDetalle("con 95 % de seguridad");

        agregarFila(r);
        convergencia.mostrar(r.convergenciaEnsambles(), r.convergenciaProbabilidad(),
                r.referencia(), ParametrosInterferencia.ERROR_OBJETIVO,
                r.tamanioMuestraRequerido());
        mostrarHolgura(r);
    }

    /** Detalle de la experimentacion de primera etapa, ensamble por ensamble. */
    public void mostrarCorridaVisual(ResultadoInterferencia r) {
        bitacora.limpiar();
        bitacora.escribirLinea("Corrida visual de " + r.ensambles() + " ensambles",
                Bitacora.Estilo.TITULO);
        bitacora.separar();
        for (EnsambleSimulado e : r.traza()) {
            bitacora.escribirLinea("Ensamble " + e.numero(), Bitacora.Estilo.ACENTO);
            bitacora.escribirLinea("  R cojinete  " + uniformes(e.cojinete().uniformes()),
                    Bitacora.Estilo.TENUE);
            bitacora.escribirLinea("  R flecha    " + uniformes(e.flecha().uniformes()),
                    Bitacora.Estilo.TENUE);
            bitacora.escribirLinea(String.format(Formatos.locale(),
                    "  z1 = %+.4f   x1 = %.4f", e.cojinete().z(), e.x1()));
            bitacora.escribirLinea(String.format(Formatos.locale(),
                    "  z2 = %+.4f   x2 = %.4f", e.flecha().z(), e.x2()));
            bitacora.escribirLinea(String.format(Formatos.locale(),
                    "  holgura = %+.4f     %s", e.holgura(),
                    e.interferencia() ? "hay interferencia" : "ensambla"),
                    e.interferencia() ? Bitacora.Estilo.RECHAZADO : Bitacora.Estilo.ACEPTADO);
            bitacora.separar();
        }
        bitacora.escribirLinea("Media generada de x1: " + Formatos.probabilidad(
                r.mediaCojineteGenerada()), Bitacora.Estilo.TITULO);
        bitacora.escribirLinea("Media generada de x2: " + Formatos.probabilidad(
                r.mediaFlechaGenerada()), Bitacora.Estilo.TITULO);
        bitacora.irAlInicio();
    }

    private void mostrarHolgura(ResultadoInterferencia r) {
        double[] bordes = r.bordesHolgura();
        double[] alturas = new double[r.conteosHolgura().length];
        for (int i = 0; i < alturas.length; i++) {
            alturas[i] = r.conteosHolgura()[i];
        }
        holgura.rotular("Holgura x1 − x2", "holgura", "ensambles");
        holgura.mostrar(bordes, alturas);
        holgura.colorearPor(clase -> bordes[clase + 1] <= 0 ? Paleta.ROJO : Paleta.VERDE);
        holgura.limpiarLeyenda();
        holgura.agregarLeyenda("Interferencia", Paleta.ROJO);
        holgura.agregarLeyenda("Ensambla", Paleta.VERDE);
    }

    private void agregarFila(ResultadoInterferencia r) {
        List<Object[]> filas = new ArrayList<>(tabla.filas());
        filas.add(new Object[] {
                Formatos.entero(r.ensambles()),
                Formatos.entero(r.conInterferencia()),
                Formatos.probabilidad(r.probabilidad()),
                Formatos.probabilidad(r.diferenciaAbsoluta()),
                Formatos.probabilidad(r.mediaCojineteGenerada()),
                Formatos.probabilidad(r.mediaFlechaGenerada()) });
        tabla.establecerFilas(filas);
        tabla.irAlFinal();
    }

    private static String uniformes(double[] valores) {
        StringBuilder texto = new StringBuilder();
        for (double v : valores) {
            texto.append(String.format(Formatos.locale(), "%.4f ", v));
        }
        return texto.toString().trim();
    }

    private void armarParametros() {
        parametros.agregar(ensambles);
        parametros.agregarAccesosRapidos("Tamaños del informe",
                new String[] { "100", "1.000", "5.000", "8.677", "20.000", "100.000" },
                indice -> ensambles.setValor(TAMANIOS[indice]));
        parametros.agregar(semilla);

        parametros.agregarSubtitulo("Piezas");
        parametros.agregarFila(mediaCojinete, desviacionCojinete);
        parametros.agregarFila(mediaFlecha, desviacionFlecha);

        parametros.agregarSubtitulo("Corrida visual");
        parametros.agregar(ensamblesVisuales);
        parametros.agregarNota("Muestra los doce uniformes de cada pieza.");

        Runnable revisar = () -> experimento.setEnabled(parametrosValidos());
        for (CampoNumerico campo : new CampoNumerico[] { mediaCojinete, desviacionCojinete,
                mediaFlecha, desviacionFlecha, ensambles, ensamblesVisuales, semilla }) {
            campo.alCambiar(revisar);
            campo.alConfirmar(e -> pedir(ACCION_EXPERIMENTO));
        }
        experimento.addActionListener(e -> pedir(ACCION_EXPERIMENTO));
        visual.addActionListener(e -> pedir(ACCION_VISUAL));
        exportar.addActionListener(e -> pedir(ACCION_EXPORTAR));

        parametros.agregarAccion(experimento);
        parametros.agregarFilaAccion(visual, exportar);
    }

    private void armarResultados() {
        tabla.definirColumnas(
                new String[] { "Ensambles", "Con interferencia", "p estimada",
                        "Diferencia", "Media x1", "Media x2" },
                new boolean[] { true, true, true, true, true, true });

        resultados.add(filaMetricas(probabilidad, referencia, diferencia, muestra),
                BorderLayout.NORTH);

        JPanel izquierda = new JPanel(new BorderLayout(0, Medidas.E3));
        izquierda.setOpaque(false);
        tabla.setPreferredSize(new Dimension(320, 170));
        izquierda.add(tabla, BorderLayout.NORTH);
        izquierda.add(bitacora, BorderLayout.CENTER);
        bitacora.invitar("Pulse «Corrida visual» para ver los doce uniformes de cada pieza.");

        JPanel derecha = new JPanel(new BorderLayout(0, Medidas.E3));
        derecha.setOpaque(false);
        holgura.setPreferredSize(new Dimension(320, 190));
        derecha.add(convergencia, BorderLayout.CENTER);
        derecha.add(holgura, BorderLayout.SOUTH);

        resultados.add(division(izquierda, derecha, 0.42), BorderLayout.CENTER);
    }
}
