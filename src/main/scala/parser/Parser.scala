package parser

import scanner.Scanner
import tokens.{Token, TokenType}

import scala.collection.BufferedIterator

/**
 * Парсер исходного кода на языке Java.
 *
 * @param tokens Поток лексем исходного кода модуля
 */
class Parser(private val tokens: BufferedIterator[Token]) {
  class ParseError extends RuntimeException

  /**
   * Производит синтаксический разбор кода модуля.
   */
  def parse(): Unit = {
    try {
      consume("'class' keyword expected", TokenType.CLASS)
      classDeclaration()
    } catch {
      case _: ParseError =>
    }
  }

  /**
   * Разбор синтаксической конструкции __"Объявление класса"__.
   */
  private def classDeclaration(): Unit = {
    // Имя класса
    consume("class name expected", TokenType.IDENTIFIER)
    // Тело класса: начало
    consume("'{' expected", TokenType.LEFT_BRACE)
    // Члены класса
    classMembers()
    // Тело класса: конец
    consume("'}' expected", TokenType.RIGHT_BRACE)
  }

  /**
   * Разбор синтаксической конструкции __"Члены класса"__.
   */
  private def classMembers(): Unit = {
    while (!tokens.headOption.exists(_.tpe == TokenType.RIGHT_BRACE)) {
      classMember()
    }
  }

  /**
   * Разбор синтаксической конструкции __"Член класса"__.
   */
  private def classMember(): Unit = {
    // Объявление класса
    if (accept(TokenType.CLASS)) return classDeclaration()
    // Тип данных члена класса
    consume(
      "'void', 'int', 'short', 'long' or 'double' keyword expected",
      TokenType.VOID, TokenType.INT, TokenType.SHORT, TokenType.LONG, TokenType.DOUBLE
    )
    // Имя члена класса
    consume("member name expected", TokenType.IDENTIFIER)
    // Метод
    if (accept(TokenType.LEFT_PAREN)) return method()
    // Объявление поля
    dataDeclaration()
  }

  /**
   * Разбор синтаксической конструкции __"Метод"__.
   */
  private def method(): Unit = {
    consume("')' expected", TokenType.RIGHT_PAREN)
    consume("'{' expected", TokenType.LEFT_BRACE)
    block()
  }

  /**
   * Разбор синтаксической конструкции __"Объявление и определение данных"__.
   */
  private def dataDeclaration(): Unit = {
    // Опциональная инциализация
    if (accept(TokenType.EQUAL)) expression()
    consume("';' after declaration expected", TokenType.SEMICOLON)
  }

  /**
   * Разбор синтаксической конструкции __"Составной оператор"__.
   */
  private def block(): Unit = {
    while (!tokens.headOption.exists(_.tpe == TokenType.RIGHT_BRACE)) {
      statement()
    }
    consume("'}' after block expected", TokenType.RIGHT_BRACE)
  }

  /**
   * Разбор синтаксической конструкции __"Оператор"__.
   */
  private def statement(): Unit = {
    // Объявление переменной
    if (accept(TokenType.INT, TokenType.SHORT, TokenType.LONG, TokenType.DOUBLE)) return variableDeclaration()
    // Объявление класса
    if (accept(TokenType.CLASS)) return classDeclaration()
    // Составной оператор
    if (accept(TokenType.LEFT_BRACE)) return block()
    // Пустой оператор
    if (accept(TokenType.SEMICOLON)) return
    // Оператор break
    if (accept(TokenType.BREAK)) return consume("';' after break expected", TokenType.SEMICOLON)
    // Оператор return
    if (accept(TokenType.RETURN)) return returnStatement()
    // Оператор switch
    if (accept(TokenType.SWITCH)) return switchStatement()
    // Выражение
    expression()
    consume("';' after declaration expected", TokenType.SEMICOLON)
  }

  /**
   * Разбор синтаксической конструкции __"Объявление и определение переменной"__.
   */
  private def variableDeclaration(): Unit = {
    // Имя переменной
    consume("variable name expected", TokenType.IDENTIFIER)
    dataDeclaration()
  }

  /**
   * Разбор синтаксической конструкции __"Оператор return"__.
   */
  private def returnStatement(): Unit = {
    // Возвращаемое выражение
    if (!tokens.headOption.exists(_.tpe == TokenType.SEMICOLON)) expression()
  }

  /**
   * Разбор синтаксической конструкции __"Оператор switch"__.
   */
  private def switchStatement(): Unit = {
    consume("'(' expected", TokenType.LEFT_PAREN)
    // Условие оператора switch
    expression()
    consume("')' expected", TokenType.RIGHT_PAREN)
    consume("'{' expected", TokenType.LEFT_BRACE)
    // Тело оператора switch
    switchBody()
    consume("'}' expected", TokenType.RIGHT_BRACE)
  }

  /**
   * Разбор синтаксической конструкции __"Ветви switch"__.
   */
  private def switchBody(): Unit = {
    while (!tokens.headOption.exists(_.tpe == TokenType.RIGHT_BRACE)) {
      switchBranch()
    }
  }

