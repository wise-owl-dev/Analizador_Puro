package analizador.sintactico;

/**
 * Clase para información de símbolos en la tabla de símbolos
 */
public class SimboloInfo {
    private String nombre;
    private String tipo;
    private Object valor;
    private int numParametros;
    private int minValor;
    private int maxValor;
    private int linea;
    private int columna;

    public SimboloInfo(String nombre, String tipo) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.valor = null;
        this.numParametros = 0;
        this.minValor = 0;
        this.maxValor = 0;
        this.linea = 0;
        this.columna = 0;
    }

    public SimboloInfo(String nombre, String tipo, Object valor, int numParametros, int minValor, int maxValor) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.valor = valor;
        this.numParametros = numParametros;
        this.minValor = minValor;
        this.maxValor = maxValor;
        this.linea = 0;
        this.columna = 0;
    }

    public SimboloInfo(String nombre, String tipo, int linea, int columna) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.valor = null;
        this.numParametros = 0;
        this.minValor = 0;
        this.maxValor = 0;
        this.linea = linea;
        this.columna = columna;
    }

    // Getters
    public String getNombre() {
        return nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public Object getValor() {
        return valor;
    }

    public int getNumParametros() {
        return numParametros;
    }

    public int getMinValor() {
        return minValor;
    }

    public int getMaxValor() {
        return maxValor;
    }

    public int getLinea() {
        return linea;
    }

    public int getColumna() {
        return columna;
    }

    // Setters
    public void setValor(Object valor) {
        this.valor = valor;
    }

    public void setLinea(int linea) {
        this.linea = linea;
    }

    public void setColumna(int columna) {
        this.columna = columna;
    }

    @Override
    public String toString() {
        return "SimboloInfo[nombre=" + nombre + ", tipo=" + tipo + ", valor=" + valor +
                ", params=" + numParametros + ", rango=[" + minValor + ".." + maxValor + "], " +
                "pos=(" + linea + "," + columna + ")]";
    }
}