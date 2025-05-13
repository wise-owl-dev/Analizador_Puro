package analizador.sintactico;

import analizador.lexico.AnalizadorLexico;
import analizador.lexico.Token;
import analizador.lexico.TipoToken;
import analizador.sintactico.TablaSimbolo;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final AnalizadorLexico lexer;
    private Token tokenActual;
    private final TablaSimbolo tablaSimbolo;
    private final List<String> errores;

    public Parser(AnalizadorLexico lexer) {
        this.lexer = lexer;
        this.tablaSimbolo = new TablaSimbolo();
        this.errores = new ArrayList<>();
        avanzar();
    }

    private void avanzar() {
        tokenActual = lexer.siguienteToken();
    }

    private boolean coincidir(TipoToken tipo) {
        if (tokenActual.getTipo() == tipo) {
            avanzar();
            return true;
        }
        return false;
    }

    private void consumir(TipoToken tipo, String mensaje) {
        if (tokenActual.getTipo() == tipo) {
            avanzar();
        } else {
            error(mensaje);
        }
    }

    private void error(String mensaje) {
        String error = "Error sintáctico en línea " + tokenActual.getLinea() +
                ", columna " + tokenActual.getColumna() + ": " + mensaje;
        errores.add(error);
        System.err.println(error);

        // Modo de pánico: avanzar hasta encontrar un punto seguro
        while (tokenActual.getTipo() != TipoToken.EOF &&
                tokenActual.getTipo() != TipoToken.PUNTO_COMA &&
                tokenActual.getTipo() != TipoToken.LLAVE_C) {
            avanzar();
        }

        if (tokenActual.getTipo() != TipoToken.EOF) {
            avanzar(); // Consumir el token de sincronización
        }
    }

    // Punto de entrada del análisis sintáctico
    public void analizar() {
        programa();

        // Verificar errores semánticos adicionales
        List<String> erroresSemanticos = tablaSimbolo.verificarErrores();
        errores.addAll(erroresSemanticos);

        if (errores.isEmpty()) {
            System.out.println("Análisis completado sin errores.");
        } else {
            System.out.println("Análisis completado con " + errores.size() + " errores.");
        }
    }

    // Reglas de la gramática
    private void programa() {
        while (tokenActual.getTipo() != TipoToken.EOF) {
            instruccion();
        }
    }

    private void instruccion() {
        if (tokenActual.getTipo() == TipoToken.ROBOT) {
            declaracionRobot();
        } else if (tokenActual.getTipo() == TipoToken.IDENTIFICADOR) {
            accionRobot();
        } else {
            error("Se esperaba 'Robot' o un identificador");
            avanzar();
        }
    }

    private void declaracionRobot() {
        consumir(TipoToken.ROBOT, "Se esperaba 'Robot'");

        if (tokenActual.getTipo() == TipoToken.IDENTIFICADOR) {
            String nombreRobot = tokenActual.getLexema();
            int linea = tokenActual.getLinea();
            int columna = tokenActual.getColumna();

            if (!tablaSimbolo.agregarSimbolo(nombreRobot, "ROBOT", linea, columna)) {
                error("Robot '" + nombreRobot + "' ya declarado");
            }

            avanzar();
        } else {
            error("Se esperaba un identificador después de 'Robot'");
        }
    }

    private void accionRobot() {
        String idRobot = tokenActual.getLexema();

        if (!tablaSimbolo.simboloExiste(idRobot)) {
            error("Robot '" + idRobot + "' no declarado");
        }

        avanzar();

        if (coincidir(TipoToken.PUNTO)) {
            if (tokenActual.getTipo() == TipoToken.BASE ||
                    tokenActual.getTipo() == TipoToken.CUERPO ||
                    tokenActual.getTipo() == TipoToken.GARRA ||
                    tokenActual.getTipo() == TipoToken.VELOCIDAD) {

                asignacionParametro();

            } else if (tokenActual.getTipo() == TipoToken.INICIAR ||
                    tokenActual.getTipo() == TipoToken.DETENER ||
                    tokenActual.getTipo() == TipoToken.ABRIR_GARRA ||
                    tokenActual.getTipo() == TipoToken.CERRAR_GARRA ||
                    tokenActual.getTipo() == TipoToken.BASE ||
                    tokenActual.getTipo() == TipoToken.CUERPO ||
                    tokenActual.getTipo() == TipoToken.GARRA ||
                    tokenActual.getTipo() == TipoToken.VELOCIDAD) {

                llamadaMetodo();

            } else if (tokenActual.getTipo() == TipoToken.REPETIR) {
                bloqueRepeticion();
            } else {
                error("Se esperaba un método o propiedad válida después del punto");
                avanzar();
            }
        } else {
            error("Se esperaba un punto después del identificador de robot");
        }
    }

    private void asignacionParametro() {
        TipoToken propiedad = tokenActual.getTipo();
        avanzar();

        if (coincidir(TipoToken.IGUAL)) {
            if (tokenActual.getTipo() == TipoToken.NUMERO) {
                int valor = (int) tokenActual.getValor();

                // Validar rangos
                switch (propiedad) {
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

                avanzar();
            } else {
                error("Se esperaba un número");
            }
        } else {
            error("Se esperaba '='");
        }
    }

    private void llamadaMetodo() {
        TipoToken metodo = tokenActual.getTipo();
        avanzar();

        // Métodos sin parámetros
        if (metodo == TipoToken.INICIAR || metodo == TipoToken.DETENER) {
            return;
        }

        // Métodos con parámetros
        if (coincidir(TipoToken.PARENTESIS_A)) {
            if (metodo == TipoToken.ABRIR_GARRA || metodo == TipoToken.CERRAR_GARRA) {
                consumir(TipoToken.PARENTESIS_C, "Se esperaba ')'");
            } else if (tokenActual.getTipo() == TipoToken.NUMERO) {
                int valor = (int) tokenActual.getValor();

                // Validar rangos
                switch (metodo) {
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

                avanzar();
                consumir(TipoToken.PARENTESIS_C, "Se esperaba ')'");
            } else {
                error("Se esperaba un número");
                if (tokenActual.getTipo() != TipoToken.PARENTESIS_C) {
                    avanzar();
                }
                consumir(TipoToken.PARENTESIS_C, "Se esperaba ')'");
            }
        } else if (metodo != TipoToken.INICIAR && metodo != TipoToken.DETENER) {
            error("Se esperaba '('");
        }
    }

    private void bloqueRepeticion() {
        consumir(TipoToken.REPETIR, "Se esperaba 'repetir'");

        consumir(TipoToken.PARENTESIS_A, "Se esperaba '('");

        if (tokenActual.getTipo() == TipoToken.NUMERO) {
            int valor = (int) tokenActual.getValor();
            if (valor <= 0) {
                error("El número de repeticiones debe ser positivo, se encontró: " + valor);
            }
            avanzar();
        } else {
            error("Se esperaba un número");
        }

        consumir(TipoToken.PARENTESIS_C, "Se esperaba ')'");
        consumir(TipoToken.LLAVE_A, "Se esperaba '{'");

        // Cuerpo del bloque de repetición
        while (tokenActual.getTipo() != TipoToken.LLAVE_C &&
                tokenActual.getTipo() != TipoToken.EOF) {
            instruccion();
        }

        consumir(TipoToken.LLAVE_C, "Se esperaba '}'");
    }

    public List<String> getErrores() {
        return errores;
    }

    public TablaSimbolo getTablaSimbolo() {
        return tablaSimbolo;
    }
}
