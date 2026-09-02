package tss.vista.paneles;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import tss.modelo.renta.DiaRenta;
import tss.modelo.renta.ParametrosRenta;
import tss.modelo.renta.ResultadoAnioRenta;
import tss.modelo.renta.ResumenFlota;
import tss.vista.PanelEjercicio;
import tss.vista.componentes.Bitacora;
import tss.vista.componentes.BotonAccion;
import tss.vista.componentes.CampoNumerico;
import tss.vista.componentes.TablaTecnica;
import tss.vista.componentes.TarjetaMetrica;
import tss.vista.diseno.Formatos;
import tss.vista.diseno.Medidas;
import tss.vista.diseno.Paleta;
import tss.vista.graficas.GraficaLineas;

/**
 * Panel de la renta de autos: barre el tamano de flota, promedia corridas
 * independientes y senala el N que maximiza la utilidad anual.
 * Informe: seccion 5.5.
 */
@SuppressWarnings({ "serial", "this-escape" })
public final class PanelRenta extends PanelEjercicio {

    private final CampoNumerico flotaMinima =
            CampoNumerico.deEntero("Flota mínima", 1, 30, "autos", 1);
    private final CampoNumerico flotaMaxima =
            CampoNumerico.deEntero("Flota máxima", 1, 30, "autos", 8);
    private final CampoNumerico corridas =
            CampoNumerico.deEntero("Corridas", 100, 50000, "corridas", 5000);
    private final CampoNumerico flotaVisual =
            CampoNumerico.deEntero("Flota de la corrida visual", 1, 30, "autos", 5);
    private final CampoNumerico diasVisibles =
            CampoNumerico.deEntero("Días a mostrar", 5, 365, "días", 30);
    private final CampoNumerico semilla =
            CampoNumerico.deEntero("Semilla", 1, 999999999, "para la semilla", 12345);

    private final CampoNumerico renta =
            CampoNumerico.deDecimal("Renta por auto y día", 1, 100000, "bolivianos", 350);
    private final CampoNumerico costoFaltante =
            CampoNumerico.deDecimal("Costo de faltante", 0, 100000, "bolivianos", 200);
    private final CampoNumerico costoOcioso =
            CampoNumerico.deDecimal("Costo de ociosidad", 0, 100000, "bolivianos", 50);
    private final CampoNumerico costoAuto =
            CampoNumerico.deDecimal("Costo anual por auto", 0, 10000000, "bolivianos", 75000);
    private final CampoNumerico dias =
            CampoNumerico.deEntero("Días de operación", 30, 3650, "días", 365);

    private final TarjetaMetrica optima = new TarjetaMetrica("Flota óptima");
    private final TarjetaMetrica utilidad = new TarjetaMetrica("Utilidad promedio");
    private final TarjetaMetrica faltantes = new TarjetaMetrica("Faltantes por año");
    private final TarjetaMetrica ociosos = new TarjetaMetrica("Autos ociosos por año");

    private final TablaTecnica tabla = new TablaTecnica();
    private final Bitacora bitacora = new Bitacora();
    private final GraficaLineas grafica = new GraficaLineas();

    private final BotonAccion experimento = BotonAccion.primaria("Ejecutar experimento");
    private final BotonAccion visual = BotonAccion.secundaria("Corrida visual");
    private final BotonAccion exportar = BotonAccion.secundaria("Exportar");

    public PanelRenta() {
        super("Ejercicio 3 — Renta de autos",
                "¿Cuántos autos conviene comprar para maximizar la utilidad anual?");
        armarParametros();
        armarResultados();
    }

    public ParametrosRenta parametrosModelo() {
        return new ParametrosRenta(renta.getDecimal(), costoFaltante.getDecimal(),
                costoOcioso.getDecimal(), costoAuto.getDecimal(), dias.getEntero());
    }

    public int flotaMinima() {
        return Math.min(flotaMinima.getEntero(), flotaMaxima.getEntero());
    }

    public int flotaMaxima() {
        return Math.max(flotaMinima.getEntero(), flotaMaxima.getEntero());
    }

