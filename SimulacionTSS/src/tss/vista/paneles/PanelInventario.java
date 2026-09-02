package tss.vista.paneles;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import tss.modelo.inventario.DiaInventario;
import tss.modelo.inventario.ParametrosInventario;
import tss.modelo.inventario.PoliticaInventario;
import tss.modelo.inventario.ResultadoCorridaInventario;
import tss.modelo.inventario.ResumenPolitica;
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
 * Panel de la politica de inventario: compara la revision periodica contra el
 * punto de reorden y muestra el diente de sierra del inventario diario.
 * Informe: seccion 5.7.
 */
@SuppressWarnings({ "serial", "this-escape" })
public final class PanelInventario extends PanelEjercicio {

    private static final String[] INDICADORES = {
            "Costo de mantener", "Costo de faltante", "Costo de ordenar",
            "Costo total anual", "Costo por día", "Órdenes por año",
            "Unidades faltantes" };

    private final SelectorOpcion politica =
            new SelectorOpcion("Política 1", "Política 2", "Comparar");
    private final CampoNumerico corridas =
            CampoNumerico.deEntero("Corridas", 100, 50000, "corridas", 5000);
    private final CampoNumerico horizonte =
            CampoNumerico.deEntero("Horizonte", 30, 3650, "días", 365);
    private final CampoNumerico inventarioInicial =
            CampoNumerico.deEntero("Inventario inicial", 0, 1000, "artículos", 30);
    private final CampoNumerico nivelObjetivo =
            CampoNumerico.deEntero("Nivel objetivo", 1, 1000, "artículos", 30);
    private final CampoNumerico puntoReorden =
            CampoNumerico.deEntero("Punto de reorden", 0, 1000, "artículos", 10);
    private final CampoNumerico periodoRevision =
            CampoNumerico.deEntero("Periodo de revisión", 1, 365, "días", 8);
    private final CampoNumerico costoMantener =
            CampoNumerico.deDecimal("Costo de mantener", 0, 10000, "dólares", 1);
    private final CampoNumerico costoFaltante =
            CampoNumerico.deDecimal("Costo de faltante", 0, 10000, "dólares", 10);
    private final CampoNumerico costoOrdenar =
            CampoNumerico.deDecimal("Costo de ordenar", 0, 10000, "dólares", 50);
    private final CampoNumerico diasVisibles =
            CampoNumerico.deEntero("Días a mostrar", 5, 120, "días", 20);
    private final CampoNumerico semilla =
            CampoNumerico.deEntero("Semilla", 1, 999999999, "para la semilla", 12345);

    private final TarjetaMetrica costoUno = new TarjetaMetrica("Costo total, política 1");
    private final TarjetaMetrica costoDos = new TarjetaMetrica("Costo total, política 2");
    private final TarjetaMetrica ganadora = new TarjetaMetrica("Política más económica");
    private final TarjetaMetrica ahorro = new TarjetaMetrica("Ahorro anual");

    private final TablaTecnica tabla = new TablaTecnica();
    private final Bitacora bitacora = new Bitacora();
    private final GraficaBarrasApiladas barras = new GraficaBarrasApiladas();
    private final GraficaLineas evolucion = new GraficaLineas();

    private final BotonAccion experimento = BotonAccion.primaria("Ejecutar experimento");
    private final BotonAccion visual = BotonAccion.secundaria("Corrida visual");
    private final BotonAccion exportar = BotonAccion.secundaria("Exportar");

    public PanelInventario() {
        super("Ejercicio 5 — Política de inventario",
                "¿Cuál de las dos políticas de reabastecimiento cuesta menos al año?");
        politica.seleccionar(2);
        armarParametros();
        armarResultados();
    }

    public ParametrosInventario parametrosModelo() {
        return new ParametrosInventario(horizonte.getEntero(), inventarioInicial.getEntero(),
                nivelObjetivo.getEntero(), puntoReorden.getEntero(), periodoRevision.getEntero(),
                costoMantener.getDecimal(), costoFaltante.getDecimal(), costoOrdenar.getDecimal(),
                6, 0.5);
    }

    /** Devuelve null cuando el usuario pidio comparar las dos politicas. */
    public PoliticaInventario politicaElegida() {
        return switch (politica.seleccionado()) {
            case 0 -> PoliticaInventario.PERIODICA;
            case 1 -> PoliticaInventario.PUNTO_REORDEN;
            default -> null;
        };
    }

    /** La corrida visual necesita una politica concreta; al comparar usa la segunda. */
    public PoliticaInventario politicaVisual() {
        PoliticaInventario elegida = politicaElegida();
        return elegida == null ? PoliticaInventario.PUNTO_REORDEN : elegida;
    }

    public int corridas() {
        return corridas.getEntero();
    }