  /**
   * Разбор синтаксической конструкции __"Ветвь switch"__.
   */
  private def switchBranch(): Unit = {
    // Тип ветки switch
    val caseType = consume("switch case expected", TokenType.CASE, TokenType.DEFAULT)
    caseType.tpe match {
      case TokenType.CASE =>
        // Сопоставляемое выражение
        expression()
        consume("':' after switch case value expected", TokenType.COLON)
      case TokenType.DEFAULT =>
        consume("':' after 'default' case label expected", TokenType.COLON)
      case _ =>
    }
    // Операторы ветви switch
    while (!tokens.headOption.map(_.tpe).exists(t => t == TokenType.CASE || t == TokenType.DEFAULT || t == TokenType.RIGHT_BRACE)) {
      statement()
    }
  }

  /**
   * Разбор синтаксической конструкции __"Выражение"__.
   */
  private def expression(): Unit = {
    assignment()
  }

  /**
   * Разбор синтаксической конструкции __"Присваивание"__.
   */
  private def assignment(): Unit = {
    // Левая часть присваивания
    or()
    while (accept(TokenType.EQUAL)) {
      // Правая часть присваивания
      or()
    }
  }

  /**
   * Разбор синтаксической конструкции __"Логическое ИЛИ"__.
   */
  private def or(): Unit = {
    // Левая часть оператора ИЛИ
    and()
    while (accept(TokenType.OR)) {
      // Правая часть оператора ИЛИ
      and()
    }
  }

  /**
   * Разбор синтаксической конструкции __"Логическое И"__.
   */
  private def and(): Unit = {
    // Левая часть оператора И
    comparison()
    while (accept(TokenType.AND)) {
      // Правая часть оператора И
      comparison()
    }
  }

  /**
   * Разбор синтаксической конструкции __"Операция сравнения"__.
   */
  private def comparison(): Unit = {
    // Левая часть сравнения
    addition()
    while (accept(
      TokenType.LESS, TokenType.LESS_EQUAL,
      TokenType.GREATER, TokenType.GREATER_EQUAL,
      TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL
    )) {
      // Правая часть сравнения
      addition()
    }
  }

  /**
   * Разбор синтаксической конструкции __"Сложение-вычитание"__.
   */
  private def addition(): Unit = {
    // Левая часть аддитивных операций
    multiplication()
    while (accept(TokenType.MINUS, TokenType.PLUS)) {
      // Правая часть аддитивных операций
      multiplication()
    }
  }

  /**
   * Разбор синтаксической конструкции __"Умножение-деление"__.
   */
  private def multiplication(): Unit = {
    // Левая часть мультипликативных операций
    unary()
    while (accept(TokenType.SLASH, TokenType.STAR)) {
      // Правая часть мультипликативных операций
      unary()
    }
  }

  /**
   * Разбор синтаксической конструкции __"Унарная операция"__.
   */
  private def unary(): Unit = {
    // Префиксы унарных операций для элементарной операции
    accept(TokenType.PLUS, TokenType.MINUS, TokenType.PLUS_PLUS, TokenType.MINUS_MINUS, TokenType.BANG)
    // Элементарная операция
    primary()
    // Возможно, суффиксы унарных операциий
    accept(TokenType.PLUS_PLUS, TokenType.MINUS_MINUS)
  }

  /**
   * Разбор синтаксической конструкции __"Элементарное выражение"__.
   */
  private def primary(): Unit = {
    // Константа
    if (accept(TokenType.NUMBER_INT, TokenType.NUMBER_EXP)) return
    if (accept(TokenType.LEFT_PAREN)) {
      // Выражение в скобках
      expression()
      consume("')' expected after expression", TokenType.RIGHT_PAREN)
      return
    }
    // Идентификатор (объекта, переменной)
    consume("name expected", TokenType.IDENTIFIER)
    // Доступ к полю объекта
    while (accept(TokenType.DOT)) {
      consume("access member name expected", TokenType.IDENTIFIER)
    }
    // Вызов метода объекта
    if (accept(TokenType.LEFT_PAREN)) {
      consume("')' expected in method call", TokenType.RIGHT_PAREN)
      return
    }
  }

  /**
   * Сравнивает тип лексемы в голове потока с указанными типами, и если тип совпал, принимает лексему.
   * @param types Перечень принимаемых типов
   * @return true - если лексема в голове принята, false - в ином случае.
   */
  @inline
  private def accept(types: TokenType.Value*): Boolean = {
    tokens.headOption match {
      case Some(token) if types.contains(token.tpe) =>
        tokens.next()
        true
      case _ => false
    }
  }

  /**
   * Сравнивает тип лексемы в голове потока с указанными типами.
   *
   * Если тип совпал, принимает лексему и возвращает ее.
   *
   * Если тип не совпал, выводит сообщение в `stderr` и бросает `ParseError`.
   *
   * @param message Сообщение, которое выводится при ошибке поглощения
   * @param types Перечень принимаемых типов
   * @return Принятая лексема
   */
  @inline
  private def consume(message: String, types: TokenType.Value*): Token = {
    tokens.headOption match {
      case Some(token) if types.contains(token.tpe) => tokens.next()
      case v =>
        v.foreach(Scanner.printError)
        Parser.printError(v, message)
        throw new ParseError()
    }
  }
}

/**
 * Объект-компаньон класса [Parser].
 */
object Parser {
  /**
   * Печатает сообщение об ошибке.
   */
  def printError(token: Option[Token], message: String): Unit = {
    System.err.println(s"Parsing error: $message, got: ${token.map(t => s"'${t.lexeme}' at line ${t.line}, pos ${t.pos+1}").getOrElse("nothing")}")
  }
}