    public int corridas() {
        return corridas.getEntero();
    }

    public int flotaVisual() {
        return flotaVisual.getEntero();
    }

    public int diasVisibles() {
        return diasVisibles.getEntero();
    }

    public long semilla() {
        return semilla.getEnteroLargo();
    }

    public boolean parametrosValidos() {
        return flotaMinima.esValido() && flotaMaxima.esValido() && corridas.esValido()
                && flotaVisual.esValido() && diasVisibles.esValido() && semilla.esValido()
                && renta.esValido() && costoFaltante.esValido() && costoOcioso.esValido()
                && costoAuto.esValido() && dias.esValido();
    }

    public TablaTecnica tabla() {
        return tabla;
    }

    public GraficaLineas grafica() {
        return grafica;
    }

    public void habilitarAcciones(boolean activo) {
        experimento.setEnabled(activo && parametrosValidos());
        visual.setEnabled(activo);
        exportar.setEnabled(activo);
    }

    @Override
    public String claveArchivo() {
        return "ejercicio3";
    }

    @Override
    public void limpiar() {
        tabla.limpiar();
        bitacora.limpiar();
        grafica.limpiarSeries();
        optima.limpiar();
        utilidad.limpiar();
        faltantes.limpiar();
        ociosos.limpiar();
        barra.terminar("Sin corridas.");
    }

    public void mostrarResumen(List<ResumenFlota> resumenes, ResumenFlota mejor) {
        List<Object[]> filas = new ArrayList<>();
        int indiceMejor = -1;
        for (int i = 0; i < resumenes.size(); i++) {
            ResumenFlota r = resumenes.get(i);
            if (mejor != null && r.n() == mejor.n()) {
                indiceMejor = i;
            }
            filas.add(new Object[] {
                    Formatos.entero(r.n()),
                    Formatos.dinero(r.ingresoPromedio()),
                    Formatos.decimal(r.faltantesPromedio()),
                    Formatos.decimal(r.ociososPromedio()),
                    Formatos.dinero(r.costoFlota()),
                    Formatos.dinero(r.utilidadPromedio()) });
        }
        tabla.establecerFilas(filas);
        tabla.resaltarFila(indiceMejor);
        final int fijo = indiceMejor;
        tabla.setColoreador((fila, columna, valor) -> {
            if (columna != 5) {
                return null;
            }
            if (fila == fijo) {
                return Paleta.VERDE;
            }
            return resumenes.get(fila).utilidadPromedio() < 0 ? Paleta.ROJO : null;
        });

        if (mejor != null) {
            optima.mostrarTexto("N = " + mejor.n(), Paleta.AMBAR);
            optima.mostrarDetalle("entre " + flotaMinima() + " y " + flotaMaxima() + " autos");
            utilidad.mostrarNumeroAnimado(mejor.utilidadPromedio(), Formatos::dinero,
                    mejor.utilidadPromedio() >= 0 ? Paleta.VERDE : Paleta.ROJO);
            utilidad.mostrarDetalle("promedio de " + Formatos.entero(mejor.corridas())
                    + " corridas");
            faltantes.mostrarTexto(Formatos.decimal(mejor.faltantesPromedio()), Paleta.ROJO);
            ociosos.mostrarTexto(Formatos.decimal(mejor.ociososPromedio()), Paleta.RETICULA);
        }
        dibujarGrafica(resumenes);
    }

