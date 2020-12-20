package semantic

import symbol_table.SymbolNode
import tokens.Token

import scala.PartialFunction.cond
import scala.annotation.tailrec

/**
 * Процедуры семантического анализа.
 */
object SemanticAnalyzer {
  class SemanticError extends RuntimeException

  private var _switchNesting = 0

  /**
   * Количество вхождений в оператор switch в текущем месте программы.
   */
  def switchNesting: Int = _switchNesting

  /**
   * Войти в очередной switch.
   */
  def enterSwitch(): Unit = _switchNesting += 1

  /**
   * Выйти из очередного switch.
   */
  def leaveSwitch(): Unit = _switchNesting -= 1

  private var methodHasReturn = false

  /**
   * Войти в очередной метод.
   */
  def enterMethod(): Unit = methodHasReturn = false

  /**
   * Зафиксировать наличие оператора return в методе.
   */
  def captureReturn(): Unit = methodHasReturn = true

  /**
   * Покинуть очередной метод
   * @param omitReturn Следует ли опустить оператор return.
   */
  def leaveMethod(omitReturn: Boolean): Unit = {
    assertSemantic(omitReturn || methodHasReturn, "missing return statement")
    methodHasReturn = false
  }

  /**
   * Условие, что самый верхний класс называется Main.
   */
  def checkTopmostClassIsMain(classDecl: SymbolNode.Class): Unit = {
    assertSemantic(classDecl.name == "Main", s"topmost class is named '${classDecl.name}', should be named 'Main'")
  }

  /**
   * Условие, что указанный класс имеет метод с заданным именем (и возможно, указанным типом возвращаемого значения).
   */
  def checkClassHasMethod(classDecl: SymbolNode.Class, methodName: String, methodType: SymbolNode.Type.Value = null): Unit = {
    @tailrec
    def search(current: SymbolNode): Boolean = {
      current match {
        case null => false
        case SymbolNode.Method(`methodName`, tpe) => methodType == null || methodType == tpe
        case _ => search(current.leftChild)
      }
    }
    assertSemantic(search(classDecl.rightChild), s"class '${classDecl.name}' doesn't have method '$methodName'${if (methodType != null) s" with return type $methodType" else ""}")
  }

  /**
   * Условие, что в области видимости нет объекта с тем же именем, что и у заданного.
   */
  def checkNoSameDeclarationsInScope(decl: SymbolNode): Unit = {
    def formatNode(x: SymbolNode): String = {
      x match {
        case SymbolNode.Class(name) => s"class $name"
        case SymbolNode.Method(name, _) => s"method $name"
        case SymbolNode.Field(name, _, _) => s"field $name"
        case SymbolNode.Variable(name, _, _) => s"variable $name"
        case _ => throw new IllegalArgumentException()
      }
    }

    @tailrec
    def lookup(current: SymbolNode): Boolean = {
      current match {
        case null | SymbolNode.Synthetic() => true
        case SymbolNode.Field(name, _, _) =>
          decl match {
            case SymbolNode.Field(`name`, _, _) => false
            case _ => lookup(current.parent)
          }
        case SymbolNode.Variable(name, _, _) =>
          decl match {
            case SymbolNode.Variable(`name`, _, _) => false
            case _ => lookup(current.parent)
          }
        case SymbolNode.Method(name, _) =>
          decl match {
            case SymbolNode.Method(`name`, _) => false
            case _ => lookup(current.parent)
          }
        case x => if (x == decl) false else lookup(current.parent)
      }
    }
    assertSemantic(lookup(decl.parent), s"multiple declaration of ${formatNode(decl)}")
  }

  /**
   * Условие, что встреченный оператор return возвращает нужное значение.
   */
  def checkProperReturn(start: SymbolNode, valueOption: Option[Expr]): Unit = {
    val returnType: SymbolNode.Type.Value = valueOption match {
      case Some(Expr.Value(tpe, _)) => tpe
      case Some(Expr.Reference(_, tpe)) => tpe
      case None => SymbolNode.Type.VOID
    }

    @tailrec
    def lookup(current: SymbolNode): (Boolean, Option[SymbolNode.Type.Value]) = {
      current match {
        case null | _: SymbolNode.Class | _: SymbolNode.Field => (false, None)
        case SymbolNode.Method(_, tpe) => (cast(from = returnType, to = tpe).isDefined, Some(tpe))
        case _ => lookup(current.parent)
      }
    }
    lookup(start) match {
      case (false, None) => throw new IllegalStateException("shouldn't reach here")
      case (false, Some(tpe)) => assertSemantic(assertion = false, s"incompatible types: $tpe required, got $returnType")
      case _ =>
    }
  }

