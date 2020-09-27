package tokens

/**
 * Лексема.
 * @param tpe Тип лексемы.
 * @param lexeme Представление лексемы в тексте модуля.
 * @param extra Дополнительная информация о лексеме (например, сообщение ошибки)
 * @param line Номер строки, на которой находится лексема.
 * @param pos Позиция в строке, с которой начинается лексема.
 */
case class Token(tpe: TokenType.Value, lexeme: String, extra: String, line: Int, pos: Int) {
  override def toString: String =
    s"Type: $tpe,${if (lexeme != "") s" lexeme: $lexeme," else ""} line: $line, pos: ${pos+1}"
}
