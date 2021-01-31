package interpreter

import evaluator.Evaluator
import parser.Parser
import printer.TreePrinter
import scanner.Scanner
import semantic.{Expr, SemanticAnalyzer}
import symbol_table.{SymbolNode, SymbolTable}
import tokens.{Token, TokenType}

/**
 * Парсер и интерпретатор исходного кода на языке Java.
 *
 * @param source Исходный код модуля
 */
class ParserInterpreter(private val source: String) extends Parser with Evaluator with SemanticAnalyzer with TreePrinter {
  private val tokens = new Scanner(source).buffered

  def run(): Unit = {
    try {
      parse()
    } catch {
      case _: SemanticError =>
    }
  }

  def parse(): Option[SymbolTable] = {
    try {
      program()
    } catch {
      case _: ParseError =>
    }
    Some(symbolTable).filter(_.root != null)
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
  private def classDeclaration(isMainClass: Boolean = false, _root: SymbolNode = null): Unit = {
    // Имя класса
    val name = consume("class name expected", TokenType.IDENTIFIER).lexeme
    // Тело класса: начало
    consume("'{' expected", TokenType.LEFT_BRACE)
    // Соответствующий узел таблицы
    val node = symbolTable.setCurrent(SymbolNode.Class(name), _root, isSameScope = true)
    // Семантическое условие "В области видимости нет классов с таким же именем".
    checkNoSameDeclarationsInScope(node)
    if (isMainClass) {
      symbolTable.root = node
      // Семантическое условие "На самом верхнем уровне должен быть класс Main."
      checkTopmostClassIsMain(node)
    }
    // Члены класса
    classMembers()
    symbolTable.setCurrent(node)
    // Семантическое условие "Главный класс должен иметь метод `void main()`
    if (isMainClass) checkClassHasMethod(node, "main", SymbolNode.Type.VOID)
    // Тело класса: конец
    consume("'}' expected", TokenType.RIGHT_BRACE)
  }

  /**
   * Разбор синтаксической конструкции __"Члены класса"__.
   */
  private def classMembers(): Unit = {
    // Узел таблицы
    val prev = symbolTable.current
    val root = symbolTable.setCurrent(SymbolNode.Synthetic())
    prev.rightChild = root
    while (!tokens.headOption.exists(_.tpe == TokenType.RIGHT_BRACE)) {
      classMember(_root = root)
    }
    symbolTable.setCurrent(root)
  }

  /**
   * Разбор синтаксической конструкции __"Член класса"__.
   */
  private def classMember(_root: SymbolNode = null): Unit = {
    // Объявление класса
    if (accept(TokenType.CLASS)) { classDeclaration(_root = _root); return }
    // Тип данных члена класса
    val tpe: SymbolNode.Type.Value = consume(
      "'void', 'int', 'short', 'long' or 'double' keyword expected",
      TokenType.VOID, TokenType.INT, TokenType.SHORT, TokenType.LONG, TokenType.DOUBLE
    ).tpe
    // Имя члена класса
    val name = consume("member name expected", TokenType.IDENTIFIER).lexeme
    // Метод
    if (accept(TokenType.LEFT_PAREN)) {
      symbolTable.setCurrent(SymbolNode.Method(name, tpe), _root, isSameScope = true)
      // Семантическое условие "В области видимости нет методов с таким же именем."
      checkNoSameDeclarationsInScope(symbolTable.current)
      // Семантическое условие "Наличие или отсутствие return в методе."
      enterMethod()
      method()
      leaveMethod(tpe == SymbolNode.Type.VOID)
      return
    }
    // Объявление поля
    val (_, value) = dataDeclaration(tpe)
    symbolTable.setCurrent(SymbolNode.Field(name, tpe, value), _root, isSameScope = true)
    // Семантическое условие "В области видимости нет полей с таким же именем."
    checkNoSameDeclarationsInScope(symbolTable.current)
    // Печать дерева при выделении памяти под поле
    printTable(s"Create field $name")
  }

  /**
   * Разбор синтаксической конструкции __"Метод"__.
   */
  private def method(): Unit = {
    consume("')' expected", TokenType.RIGHT_PAREN)
    consume("'{' expected", TokenType.LEFT_BRACE)
    val prev = symbolTable.current
    block(isMethodBlock = true)
    symbolTable.setCurrent(prev)
  }

  /**
   * Разбор синтаксической конструкции __"Объявление и определение данных"__.
   */
  private def dataDeclaration(tpe: SymbolNode.Type.Value): (SymbolNode.Type.Value, Any) = {
    // Опциональная инциализация
    val initValue = if (accept(TokenType.EQUAL)) {
      expression().map {
        case Expr.Value(tpe, value) => (tpe, value)
        case Expr.Reference(_, tpe, ref) => (tpe, ref.value)
      }.getOrElse(tpe, SymbolNode.Undefined)
    } else {
      (tpe, SymbolNode.Type.default(tpe))
    }
    // Семантическое условие "Корректное приведение типов при объявлении данных".
    checkTypeConsistency(from = initValue._1, to = tpe, literal = initValue._2 != SymbolNode.Undefined)
    consume("';' after declaration expected", TokenType.SEMICOLON)
    initValue
  }

  /**
   * Разбор синтаксической конструкции __"Составной оператор"__.
   */
  private def block(isMethodBlock: Boolean = false, _root: SymbolNode = null): Boolean = {
    val prev = symbolTable.current
    val root = symbolTable.setCurrent(SymbolNode.Synthetic(), _root)
    if (isMethodBlock) prev.rightChild = symbolTable.current
    while (!tokens.headOption.exists(_.tpe == TokenType.RIGHT_BRACE)) {
      statement(_root = root)
    }
    // Печать дерева при выделении памяти под блок
    printTable(s"Create block $root")
    // Освобождение памяти из-под блока
    symbolTable.setCurrent(prev)
    if (isMethodBlock) prev.rightChild = null
    consume("'}' after block expected", TokenType.RIGHT_BRACE)
    // Печать дерева при освобождении памяти из-под блока
    printTable(s"Remove block $root")
    !root.isEmpty
  }

  /**
   * Разбор синтаксической конструкции __"Оператор"__.
   */
  private def statement(_root: SymbolNode = null): Boolean = {
    // Объявление переменной
    val tpe = acceptOption(TokenType.INT, TokenType.SHORT, TokenType.LONG, TokenType.DOUBLE)
    if (tpe.isDefined) tpe.foreach { t => variableDeclaration(t.tpe, _root = _root); return true }
    // Составной оператор
    if (accept(TokenType.LEFT_BRACE)) { return block(_root = _root) }
    // Пустой оператор
    if (accept(TokenType.SEMICOLON)) return false
    // Оператор break
    if (accept(TokenType.BREAK)) {
      // Семантическое условие "break только внутри switch."
      assertSemantic(switchNesting > 0, "break outside of switch statement")
      consume("';' after break expected", TokenType.SEMICOLON)
      return false
    }
    // Оператор return
    if (accept(TokenType.RETURN)) { returnStatement(); return false }
    // Оператор switch
    if (accept(TokenType.SWITCH)) { switchStatement(_root = _root); return true }
    // Оператор println
    if (accept(TokenType.PRINTLN)) { printlnStatement(); return false }
    // Выражение
    expression()
    consume("';' after declaration expected", TokenType.SEMICOLON)
    false
  }

  /**
   * Разбор синтаксической конструкции __"Печать значения"__.
   */
  private def printlnStatement(): Unit = {
    consume("'(' expected", TokenType.LEFT_PAREN)
    if (accept(TokenType.RIGHT_PAREN)) {
      println()
    } else {
      expression().foreach(println)
      consume("')' expected", TokenType.RIGHT_PAREN)
    }
    consume("';' after println expected", TokenType.SEMICOLON)
  }

  /**
   * Разбор синтаксической конструкции __"Объявление и определение переменной"__.
   */
  private def variableDeclaration(tpe: SymbolNode.Type.Value, _root: SymbolNode = null): Unit = {
    // Имя переменной
    val name = consume("variable name expected", TokenType.IDENTIFIER).lexeme
    val (_, value) = dataDeclaration(tpe)
    symbolTable.setCurrent(SymbolNode.Variable(name, tpe, value), _root)
    // Семантическое условие "В области видимости нет переменных с таким же именем."
    checkNoSameDeclarationsInScope(symbolTable.current)
  }

  /**
   * Разбор синтаксической конструкции __"Оператор return"__.
   */
  private def returnStatement(): Unit = {
    // Возвращаемое значение
    val returnValue = if (!tokens.headOption.exists(_.tpe == TokenType.SEMICOLON)) expression() else None
    // Семантическое условие "return возвращает правильное значение"
    checkProperReturn(symbolTable.current, returnValue)
    // Семантическое условие "return должен быть указан/не обязателен"
    captureReturn()
  }

  /**
   * Разбор синтаксической конструкции __"Оператор switch"__.
   */
  private def switchStatement(_root: SymbolNode = null): Unit = {
    // Семантика: вход в switch
    enterSwitch()
    consume("'(' expected", TokenType.LEFT_PAREN)
    // Условие оператора switch
    // Семантическое условие "Ограничение на тип выражения оператора switch"
    assertSemantic(
      expression().exists(_.tpe != SymbolNode.Type.VOID),
      "switch expression should be a value of type INT, LONG, SHORT or DOUBLE, got VOID"
    )
    consume("')' expected", TokenType.RIGHT_PAREN)
    consume("'{' expected", TokenType.LEFT_BRACE)
    // Тело оператора switch
    switchBody( _root = _root)
    consume("'}' expected", TokenType.RIGHT_BRACE)
    // Семантика: выход из switch
    leaveSwitch()
  }

  /**
   * Разбор синтаксической конструкции __"Ветви switch"__.
   */
  private def switchBody(_root: SymbolNode = null): Unit = {
    val root = symbolTable.setCurrent(SymbolNode.Synthetic(), _root)
    while (!tokens.headOption.exists(_.tpe == TokenType.RIGHT_BRACE)) {
      switchBranch(_root = root)
    }
    symbolTable.setCurrent(root)
  }

  /**
   * Разбор синтаксической конструкции __"Ветвь switch"__.
   */
  private def switchBranch(_root: SymbolNode = null): Boolean = {
    // Тип ветки switch
    val caseType = consume("switch case expected", TokenType.CASE, TokenType.DEFAULT)
    caseType.tpe match {
      case TokenType.CASE =>
        // Сопоставляемое выражение
        // Семантическое условие: "выражения в условиях веток switch должны быть согласованы по типам"
        // Т.к. сопоставление условий это по сути, cmp, тут работает widening cast, как в операторах сравнения
        assertSemantic(
          expression().exists(_.tpe != SymbolNode.Type.VOID),
          "case expression should be a value of type INT, LONG, SHORT or DOUBLE, got VOID"
        )
        consume("':' after switch case value expected", TokenType.COLON)
      case TokenType.DEFAULT =>
        consume("':' after 'default' case label expected", TokenType.COLON)
      case _ =>
    }
    val prev = symbolTable.current
    val root = symbolTable.setCurrent(SymbolNode.Synthetic(), _root)
    // Операторы ветви switch
    while (!tokens.headOption.map(_.tpe).exists(t => t == TokenType.CASE || t == TokenType.DEFAULT || t == TokenType.RIGHT_BRACE)) {
      statement(_root = root)
    }
    // Печать дерева при выделении памяти под блок
    printTable(s"Create block $root")
    // Освобождение памяти из-под блока
    symbolTable.setCurrent(prev)
    // Печать дерева при освобождении памяти из-под блока
    printTable(s"Remove block $root")
    !root.isEmpty
  }

  /**
   * Разбор синтаксической конструкции __"Выражение"__.
   */
  private def expression(): Option[Expr] = {
    assignment()
  }

  /**
   * Разбор синтаксической конструкции __"Присваивание"__.
   */
  private def assignment(): Option[Expr] = {
    // Левая часть присваивания
    val expr = or()
    while (accept(TokenType.EQUAL)) {
      // Семантическое условие "Присваивать можно только именованным значениям."
      assertSemantic(expr.exists(_.isInstanceOf[Expr.Reference]), "cannot assign to value")
      // Правая часть присваивания
      // Семантическое условие "Корректное приведение типов при присваивании."
      expr.foreach(l => expression().foreach { r =>
        checkTypeConsistency(from = r.tpe, to = l.tpe, literal = r match {
          case Expr.Value(_, value) => value != SymbolNode.Undefined
          case _ => false
        })
        l.asInstanceOf[Expr.Reference].ref.value = r.value
      })
    }
    expr
  }

  /**
   * Разбор синтаксической конструкции __"Логическое ИЛИ"__.
   */
  private def or(): Option[Expr] = {
    // Левая часть оператора ИЛИ
    val expr = and()
    var acc = expr.map(_.value)
    while (accept(TokenType.OR)) {
      // Семантическое условие "Логические операторы работают только с целочисленными типами"
      assertSemantic(
        expr.exists(v => v.tpe != SymbolNode.Type.VOID && v.tpe != SymbolNode.Type.DOUBLE),
        "logical operations works with operands of type INT, SHORT, LONG"
      )
      // Правая часть оператора ИЛИ
      expr.foreach(_ => and().foreach { r =>
        acc = acc.map(evalBinary(TokenType.OR, _, r.value))
      })
      // Семантика: приведение к типу возвращаемого значения
      expr.foreach(_.tpe = TokenType.SHORT)
    }
    expr.flatMap(v => acc.map(Expr.Value(v.tpe, _)))
  }

  /**
   * Разбор синтаксической конструкции __"Логическое И"__.
   */
  private def and(): Option[Expr] = {
    // Левая часть оператора И
    val expr = comparison()
    var acc = expr.map(_.value)
    while (accept(TokenType.AND)) {
      // Семантическое условие "Логические операторы работают только с целочисленными типами"
      assertSemantic(
        expr.exists(v => v.tpe != SymbolNode.Type.VOID && v.tpe != SymbolNode.Type.DOUBLE),
        "logical operations works with operands of type INT, SHORT, LONG"
      )
      // Правая часть оператора И
      expr.foreach(_ => comparison().foreach { r =>
        acc = acc.map(evalBinary(TokenType.AND, _, r.value))
      })
      // Семантика: приведение к типу возвращаемого значения
      expr.foreach(_.tpe = TokenType.SHORT)
    }
    expr.flatMap(v => acc.map(Expr.Value(v.tpe, _)))
  }

  /**
   * Разбор синтаксической конструкции __"Операция сравнения"__.
   */
  private def comparison(): Option[Expr] = {
    // Левая часть сравнения
    val expr = addition()
    var acc = expr.map(_.value)
    var opcode: Option[TokenType.Value] = None
    while ({
      opcode = acceptOption(
        TokenType.LESS, TokenType.LESS_EQUAL,
        TokenType.GREATER, TokenType.GREATER_EQUAL,
        TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL
      ).map(_.tpe)

      opcode.isDefined
    }) {
      // Семантическое условие "Выражение типа void не может быть операндом бинарной операции"
      assertSemantic(expr.exists(_.tpe != SymbolNode.Type.VOID), "comparison operand shouldn't be VOID")
      // Правая часть сравнения
      expr.foreach(_ => addition().foreach { r =>
        acc = opcode.flatMap(code => acc.map(evalBinary(code, _, r.value)))
      })
      // Семантика: приведение к типу возвращаемого значения
      expr.foreach(_.tpe = TokenType.SHORT)
    }
    expr.flatMap(v => acc.map(Expr.Value(v.tpe, _)))
  }

  /**
   * Разбор синтаксической конструкции __"Сложение-вычитание"__.
   */
  private def addition(): Option[Expr] = {
    // Левая часть аддитивных операций
    val expr = multiplication()
    var acc = expr.map(_.value)
    var opcode: Option[TokenType.Value] = None
    while ({ opcode = acceptOption(TokenType.MINUS, TokenType.PLUS).map(_.tpe); opcode.isDefined }) {
      // Семантическое условие "Выражение типа void не может быть операндом бинарной операции"
      assertSemantic(expr.exists(_.tpe != SymbolNode.Type.VOID), "addition operand shouldn't be VOID")
      // Правая часть аддитивных операций
      // Семантика: расширение типа значения
      expr.foreach(l => multiplication().foreach { r =>
        wideningCast(l.tpe, r.tpe).foreach(l.tpe = _)
        acc = opcode.flatMap(code => acc.map(evalBinary(code, _, r.value)))
      })
    }
    expr.flatMap(v => acc.map(Expr.Value(v.tpe, _)))
  }

  /**
   * Разбор синтаксической конструкции __"Умножение-деление"__.
   */
  private def multiplication(): Option[Expr] = {
    // Левая часть мультипликативных операций
    val expr = unary()
    var acc = expr.map(_.value)
    var opcode: Option[TokenType.Value] = None
    while ({ opcode = acceptOption(TokenType.SLASH, TokenType.STAR).map(_.tpe); opcode.isDefined }) {
      // Семантическое условие "Выражение типа void не может быть операндом бинарной операции"
      assertSemantic(expr.exists(_.tpe != SymbolNode.Type.VOID), "multiplication operand shouldn't be VOID")
      // Правая часть мультипликативных операций
      // Семантика: расширение типа значения
      expr.foreach(l => unary().foreach { r =>
        wideningCast(l.tpe, r.tpe).foreach(l.tpe = _)
        acc = opcode.flatMap(code => acc.map(evalBinary(code, _, r.value)))
      })
    }
    expr.flatMap(v => acc.map(Expr.Value(v.tpe, _)))
  }

  /**
   * Разбор синтаксической конструкции __"Унарная операция"__.
   */
  private def unary(): Option[Expr] = {
    // Префиксы унарных операций для элементарной операции
    val hasPref = acceptOption(TokenType.PLUS, TokenType.MINUS, TokenType.PLUS_PLUS, TokenType.MINUS_MINUS, TokenType.BANG)
    // Элементарная операция
    val prim = primary()
    // Возможно, суффиксы унарных операциий
    val hasSuf = acceptOption(TokenType.PLUS_PLUS, TokenType.MINUS_MINUS)

    if (hasPref.isDefined || hasSuf.isDefined) {
      // Семантическое условие: "Инкремент и декремент работают только с именованными значениями"
      assertSemantic(
        hasPref.map(_.tpe).exists(t => t != TokenType.MINUS_MINUS && t != TokenType.PLUS_PLUS)
        || hasSuf.isEmpty
        || prim.exists(!_.isInstanceOf[Expr.Value]),
        "++ and -- operators work only with named values (such as variables and fields)"
      )
      // Семантическое условие "Выражение типа void не может быть операндом унарной операции"
      assertSemantic(prim.exists(_.tpe != SymbolNode.Type.VOID), "unary operand shouldn't be VOID")
      // Семантическое условие "Логическое отрицание не работает с double"
      assertSemantic(
        hasPref.exists(_.tpe != TokenType.BANG) || prim.exists(_.tpe != SymbolNode.Type.DOUBLE),
        "bang operator operand shouldn't be DOUBLE"
      )
    }
    evalUnary(prim, hasPref.map(_.tpe), hasSuf.map(_.tpe))
  }

  /**
   * Разбор синтаксической конструкции __"Элементарное выражение"__.
   */
  private def primary(): Option[Expr] = {
    // Константа
    val number = acceptOption(TokenType.NUMBER_INT, TokenType.NUMBER_EXP)
    if (number.isDefined) return number.map(x => Expr.Value(x.tpe, SymbolNode.Type.parseLiteral(x.lexeme, x.tpe)))
    if (accept(TokenType.LEFT_PAREN)) {
      // Выражение в скобках
      val expr = expression()
      consume("')' expected after expression", TokenType.RIGHT_PAREN)
      return expr
    }
    // Идентификатор (объекта, переменной)
    var access = Seq(consume("name expected", TokenType.IDENTIFIER))
    // Доступ к полю объекта
    while (accept(TokenType.DOT)) {
      access :+= consume("access member name expected", TokenType.IDENTIFIER)
    }
    val accessors :+ identifier = access.map(_.lexeme)
    // Семантическое условие "Доступ к ресурсу должен быть описан явно"
    assertSemantic(accessors.isEmpty || accessors.head == "Main", "access chain must be written explicitly")
    val lookupStart = if (accessors.nonEmpty) symbolTable.root else symbolTable.current
    // Вызов метода объекта
    if (accept(TokenType.LEFT_PAREN)) {
      consume("')' expected in method call", TokenType.RIGHT_PAREN)
      // Семантика: поиск метода
      return findReference(lookupStart, accessors, identifier) {
        case SymbolNode.Method(`identifier`, _) => true
      }
    }
    // Семантика: поиск переменной или поля
    findReference(lookupStart, accessors, identifier) {
      case SymbolNode.Variable(`identifier`, _, _) | SymbolNode.Field(`identifier`, _, _) => true
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
        setCursor(token)
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
        setCursor(token)
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
      case Some(token) if types.contains(token.tpe) =>
        setCursor(token)
        tokens.next()
      case v =>
        v.foreach(Scanner.printError)
        ParserInterpreter.printError(v, message)
        throw new ParseError()
    }
  }
}

/**
 * Объект-компаньон класса [Parser].
 */
object ParserInterpreter {
  /**
   * Печатает сообщение об ошибке.
   */
  def printError(token: Option[Token], message: String): Unit = {
    System.err.println(s"Parsing error: $message, got: ${token.map(t => s"'${t.lexeme}' at line ${t.line}, pos ${t.pos+1}").getOrElse("nothing")}")
  }
}