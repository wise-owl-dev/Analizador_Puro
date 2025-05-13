package analizador.sintactico;

import java.util.ArrayList;
import java.util.List;

import analizador.lexico.TipoToken;
import analizador.lexico.Token;

/**
 * Analizador sintáctico manual (descendente recursivo)
 */
public class AnalizadorSintactico {
    private List<Token> tokens;
    private int posicion;
    private Token tokenActual;
    private List<String> errores;
    private TablaSimbolo tablaSimbolo;

    public AnalizadorSintactico(List<Token> tokens) {
        this.tokens = tokens;
        this.posicion = 0;
        this.errores = new ArrayList<>();
        this.tablaSimbolo = new TablaSimbolo();
        avanzar();
    }

    // Avanza al siguiente token
    private void avanzar() {
        if (posicion < tokens.size()) {
            tokenActual = tokens.get(posicion++);
        } else {
            // Token EOF ficticio si nos pasamos
            tokenActual = new Token("EOF", TipoToken.EOF, 0, 0);
        }
    }

    // Verifica si el token actual coincide con el tipo esperado
    private boolean coincidir(TipoToken tipo) {
        if (tokenActual.getTipo() == tipo) {
            avanzar();
            return true;
        }
        return false;
    }

    // Consume un token del tipo esperado o reporta un error
    private void consumir(TipoToken tipo, String mensaje) {
        if (tokenActual.getTipo() == tipo) {
            avanzar();
        } else {
            error(mensaje);
        }
    }

    // Reporta un error sintáctico
    private void error(String mensaje) {
        String error = "Error sintáctico en línea " + tokenActual.getLinea() +
                ", columna " + tokenActual.getColumna() + ": " + mensaje;
        errores.add(error);
        System.err.println(error);
    }

    // Analiza el programa completo
    public void analizar() {
        programa();

        // Verificar errores semánticos adicionales
        List<String> erroresSemanticos = tablaSimbolo.verificarErrores();
        errores.addAll(erroresSemanticos);
    }

    // Reglas gramaticales

    // programa ::= instruccion*
    private void programa() {
        while (tokenActual.getTipo() != TipoToken.EOF) {
            instruccion();
        }
    }

    // instruccion ::= declaracionRobot | accionRobot
    private void instruccion() {
        if (tokenActual.getTipo() == TipoToken.ROBOT) {
            declaracionRobot();
        } else if (tokenActual.getTipo() == TipoToken.IDENTIFICADOR) {
            accionRobot();
        } else {
            error("Se esperaba 'Robot' o un identificador");
            // Modo de pánico: avanzar hasta encontrar algo reconocible
            while (tokenActual.getTipo() != TipoToken.EOF &&
                    tokenActual.getTipo() != TipoToken.ROBOT &&
                    tokenActual.getTipo() != TipoToken.IDENTIFICADOR) {
                avanzar();
            }
        }
    }

    // declaracionRobot ::= ROBOT IDENTIFICADOR
    private void declaracionRobot() {
        consumir(TipoToken.ROBOT, "Se esperaba 'Robot'");

        if (tokenActual.getTipo() == TipoToken.IDENTIFICADOR) {
            String nombreRobot = tokenActual.getLexema();
            int linea = tokenActual.getLinea();
            int columna = tokenActual.getColumna();

            // Verificar si el robot ya existe
            if (!tablaSimbolo.agregarSimbolo(nombreRobot, "ROBOT", linea, columna)) {
                error("Robot '" + nombreRobot + "' ya declarado");
            }

            avanzar();
        } else {
            error("Se esperaba un identificador después de 'Robot'");
        }
    }

