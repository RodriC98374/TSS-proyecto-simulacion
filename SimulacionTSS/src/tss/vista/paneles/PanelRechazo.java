package tss.vista.paneles;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import tss.modelo.rechazo.DensidadPorTramos;
import tss.modelo.rechazo.IteracionRechazo;
import tss.modelo.rechazo.ResultadoRechazo;
import tss.modelo.rechazo.Tramo;
import tss.vista.PanelEjercicio;
import tss.vista.componentes.BotonAccion;
import tss.vista.componentes.CampoNumerico;
import tss.vista.componentes.TablaTecnica;
import tss.vista.componentes.TarjetaMetrica;
import tss.vista.diseno.Formatos;
import tss.vista.diseno.Medidas;
import tss.vista.diseno.Paleta;
import tss.vista.graficas.GraficaDensidad;
import tss.vista.graficas.GraficaHistograma;

/**
 * Panel del metodo de rechazo. Se instancia dos veces, una por ejercicio, con
 * distinta densidad y distinto enunciado; el resto es identico.
 * Informe: secciones 5.1 y 5.2.
 */
@SuppressWarnings({ "serial", "this-escape" })
public final class PanelRechazo extends PanelEjercicio {

    private static final int MAXIMO_FILAS = 5000;

    private final String clave;
    private final DensidadPorTramos densidad;
    private final double[] secuenciaPrueba;

    private final CampoNumerico iteraciones =
            CampoNumerico.deEntero("Iteraciones", 1, 200000, "iteraciones", 30);
    private final CampoNumerico semilla =
            CampoNumerico.deEntero("Semilla", 1, 999999999, "para la semilla", 12345);

    private final TarjetaMetrica aceptados = new TarjetaMetrica("Aceptados");
    private final TarjetaMetrica rechazados = new TarjetaMetrica("Rechazados");
    private final TarjetaMetrica tasa = new TarjetaMetrica("Tasa de aceptación");
    private final TarjetaMetrica teorica = new TarjetaMetrica("Tasa teórica");

    private final TablaTecnica tabla = new TablaTecnica();
    private final GraficaDensidad graficaDensidad = new GraficaDensidad();
    private final GraficaHistograma histograma = new GraficaHistograma();

    private final BotonAccion generar = BotonAccion.primaria("Generar muestra");
    private final BotonAccion escritorio = BotonAccion.secundaria("Prueba de escritorio");
    private final BotonAccion exportar = BotonAccion.secundaria("Exportar");

    public PanelRechazo(String titulo, String pregunta, String clave,
            DensidadPorTramos densidad, double[] secuenciaPrueba) {
        super(titulo, pregunta);
        this.clave = clave;
        this.densidad = densidad;
        this.secuenciaPrueba = secuenciaPrueba.clone();

        armarParametros();
        armarResultados();
    }

    public DensidadPorTramos densidad() {
        return densidad;
    }

    public double[] secuenciaPrueba() {
        return secuenciaPrueba.clone();
    }

    public int iteraciones() {
        return iteraciones.getEntero();
    }

    public long semilla() {
        return semilla.getEnteroLargo();
    }

    public boolean parametrosValidos() {
        return iteraciones.esValido() && semilla.esValido();
    }

    public TablaTecnica tabla() {
        return tabla;
    }

    public GraficaDensidad graficaDensidad() {
        return graficaDensidad;
    }

    public GraficaHistograma histograma() {
        return histograma;
    }

    public void habilitarAcciones(boolean activo) {
        generar.setEnabled(activo && parametrosValidos());
        escritorio.setEnabled(activo);
        exportar.setEnabled(activo);
    }

    @Override
    public String claveArchivo() {
        return clave;
    }

    @Override
    public void limpiar() {
        tabla.limpiar();
        graficaDensidad.reiniciar();
        histograma.reiniciar();
        aceptados.limpiar();
        rechazados.limpiar();
        tasa.limpiar();
        teorica.limpiar();
        barra.terminar("Sin corridas.");
    }

    public void mostrarResultado(ResultadoRechazo resultado) {
        aceptados.mostrarTexto(Formatos.entero(resultado.aceptados()), Paleta.VERDE);
        rechazados.mostrarTexto(Formatos.entero(resultado.rechazados()), Paleta.ROJO);
        tasa.mostrarNumeroAnimado(resultado.tasaAceptacion(),
                Formatos::probabilidad, Paleta.AMBAR);
        tasa.mostrarDetalle("de " + Formatos.entero(resultado.iteraciones()) + " iteraciones");
        teorica.mostrarTexto(Formatos.probabilidad(resultado.tasaTeorica()), Paleta.RETICULA);
        teorica.mostrarDetalle("1 / (M × (b − a))");

        tabla.establecerFilas(filasDe(resultado));
        tabla.setColoreador((fila, columna, valor) -> {
            if (columna != 8) {
                return null;
            }
            return String.valueOf(valor).startsWith("Aceptado") ? Paleta.VERDE : Paleta.ROJO;
        });

        graficaDensidad.mostrar(resultado);
        mostrarHistograma(resultado);
    }

