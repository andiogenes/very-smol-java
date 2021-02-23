package compiler

import codegen.CodeGenerator
import evaluation.{EvaluationContext, Evaluator}
import ir.IRBuilder
import ir.Instruction._
import ir.ModuleEntry.{FunctionDefinition, GlobalVariable}
import ir.Terminator.{Goto, Return}
import llvm.LLVMToolbox
import parser.Parser
import printer.TreePrinter
import scanner.Scanner
import semantic.{Expr, SemanticAnalyzer}
import symbol_table.{SymbolNode, SymbolTable}
import tokens.{Token, TokenType}

import scala.collection.{BufferedIterator, View, mutable}

/**
 * Парсер, транслятор и интерпретатор исходного кода на языке Java.
 *
 * @param source Исходный код модуля
 */
class ParserCompiler(
    private val source: String,
    override val destination: String,
    val interpret: Boolean,
    val compile: Boolean,
    val interpretLLVM: Boolean,
    val justEmit: Boolean
)
  extends Parser
  with IRBuilder
  with CodeGenerator
  with Evaluator
  with EvaluationContext
  with SemanticAnalyzer
  with TreePrinter
  with LLVMToolbox {

  isInterpreting = interpret

  /**
   * Контекст единиц кода.
   */
  private val codeUnits = new mutable.Stack[BufferedIterator[Token]]()

  /**
   * Общая единица кода и итератор.
   */
  private val (wholeUnit, _peekUnit) = {
    val seq = new Scanner(source).toSeq
    val it = seq.iterator.zipWithIndex.buffered
    codeUnits.push(it.map(_._1).buffered)
    (seq, it)
  }

  /**
   * Текущая позиция разбора токенов.
   */
  private def peekUnit: Int = _peekUnit.head._2

  private def tokens = codeUnits.head

  def run(): Unit = {
    try {
      parse()
      if (compile) {
        emitAll()
        if (justEmit) return
        if (interpretLLVM) {
          lli(s"$destination.ll")
        } else {
          llc(s"$destination.ll")
          clang(s"$destination.o", destination)
        }
      }
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
    // Добавляем имя класса в префикс пространства имен
    if (compile) namespacePrefix.push(name)
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
    classMembers(isMainClass)
    symbolTable.setCurrent(node)
    // Семантическое условие "Главный класс должен иметь метод `void main()`
    if (isMainClass) checkClassHasMethod(node, "main", SymbolNode.Type.VOID)
    // Тело класса: конец
    consume("'}' expected", TokenType.RIGHT_BRACE)
    // Убираем имя класса из префикса пространства имен
    if (compile) namespacePrefix.pop()
  }

  /**
   * Разбор синтаксической конструкции __"Члены класса"__.
   */
  private def classMembers(isMainClass: Boolean): Unit = {
    // Узел таблицы
    val prev = symbolTable.current
    val root = symbolTable.setCurrent(SymbolNode.Synthetic())
    prev.rightChild = root
    while (!tokens.headOption.exists(_.tpe == TokenType.RIGHT_BRACE)) {
      classMember(_root = root, isMainClass)
    }
    symbolTable.setCurrent(root)
  }

  /**
   * Разбор синтаксической конструкции __"Член класса"__.
   */
  private def classMember(_root: SymbolNode = null, isMainClass: Boolean): Unit = {
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
      val entryName = formatEntryName(name)
      if (compile) definition = module.add(FunctionDefinition(entryName, tpe))
      val decl = SymbolNode.Method(name, tpe, SymbolNode.Type.default(tpe), identifier = s"@$entryName")
      symbolTable.setCurrent(decl, _root, isSameScope = true)
      // Семантическое условие "В области видимости нет методов с таким же именем."
      checkNoSameDeclarationsInScope(symbolTable.current)
      // Семантическое условие "Наличие или отсутствие return в методе."
      enterMethod()
      method(decl, isMainClass)
      leaveMethod(tpe == SymbolNode.Type.VOID)
      if (compile && tpe == SymbolNode.Type.VOID) {
        bblock.terminator = Return(SymbolNode.Type.VOID)
      }
      return
    }
    // Объявление поля
    val entryName = formatEntryName(name)
    if (compile) {
      module.add(GlobalVariable(entryName, tpe, SymbolNode.Type.default(tpe).toString))
      switchToInitialization()
    }
    val (declType, declValue) = dataDeclaration(tpe)
    val value = SymbolNode.Type.cast(declValue, tpe)
    if (compile) {
      tryAddCast(from = declType, to = tpe)
      addInstruction(_ => Store(tpe, s"%${currentDefinition.localVariableCounter - 1}", s"@$entryName"), increment = false)
      switchToCommon()
    }
    // Печать данных о присваивании
    if (isInterpreting) System.out.println(s"$name : $tpe = $value : $tpe")
    symbolTable.setCurrent(SymbolNode.Field(name, tpe, value, s"@$entryName"), _root, isSameScope = true)
    // Семантическое условие "В области видимости нет полей с таким же именем."
    checkNoSameDeclarationsInScope(symbolTable.current)
    // Печать дерева при выделении памяти под поле
    printTable(s"Create field $name")
  }

  /**
   * Разбор синтаксической конструкции __"Метод"__.
   */
  private def method(decl: SymbolNode.Method, isMainClass: Boolean): Unit = {
    consume("')' expected", TokenType.RIGHT_PAREN)
    consume("'{' expected", TokenType.LEFT_BRACE)
    // Устанавливаем начало тела метода
    decl.startPos = peekUnit
    // Отключаем интерпретацию, если метод не main
    if (!(isMainClass && decl.name == "main")) isInterpreting = false
    val prev = symbolTable.current
    block(isMethodBlock = true)
    symbolTable.setCurrent(prev)
    // Включаем интерпретацию обратно
    if (interpret) isInterpreting = true
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
      val default = SymbolNode.Type.default(tpe)
      if (compile) addInstruction(id => Value(s"$id", tpe, default.toString))
      (tpe, default)
    }
    // Семантическое условие "Корректное приведение типов при объявлении данных".
    checkTypeConsistency(from = initValue._1, to = tpe, literal = initValue._2 != SymbolNode.Undefined)
    consume("';' after declaration expected", TokenType.SEMICOLON)
    initValue
  }

  /**
   * Разбор синтаксической конструкции __"Составной оператор"__.
   */
  private def block(isMethodBlock: Boolean = false, isInvokeBlock: Boolean = false, _root: SymbolNode = null): Boolean = {
    if (isInvokeBlock && isMethodBlock) throw  new IllegalArgumentException("block must be either method or invoke, not both")

    if (compile && isMethodBlock) bblock = splitBlock()

    val prev = symbolTable.current
    val root = symbolTable.setCurrent(SymbolNode.Synthetic(), _root)
    if (isMethodBlock) prev.rightChild = symbolTable.current
    // Привязываем тело метода к соответствующей области видимости, не добавляя поддерево в таблицу символов
    if (isInvokeBlock) root.parent = prev
    while (!tokens.headOption.exists(_.tpe == TokenType.RIGHT_BRACE)) {
      statement(_root = root)
    }
    // Печать дерева при выделении памяти под блок
    printTable(s"Create block $root")
    // Освобождение памяти из-под блока
    symbolTable.setCurrent(prev)
    if (isMethodBlock) prev.rightChild = null
    if (isInvokeBlock) root.parent = null
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
      if (compile) {
        bblock.terminator = Goto(s"switch_${switchIdentifier}_end")
        bblock = addBlock()
      }
      // Сброс флага интерпретации
      if (isInterpreting) {
        isInterpreting = false
        isBreakExecuted = true
      }
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
      if (isInterpreting) System.out.println()
      if (compile) addInstruction(_ => PrintlnVoid())
    } else {
      expression().foreach { x =>
        if (isInterpreting) System.out.println(x)
        if (compile) addInstruction(_ => Println(x.tpe, s"${definition.localVariableCounter - 1}"))
      }
      consume("')' expected", TokenType.RIGHT_PAREN)
    }
    consume("';' after println expected", TokenType.SEMICOLON)
  }

  /**
   * Разбор синтаксической конструкции __"Объявление и определение переменной"__.
   */
  private def variableDeclaration(tpe: SymbolNode.Type.Value, _root: SymbolNode = null): Unit = {
    val alloca = addInstruction(id => Alloca(s"$id", tpe))
    // Имя переменной
    val name = consume("variable name expected", TokenType.IDENTIFIER).lexeme
    val (declType, declValue) = dataDeclaration(tpe)
    val value = SymbolNode.Type.cast(declValue, tpe)
    if (compile) tryAddCast(from = declType, to = tpe)
    // Печать данных о присваивании
    if (isInterpreting) System.out.println(s"$name : $tpe = $value : $tpe")
    symbolTable.setCurrent(SymbolNode.Variable(name, tpe, value, s"%${alloca.identifier}"), _root)
    // Семантическое условие "В области видимости нет переменных с таким же именем."
    checkNoSameDeclarationsInScope(symbolTable.current)
    if (compile) addInstruction(_ => Store(tpe, s"%${definition.localVariableCounter - 1}", s"%${alloca.identifier}"), increment = false)
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
    // Считаются только return вне условных операторов
    // TODO: если появится другой случай использования контекста, заменить на отдельный флаг switch
    if (isContextEmpty) captureReturn()
    // Завершение интерпретации блока и передача возвращаемого значения
    if (isInterpreting && !isReturnExecuted) {
      isInterpreting = false
      isReturnExecuted = true
      this.returnValue = returnValue.map(v => Expr.Value(v.tpe, SymbolNode.Type.cast(v.value, v.tpe)))
    }
    if (compile) {
      returnValue
        .filter(v => !v.value.isInstanceOf[Double] && v.tpe == SymbolNode.Type.DOUBLE)
        .foreach(v => tryAddCast(from = SymbolNode.Type.INT, to = v.tpe))
      bblock.terminator = returnValue
        .map(v => Return(v.tpe, s"%${definition.localVariableCounter - 1}"))
        .getOrElse(Return(SymbolNode.Type.VOID))
      bblock = addBlock()
    }
  }

  /**
   * Разбор синтаксической конструкции __"Оператор switch"__.
   */
  private def switchStatement(_root: SymbolNode = null): Unit = {
    // Семантика: вход в switch
    enterSwitch()
    if (compile) startSwitchInstruction()
    // Сохранение контекста
    saveContext()
    consume("'(' expected", TokenType.LEFT_PAREN)
    // Условие switch
    switchCondition = expression()
    if (compile) refillSwitchInstruction(switchCondition)
    // Отключение интерпретации
    isInterpreting = false
    // Условие оператора switch
    // Семантическое условие "Ограничение на тип выражения оператора switch"
    assertSemantic(
      switchCondition.exists(_.tpe != SymbolNode.Type.VOID),
      "switch expression should be a value of type INT, LONG, SHORT or DOUBLE, got VOID"
    )
    consume("')' expected", TokenType.RIGHT_PAREN)
    consume("'{' expected", TokenType.LEFT_BRACE)
    // Тело оператора switch
    switchBody( _root = _root)
    consume("'}' expected", TokenType.RIGHT_BRACE)
    // Семантика: выход из switch
    leaveSwitch()
    if (compile) {
      if (!switchHasDefault) {
        bblock = addDefaultBlock()
      }
      bblock = endSwitchInstruction()
    }
    // Восстановление контекста
    if (isReturnExecuted) {
      val returnValue = this.returnValue
      restoreContext()
      // В случае вызова return надо пробросить флаги интерпретации и return из switch наружу
      isInterpreting = false
      isReturnExecuted = true
      this.returnValue = returnValue
    } else {
      restoreContext()
    }
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
        val condition = expression()
        // Семантическое условие: "выражения в условиях веток switch должны быть согласованы по типам"
        // Т.к. сопоставление условий это по сути, cmp, тут работает widening cast, как в операторах сравнения
        assertSemantic(
          condition.exists(_.tpe != SymbolNode.Type.VOID),
          "case expression should be a value of type INT, LONG, SHORT or DOUBLE, got VOID"
        )
        consume("':' after switch case value expected", TokenType.COLON)
        if (compile) bblock = addCaseBlock(condition, condition.flatMap(e => wideningCast(switchType, e.tpe)).get)
        // Включаем интерпретацию, если switch интерпретируется, "хорошая" ветвь и не был вызван break или return
        if (peekContext().isInterpreting && switchCondition.exists(l => condition.exists(l.value == _.value)) && !isBreakExecuted && !isReturnExecuted) {
          isInterpreting = true
        }
      case TokenType.DEFAULT =>
        consume("':' after 'default' case label expected", TokenType.COLON)
        if (compile) bblock = addDefaultBlock()
        // Включаем интерпретацию, если switch интерпретируется и не был вызван break или return
        if (peekContext().isInterpreting && !isBreakExecuted && !isReturnExecuted) isInterpreting = true
      case _ =>
    }
    val prev = symbolTable.current
    val root = symbolTable.setCurrent(SymbolNode.Synthetic(), _root)
    if (compile && caseType.tpe == TokenType.CASE) {
      addInstruction(_ => SetFallthrough(switchFallthrough), increment = false)
    }
    // Операторы ветви switch
    while (!tokens.headOption.map(_.tpe).exists(t => t == TokenType.CASE || t == TokenType.DEFAULT || t == TokenType.RIGHT_BRACE)) {
      statement(_root = root)
    }
    if (compile && caseType.tpe == TokenType.CASE) {
      bblock = addDefaultBlock(refill = true)
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
    val expr = assignment()
    if (compile) expr.foreach(tryDereference)
    expr
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
        val lRef = l.asInstanceOf[Expr.Reference]
        if (compile) {
          tryAddCast(from = r.tpe, to = l.tpe)
          addInstruction(_ => Store(l.tpe, s"%${definition.localVariableCounter - 1}", lRef.ref.identifier), increment = false)
        }
        if (isInterpreting) {
          // Присваивание значения по ссылке
          lRef.ref.value = SymbolNode.Type.cast(r.value, lRef.tpe)
          // Печать информации о присваивании
          System.out.println(s"${lRef.name} : ${lRef.tpe} = ${lRef.ref.value} : ${lRef.tpe}")
        }
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
    var isEvaluated = false
    var leftOperandId: String = ""
    while (accept(TokenType.OR)) {
      // Семантическое условие "Логические операторы работают только с целочисленными типами"
      assertSemantic(
        expr.exists(v => v.tpe != SymbolNode.Type.VOID && v.tpe != SymbolNode.Type.DOUBLE),
        "logical operations works with operands of type INT, SHORT, LONG"
      )
      // Правая часть оператора ИЛИ
      expr.foreach { l =>
        if (compile && !isEvaluated) {
          tryDereference(l)
          leftOperandId = s"%${currentDefinition.localVariableCounter - 1}"
          isEvaluated = true
        }
        and().foreach { r =>
          assertSemantic(
            r.tpe != SymbolNode.Type.VOID && r.tpe != SymbolNode.Type.DOUBLE,
            "logical operations works with operands of type INT, SHORT, LONG"
          )
          if (compile) {
            tryDereference(r)
            val rightOperandId = s"%${currentDefinition.localVariableCounter - 1}"
            leftOperandId = s"%${addBinaryInstruction(TokenType.OR, l.tpe, leftOperandId, rightOperandId).identifier}"
          }
          if (isInterpreting) {
            acc = acc.map(evalBinary(TokenType.OR, _, r.value))
            isEvaluated = true
          }
        }
      }
      // Семантика: приведение к типу возвращаемого значения
      expr.foreach(_.tpe = TokenType.SHORT)
    }
    if (isEvaluated) expr.flatMap(v => acc.map(Expr.Value(v.tpe, _))) else expr
  }

  /**
   * Разбор синтаксической конструкции __"Логическое И"__.
   */
  private def and(): Option[Expr] = {
    // Левая часть оператора И
    val expr = comparison()
    var acc = expr.map(_.value)
    var isEvaluated = false
    var leftOperandId: String = ""
    while (accept(TokenType.AND)) {
      // Семантическое условие "Логические операторы работают только с целочисленными типами"
      assertSemantic(
        expr.exists(v => v.tpe != SymbolNode.Type.VOID && v.tpe != SymbolNode.Type.DOUBLE),
        "logical operations works with operands of type INT, SHORT, LONG"
      )
      // Правая часть оператора И
      expr.foreach { l =>
        if (compile && !isEvaluated) {
          tryDereference(l)
          leftOperandId = s"%${currentDefinition.localVariableCounter - 1}"
          isEvaluated = true
        }
        comparison().foreach { r =>
          assertSemantic(
            r.tpe != SymbolNode.Type.VOID && r.tpe != SymbolNode.Type.DOUBLE,
            "logical operations works with operands of type INT, SHORT, LONG"
          )
          if (compile) {
            tryDereference(r)
            val rightOperandId = s"%${currentDefinition.localVariableCounter - 1}"
            leftOperandId = s"%${addBinaryInstruction(TokenType.AND, l.tpe, leftOperandId, rightOperandId).identifier}"
          }
          if (isInterpreting) {
            acc = acc.map(evalBinary(TokenType.AND, _, r.value))
            isEvaluated = true
          }
        }
      }
      // Семантика: приведение к типу возвращаемого значения
      expr.foreach(_.tpe = TokenType.SHORT)
    }
    if (isEvaluated) expr.flatMap(v => acc.map(Expr.Value(v.tpe, _))) else expr
  }

  /**
   * Разбор синтаксической конструкции __"Операция сравнения"__.
   */
  private def comparison(): Option[Expr] = {
    // Левая часть сравнения
    val expr = addition()
    var acc = expr.map(_.value)
    var opcode: Option[TokenType.Value] = None
    var isEvaluated = false
    var leftOperandId: String = ""
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
      expr.foreach { l =>
        if (compile && !isEvaluated) {
          tryDereference(l)
          leftOperandId = s"%${currentDefinition.localVariableCounter - 1}"
          isEvaluated = true
        }
        addition().foreach { r =>
          if (compile) tryDereference(r)
          var rightOperandId = s"%${currentDefinition.localVariableCounter - 1}"
          wideningCast(l.tpe, r.tpe).foreach { t =>
            if (compile) {
              tryAddCast(l.tpe, t, leftOperandId).foreach(i => leftOperandId = s"%${i.identifier}")
              tryAddCast(r.tpe, t, rightOperandId).foreach(i => rightOperandId = s"%${i.identifier}")
            }
            l.tpe = t
          }
          if (compile) {
            leftOperandId = s"%${addBinaryInstruction(opcode.get, l.tpe, leftOperandId, rightOperandId).identifier}"
          }
          if (isInterpreting) {
            acc = opcode.flatMap(code => acc.map(evalBinary(code, _, r.value)))
            isEvaluated = true
          }
        }
      }
      // Семантика: приведение к типу возвращаемого значения
      expr.foreach(_.tpe = TokenType.SHORT)
    }
    if (isEvaluated) expr.flatMap(v => acc.map(Expr.Value(v.tpe, _))) else expr
  }

  /**
   * Разбор синтаксической конструкции __"Сложение-вычитание"__.
   */
  private def addition(): Option[Expr] = {
    // Левая часть аддитивных операций
    val expr = multiplication()
    var acc = expr.map(_.value)
    var opcode: Option[TokenType.Value] = None
    var isEvaluated = false
    var leftOperandId: String = ""
    while ({ opcode = acceptOption(TokenType.MINUS, TokenType.PLUS).map(_.tpe); opcode.isDefined }) {
      // Семантическое условие "Выражение типа void не может быть операндом бинарной операции"
      assertSemantic(expr.exists(_.tpe != SymbolNode.Type.VOID), "addition operand shouldn't be VOID")
      // Правая часть аддитивных операций
      // Семантика: расширение типа значения
      expr.foreach { l =>
        if (compile && !isEvaluated) {
          tryDereference(l)
          leftOperandId = s"%${currentDefinition.localVariableCounter - 1}"
          isEvaluated = true
        }
        multiplication().foreach { r =>
          if (compile) tryDereference(r)
          var rightOperandId = s"%${currentDefinition.localVariableCounter - 1}"
          wideningCast(l.tpe, r.tpe).foreach { t =>
            if (compile) {
              tryAddCast(l.tpe, t, leftOperandId).foreach(i => leftOperandId = s"%${i.identifier}")
              tryAddCast(r.tpe, t, rightOperandId).foreach(i => rightOperandId = s"%${i.identifier}")
            }
            l.tpe = t
          }
          if (compile) {
            leftOperandId = s"%${addBinaryInstruction(opcode.get, l.tpe, leftOperandId, rightOperandId).identifier}"
          }
          if (isInterpreting) {
            acc = opcode.flatMap(code => acc.map(evalBinary(code, _, r.value)))
            isEvaluated = true
          }
        }
      }
    }
    if (isEvaluated) expr.flatMap(v => acc.map(Expr.Value(v.tpe, _))) else expr
  }

  /**
   * Разбор синтаксической конструкции __"Умножение-деление"__.
   */
  private def multiplication(): Option[Expr] = {
    // Левая часть мультипликативных операций
    val expr = unary()
    var acc = expr.map(_.value)
    var opcode: Option[TokenType.Value] = None
    var isEvaluated = false
    var leftOperandId: String = ""
    while ({ opcode = acceptOption(TokenType.SLASH, TokenType.STAR).map(_.tpe); opcode.isDefined }) {
      // Семантическое условие "Выражение типа void не может быть операндом бинарной операции"
      assertSemantic(expr.exists(_.tpe != SymbolNode.Type.VOID), "multiplication operand shouldn't be VOID")
      // Правая часть мультипликативных операций
      // Семантика: расширение типа значения
      expr.foreach { l =>
        if (compile && !isEvaluated) {
          tryDereference(l)
          leftOperandId = s"%${currentDefinition.localVariableCounter - 1}"
          isEvaluated = true
        }
        unary().foreach { r =>
          if (compile) tryDereference(r)
          var rightOperandId = s"%${currentDefinition.localVariableCounter - 1}"
          wideningCast(l.tpe, r.tpe).foreach { t =>
            if (compile) {
              tryAddCast(l.tpe, t, leftOperandId).foreach(i => leftOperandId = s"%${i.identifier}")
              tryAddCast(r.tpe, t, rightOperandId).foreach(i => rightOperandId = s"%${i.identifier}")
            }
            l.tpe = t
          }
          if (compile) {
            leftOperandId = s"%${addBinaryInstruction(opcode.get, l.tpe, leftOperandId, rightOperandId).identifier}"
          }
          if (isInterpreting) {
            acc = opcode.flatMap(code => acc.map(evalBinary(code, _, r.value)))
            isEvaluated = true
          }
        }
      }
    }
    if (isEvaluated) expr.flatMap(v => acc.map(Expr.Value(v.tpe, _))) else expr
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
      // Семантическое условие: "Не смешаны префиксные и суффиксные операции"
      assertSemantic(hasPref.isEmpty || hasSuf.isEmpty, "couldn't mix prefix and suffix operators")
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
        hasPref.forall(_.tpe != TokenType.BANG) || prim.exists(_.tpe != SymbolNode.Type.DOUBLE),
        "bang operator operand shouldn't be DOUBLE"
      )
    }

    if (compile) {
      if (hasPref.isDefined || hasSuf.isDefined) return prim.map { v =>
        hasPref.foreach { p =>
          if (p.tpe == TokenType.MINUS_MINUS || p.tpe == TokenType.PLUS_PLUS) {
            addCountingInstruction(p.tpe, v, isPrefix = true)
          } else {
            tryDereference(v)
            addUnaryInstruction(p.tpe, v.tpe, s"%${currentDefinition.localVariableCounter - 1}")
          }
        }
        hasSuf.foreach(p => addCountingInstruction(p.tpe, v, isPrefix = false))
        Expr.Value(v.tpe, v.value)
      }
    }

    if (isInterpreting) evalUnary(prim, hasPref.map(_.tpe), hasSuf.map(_.tpe)) else prim
  }

  /**
   * Вызов метода.
   */
  private def invocation(ref: Option[Expr]): Option[Expr] = {
    if (compile) {
      ref.foreach { v =>
        val decl = v.asInstanceOf[Expr.Reference].ref.asInstanceOf[SymbolNode.Method]
        addInstruction(id => Call(s"$id", decl.tpe, decl.identifier), increment = decl.tpe != SymbolNode.Type.VOID)
      }
    }

    if (!isInterpreting) return ref.map(v => Expr.Value(v.tpe, v.value))

    ref.flatMap { v =>
      val decl = v.asInstanceOf[Expr.Reference].ref.asInstanceOf[SymbolNode.Method]
      // Сохраняем контекст вычисления
      val prev = symbolTable.current
      symbolTable.setCurrent(decl)
      // Делаем текущей единицей кода блок метода
      codeUnits.push(new View.Drop(wholeUnit, decl.startPos).iterator.buffered)
      // Разбираем и интерпретируем блок
      block(isInvokeBlock = true)
      // Возвращаем предыдущую единицу кода
      codeUnits.pop()
      symbolTable.setCurrent(prev)

      // Получение возвращаемого значения
      val returnValue = this.returnValue

      // Перевод в режим интерпретации после вызова return и сброс возвращаемого значения
      if (interpret) isInterpreting = true
      isReturnExecuted = false
      this.returnValue = None

      returnValue.map(v => Expr.Value(v.tpe, v.value))
    }
  }

  /**
   * Разбор синтаксической конструкции __"Элементарное выражение"__.
   */
  private def primary(): Option[Expr] = {
    // Константа
    val number = acceptOption(TokenType.NUMBER_INT, TokenType.NUMBER_EXP)
    // TODO: при выключенной интерпретации возвращать Value(tpe, undefined)
    if (number.isDefined) return number.map { x =>
      val value = SymbolNode.Type.parseLiteral(x.lexeme, x.tpe)
      if (compile) addInstruction(id => Value(s"$id", x.tpe, value.toString))
      Expr.Value(x.tpe, value)
    }
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
      return invocation(findReference(lookupStart, accessors, identifier) {
        case SymbolNode.Method(`identifier`, _, _, _, _) => true
      })
    }
    // Семантика: поиск переменной или поля
    findReference(lookupStart, accessors, identifier) {
      case SymbolNode.Variable(`identifier`, _, _, _) | SymbolNode.Field(`identifier`, _, _, _) => true
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
        ParserCompiler.printError(v, message)
        throw new ParseError()
    }
  }
}

/**
 * Объект-компаньон класса [Parser].
 */
object ParserCompiler {
  /**
   * Печатает сообщение об ошибке.
   */
  def printError(token: Option[Token], message: String): Unit = {
    System.err.println(s"Parsing error: $message, got: ${token.map(t => s"'${t.lexeme}' at line ${t.line}, pos ${t.pos+1}").getOrElse("nothing")}")
  }
}