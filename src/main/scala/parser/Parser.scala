package parser

import context.ContextNode
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

  type ContextTree = ContextNode

  /**
   * Производит синтаксический разбор кода модуля.
   */
  def parse(): Option[ContextTree] = {
    try {
      Some(program())
    } catch {
      case _: ParseError =>
        None
    }
  }

  /**
   * Разбор синтаксической конструкции __"Программа"__.
   */
  private def program(): ContextNode = {
    consume("'class' keyword expected", TokenType.CLASS)
    val mainClass = classDeclaration()
    consume("unexpected token after main class declaration", TokenType.EOF)
    mainClass
  }

  /**
   * Разбор синтаксической конструкции __"Объявление класса"__.
   */
  private def classDeclaration(): ContextNode = {
    // Имя класса
    val name = consume("class name expected", TokenType.IDENTIFIER).lexeme
    // Тело класса: начало
    consume("'{' expected", TokenType.LEFT_BRACE)
    val node = ContextNode.Class(name)
    // Члены класса
    val members = classMembers()
    if (!members.isEmpty) {
      node.rightChild = members
    }
    // Тело класса: конец
    consume("'}' expected", TokenType.RIGHT_BRACE)
    node
  }

  /**
   * Разбор синтаксической конструкции __"Члены класса"__.
   */
  private def classMembers(): ContextNode = {
    val root = ContextNode.Synthetic()
    var current: ContextNode = root
    while (!tokens.headOption.exists(_.tpe == TokenType.RIGHT_BRACE)) {
      current.leftChild = classMember()
      current = current.leftChild
    }
    root
  }

  /**
   * Разбор синтаксической конструкции __"Член класса"__.
   */
  private def classMember(): ContextNode = {
    // Объявление класса
    if (accept(TokenType.CLASS)) return classDeclaration()
    // Тип данных члена класса
    val tpe: ContextNode.Type.Value = consume(
      "'void', 'int', 'short', 'long' or 'double' keyword expected",
      TokenType.VOID, TokenType.INT, TokenType.SHORT, TokenType.LONG, TokenType.DOUBLE
    ).tpe
    // Имя члена класса
    val name = consume("member name expected", TokenType.IDENTIFIER).lexeme
    // Метод
    if (accept(TokenType.LEFT_PAREN)) {
      val methodRoot = ContextNode.Method(name, tpe)
      val methodBody = method()
      if (!methodBody.isEmpty) {
        methodRoot.rightChild = if (methodBody.leftChild != null) methodBody else methodBody.rightChild
      }
      return methodRoot
    }
    // Объявление поля
    val fieldRoot = ContextNode.Field(name, tpe)
    fieldRoot.rightChild = dataDeclaration(tpe)
    fieldRoot
  }

  /**
   * Разбор синтаксической конструкции __"Метод"__.
   */
  private def method(): ContextNode = {
    consume("')' expected", TokenType.RIGHT_PAREN)
    consume("'{' expected", TokenType.LEFT_BRACE)
    block()
  }

  /**
   * Разбор синтаксической конструкции __"Объявление и определение данных"__.
   */
  private def dataDeclaration(tpe: ContextNode.Type.Value): ContextNode = {
    // Опциональная инциализация
    val initValue = if (accept(TokenType.EQUAL)) {
      expression().getOrElse(ContextNode.Value(tpe, null))
    } else {
      ContextNode.Value(tpe, ContextNode.Type.default(tpe))
    }
    consume("';' after declaration expected", TokenType.SEMICOLON)
    initValue
  }

  /**
   * Разбор синтаксической конструкции __"Составной оператор"__.
   */
  private def block(): ContextNode = {
    val root = ContextNode.Synthetic()
    var current: ContextNode = null
    while (!tokens.headOption.exists(_.tpe == TokenType.RIGHT_BRACE)) {
      statement()
        .filterNot(v => v.isInstanceOf[ContextNode.Synthetic] && v.isEmpty)
        .foreach { v =>
        if (current == null) {
          root.rightChild = v
        } else {
          current.leftChild = v
        }
        current = v
      }
    }
    consume("'}' after block expected", TokenType.RIGHT_BRACE)
    root
  }

  /**
   * Разбор синтаксической конструкции __"Оператор"__.
   */
  private def statement(): Option[ContextNode] = {
    // Объявление переменной
    val tpe = acceptOption(TokenType.INT, TokenType.SHORT, TokenType.LONG, TokenType.DOUBLE)
    if (tpe.isDefined) tpe.foreach(t => return Some(variableDeclaration(t.tpe)))
    // Объявление класса
    if (accept(TokenType.CLASS)) return Some(classDeclaration())
    // Составной оператор
    if (accept(TokenType.LEFT_BRACE)) return Some(block())
    // Пустой оператор
    if (accept(TokenType.SEMICOLON)) return None
    // Оператор break
    if (accept(TokenType.BREAK)) { consume("';' after break expected", TokenType.SEMICOLON); return None }
    // Оператор return
    if (accept(TokenType.RETURN)) { returnStatement(); return None }
    // Оператор switch
    if (accept(TokenType.SWITCH)) return Some(switchStatement())
    // Выражение
    expression()
    consume("';' after declaration expected", TokenType.SEMICOLON)
    None
  }

  /**
   * Разбор синтаксической конструкции __"Объявление и определение переменной"__.
   */
  private def variableDeclaration(tpe: ContextNode.Type.Value): ContextNode = {
    // Имя переменной
    val name = consume("variable name expected", TokenType.IDENTIFIER).lexeme
    val variable = ContextNode.Variable(name, tpe)
    variable.rightChild = dataDeclaration(tpe)
    variable
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
  private def switchStatement(): ContextNode = {
    consume("'(' expected", TokenType.LEFT_PAREN)
    // Условие оператора switch
    expression()
    consume("')' expected", TokenType.RIGHT_PAREN)
    consume("'{' expected", TokenType.LEFT_BRACE)
    // Тело оператора switch
    val body = switchBody()
    consume("'}' expected", TokenType.RIGHT_BRACE)
    body
  }

  /**
   * Разбор синтаксической конструкции __"Ветви switch"__.
   */
  private def switchBody(): ContextNode = {
    val root = ContextNode.Synthetic()
    var current: ContextNode = null
    while (!tokens.headOption.exists(_.tpe == TokenType.RIGHT_BRACE)) {
      val branch = switchBranch()
      if (!branch.isEmpty) {
        if (current == null) {
          root.rightChild = branch
        } else {
          current.leftChild = branch
        }
        current = branch
      }
    }
    root
  }

  /**
   * Разбор синтаксической конструкции __"Ветвь switch"__.
   */
  private def switchBranch(): ContextNode = {
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
    val root = ContextNode.Synthetic()
    var current: ContextNode = null
    // Операторы ветви switch
    while (!tokens.headOption.map(_.tpe).exists(t => t == TokenType.CASE || t == TokenType.DEFAULT || t == TokenType.RIGHT_BRACE)) {
      statement()
        .filterNot(v => v.isInstanceOf[ContextNode.Synthetic] && v.isEmpty)
        .foreach { v =>
        if (current == null) {
          root.rightChild = v
        } else {
          current.leftChild = v
        }
        current = v
      }
    }
    root
  }

  /**
   * Разбор синтаксической конструкции __"Выражение"__.
   */
  private def expression(): Option[ContextNode] = {
    assignment()
  }

  /**
   * Разбор синтаксической конструкции __"Присваивание"__.
   */
  private def assignment(): Option[ContextNode] = {
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
  private def or(): Option[ContextNode] = {
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
  private def and(): Option[ContextNode] = {
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
  private def comparison(): Option[ContextNode] = {
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
  private def addition(): Option[ContextNode] = {
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
  private def multiplication(): Option[ContextNode] = {
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
  private def unary(): Option[ContextNode] = {
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
  private def primary(): Option[ContextNode] = {
    // Константа
    val number = acceptOption(TokenType.NUMBER_INT, TokenType.NUMBER_EXP)
    if (number.isDefined) return number.map(x => ContextNode.Value(x.tpe, x.lexeme))
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