    // accionRobot ::= IDENTIFICADOR PUNTO (asignacionParametro | llamadaMetodo |
    // bloqueRepeticion)
    private void accionRobot() {
        String idRobot = tokenActual.getLexema();

        // Verificar si el robot existe
        if (!tablaSimbolo.simboloExiste(idRobot)) {
            error("Robot '" + idRobot + "' no declarado");
        }

        avanzar(); // Consumir el identificador

        if (coincidir(TipoToken.PUNTO)) {
            // Verificar qué tipo de acción es
            if (tokenActual.getTipo() == TipoToken.BASE ||
                    tokenActual.getTipo() == TipoToken.CUERPO ||
                    tokenActual.getTipo() == TipoToken.GARRA ||
                    tokenActual.getTipo() == TipoToken.VELOCIDAD) {

                // Puede ser asignación o llamada a método con parámetros
                TipoToken tipoPropiedad = tokenActual.getTipo();
                avanzar(); // Consumir el nombre de propiedad

                if (tokenActual.getTipo() == TipoToken.IGUAL) {
                    // Es una asignación
                    retroceder(); // Retroceder para procesar correctamente
                    asignacionParametro();
                } else if (tokenActual.getTipo() == TipoToken.PARENTESIS_A) {
                    // Es una llamada a método con parámetros
                    retroceder(); // Retroceder para procesar correctamente
                    llamadaMetodo();
                } else {
                    error("Se esperaba '=' o '(' después de la propiedad");
                }
            } else if (tokenActual.getTipo() == TipoToken.INICIAR ||
                    tokenActual.getTipo() == TipoToken.DETENER ||
                    tokenActual.getTipo() == TipoToken.ABRIR_GARRA ||
                    tokenActual.getTipo() == TipoToken.CERRAR_GARRA) {
                llamadaMetodo();
            } else if (tokenActual.getTipo() == TipoToken.REPETIR) {
                bloqueRepeticion();
            } else {
                error("Se esperaba un método o propiedad válida después del punto");
                avanzar(); // Consumir el token no reconocido
            }
        } else {
            error("Se esperaba un punto después del identificador de robot");
        }
    }

    // Retrocede una posición en la lista de tokens
    private void retroceder() {
        if (posicion > 1) { // Asegurarse de no retroceder antes del inicio
            posicion -= 2; // Retroceder 2 porque avanzar() incrementa la posición
            avanzar(); // Actualizar tokenActual
        }
    }

    // asignacionParametro ::= (BASE | CUERPO | GARRA | VELOCIDAD) IGUAL NUMERO
    private void asignacionParametro() {
        TipoToken tipoPropiedad = tokenActual.getTipo();
        String nombrePropiedad = tokenActual.getLexema(); // Guardar el nombre de la propiedad
        int linea = tokenActual.getLinea();
        int columna = tokenActual.getColumna();
        avanzar(); // Consumir el nombre de propiedad

        if (coincidir(TipoToken.IGUAL)) {
            if (tokenActual.getTipo() == TipoToken.NUMERO) {
                int valor = (int) tokenActual.getValor();

                // Actualizar en la tabla de símbolos con línea y columna
                tablaSimbolo.actualizarMetodo(nombrePropiedad, valor, linea, columna);

                // AÑADIR AQUÍ: Almacenar el valor en la tabla de símbolos
                SimboloInfo metodoInfo = tablaSimbolo.getMetodoInfo(nombrePropiedad);
                if (metodoInfo != null) {
                    metodoInfo.setValor(valor); // Guardar el valor asignado
                }

                // Validar rangos según el tipo de propiedad
                switch (tipoPropiedad) {
                    case BASE:
                        if (valor < 0 || valor > 360) {
                            error("Valor fuera de rango para 'base': " + valor + " (rango permitido: [0..360])");
                        }
                        break;
                    case CUERPO:
                        if (valor < 0 || valor > 180) {
                            error("Valor fuera de rango para 'cuerpo': " + valor + " (rango permitido: [0..180])");
                        }
                        break;
                    case GARRA:
                        if (valor < 0 || valor > 90) {
                            error("Valor fuera de rango para 'garra': " + valor + " (rango permitido: [0..90])");
                        }
                        break;
                    case VELOCIDAD:
                        if (valor < 1 || valor > 100) {
                            error("Valor fuera de rango para 'velocidad': " + valor + " (rango permitido: [1..100])");
                        }
                        break;
                }

                avanzar(); // Consumir el número
            } else {
                error("Se esperaba un número");
            }
        } else {
            error("Se esperaba '='");
        }
    }

