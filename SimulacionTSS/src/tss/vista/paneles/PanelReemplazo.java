package tss.vista.paneles;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import tss.modelo.reemplazo.EventoFalla;
import tss.modelo.reemplazo.ParametrosReemplazo;
import tss.modelo.reemplazo.PoliticaReemplazo;
import tss.modelo.reemplazo.ResultadoCorridaReemplazo;
import tss.modelo.reemplazo.ResumenReemplazo;
import tss.vista.PanelEjercicio;
import tss.vista.componentes.Bitacora;
import tss.vista.componentes.BotonAccion;
import tss.vista.componentes.CampoNumerico;
import tss.vista.componentes.SelectorOpcion;
import tss.vista.componentes.TablaTecnica;
import tss.vista.componentes.TarjetaMetrica;
import tss.vista.diseno.Formatos;
import tss.vista.diseno.Medidas;
import tss.vista.diseno.Paleta;
import tss.vista.graficas.GraficaBarrasApiladas;
import tss.vista.graficas.GraficaLineas;

/**
 * Panel del reemplazo de componentes: compara reemplazar solo el que falla contra
 * reemplazar los cuatro, y muestra el intercambio entre piezas y horas de paro.
 * Informe: seccion 5.8.
 */
@SuppressWarnings({ "serial", "this-escape" })
public final class PanelReemplazo extends PanelEjercicio {

    private static final String[] INDICADORES = {
            "Intervenciones", "Componentes usados", "Horas de desconexión",
            "Costo en componentes", "Costo por desconexión", "Costo total",
            "Costo por hora" };

    private final SelectorOpcion politica =
            new SelectorOpcion("Política A", "Política B", "Comparar");
    private final CampoNumerico corridas =
            CampoNumerico.deEntero("Corridas", 100, 50000, "corridas", 5000);
    private final CampoNumerico horizonte =
            CampoNumerico.deDecimal("Horizonte", 100, 1000000, "horas", 20000);
    private final CampoNumerico componentes =
            CampoNumerico.deEntero("Componentes", 1, 20, "componentes", 4);
    private final CampoNumerico mediaVida =
            CampoNumerico.deDecimal("Vida media", 1, 100000, "horas", 600);
    private final CampoNumerico desviacionVida =
            CampoNumerico.deDecimal("Desv. de la vida", 1, 100000, "horas", 100);
    private final CampoNumerico costoComponente =
            CampoNumerico.deDecimal("Costo componente", 0, 1000000, "dólares", 200);
    private final CampoNumerico costoHora =
            CampoNumerico.deDecimal("Costo hora de paro", 0, 1000000, "dólares", 100);
    private final CampoNumerico paroIndividual =
            CampoNumerico.deDecimal("Paro al cambiar uno", 0, 100, "horas", 1);
    private final CampoNumerico paroTotal =
            CampoNumerico.deDecimal("Paro al cambiar todos", 0, 100, "horas", 2);
    private final CampoNumerico eventosVisibles =
            CampoNumerico.deEntero("Eventos a mostrar", 3, 60, "eventos", 8);
    private final CampoNumerico semilla =
            CampoNumerico.deEntero("Semilla", 1, 999999999, "para la semilla", 12345);

    private final TarjetaMetrica costoA = new TarjetaMetrica("Costo total, política A");
    private final TarjetaMetrica costoB = new TarjetaMetrica("Costo total, política B");
    private final TarjetaMetrica ganadora = new TarjetaMetrica("Política más económica");
    private final TarjetaMetrica diferencia = new TarjetaMetrica("Diferencia");

    private final TablaTecnica tabla = new TablaTecnica();
    private final Bitacora bitacora = new Bitacora();
    private final GraficaBarrasApiladas barras = new GraficaBarrasApiladas();
    private final GraficaLineas ritmo = new GraficaLineas();

