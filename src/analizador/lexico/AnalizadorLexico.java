package analizador.lexico;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Analizador léxico manual para el lenguaje de robots
 */
public class AnalizadorLexico {
    private Reader entrada;
    private StringBuilder lexema;
    private int caracterActual;
    private boolean fin;
    private int linea;
    private int columna;
    private List<Token> tokens;
    private static final Map<String, TipoToken> palabrasReservadas;
    private boolean despuesDePunto;

    // Inicialización de palabras reservadas
    static {
        palabrasReservadas = new HashMap<>();
        palabrasReservadas.put("Robot", TipoToken.ROBOT);
    }

    // Inicialización de propiedades y métodos del robot
    private static final Map<String, TipoToken> metodosRobot;
    static {
        metodosRobot = new HashMap<>();
        metodosRobot.put("iniciar", TipoToken.INICIAR);
        metodosRobot.put("detener", TipoToken.DETENER);
        metodosRobot.put("base", TipoToken.BASE);
        metodosRobot.put("cuerpo", TipoToken.CUERPO);
        metodosRobot.put("garra", TipoToken.GARRA);
        metodosRobot.put("velocidad", TipoToken.VELOCIDAD);
        metodosRobot.put("abrirGarra", TipoToken.ABRIR_GARRA);
        metodosRobot.put("cerrarGarra", TipoToken.CERRAR_GARRA);
        metodosRobot.put("repetir", TipoToken.REPETIR);
    }

    public AnalizadorLexico(Reader entrada) {
        this.entrada = entrada;
        this.lexema = new StringBuilder();
        this.fin = false;
        this.linea = 1;
        this.columna = 0;
        this.tokens = new ArrayList<>();
        this.despuesDePunto = false;
        avanzar();
    }

    // Avanza al siguiente carácter en la entrada
    private void avanzar() {
        try {
            caracterActual = entrada.read();
            if (caracterActual != -1) {
                columna++;
                if (caracterActual == '\n') {
                    linea++;
                    columna = 0;
                }
            }
        } catch (IOException e) {
            error("Error de lectura: " + e.getMessage());
            caracterActual = -1;
        }

        fin = (caracterActual == -1);
    }

    // Registra un error léxico
    private void error(String mensaje) {
        System.err.println("Error léxico en línea " + linea + ", columna " + columna + ": " + mensaje);
    }

    // Agrega un token a la lista
    private void agregarToken(String lexema, TipoToken tipo, int linea, int columna) {
        tokens.add(new Token(lexema, tipo, linea, columna));
    }

    // Agrega un token a la lista con valor
    private void agregarToken(String lexema, TipoToken tipo, int linea, int columna, Object valor) {
        tokens.add(new Token(lexema, tipo, linea, columna, valor));
    }

    // Verifica si ya se llegó al final de la entrada
    public boolean finEntrada() {
        return fin;
    }

    // Obtiene todos los tokens generados
    public List<Token> getTokens() {
        return tokens;
    }

    // Obtiene el siguiente token
    public Token siguienteToken() {
        // Saltar espacios en blanco
        while (!fin && Character.isWhitespace(caracterActual)) {
            avanzar();
        }

        if (fin) {
            Token token = new Token("EOF", TipoToken.EOF, linea, columna);
            tokens.add(token);
            return token;
        }

        // Reiniciar el lexema
        lexema.setLength(0);

        // Posición inicial del token
        int inicioLinea = linea;
        int inicioColumna = columna;

        // Identificadores y palabras reservadas
        if (Character.isLetter(caracterActual)) {
            return identificadorOPalabraReservada(inicioLinea, inicioColumna);
        }

        // Números
        if (Character.isDigit(caracterActual)) {
            return numero(inicioLinea, inicioColumna);
        }

        // Comentarios
        if (caracterActual == '/' && siguienteEs('/')) {
            return manejarComentario(inicioLinea, inicioColumna);
        }

        // Símbolos especiales y operadores
        switch (caracterActual) {
            case '.':
                lexema.append((char) caracterActual);
                avanzar();
                despuesDePunto = true;
                Token token = new Token(lexema.toString(), TipoToken.PUNTO, inicioLinea, inicioColumna);
                tokens.add(token);
                return token;

            case '=':
                lexema.append((char) caracterActual);
                avanzar();
                token = new Token(lexema.toString(), TipoToken.IGUAL, inicioLinea, inicioColumna);
                tokens.add(token);
                return token;

            case '(':
                lexema.append((char) caracterActual);
                avanzar();
                token = new Token(lexema.toString(), TipoToken.PARENTESIS_A, inicioLinea, inicioColumna);
                tokens.add(token);
                return token;

            case ')':
                lexema.append((char) caracterActual);
                avanzar();
                token = new Token(lexema.toString(), TipoToken.PARENTESIS_C, inicioLinea, inicioColumna);
                tokens.add(token);
                return token;

            case '{':
                lexema.append((char) caracterActual);
                avanzar();
                token = new Token(lexema.toString(), TipoToken.LLAVE_A, inicioLinea, inicioColumna);
                tokens.add(token);
                return token;

            case '}':
                lexema.append((char) caracterActual);
                avanzar();
                token = new Token(lexema.toString(), TipoToken.LLAVE_C, inicioLinea, inicioColumna);
                tokens.add(token);
                return token;

            case ',':
                lexema.append((char) caracterActual);
                avanzar();
                token = new Token(lexema.toString(), TipoToken.COMA, inicioLinea, inicioColumna);
                tokens.add(token);
                return token;

            case ';':
                lexema.append((char) caracterActual);
                avanzar();
                token = new Token(lexema.toString(), TipoToken.PUNTO_COMA, inicioLinea, inicioColumna);
                tokens.add(token);
                return token;

            default:
                // Carácter no reconocido
                lexema.append((char) caracterActual);
                avanzar();
                String mensaje = "Símbolo no reconocido: " + lexema.toString();
                error(mensaje);
                token = new Token(lexema.toString(), TipoToken.ERROR, inicioLinea, inicioColumna, mensaje);
                tokens.add(token);
                return token;
        }
    }

