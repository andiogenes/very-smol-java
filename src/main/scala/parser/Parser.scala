package parser

import symbol_table.{SymbolNode, SymbolTable}
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

  val symbolTable = new SymbolTable()

  /**
   * Производит синтаксический разбор кода модуля.
   */
  def parse(): Option[SymbolTable] = {
    try {
      program()
      Some(symbolTable).filter(_.root != null)
    } catch {
      case _: ParseError =>
        None
    }
  }

  /**
   * Разбор синтаксической конструкции __"Программа"__.
   */
  private def program(): Unit = {
    consume("'class' keyword expected", TokenType.CLASS)
    classDeclaration(isMainClass = true)
    consume("unexpected token after main class declaration", TokenType.EOF)
  }

  /**
   * Разбор синтаксической конструкции __"Объявление класса"__.
   */
  private def classDeclaration(isMainClass: Boolean = false): Unit = {
    // Имя класса
    val name = consume("class name expected", TokenType.IDENTIFIER).lexeme
    // Тело класса: начало
    consume("'{' expected", TokenType.LEFT_BRACE)
    // Соответствующий узел таблицы
    val node = symbolTable.setCurrent(SymbolNode.Class(name))
    if (isMainClass) symbolTable.root = node
    // Члены класса
    classMembers()
    symbolTable.setCurrent(node)
    // Тело класса: конец
    consume("'}' expected", TokenType.RIGHT_BRACE)
  }

  /**
   * Разбор синтаксической конструкции __"Члены класса"__.
   */
  private def classMembers(): Unit = {
    // Узел таблицы
    val root = SymbolNode.Synthetic()
    symbolTable.current.rightChild = symbolTable.setCurrent(root)
    while (!tokens.headOption.exists(_.tpe == TokenType.RIGHT_BRACE)) {
      val prev = symbolTable.current
      classMember()
      prev.leftChild = symbolTable.current
    }
    symbolTable.setCurrent(root)
  }

  /**
   * Разбор синтаксической конструкции __"Член класса"__.
   */
  private def classMember(): Unit = {
    // Объявление класса
    if (accept(TokenType.CLASS)) { classDeclaration(); return }
    // Тип данных члена класса
    val tpe: SymbolNode.Type.Value = consume(
      "'void', 'int', 'short', 'long' or 'double' keyword expected",
      TokenType.VOID, TokenType.INT, TokenType.SHORT, TokenType.LONG, TokenType.DOUBLE
    ).tpe
    // Имя члена класса
    val name = consume("member name expected", TokenType.IDENTIFIER).lexeme
    // Метод
    if (accept(TokenType.LEFT_PAREN)) {
      val root = symbolTable.setCurrent(SymbolNode.Method(name, tpe))
      method()
      return
    }
    // Объявление поля
    val data = dataDeclaration(tpe).asInstanceOf[SymbolNode.Value]
    symbolTable.setCurrent(SymbolNode.Field(name, tpe, data.value))
  }

  /**
   * Разбор синтаксической конструкции __"Метод"__.
   */
  private def method(): Unit = {
    consume("')' expected", TokenType.RIGHT_PAREN)
    consume("'{' expected", TokenType.LEFT_BRACE)
    val prev = symbolTable.current
    block()
    prev.rightChild = symbolTable.current
    symbolTable.setCurrent(prev)
  }

  /**
   * Разбор синтаксической конструкции __"Объявление и определение данных"__.
   */
  private def dataDeclaration(tpe: SymbolNode.Type.Value): SymbolNode = {
    // Опциональная инциализация
    val initValue = if (accept(TokenType.EQUAL)) {
      expression().getOrElse(SymbolNode.Value(tpe, SymbolNode.Undefined))
    } else {
      SymbolNode.Value(tpe, SymbolNode.Type.default(tpe))
    }
    consume("';' after declaration expected", TokenType.SEMICOLON)
    initValue
  }

  /**
   * Разбор синтаксической конструкции __"Составной оператор"__.
   */
  private def block(): Unit = {
    val root = symbolTable.setCurrent(SymbolNode.Synthetic())
    while (!tokens.headOption.exists(_.tpe == TokenType.RIGHT_BRACE)) {
      val prev = symbolTable.current
      if (statement()) {
        if (prev == root) {
          prev.rightChild = symbolTable.current
        } else {
          prev.leftChild = symbolTable.current
        }
      }
    }
    symbolTable.setCurrent(root)
    consume("'}' after block expected", TokenType.RIGHT_BRACE)
  }

  /**
   * Разбор синтаксической конструкции __"Оператор"__.
   */
  private def statement(): Boolean = {
    // Объявление переменной
    val tpe = acceptOption(TokenType.INT, TokenType.SHORT, TokenType.LONG, TokenType.DOUBLE)
    if (tpe.isDefined) tpe.foreach { t => variableDeclaration(t.tpe); return true }
    // Объявление класса
    if (accept(TokenType.CLASS)) { classDeclaration(); return true }
    // Составной оператор
    if (accept(TokenType.LEFT_BRACE)) { block(); return true }
    // Пустой оператор
    if (accept(TokenType.SEMICOLON)) return false
    // Оператор break
    if (accept(TokenType.BREAK)) { consume("';' after break expected", TokenType.SEMICOLON); return false }
    // Оператор return
    if (accept(TokenType.RETURN)) { returnStatement(); return false }
    // Оператор switch
    if (accept(TokenType.SWITCH)) { switchStatement(); return true }
    // Выражение
    expression()
    consume("';' after declaration expected", TokenType.SEMICOLON)
    false
  }

  /**
   * Разбор синтаксической конструкции __"Объявление и определение переменной"__.
   */
  private def variableDeclaration(tpe: SymbolNode.Type.Value): Unit = {
    // Имя переменной
    val name = consume("variable name expected", TokenType.IDENTIFIER).lexeme
    val data = dataDeclaration(tpe).asInstanceOf[SymbolNode.Value]
    symbolTable.setCurrent(SymbolNode.Variable(name, tpe, data.value))
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
    val root = symbolTable.setCurrent(SymbolNode.Synthetic())
    while (!tokens.headOption.exists(_.tpe == TokenType.RIGHT_BRACE)) {
      val prev = symbolTable.current
      switchBranch()
      if (prev == root) {
        prev.rightChild = symbolTable.current
      } else {
        prev.leftChild = symbolTable.current
      }
    }
    symbolTable.setCurrent(root)
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
    val root = symbolTable.setCurrent(SymbolNode.Synthetic())
    // Операторы ветви switch
    while (!tokens.headOption.map(_.tpe).exists(t => t == TokenType.CASE || t == TokenType.DEFAULT || t == TokenType.RIGHT_BRACE)) {
      val prev = symbolTable.current
      if (statement()) {
        if (prev == root) {
          prev.rightChild = symbolTable.current
        } else {
          prev.leftChild = symbolTable.current
        }
      }
    }
    symbolTable.setCurrent(root)
  }

  /**
   * Разбор синтаксической конструкции __"Выражение"__.
   */
  private def expression(): Option[SymbolNode] = {
    assignment()
  }

  /**
   * Разбор синтаксической конструкции __"Присваивание"__.
   */
  private def assignment(): Option[SymbolNode] = {
    // Левая часть присваивания
    var expr = or()
    while (accept(TokenType.EQUAL)) {
      // Правая часть присваивания
      or()
      expr = None
    }
    expr
  }

  /**
   * Разбор синтаксической конструкции __"Логическое ИЛИ"__.
   */
  private def or(): Option[SymbolNode] = {
    // Левая часть оператора ИЛИ
    var expr = and()
    while (accept(TokenType.OR)) {
      // Правая часть оператора ИЛИ
      and()
      expr = None
    }
    expr
  }

  /**
   * Разбор синтаксической конструкции __"Логическое И"__.
   */
  private def and(): Option[SymbolNode] = {
    // Левая часть оператора И
    var expr = comparison()
    while (accept(TokenType.AND)) {
      // Правая часть оператора И
      comparison()
      expr = None
    }
    expr
  }

  /**
   * Разбор синтаксической конструкции __"Операция сравнения"__.
   */
  private def comparison(): Option[SymbolNode] = {
    // Левая часть сравнения
    var expr = addition()
    while (accept(
      TokenType.LESS, TokenType.LESS_EQUAL,
      TokenType.GREATER, TokenType.GREATER_EQUAL,
      TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL
    )) {
      // Правая часть сравнения
      addition()
      expr = None
    }
    expr
  }

  /**
   * Разбор синтаксической конструкции __"Сложение-вычитание"__.
   */
  private def addition(): Option[SymbolNode] = {
    // Левая часть аддитивных операций
    var expr = multiplication()
    while (accept(TokenType.MINUS, TokenType.PLUS)) {
      // Правая часть аддитивных операций
      multiplication()
      expr = None
    }
    expr
  }

  /**
   * Разбор синтаксической конструкции __"Умножение-деление"__.
   */
  private def multiplication(): Option[SymbolNode] = {
    // Левая часть мультипликативных операций
    var expr = unary()
    while (accept(TokenType.SLASH, TokenType.STAR)) {
      // Правая часть мультипликативных операций
      unary()
      expr = None
    }
    expr
  }

  /**
   * Разбор синтаксической конструкции __"Унарная операция"__.
   */
  private def unary(): Option[SymbolNode] = {
    // Префиксы унарных операций для элементарной операции
    val hasPref = accept(TokenType.PLUS, TokenType.MINUS, TokenType.PLUS_PLUS, TokenType.MINUS_MINUS, TokenType.BANG)
    // Элементарная операция
    val prim = primary()
    // Возможно, суффиксы унарных операциий
    val hasSuf = accept(TokenType.PLUS_PLUS, TokenType.MINUS_MINUS)

    if (!hasPref && !hasSuf) prim else None
  }

  /**
   * Разбор синтаксической конструкции __"Элементарное выражение"__.
   */
  private def primary(): Option[SymbolNode] = {
    // Константа
    val number = acceptOption(TokenType.NUMBER_INT, TokenType.NUMBER_EXP)
    if (number.isDefined) return number.map(x => SymbolNode.Value(x.tpe, x.lexeme))
    if (accept(TokenType.LEFT_PAREN)) {
      // Выражение в скобках
      val expr = expression()
      consume("')' expected after expression", TokenType.RIGHT_PAREN)
      return expr
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
    }
    None
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
   * Сравнивает тип лексемы в голове потока с указанными типами, и если тип совпал, принимает лексему.
   * @param types Перечень принимаемых типов
   * @return лексему в голове - если та принята, None - в ином случае.
   */
  @inline
  private def acceptOption(types: TokenType.Value*): Option[Token] = {
    tokens.headOption match {
      case Some(token) if types.contains(token.tpe) =>
        tokens.next()
        Some(token)
      case _ => None
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