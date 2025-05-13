package analizador.lexico;

/**
 * Enumera todos los tipos de tokens posibles en el lenguaje
 */
public enum TipoToken {
    // Palabras reservadas
    ROBOT,

    // Métodos y propiedades
    INICIAR, DETENER,
    BASE, CUERPO, GARRA, VELOCIDAD,
    ABRIR_GARRA, CERRAR_GARRA, REPETIR,

    // Operadores y símbolos
    PUNTO, IGUAL, PARENTESIS_A, PARENTESIS_C,
    LLAVE_A, LLAVE_C, COMA, PUNTO_COMA,

    // Tipos de datos
    IDENTIFICADOR, NUMERO,

    // Especiales
    ERROR, EOF
}