    // llamadaMetodo ::= (INICIAR | DETENER) |
    // (BASE | CUERPO | GARRA | VELOCIDAD) PARENTESIS_A NUMERO PARENTESIS_C |
    // (ABRIR_GARRA | CERRAR_GARRA) PARENTESIS_A PARENTESIS_C
    private void llamadaMetodo() {
        TipoToken tipoMetodo = tokenActual.getTipo();
        String nombreMetodo = tokenActual.getLexema(); // Guardar el nombre del método
        int linea = tokenActual.getLinea();
        int columna = tokenActual.getColumna();
        avanzar(); // Consumir el nombre del método

        // Métodos sin parámetros
        if (tipoMetodo == TipoToken.INICIAR || tipoMetodo == TipoToken.DETENER) {
            // Actualizar posición aunque no tenga parámetros
            tablaSimbolo.actualizarMetodo(nombreMetodo, null, linea, columna);
            return;
        }

        // Métodos con parámetros
        if (coincidir(TipoToken.PARENTESIS_A)) {
            if (tipoMetodo == TipoToken.ABRIR_GARRA || tipoMetodo == TipoToken.CERRAR_GARRA) {
                // Actualizar posición
                tablaSimbolo.actualizarMetodo(nombreMetodo, null, linea, columna);
                consumir(TipoToken.PARENTESIS_C, "Se esperaba ')'");
            } else if (tokenActual.getTipo() == TipoToken.NUMERO) {
                // Métodos con argumentos numéricos
                int valor = (int) tokenActual.getValor();

                // Actualizar valor y posición
                tablaSimbolo.actualizarMetodo(nombreMetodo, valor, linea, columna);

                // AÑADIR AQUÍ: Almacenar el valor en la tabla de símbolos
                SimboloInfo metodoInfo = tablaSimbolo.getMetodoInfo(nombreMetodo);
                if (metodoInfo != null) {
                    metodoInfo.setValor(valor); // Guardar el valor pasado al método
                }

                // Validar rangos según el tipo de método
                switch (tipoMetodo) {
                    case BASE:
                        if (valor < 0 || valor > 360) {
                            error("Valor fuera de rango para 'base': " + valor + " (rango permitido: [0..360])");
                        }
                        break;
                    case CUERPO:
                        if (valor < 0 || valor > 180) {
                            error("Valor fuera de rango para 'cuerpo': " + valor + " (rango permitido: [0..180])");
                        }
                        break;
                    case GARRA:
                        if (valor < 0 || valor > 90) {
                            error("Valor fuera de rango para 'garra': " + valor + " (rango permitido: [0..90])");
                        }
                        break;
                    case VELOCIDAD:
                        if (valor < 1 || valor > 100) {
                            error("Valor fuera de rango para 'velocidad': " + valor + " (rango permitido: [1..100])");
                        }
                        break;
                }

                avanzar(); // Consumir el número
                consumir(TipoToken.PARENTESIS_C, "Se esperaba ')'");
            } else {
                error("Se esperaba un número");
                // Intentar recuperarse
                if (tokenActual.getTipo() != TipoToken.PARENTESIS_C) {
                    avanzar(); // Consumir lo que sea que esté ahí
                }
                consumir(TipoToken.PARENTESIS_C, "Se esperaba ')'");
            }
        } else if (tipoMetodo != TipoToken.INICIAR && tipoMetodo != TipoToken.DETENER) {
            error("Se esperaba '('");
        }
    }

    // bloqueRepeticion ::= REPETIR PARENTESIS_A NUMERO PARENTESIS_C LLAVE_A
    // instruccion* LLAVE_C
    private void bloqueRepeticion() {
        consumir(TipoToken.REPETIR, "Se esperaba 'repetir'");

        consumir(TipoToken.PARENTESIS_A, "Se esperaba '('");

        if (tokenActual.getTipo() == TipoToken.NUMERO) {
            int valor = (int) tokenActual.getValor();
            if (valor <= 0) {
                error("El número de repeticiones debe ser positivo, se encontró: " + valor);
            }
            avanzar(); // Consumir el número
        } else {
            error("Se esperaba un número");
        }

        consumir(TipoToken.PARENTESIS_C, "Se esperaba ')'");
        consumir(TipoToken.LLAVE_A, "Se esperaba '{'");

        // Procesar instrucciones dentro del bloque de repetición
        while (tokenActual.getTipo() != TipoToken.LLAVE_C &&
                tokenActual.getTipo() != TipoToken.EOF) {
            instruccion();
        }

        consumir(TipoToken.LLAVE_C, "Se esperaba '}'");
    }

    // Getters
    public List<String> getErrores() {
        return errores;
    }

    public TablaSimbolo getTablaSimbolo() {
        return tablaSimbolo;
    }
}