    private final BotonAccion experimento = BotonAccion.primaria("Ejecutar experimento");
    private final BotonAccion visual = BotonAccion.secundaria("Corrida visual");
    private final BotonAccion exportar = BotonAccion.secundaria("Exportar");

    public PanelReemplazo() {
        super("Ejercicio 6 — Reemplazo de componentes",
                "¿Conviene cambiar solo el componente que falla o los cuatro a la vez?");
        politica.seleccionar(2);
        armarParametros();
        armarResultados();
    }

    public ParametrosReemplazo parametrosModelo() {
        return new ParametrosReemplazo(componentes.getEntero(), horizonte.getDecimal(),
                mediaVida.getDecimal(), desviacionVida.getDecimal(),
                costoComponente.getDecimal(), costoHora.getDecimal(),
                paroIndividual.getDecimal(), paroTotal.getDecimal());
    }

    public PoliticaReemplazo politicaElegida() {
        return switch (politica.seleccionado()) {
            case 0 -> PoliticaReemplazo.INDIVIDUAL;
            case 1 -> PoliticaReemplazo.TOTAL;
            default -> null;
        };
    }

    public PoliticaReemplazo politicaVisual() {
        PoliticaReemplazo elegida = politicaElegida();
        return elegida == null ? PoliticaReemplazo.INDIVIDUAL : elegida;
    }

    public int corridas() {
        return corridas.getEntero();
    }

    public int eventosVisibles() {
        return eventosVisibles.getEntero();
    }

    public long semilla() {
        return semilla.getEnteroLargo();
    }

    public boolean parametrosValidos() {
        for (CampoNumerico campo : campos()) {
            if (!campo.esValido()) {
                return false;
            }
        }
        return true;
    }

    public TablaTecnica tabla() {
        return tabla;
    }

    public GraficaBarrasApiladas barras() {
        return barras;
    }

    public GraficaLineas ritmo() {
        return ritmo;
    }

    public void habilitarAcciones(boolean activo) {
        experimento.setEnabled(activo && parametrosValidos());
        visual.setEnabled(activo);
        exportar.setEnabled(activo);
    }

    @Override
    public String claveArchivo() {
        return "ejercicio6";
    }

    @Override
    public void limpiar() {
        tabla.limpiar();
        bitacora.limpiar();
        barras.reiniciar();
        ritmo.limpiarSeries();
        costoA.limpiar();
        costoB.limpiar();
        ganadora.limpiar();
        diferencia.limpiar();
        barra.terminar("Sin corridas.");
    }

    public void mostrarComparacion(ResumenReemplazo a, ResumenReemplazo b) {
        boolean ambas = a != null && b != null;
        int columnaGanadora = !ambas ? -1 : (a.costoTotal() <= b.costoTotal() ? 1 : 2);
        tabla.establecerFilas(filasDe(a, b));
        tabla.resaltarColumna(columnaGanadora);
        tabla.setColoreador((fila, columna, valor) ->
                fila == 5 && columna == columnaGanadora ? Paleta.VERDE : null);

        mostrarCosto(costoA, a, columnaGanadora == 1,
                PoliticaReemplazo.INDIVIDUAL.descripcion(), true);
        mostrarCosto(costoB, b, columnaGanadora == 2,
                PoliticaReemplazo.TOTAL.descripcion(), false);

        if (ambas) {
            ganadora.mostrarTexto(columnaGanadora == 1 ? "Política A" : "Política B", Paleta.AMBAR);
            ganadora.mostrarDetalle("por costo del horizonte");
            diferencia.mostrarTexto(Formatos.dinero(Math.abs(a.costoTotal() - b.costoTotal())),
                    Paleta.VERDE);
            diferencia.mostrarDetalle("a favor de la ganadora");
        } else {
            ganadora.limpiar();
            diferencia.limpiar();
            ganadora.mostrarDetalle("elija Comparar para enfrentarlas");
        }
        dibujarBarras(a, b, columnaGanadora - 1);
    }

