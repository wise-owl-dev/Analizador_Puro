package analizador.sintactico;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tabla de símbolos para el analizador
 */
public class TablaSimbolo {
    private Map<String, SimboloInfo> simbolos;
    private Map<String, SimboloInfo> metodos;

    public TablaSimbolo() {
        simbolos = new HashMap<>();
        metodos = new HashMap<>();

        // Agregar métodos predefinidos con sus rangos
        agregarMetodo("base", 1, 0, 360);
        agregarMetodo("cuerpo", 1, 0, 180);
        agregarMetodo("garra", 1, 0, 90);
        agregarMetodo("velocidad", 1, 1, 100);
        agregarMetodo("abrirGarra", 0, 0, 0);
        agregarMetodo("cerrarGarra", 0, 0, 0);
        agregarMetodo("repetir", 1, 1, Integer.MAX_VALUE);
        agregarMetodo("iniciar", 0, 0, 0);
        agregarMetodo("detener", 0, 0, 0);
    }

    // Actualiza un método con valor, línea y columna
    public void actualizarMetodo(String nombre, Object valor, int linea, int columna) {
        SimboloInfo metodo = metodos.get(nombre);
        if (metodo != null) {
            metodo.setValor(valor);
            metodo.setLinea(linea);
            metodo.setColumna(columna);
        }
    }

    // Agrega un símbolo a la tabla
    public boolean agregarSimbolo(String nombre, String tipo) {
        if (!simbolos.containsKey(nombre)) {
            simbolos.put(nombre, new SimboloInfo(nombre, tipo));
            return true;
        }
        return false;
    }

    // Agrega un símbolo a la tabla con posición
    public boolean agregarSimbolo(String nombre, String tipo, int linea, int columna) {
        if (!simbolos.containsKey(nombre)) {
            simbolos.put(nombre, new SimboloInfo(nombre, tipo, linea, columna));
            return true;
        }
        return false;
    }

    // Actualiza el valor de un método
    public void actualizarValorMetodo(String nombre, Object valor) {
        SimboloInfo metodo = metodos.get(nombre);
        if (metodo != null) {
            metodo.setValor(valor);
        }
    }

    // Agrega un método a la tabla con sus rangos
    private void agregarMetodo(String nombre, int numParametros, int minValor, int maxValor) {
        metodos.put(nombre, new SimboloInfo(nombre, "METODO", null, numParametros, minValor, maxValor));
    }

    // Verifica si existe un símbolo
    public boolean simboloExiste(String nombre) {
        return simbolos.containsKey(nombre);
    }

    // Obtiene información de un símbolo
    public SimboloInfo getSimboloInfo(String nombre) {
        return simbolos.get(nombre);
    }

    // Obtiene información de un método
    public SimboloInfo getMetodoInfo(String nombre) {
        return metodos.get(nombre);
    }

    // Obtiene todos los símbolos
    public List<SimboloInfo> getSimbolos() {
        return new ArrayList<>(simbolos.values());
    }

    // Obtiene todos los métodos
    public List<SimboloInfo> getMetodos() {
        return new ArrayList<>(metodos.values());
    }

    // Verifica errores semánticos adicionales
    public List<String> verificarErrores() {
        List<String> errores = new ArrayList<>();

        // Buscar robots duplicados
        Map<String, SimboloInfo> robotsEncontrados = new HashMap<>();

        for (SimboloInfo simbolo : simbolos.values()) {
            if (simbolo.getTipo().equals("ROBOT")) {
                if (robotsEncontrados.containsKey(simbolo.getNombre())) {
                    SimboloInfo primero = robotsEncontrados.get(simbolo.getNombre());
                    errores.add("Error semántico en línea " + simbolo.getLinea() + ", columna " + simbolo.getColumna() +
                            ": Robot '" + simbolo.getNombre() + "' ya declarado previamente en línea " +
                            primero.getLinea() + ", columna " + primero.getColumna());
                } else {
                    robotsEncontrados.put(simbolo.getNombre(), simbolo);
                }
            }
        }

        return errores;
    }

    // Imprime el contenido de la tabla de símbolos
    public void imprimirTablaSimbolo() {
        System.out.println("===== TABLA DE SÍMBOLOS =====");
        System.out.println("--- Robots ---");
        for (SimboloInfo simbolo : simbolos.values()) {
            if (simbolo.getTipo().equals("ROBOT")) {
                System.out.println(simbolo);
            }
        }

        System.out.println("--- Métodos predefinidos ---");
        for (SimboloInfo metodo : metodos.values()) {
            System.out.println(metodo);
        }
        System.out.println("============================");
    }
}