package analizador.sintactico;

/**
 * Clase para mostrar la tabla de símbolos en forma de tabla
 */
public class TablaSimbolosUI {
    private final TablaSimbolo tablaSimbolo;

    public TablaSimbolosUI(TablaSimbolo tablaSimbolo) {
        this.tablaSimbolo = tablaSimbolo;
    }

    public void mostrarTabla() {
        System.out.println("\n===== TABLA DE SÍMBOLOS =====");

        // Mostrar robots
        System.out.println("--- ROBOTS ---");
        System.out.println(String.format("%-15s %-10s %-10s %-10s",
                "NOMBRE", "TIPO", "LÍNEA", "COLUMNA"));
        System.out.println("------------------------------------------------");

        for (SimboloInfo simbolo : tablaSimbolo.getSimbolos()) {
            if (simbolo.getTipo().equals("ROBOT")) {
                System.out.println(String.format("%-15s %-10s %-10d %-10d",
                        simbolo.getNombre(),
                        simbolo.getTipo(),
                        simbolo.getLinea(),
                        simbolo.getColumna()));
            }
        }

        // Mostrar métodos
        System.out.println("\n--- MÉTODOS ---");
        System.out.println(String.format("%-15s %-10s %-15s %-15s",
                "NOMBRE", "PARÁMETROS", "RANGO MIN", "RANGO MAX"));
        System.out.println("--------------------------------------------------------");

        for (SimboloInfo metodo : tablaSimbolo.getMetodos()) {
            System.out.println(String.format("%-15s %-10d %-15d %-15d",
                    metodo.getNombre(),
                    metodo.getNumParametros(),
                    metodo.getMinValor(),
                    metodo.getMaxValor()));
        }

        System.out.println("============================\n");
    }
}
