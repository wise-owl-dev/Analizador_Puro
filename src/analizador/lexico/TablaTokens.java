package analizador.lexico;

import java.util.List;

/**
 * Clase para mostrar los tokens en forma de tabla
 */
public class TablaTokens {
    private final List<Token> tokens;

    public TablaTokens(List<Token> tokens) {
        this.tokens = tokens;
    }

    public void mostrarTabla() {
        System.out.println("\n===== TABLA DE TOKENS =====");
        System.out.println(String.format("%-20s %-15s %-10s %-10s %-10s",
                "LEXEMA", "TIPO", "LÍNEA", "COLUMNA", "VALOR"));
        System.out.println("---------------------------------------------------------------");

        for (Token token : tokens) {
            if (token.getTipo() != TipoToken.EOF) {
                System.out.println(String.format("%-20s %-15s %-10d %-10d %-10s",
                        "'" + token.getLexema() + "'",
                        token.getTipo(),
                        token.getLinea(),
                        token.getColumna(),
                        token.getValor() != null ? token.getValor() : ""));
            }
        }
        System.out.println("===========================\n");
    }
}