    public void mostrarCorridaVisual(ResultadoCorridaReemplazo resultado) {
        bitacora.limpiar();
        bitacora.escribirLinea("Corrida visual, " + resultado.politica().nombreCorto()
                + ": " + resultado.politica().descripcion(), Bitacora.Estilo.TITULO);
        bitacora.escribirLinea(
                " evento  componente  instante   vida generada  paro  piezas  costo acumulado",
                Bitacora.Estilo.TENUE);
        int limite = Math.min(eventosVisibles(), resultado.traza().size());
        for (int i = 0; i < limite; i++) {
            EventoFalla e = resultado.traza().get(i);
            bitacora.escribirLinea(String.format(Formatos.locale(),
                    "%7d  %10d  %8.1f  %13.1f  %4.0f  %6d  %15s",
                    e.numero(), e.componente(), e.instante(), e.vidaGenerada(), e.horasParo(),
                    e.componentesAcumulados(), Formatos.dinero(e.costoAcumulado())));
        }
        bitacora.separar();
        bitacora.escribirLinea("Intervenciones del horizonte  " + resultado.intervenciones(),
                Bitacora.Estilo.ACENTO);
        bitacora.escribirLinea("Componentes consumidos  " + resultado.componentesConsumidos(),
                Bitacora.Estilo.NEUTRO);
        bitacora.escribirLinea("Horas de desconexión  "
                + Formatos.decimal(resultado.horasParo()), Bitacora.Estilo.RECHAZADO);
        bitacora.escribirLinea("Costo total  " + Formatos.dinero(resultado.costoTotal()),
                Bitacora.Estilo.ACEPTADO);
        bitacora.irAlInicio();
    }

    /** Intervenciones acumuladas contra el tiempo: la pendiente es la frecuencia. */
    public void mostrarRitmo(ResultadoCorridaReemplazo a, ResultadoCorridaReemplazo b) {
        ritmo.limpiarSeries();
        ritmo.rotular("Intervenciones acumuladas", "Horas de operación", "Intervenciones");
        if (a != null) {
            double[][] serie = acumular(a);
            ritmo.agregarSerie("Política A", serie[0], serie[1], Paleta.AMBAR, false);
        }
        if (b != null) {
            double[][] serie = acumular(b);
            ritmo.agregarSerie("Política B", serie[0], serie[1], Paleta.AZUL, false);
        }
        ritmo.ajustar(0, 0);
    }

    private static double[][] acumular(ResultadoCorridaReemplazo resultado) {
        int puntos = resultado.traza().size() + 1;
        double[] ejeX = new double[puntos];
        double[] ejeY = new double[puntos];
        for (int i = 1; i < puntos; i++) {
            ejeX[i] = resultado.traza().get(i - 1).instante();
            ejeY[i] = i;
        }
        return new double[][] { ejeX, ejeY };
    }

    private void mostrarCosto(TarjetaMetrica tarjeta, ResumenReemplazo resumen, boolean gana,
            String detalle, boolean animada) {
        if (resumen == null) {
            tarjeta.limpiar();
            tarjeta.mostrarDetalle(detalle);
            return;
        }
        Color color = gana ? Paleta.VERDE : Paleta.RETICULA;
        if (animada) {
            tarjeta.mostrarNumeroAnimado(resumen.costoTotal(), Formatos::dinero, color);
        } else {
            tarjeta.mostrarTexto(Formatos.dinero(resumen.costoTotal()), color);
        }
        tarjeta.mostrarDetalle(detalle);
    }