    // Verifica si el siguiente carácter es el esperado sin consumirlo
    private boolean siguienteEs(char esperado) {
        try {
            entrada.mark(1);
            int siguiente = entrada.read();
            entrada.reset();
            return siguiente == esperado;
        } catch (IOException e) {
            return false;
        }
    }

    // Maneja identificadores o palabras reservadas
    private Token identificadorOPalabraReservada(int inicioLinea, int inicioColumna) {
        // Consumir letras, dígitos y guiones bajos
        while (!fin && (Character.isLetterOrDigit(caracterActual) || caracterActual == '_')) {
            lexema.append((char) caracterActual);
            avanzar();
        }

        String texto = lexema.toString();

        // Verificar si es una palabra reservada o un identificador
        if (despuesDePunto) {
            despuesDePunto = false;
            TipoToken tipo = metodosRobot.getOrDefault(texto, TipoToken.ERROR);

            if (tipo == TipoToken.ERROR) {
                String mensaje = "Método o propiedad desconocida: " + texto;
                error(mensaje);
                Token token = new Token(texto, tipo, inicioLinea, inicioColumna, mensaje);
                tokens.add(token);
                return token;
            } else {
                Token token = new Token(texto, tipo, inicioLinea, inicioColumna);
                tokens.add(token);
                return token;
            }
        } else {
            TipoToken tipo = palabrasReservadas.getOrDefault(texto, TipoToken.IDENTIFICADOR);
            Token token = new Token(texto, tipo, inicioLinea, inicioColumna);
            tokens.add(token);
            return token;
        }
    }

    // Maneja números (enteros y flotantes)
    private Token numero(int inicioLinea, int inicioColumna) {
        boolean esFlotante = false;

        // Consumir dígitos y posible punto decimal
        while (!fin && (Character.isDigit(caracterActual) || caracterActual == '.')) {
            if (caracterActual == '.') {
                if (esFlotante) {
                    // Ya se encontró un punto decimal antes
                    break;
                }
                esFlotante = true;
            }

            lexema.append((char) caracterActual);
            avanzar();
        }

        String texto = lexema.toString();

        try {
            if (esFlotante) {
                float valor = Float.parseFloat(texto);
                int valorRedondeado = Math.round(valor);
                Token token = new Token(texto, TipoToken.NUMERO, inicioLinea, inicioColumna, valorRedondeado);
                tokens.add(token);
                return token;
            } else {
                int valor = Integer.parseInt(texto);
                Token token = new Token(texto, TipoToken.NUMERO, inicioLinea, inicioColumna, valor);
                tokens.add(token);
                return token;
            }
        } catch (NumberFormatException e) {
            String mensaje = "Número inválido: " + texto;
            error(mensaje);
            Token token = new Token(texto, TipoToken.ERROR, inicioLinea, inicioColumna, mensaje);
            tokens.add(token);
            return token;
        }
    }

    // Maneja comentarios de línea
    private Token manejarComentario(int inicioLinea, int inicioColumna) {
        // Consumir los dos caracteres '//'
        lexema.append((char) caracterActual);
        avanzar();
        lexema.append((char) caracterActual);
        avanzar();

        // Consumir el resto de la línea
        while (!fin && caracterActual != '\n') {
            lexema.append((char) caracterActual);
            avanzar();
        }

        // Consumir el salto de línea
        if (!fin) {
            lexema.append((char) caracterActual);
            avanzar();
        }

        // Los comentarios no generan tokens, así que obtenemos el siguiente
        return siguienteToken();
    }

    // Analiza todo el texto de entrada
    public List<Token> analizar() {
        while (!fin) {
            Token token = siguienteToken();
            if (token.getTipo() == TipoToken.EOF) {
                break;
            }
        }
        return tokens;
    }
}