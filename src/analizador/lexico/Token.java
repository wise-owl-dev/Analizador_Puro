package analizador.lexico;

/**
 * Representa un token en el análisis léxico
 */
public class Token {
    private String lexema;
    private TipoToken tipo;
    private int linea;
    private int columna;
    private Object valor;

    public Token(String lexema, TipoToken tipo, int linea, int columna) {
        this.lexema = lexema;
        this.tipo = tipo;
        this.linea = linea;
        this.columna = columna;
    }

    public Token(String lexema, TipoToken tipo, int linea, int columna, Object valor) {
        this.lexema = lexema;
        this.tipo = tipo;
        this.linea = linea;
        this.columna = columna;
        this.valor = valor;
    }

    // Getters
    public String getLexema() {
        return lexema;
    }

    public TipoToken getTipo() {
        return tipo;
    }

    public int getLinea() {
        return linea;
    }

    public int getColumna() {
        return columna;
    }

    public Object getValor() {
        return valor;
    }

    @Override
    public String toString() {
        return String.format("%-20s %-15s línea: %-4d columna: %-4d",
                "'" + lexema + "'", tipo, linea, columna);
    }
}