    public void mostrarCorridaVisual(ResultadoAnioRenta resultado) {
        bitacora.limpiar();
        bitacora.escribirLinea("Corrida visual con flota de " + resultado.flota() + " autos",
                Bitacora.Estilo.TITULO);
        bitacora.escribirLinea(
                " día  inicio  vuelven   R1  demanda  rentados  faltan  ociosos   ingreso",
                Bitacora.Estilo.TENUE);
        int limite = Math.min(diasVisibles(), resultado.traza().size());
        for (int i = 0; i < limite; i++) {
            DiaRenta d = resultado.traza().get(i);
            String linea = String.format(Formatos.locale(),
                    "%4d  %6d  %7d  %.4f  %7d  %8d  %6d  %7d  %8.0f",
                    d.dia(), d.disponiblesInicio(), d.retornos(), d.r1(), d.demanda(),
                    d.rentados(), d.faltantes(), d.ociosos(), d.ingresoDia());
            bitacora.escribirLinea(linea, d.faltantes() > 0
                    ? Bitacora.Estilo.RECHAZADO : Bitacora.Estilo.NEUTRO);
        }
        bitacora.separar();
        bitacora.escribirLinea("Ingreso del año  " + Formatos.dinero(resultado.ingreso()),
                Bitacora.Estilo.ACENTO);
        bitacora.escribirLinea("Solicitudes no atendidas  " + resultado.faltantes(),
                Bitacora.Estilo.RECHAZADO);
        bitacora.escribirLinea("Autos ociosos acumulados  " + resultado.ociosos(),
                Bitacora.Estilo.TENUE);
        bitacora.escribirLinea("Utilidad  " + Formatos.dinero(resultado.utilidad()),
                resultado.utilidad() >= 0 ? Bitacora.Estilo.ACEPTADO : Bitacora.Estilo.RECHAZADO);
        bitacora.irAlInicio();
    }

    private void dibujarGrafica(List<ResumenFlota> resumenes) {
        double[] ejeX = new double[resumenes.size()];
        double[] ejeY = new double[resumenes.size()];
        for (int i = 0; i < resumenes.size(); i++) {
            ejeX[i] = resumenes.get(i).n();
            ejeY[i] = resumenes.get(i).utilidadPromedio();
        }
        grafica.limpiarSeries();
        grafica.rotular("Utilidad promedio según el tamaño de flota", "Autos comprados",
                "Utilidad anual");
        grafica.agregarSerie("Utilidad", ejeX, ejeY, Paleta.AMBAR, true);
        grafica.lineaReferencia(0.0, "punto de equilibrio", Paleta.TINTA_TENUE);
        grafica.sombrearNegativo(true);
        grafica.marcarMaximo(true, "dinero");
        grafica.ajustar(0, 0);
    }

    private void armarParametros() {
        parametros.agregarFila(flotaMinima, flotaMaxima);
        parametros.agregar(corridas);
        parametros.agregarAccesosRapidos("Corridas de uso frecuente",
                new String[] { "1.000", "5.000" },
                indice -> corridas.setValor(indice == 0 ? 1000 : 5000));
        parametros.agregar(semilla);

        parametros.agregarSubtitulo("Economía del problema");
        parametros.agregar(renta);
        parametros.agregarFila(costoFaltante, costoOcioso);
        parametros.agregar(costoAuto);
        parametros.agregar(dias);

        parametros.agregarSubtitulo("Corrida visual");
        parametros.agregarFila(flotaVisual, diasVisibles);

        Runnable revisar = () -> experimento.setEnabled(parametrosValidos());
        for (CampoNumerico campo : new CampoNumerico[] { flotaMinima, flotaMaxima, corridas,
                flotaVisual, diasVisibles, semilla, renta, costoFaltante, costoOcioso,
                costoAuto, dias }) {
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
                new String[] { "N", "Ingreso", "Faltantes", "Ociosos", "Costo de flota",
                        "Utilidad" },
                new boolean[] { true, true, true, true, true, true });

        resultados.add(filaMetricas(optima, utilidad, faltantes, ociosos), BorderLayout.NORTH);

        JPanel izquierda = new JPanel(new BorderLayout(0, Medidas.E3));
        izquierda.setOpaque(false);
        bitacora.setPreferredSize(new Dimension(320, 230));
        izquierda.add(tabla, BorderLayout.CENTER);
        izquierda.add(bitacora, BorderLayout.SOUTH);
        bitacora.invitar("Pulse «Corrida visual» para ver el detalle día a día de un año.");

        resultados.add(division(izquierda, grafica, 0.46), BorderLayout.CENTER);
    }
}
