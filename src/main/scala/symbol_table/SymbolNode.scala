package symbol_table

import tokens.TokenType

/**
 * Представление элемента таблицы символов как узла бинарного дерева.
 */
sealed trait SymbolNode {
  private var _parent: SymbolNode = _
  private var _leftChild: SymbolNode = _
  private var _rightChild: SymbolNode = _

  /**
   * Родитель узла.
   */
  def parent: SymbolNode = _parent
  def parent_=(node: SymbolNode): Unit = _parent = node

  /**
   * Левый ребенок узла.
   */
  def leftChild: SymbolNode = _leftChild
  def leftChild_=(node: SymbolNode): Unit = {
    if (_leftChild != null) {
      _leftChild._parent = null
    }

    _leftChild = node

    if (node != null) {
      node._parent = this
    }
  }

  /**
   * Правый ребенок узла.
   */
  def rightChild: SymbolNode = _rightChild
  def rightChild_=(node: SymbolNode): Unit = {
    if (_rightChild != null) {
      _rightChild._parent = null
    }

    _rightChild = node

    if (node != null) {
      node._parent = this
    }
  }

  /**
   * Проверяет пустоту узла. Узел пуст, когда не имеет потомков.
   */
  def isEmpty: Boolean = _leftChild == null && _rightChild == null
}

/**
 * Узел таблицы символов, который хранит некоторое значение.
 */
sealed trait ValueContainer { _: SymbolNode =>
  var value: Any
}

sealed trait IdentifiedInIR { _: SymbolNode =>
  val identifier: String
}

object SymbolNode {
  /**
   * Типы данных.
   */
  object Type extends Enumeration {
    val VOID, INT, SHORT, LONG, DOUBLE = Value

    implicit def tokenToContextType(x: TokenType.Value): Type.Value = x match {
      case TokenType.VOID => VOID
      case TokenType.INT | TokenType.NUMBER_INT => INT
      case TokenType.SHORT => SHORT
      case TokenType.LONG => LONG
      case TokenType.DOUBLE | TokenType.NUMBER_EXP => DOUBLE
      case _ => throw new IllegalArgumentException("value conversion error")
    }

    /**
     * Возвращает образ стандартного значения для типа данных.
     */
    def default(x: Value): Any = x match {
      case INT | SHORT | LONG => 0
      case DOUBLE => 0E0
      case VOID => Undefined
      case _ => throw new IllegalArgumentException("value conversion error")
    }

    /**
     * Приводит значение к указанному типу.
     */
    def cast(value: Any, tpe: Value): Any = value match {
      case x: Int => tpe match {
        case INT | SHORT | LONG => x
        case DOUBLE => x.toDouble
        case _ => throw new IllegalArgumentException("value conversion error")
      }
      case x: Double => tpe match {
        case INT | SHORT | LONG => x.toDouble
        case DOUBLE => x
        case _ => throw new IllegalArgumentException("value conversion error")
      }
      case _ => throw new IllegalArgumentException("value conversion error")
    }

    /**
     * Преобразует образ значения в значение.
     */
    def parseLiteral(value: String, x: Value): Any = x match {
      case INT | SHORT | LONG => value.toInt
      case DOUBLE => value.toDouble
      case _ => throw new IllegalArgumentException("value conversion error")
    }
  }

  /**
   * Метка для значений, тип которых нельзя определить на этапе построения семантической таблицы
   * без полного семантического контроля.
   */
  object Undefined {
    override def toString: String = "UNDEFINED"
  }

  /**
   * Тип объекта "Класс".
   */
  case class Class(name: String) extends SymbolNode

  /**
   * Тип объекта "Поле".
   */
  case class Field(name: String, tpe: Type.Value, override var value: Any, override val identifier: String) extends SymbolNode with ValueContainer with IdentifiedInIR

  /**
   * Тип объекта "Метод".
   */
  case class Method(name: String, tpe: Type.Value, override var value: Any, var startPos: Int = -1, override val identifier: String) extends SymbolNode with ValueContainer with IdentifiedInIR

  /**
   * Тип объекта "Простая переменная".
   */
  case class Variable(name: String, tpe: Type.Value, override var value: Any, override val identifier: String) extends SymbolNode with ValueContainer with IdentifiedInIR

  /**
   * Искусственный узел дерева.
   */
  class Synthetic() extends SymbolNode

  object Synthetic {
    def apply(): Synthetic = new Synthetic()
    def unapply(x: Synthetic): Boolean = true
  }

  /**
   * Печатает дерево с корнем `node` в формате GraphViz.
   */
  def dotPrint(node: SymbolNode, title: String = ""): Unit = {
    System.out.println("digraph G {")
    val q = '"'
    def _print(node: SymbolNode, id: Int = 0): Unit = {
      val root = s"\tn$id [${if (node.isInstanceOf[Synthetic]) "style=filled, fillcolor=black" else s"label=$q$node$q"}];"
      System.out.println(root)
      val leftId = 2*id+1
      val rightId = 2*id+2
      System.out.println(s"\tn$id -> n$leftId;")
      System.out.println(s"\tn$id -> n$rightId;")

      if (node.leftChild != null) _print(node.leftChild, leftId) else System.out.println(s"\tn$leftId [label=${q}left$q, style=filled, fillcolor=gray];")
      if (node.rightChild != null) _print(node.rightChild, rightId) else System.out.println(s"\tn$rightId [label=${q}right$q, style=filled, fillcolor=gray];")
    }
    _print(node)
    if (title.nonEmpty) {
      System.out.println(s"labelloc=${q}t$q;")
      System.out.println(s"label=$q$title$q;")
    }
    System.out.println("}")
  }
}