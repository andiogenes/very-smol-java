package tokens

/**
 * Типы лексем.
 */
object TokenType extends Enumeration {
  // Ключевые слова
  val CLASS, VOID, SWITCH, CASE, DEFAULT, BREAK,
  INT, SHORT, LONG, DOUBLE, RETURN,

  // Идентификатор
  IDENTIFIER,

  // Константы
  NUMBER_INT, NUMBER_EXP,

  // Специальные знаки
  DOT, COMMA, COLON, SEMICOLON, LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,

  // Знаки операций
  LESS, LESS_EQUAL, GREATER, GREATER_EQUAL, EQUAL_EQUAL, BANG_EQUAL,
  PLUS, MINUS, STAR, SLASH, PERCENT, EQUAL, OR, AND, PLUS_PLUS, MINUS_MINUS, BANG,

  // Конец исходного модуля и ошибочный символ
  EOF, ERROR = Value
}