    private void dibujarBarras(ResumenReemplazo a, ResumenReemplazo b, int ganadoraCero) {
        List<String> categorias = new ArrayList<>();
        List<double[]> valores = new ArrayList<>();
        if (a != null) {
            categorias.add("Política A");
            valores.add(new double[] { a.costoComponentes(), a.costoParo() });
        }
        if (b != null) {
            categorias.add("Política B");
            valores.add(new double[] { b.costoComponentes(), b.costoParo() });
        }
        barras.rotular("Componentes frente a desconexión", "", "Dólares");
        barras.mostrar(categorias.toArray(new String[0]),
                new String[] { "Componentes", "Desconexión" },
                valores.toArray(new double[0][]),
                new Color[] { Paleta.AZUL, Paleta.ROJO },
                categorias.size() == 2 ? ganadoraCero : -1);
    }

    private List<Object[]> filasDe(ResumenReemplazo a, ResumenReemplazo b) {
        double[] valoresA = indicadoresDe(a);
        double[] valoresB = indicadoresDe(b);
        List<Object[]> filas = new ArrayList<>();
        for (int i = 0; i < INDICADORES.length; i++) {
            boolean esDinero = i >= 3;
            filas.add(new Object[] { INDICADORES[i],
                    celda(a, valoresA[i], esDinero), celda(b, valoresB[i], esDinero) });
        }
        return filas;
    }

    private static double[] indicadoresDe(ResumenReemplazo r) {
        if (r == null) {
            return new double[INDICADORES.length];
        }
        return new double[] { r.intervenciones(), r.componentesConsumidos(), r.horasParo(),
                r.costoComponentes(), r.costoParo(), r.costoTotal(), r.costoPorHora() };
    }

    private static String celda(ResumenReemplazo r, double valor, boolean esDinero) {
        if (r == null) {
            return "—";
        }
        return esDinero ? Formatos.dinero(valor) : Formatos.decimal(valor);
    }

    private CampoNumerico[] campos() {
        return new CampoNumerico[] { corridas, horizonte, componentes, mediaVida, desviacionVida,
                costoComponente, costoHora, paroIndividual, paroTotal, eventosVisibles, semilla };
    }

    private void armarParametros() {
        parametros.agregar(politica);
        parametros.agregar(corridas);
        parametros.agregarAccesosRapidos("Corridas de uso frecuente",
                new String[] { "1.000", "5.000" },
                indice -> corridas.setValor(indice == 0 ? 1000 : 5000));
        parametros.agregar(semilla);

        parametros.agregarSubtitulo("Equipo");
        parametros.agregarFila(componentes, horizonte);
        parametros.agregarFila(mediaVida, desviacionVida);
        parametros.agregarNota("Vida por teorema del límite central, doce uniformes.");

        parametros.agregarSubtitulo("Costos y paros");
        parametros.agregarFila(costoComponente, costoHora);
        parametros.agregarFila(paroIndividual, paroTotal);

        parametros.agregarSubtitulo("Corrida visual");
        parametros.agregar(eventosVisibles);

        Runnable revisar = () -> experimento.setEnabled(parametrosValidos());
        for (CampoNumerico campo : campos()) {
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
        tabla.definirColumnas(new String[] { "Indicador", "Política A", "Política B" },
                new boolean[] { false, true, true });

        resultados.add(filaMetricas(costoA, costoB, ganadora, diferencia), BorderLayout.NORTH);

        JPanel izquierda = new JPanel(new BorderLayout(0, Medidas.E3));
        izquierda.setOpaque(false);
        tabla.setPreferredSize(new Dimension(320, 250));
        izquierda.add(tabla, BorderLayout.NORTH);
        izquierda.add(bitacora, BorderLayout.CENTER);
        bitacora.invitar("Pulse «Corrida visual» para ver los primeros eventos de falla.");

        JPanel derecha = new JPanel(new BorderLayout(0, Medidas.E3));
        derecha.setOpaque(false);
        ritmo.setPreferredSize(new Dimension(320, 190));
        derecha.add(barras, BorderLayout.CENTER);
        derecha.add(ritmo, BorderLayout.SOUTH);

        resultados.add(division(izquierda, derecha, 0.52), BorderLayout.CENTER);
    }
}
