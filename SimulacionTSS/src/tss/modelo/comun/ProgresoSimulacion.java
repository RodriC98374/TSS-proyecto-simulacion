package tss.modelo.comun;

/**
 * Dato de avance que un simulador publica hacia la interfaz, junto con el
 * contrato Monitor que permite informar progreso y consultar la cancelacion
 * sin que el modelo conozca nada de Swing.
 */
public record ProgresoSimulacion(int hechas, int total, String mensaje) {

    public int porcentaje() {
        return total <= 0 ? 0 : (int) Math.round(100.0 * hechas / total);
    }

    /** Monitor pasivo: no informa nada y nunca cancela. */
    public static final Monitor SILENCIOSO = new Monitor() {
        @Override
        public void reportar(ProgresoSimulacion progreso) {
        }

        @Override
        public boolean cancelado() {
            return false;
        }
    };

    public interface Monitor {

        void reportar(ProgresoSimulacion progreso);

        boolean cancelado();
    }
}