  /**
   * Поиск ссылочного значения по указанной цепочке запросов.
   *
   * Если цепочка состоит из одного элемента, считается, что поиск начинается из ближайшей области видимости.
   * В ином случае, доступ разрешается начиная с корневого класса.
   */
  def findReference(start: SymbolNode, accessors: Seq[String], identifier: String)(pf: PartialFunction[SymbolNode, Boolean]): Option[Expr] = {
    val fullName = s"${if (accessors.nonEmpty) s"${accessors.mkString(".")}." else ""}$identifier"

    @tailrec
    def findClass(current: SymbolNode, name: String): Option[SymbolNode.Class] = {
      current match {
        case classDecl: SymbolNode.Class if classDecl.name == name => Some(classDecl)
        case null => None
        case x => findClass(x.leftChild, name)
      }
    }

    @tailrec
    def searchDownward(current: SymbolNode, pf: PartialFunction[SymbolNode, Boolean]): Option[SymbolNode] = {
      current match {
        case null => None
        case x if cond(x)(pf) => Some(x)
        case x => searchDownward(x.leftChild, pf)
      }
    }

    @tailrec
    def searchUpward(current: SymbolNode, pf: PartialFunction[SymbolNode, Boolean]): Option[SymbolNode] = {
      current match {
        case null => None
        case x if cond(x)(pf) => Some(x)
        case x => searchUpward(x.parent, pf)
      }
    }

    val node = if (accessors.nonEmpty) {
      accessors.tail
        .foldLeft(findClass(start, accessors.head)) { (acc, v) => acc.flatMap(cl => findClass(cl.rightChild, v)) }
        .flatMap { cl => searchDownward(cl.rightChild, pf) }
    } else {
      searchUpward(start, pf)
    }

    node match {
      case Some(SymbolNode.Variable(_, tpe, _)) => Some(Expr.Reference(fullName, tpe))
      case Some(SymbolNode.Field(_, tpe, _)) => Some(Expr.Reference(fullName, tpe))
      case Some(SymbolNode.Method(_, tpe)) => Some(Expr.Value(tpe, SymbolNode.Undefined))
      case _ => assertSemantic(assertion = false, s"$fullName not found"); None
    }
  }

  /**
   * Условие, что приведение типа from к to корректное (т.е., что приведение не приводит к сужению размеру типа, и нет приведений с `void`).
   */
  def checkTypeConsistency(from: SymbolNode.Type.Value, to: SymbolNode.Type.Value, literal: Boolean = false): Unit = {
    assertSemantic(cast(from, to, literal).contains(to), s"type mismatch - expected $to, got $from")
  }

  /**
   * Строго расширяющее приведение типа from к to.
   */
  def cast(from: SymbolNode.Type.Value, to: SymbolNode.Type.Value, literal: Boolean = false): Option[SymbolNode.Type.Value] = {
    (from, to) match {
      case (SymbolNode.Type.VOID, SymbolNode.Type.VOID) => Some(SymbolNode.Type.VOID)
      case (SymbolNode.Type.VOID, _) | (_, SymbolNode.Type.VOID) => None
      case (SymbolNode.Type.SHORT, t) => Some(t)
      case (SymbolNode.Type.INT, t) if literal || t != SymbolNode.Type.SHORT => Some(t)
      case (SymbolNode.Type.LONG, SymbolNode.Type.DOUBLE) => Some(SymbolNode.Type.DOUBLE)
      case (`from`, `from`) => Some(from)
      case _ => None
    }
  }

  /**
   * Симметричное расширяющее приведение типа from к to.
   *
   * Используется в разрешении типов операций.
   *
   * Пример: `long + int = long; double + long = double; int + short = int;`
   */
  def wideningCast(l: SymbolNode.Type.Value, r: SymbolNode.Type.Value): Option[SymbolNode.Type.Value] = {
    (l, r) match {
      case (SymbolNode.Type.VOID, _) | (_, SymbolNode.Type.VOID) => None
      case (SymbolNode.Type.DOUBLE, _) | (_, SymbolNode.Type.DOUBLE) => Some(SymbolNode.Type.DOUBLE)
      case (SymbolNode.Type.LONG, _) | (_, SymbolNode.Type.LONG) => Some(SymbolNode.Type.LONG)
      case (SymbolNode.Type.INT, _) | (_, SymbolNode.Type.INT) => Some(SymbolNode.Type.INT)
      case (SymbolNode.Type.SHORT, _) | (_, SymbolNode.Type.SHORT) => Some(SymbolNode.Type.SHORT)
      case _ => None
    }
  }

  /**
   * Утверждение, что семантическое условие выполняется.
   *
   * Если не выполняется, печатает в поток ошибок сообщение
   * и бросает `SemanticError`.
   */
  def assertSemantic(assertion: Boolean, message: String): Unit = {
    if (!assertion) {
      printError(message)
      throw new SemanticError()
    }
  }

  private var token: Option[Token] = None

  /**
   * Устанавливает текущую позицию семантического анализа в исходном коде.
   */
  def setCursor(t: Token): Unit = token = Some(t)

  /**
   * Печатает сообщение об ошибке.
   */
  def printError(message: String): Unit = {
    System.err.println(s"Semantic error: $message, ${token.map(t => s"line ${t.line}, pos ${t.pos+1}").getOrElse("")}")
  }
}