    private void mostrarHistograma(ResultadoRechazo resultado) {
        double[] alturas = new double[resultado.conteosHistograma().length];
        for (int i = 0; i < alturas.length; i++) {
            alturas[i] = resultado.densidadEmpirica(i);
        }
        histograma.rotular("Valores aceptados frente a f(x)", "x", "frecuencia / ancho");
        histograma.mostrar(resultado.bordesHistograma(), alturas);
        histograma.colorearPor(clase -> Paleta.VERDE);
        histograma.superponer(densidad);
        histograma.limpiarLeyenda();
        histograma.agregarLeyenda("Muestra aceptada", Paleta.VERDE);
        histograma.agregarLeyenda("f(x) teórica", Paleta.TINTA);
    }

    private List<Object[]> filasDe(ResultadoRechazo resultado) {
        List<Object[]> filas = new ArrayList<>();
        for (IteracionRechazo it : resultado.traza()) {
            filas.add(new Object[] {
                    Formatos.entero(it.numero()),
                    Formatos.probabilidad(it.r1()),
                    Formatos.probabilidad(it.r2()),
                    Formatos.probabilidad(it.x()),
                    it.textoTramo(),
                    Formatos.probabilidad(it.fx()),
                    Formatos.probabilidad(it.razon()),
                    it.aceptado() ? "Sí" : "No",
                    it.textoResultado() });
            if (filas.size() >= MAXIMO_FILAS) {
                break;
            }
        }
        return filas;
    }

    private void armarParametros() {
        parametros.agregar(iteraciones);
        parametros.agregar(semilla);
        parametros.agregarAccesosRapidos("Tamaños de uso frecuente",
                new String[] { "30", "1.000", "100.000" },
                indice -> iteraciones.setValor(new int[] { 30, 1000, 100000 }[indice]));

        parametros.agregarSubtitulo("Densidad");
        List<Tramo> tramos = densidad.tramos();
        for (int i = 0; i < tramos.size(); i++) {
            Tramo t = tramos.get(i);
            String intervalo = (i == 0 ? "[" : "(") + Formatos.conDecimales(t.inicio(), 2)
                    + " ; " + Formatos.conDecimales(t.fin(), 2) + "]";
            parametros.agregarNota("f(x) = " + t.etiqueta() + "  en  " + intervalo);
        }
        parametros.agregarLectura("M", Formatos.probabilidad(densidad.maximo()));
        parametros.agregarLectura("b − a",
                Formatos.probabilidad(densidad.limiteSuperior() - densidad.limiteInferior()));
        parametros.agregarLectura("Integral", Formatos.probabilidad(densidad.integral()));
        parametros.agregarLectura("Eficiencia teórica",
                Formatos.probabilidad(densidad.eficienciaTeorica()));

        Runnable revisar = () -> generar.setEnabled(parametrosValidos());
        iteraciones.alCambiar(revisar);
        semilla.alCambiar(revisar);
        iteraciones.alConfirmar(e -> pedir(ACCION_EXPERIMENTO));
        semilla.alConfirmar(e -> pedir(ACCION_EXPERIMENTO));

        generar.addActionListener(e -> pedir(ACCION_EXPERIMENTO));
        escritorio.addActionListener(e -> pedir(ACCION_VISUAL));
        exportar.addActionListener(e -> pedir(ACCION_EXPORTAR));

        parametros.agregarAccion(generar);
        parametros.agregarFilaAccion(escritorio, exportar);
    }

    private void armarResultados() {
        tabla.definirColumnas(
                new String[] { "It.", "R1", "R2", "x", "Tramo", "f(x)", "f(x)/M",
                        "¿Acepta?", "Resultado" },
                new boolean[] { true, true, true, true, false, true, true, false, false });
        resultados.add(filaMetricas(aceptados, rechazados, tasa, teorica), BorderLayout.NORTH);

        JPanel columnaGraficas = new JPanel(new BorderLayout(0, Medidas.E3));
        columnaGraficas.setOpaque(false);
        histograma.setPreferredSize(new Dimension(320, 190));
        columnaGraficas.add(graficaDensidad, BorderLayout.CENTER);
        columnaGraficas.add(histograma, BorderLayout.SOUTH);

        resultados.add(division(tabla, columnaGraficas, 0.48), BorderLayout.CENTER);
    }
}