    public int diasVisibles() {
        return diasVisibles.getEntero();
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

    public GraficaLineas evolucion() {
        return evolucion;
    }

    public void habilitarAcciones(boolean activo) {
        experimento.setEnabled(activo && parametrosValidos());
        visual.setEnabled(activo);
        exportar.setEnabled(activo);
    }

    @Override
    public String claveArchivo() {
        return "ejercicio5";
    }

    @Override
    public void limpiar() {
        tabla.limpiar();
        bitacora.limpiar();
        barras.reiniciar();
        evolucion.limpiarSeries();
        costoUno.limpiar();
        costoDos.limpiar();
        ganadora.limpiar();
        ahorro.limpiar();
        barra.terminar("Sin corridas.");
    }

    /** Cualquiera de las dos puede venir en nulo si solo se corrio una politica. */
    public void mostrarComparacion(ResumenPolitica uno, ResumenPolitica dos) {
        boolean ambas = uno != null && dos != null;
        int columnaGanadora = !ambas ? -1 : (uno.costoTotal() <= dos.costoTotal() ? 1 : 2);
        tabla.establecerFilas(filasDe(uno, dos));
        tabla.resaltarColumna(columnaGanadora);
        tabla.setColoreador((fila, columna, valor) ->
                fila == 3 && columna == columnaGanadora ? Paleta.VERDE : null);

        mostrarCosto(costoUno, uno, columnaGanadora == 1,
                PoliticaInventario.PERIODICA.descripcion(), false);
        mostrarCosto(costoDos, dos, columnaGanadora == 2,
                PoliticaInventario.PUNTO_REORDEN.descripcion(), true);

        if (ambas) {
            ganadora.mostrarTexto(columnaGanadora == 1 ? "Política 1" : "Política 2", Paleta.AMBAR);
            ganadora.mostrarDetalle("por costo total anual");
            ahorro.mostrarTexto(Formatos.dinero(Math.abs(uno.costoTotal() - dos.costoTotal())),
                    Paleta.VERDE);
            ahorro.mostrarDetalle("frente a la otra política");
        } else {
            ganadora.limpiar();
            ahorro.limpiar();
            ganadora.mostrarDetalle("elija Comparar para enfrentarlas");
        }
        dibujarBarras(uno, dos, columnaGanadora - 1);
    }

    private void mostrarCosto(TarjetaMetrica tarjeta, ResumenPolitica resumen, boolean gana,
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

    private void dibujarBarras(ResumenPolitica uno, ResumenPolitica dos, int ganadoraCero) {
        List<String> categorias = new ArrayList<>();
        List<double[]> valores = new ArrayList<>();
        if (uno != null) {
            categorias.add("Política 1");
            valores.add(new double[] {
                    uno.costoMantener(), uno.costoFaltante(), uno.costoOrdenar() });
        }
        if (dos != null) {
            categorias.add("Política 2");
            valores.add(new double[] {
                    dos.costoMantener(), dos.costoFaltante(), dos.costoOrdenar() });
        }
        barras.rotular("Composición del costo anual", "", "Dólares");
        barras.mostrar(categorias.toArray(new String[0]),
                new String[] { "Mantener", "Faltante", "Ordenar" },
                valores.toArray(new double[0][]),
                new Color[] { Paleta.AZUL, Paleta.ROJO, Paleta.AMBAR },
                categorias.size() == 2 ? ganadoraCero : -1);
    }

    public void mostrarCorridaVisual(ResultadoCorridaInventario resultado) {
        bitacora.limpiar();
        bitacora.escribirLinea("Corrida visual, " + resultado.politica().nombreCorto()
                + ": " + resultado.politica().descripcion(), Bitacora.Estilo.TITULO);
        bitacora.escribirLinea(
                " día  inv.ini  llegan  demanda  faltan  inv.fin  posic  ordena     q   L  llega",
                Bitacora.Estilo.TENUE);
        int limite = Math.min(diasVisibles(), resultado.traza().size());
        for (int i = 0; i < limite; i++) {
            DiaInventario d = resultado.traza().get(i);
            String linea = String.format(Formatos.locale(),
                    "%4d  %7d  %6d  %7d  %6d  %7d  %5d  %6s  %4s  %2s  %5s",
                    d.dia(), d.inventarioInicial(), d.llegadas(), d.demanda(), d.faltante(),
                    d.inventarioFinal(), d.posicion(), d.ordena() ? "sí" : "no",
                    d.ordena() ? String.valueOf(d.cantidad()) : "-",
                    d.ordena() ? String.valueOf(d.tiempoEntrega()) : "-",
                    d.ordena() ? String.valueOf(d.diaLlegada()) : "-");
            bitacora.escribirLinea(linea, d.faltante() > 0 ? Bitacora.Estilo.RECHAZADO
                    : d.ordena() ? Bitacora.Estilo.ACENTO : Bitacora.Estilo.NEUTRO);
        }
        bitacora.separar();
        bitacora.escribirLinea("Costo total de la corrida  "
                + Formatos.dinero(resultado.costoTotal()), Bitacora.Estilo.ACEPTADO);
        bitacora.irAlInicio();
        dibujarEvolucion(resultado);
    }

    private void dibujarEvolucion(ResultadoCorridaInventario resultado) {
        // Solo los dias que pidio la corrida visual: el diente de sierra se lee
        // en veinte dias y desaparece si se dibuja el ano entero.
        int puntos = Math.min(diasVisibles(), resultado.traza().size());
        double[] ejeX = new double[puntos];
        double[] ejeY = new double[puntos];
        for (int i = 0; i < puntos; i++) {
            ejeX[i] = resultado.traza().get(i).dia();
            ejeY[i] = resultado.traza().get(i).inventarioFinal();
        }
        evolucion.limpiarSeries();
        evolucion.rotular("Inventario de los primeros " + puntos + " días", "Día", "Artículos");
        evolucion.agregarSerie("Inventario", ejeX, ejeY, Paleta.AMBAR, false);
        evolucion.lineaReferencia(puntoReorden.getEntero(), "punto de reorden",
                Paleta.TINTA_SUAVE);
        evolucion.sombrearNegativo(true);
        evolucion.marcarMaximo(false, "decimal");
        evolucion.ajustar(0, 0);
    }

    private List<Object[]> filasDe(ResumenPolitica uno, ResumenPolitica dos) {
        double[] valoresUno = indicadoresDe(uno);
        double[] valoresDos = indicadoresDe(dos);
        List<Object[]> filas = new ArrayList<>();
        for (int i = 0; i < INDICADORES.length; i++) {
            boolean esDinero = i <= 4;
            filas.add(new Object[] { INDICADORES[i],
                    celda(uno, valoresUno[i], esDinero), celda(dos, valoresDos[i], esDinero) });
        }
        return filas;
    }

    private static double[] indicadoresDe(ResumenPolitica r) {
        if (r == null) {
            return new double[INDICADORES.length];
        }
        return new double[] { r.costoMantener(), r.costoFaltante(), r.costoOrdenar(),
                r.costoTotal(), r.costoPorDia(), r.ordenesPorAnio(),
                r.unidadesFaltantesPorAnio() };
    }

    private static String celda(ResumenPolitica r, double valor, boolean esDinero) {
        if (r == null) {
            return "—";
        }
        return esDinero ? Formatos.dinero(valor) : Formatos.decimal(valor);
    }

    private CampoNumerico[] campos() {
        return new CampoNumerico[] { corridas, horizonte, inventarioInicial, nivelObjetivo,
                puntoReorden, periodoRevision, costoMantener, costoFaltante, costoOrdenar,
                diasVisibles, semilla };
    }

    private void armarParametros() {
        parametros.agregar(politica);
        parametros.agregar(corridas);
        parametros.agregarAccesosRapidos("Corridas de uso frecuente",
                new String[] { "1.000", "5.000" },
                indice -> corridas.setValor(indice == 0 ? 1000 : 5000));
        parametros.agregar(semilla);

        parametros.agregarSubtitulo("Reglas de reabastecimiento");
        parametros.agregarFila(nivelObjetivo, puntoReorden);
        parametros.agregarFila(periodoRevision, inventarioInicial);
        parametros.agregar(horizonte);

        parametros.agregarSubtitulo("Costos unitarios");
        parametros.agregarFila(costoMantener, costoFaltante);
        parametros.agregar(costoOrdenar);
        parametros.agregarNota("Demanda diaria binomial de 6 ensayos con θ = 0,5.");
        parametros.agregarNota("Entrega Poisson tabulada con λ = 3.");

        parametros.agregarSubtitulo("Corrida visual");
        parametros.agregar(diasVisibles);

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
        tabla.definirColumnas(new String[] { "Indicador", "Política 1", "Política 2" },
                new boolean[] { false, true, true });

        resultados.add(filaMetricas(costoUno, costoDos, ganadora, ahorro), BorderLayout.NORTH);

        JPanel izquierda = new JPanel(new BorderLayout(0, Medidas.E3));
        izquierda.setOpaque(false);
        tabla.setPreferredSize(new Dimension(320, 250));
        izquierda.add(tabla, BorderLayout.NORTH);
        izquierda.add(bitacora, BorderLayout.CENTER);
        bitacora.invitar("Pulse «Corrida visual» para ver el inventario día a día.");

        JPanel derecha = new JPanel(new BorderLayout(0, Medidas.E3));
        derecha.setOpaque(false);
        evolucion.setPreferredSize(new Dimension(320, 190));
        derecha.add(barras, BorderLayout.CENTER);
        derecha.add(evolucion, BorderLayout.SOUTH);

        resultados.add(division(izquierda, derecha, 0.52), BorderLayout.CENTER);
    }
}
