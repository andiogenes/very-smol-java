package parser

import symbol_table.SymbolTable

/**
 * Парсер исходного кода на языке Java.
 */
trait Parser {
  class ParseError extends RuntimeException

  val symbolTable = new SymbolTable()

  /**
   * Производит синтаксический разбор кода модуля.
   */
  def parse(): Option[SymbolTable]
}