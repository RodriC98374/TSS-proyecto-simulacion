package tss.controlador;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.swing.SwingWorker;
import tss.modelo.comun.ProgresoSimulacion;

/**
 * SwingWorker generico que corre una simulacion fuera del hilo de interfaz,
 * publica su avance y entrega el resultado ya en el hilo de interfaz. Los cinco
 * controladores concretos lo reutilizan sin variantes.
 */
public final class TareaSimulacion<T> extends SwingWorker<T, ProgresoSimulacion>
        implements ProgresoSimulacion.Monitor {

    private final Function<ProgresoSimulacion.Monitor, T> trabajo;
    private final Consumer<T> alTerminar;
    private final Consumer<ProgresoSimulacion> alAvanzar;
    private final Consumer<Exception> alFallar;
    private final Runnable alCancelar;

    public TareaSimulacion(Function<ProgresoSimulacion.Monitor, T> trabajo,
            Consumer<ProgresoSimulacion> alAvanzar,
            Consumer<T> alTerminar,
            Consumer<Exception> alFallar,
            Runnable alCancelar) {
        this.trabajo = trabajo;
        this.alAvanzar = alAvanzar;
        this.alTerminar = alTerminar;
        this.alFallar = alFallar;
        this.alCancelar = alCancelar;
    }

    @Override
    protected T doInBackground() {
        return trabajo.apply(this);
    }

    @Override
    protected void process(List<ProgresoSimulacion> avances) {
        if (!avances.isEmpty()) {
            alAvanzar.accept(avances.get(avances.size() - 1));
        }
    }

    @Override
    protected void done() {
        if (isCancelled()) {
            alCancelar.run();
            return;
        }
        try {
            alTerminar.accept(get());
        } catch (Exception e) {
            alFallar.accept(e);
        }
    }

    @Override
    public void reportar(ProgresoSimulacion progreso) {
        publish(progreso);
    }

    @Override
    public boolean cancelado() {
        return isCancelled();
    }
}
