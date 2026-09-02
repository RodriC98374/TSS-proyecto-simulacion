package tss.modelo.reemplazo;

import java.util.ArrayList;
import java.util.List;
import tss.modelo.aleatorio.GeneradorCongruencialMixto;
import tss.modelo.aleatorio.GeneradorVariables;
import tss.modelo.comun.ProgresoSimulacion;
import tss.modelo.comun.ResumenEstadistico;

/**
 * Simulacion orientada a eventos del equipo de cuatro componentes: avanza al
 * proximo instante de falla y aplica la politica de reemplazo correspondiente.
 * Informe: seccion 5.8 (Aplicacion 4 - Reemplazo de componentes).
 */
public final class SimuladorReemplazo {

    /** Tope de eventos que se conservan en la traza de la corrida visual. */
    public static final int MAXIMO_TRAZA = 2000;

    private final ParametrosReemplazo parametros;
    private final long semillaBase;

    private GeneradorVariables generador;
    private boolean guardarTraza;
    private long consumidosPrevios;

    public SimuladorReemplazo(ParametrosReemplazo parametros, long semillaBase) {
        this.parametros = parametros;
        this.semillaBase = semillaBase;
        this.generador = new GeneradorVariables(new GeneradorCongruencialMixto(semillaBase));
        this.guardarTraza = true;
    }

    public ParametrosReemplazo parametros() {
        return parametros;
    }

    public long uniformesConsumidos() {
        return consumidosPrevios + generador.fuente().generados();
    }

    private void reiniciarGenerador(long semilla) {
        consumidosPrevios += generador.fuente().generados();
        generador = new GeneradorVariables(new GeneradorCongruencialMixto(semilla));
    }

    // ═══ CLAVE 5.8 · Algoritmo de resolucion — Reemplazo de componentes ══════
    // Repite: toma el componente de falla mas proxima, la atiende segun la
    // politica y reprograma, hasta que la proxima falla exceda el horizonte.
    // ═════════════════════════════════════════════════════════════════════════
    public ResultadoCorridaReemplazo simularCorrida(PoliticaReemplazo politica) {
        int piezas = parametros.componentes();
        double[] falla = new double[piezas];
        for (int i = 0; i < piezas; i++) {
            falla[i] = generarVida();
        }
        List<EventoFalla> traza = guardarTraza ? new ArrayList<>() : List.of();

        int intervenciones = 0;
        int consumidos = 0;
        double horasParo = 0.0;

        while (true) {
            int m = indiceDeMenor(falla);
            if (falla[m] > parametros.horizonte()) {
                break;
            }
            double reloj = falla[m];
            intervenciones++;
            double vida;
            if (politica == PoliticaReemplazo.INDIVIDUAL) {
                consumidos += 1;
                horasParo += parametros.horasParoIndividual();
                vida = reemplazarIndividual(falla, m, reloj);
            } else {
                consumidos += piezas;
                horasParo += parametros.horasParoTotal();
                vida = reemplazarTodos(falla, reloj);
            }
            if (guardarTraza && traza.size() < MAXIMO_TRAZA) {
                double costo = parametros.costoComponente() * consumidos
                        + parametros.costoHoraParo() * horasParo;
                traza.add(new EventoFalla(intervenciones, reloj, m + 1, vida,
                        politica == PoliticaReemplazo.INDIVIDUAL
                                ? parametros.horasParoIndividual()
                                : parametros.horasParoTotal(),
                        consumidos, costo));
            }
        }

        double costoComponentes = parametros.costoComponente() * consumidos;
        double costoParo = parametros.costoHoraParo() * horasParo;
        return new ResultadoCorridaReemplazo(politica, intervenciones, consumidos, horasParo,
                costoComponentes, costoParo, costoComponentes + costoParo, traza);
    }

    // ═══ CLAVE 5.8b · Reemplazo individual (politica A) ══════════════════════
    // Primero se desplaza la hora de paro a los cuatro instantes de falla,
    // porque durante la desconexion el equipo no opera y nadie envejece, y
    // recien despues se reprograma la pieza sustituida. El orden es normativo.
    // ═════════════════════════════════════════════════════════════════════════
    private double reemplazarIndividual(double[] falla, int m, double reloj) {
        double paro = parametros.horasParoIndividual();
        for (int i = 0; i < falla.length; i++) {
            falla[i] += paro;
        }
        double vida = generarVida();
        falla[m] = reloj + paro + vida;
        return vida;
    }

    /** Politica B: los cuatro componentes salen nuevos tras la desconexion. */
    private double reemplazarTodos(double[] falla, double reloj) {
        double paro = parametros.horasParoTotal();
        double primera = 0.0;
        for (int i = 0; i < falla.length; i++) {
            double vida = generarVida();
            falla[i] = reloj + paro + vida;
            if (i == 0) {
                primera = vida;
            }
        }
        return primera;
    }

    /** Vida por teorema del limite central, acotada a valores no negativos. */
    private double generarVida() {
        double vida = generador.normalPorTeoremaLimiteCentral(
                parametros.mediaVida(), parametros.desviacionVida());
        return Math.max(vida, 0.0);
    }

    private static int indiceDeMenor(double[] valores) {
        int m = 0;
        for (int i = 1; i < valores.length; i++) {
            if (valores[i] < valores[m]) {
                m = i;
            }
        }
        return m;
    }

    public ResumenReemplazo evaluarPolitica(PoliticaReemplazo politica, int corridas,
            ProgresoSimulacion.Monitor monitor) {
        ResumenEstadistico intervenciones = new ResumenEstadistico();
        ResumenEstadistico consumidos = new ResumenEstadistico();
        ResumenEstadistico horas = new ResumenEstadistico();
        ResumenEstadistico costoComponentes = new ResumenEstadistico();
        ResumenEstadistico costoParo = new ResumenEstadistico();
        ResumenEstadistico total = new ResumenEstadistico();
        guardarTraza = false;

        for (int c = 0; c < corridas; c++) {
            if (monitor.cancelado()) {
                break;
            }
            reiniciarGenerador(GeneradorCongruencialMixto.semillaDerivada(semillaBase, c));
            ResultadoCorridaReemplazo r = simularCorrida(politica);
            intervenciones.agregar(r.intervenciones());
            consumidos.agregar(r.componentesConsumidos());
            horas.agregar(r.horasParo());
            costoComponentes.agregar(r.costoComponentes());
            costoParo.agregar(r.costoParo());
            total.agregar(r.costoTotal());
            if (c % 200 == 0 || c == corridas - 1) {
                monitor.reportar(new ProgresoSimulacion(c + 1, corridas,
                        politica.nombreCorto() + ", corrida " + (c + 1)));
            }
        }
        guardarTraza = true;
        return new ResumenReemplazo(politica, corridas, intervenciones.media(), consumidos.media(),
                horas.media(), costoComponentes.media(), costoParo.media(), total.media(),
                total.media() / parametros.horizonte());
    }

    /** Corrida visual: traza completa de eventos con la semilla base. */
    public ResultadoCorridaReemplazo corridaVisual(PoliticaReemplazo politica) {
        reiniciarGenerador(semillaBase);
        guardarTraza = true;
        return simularCorrida(politica);
    }
}
