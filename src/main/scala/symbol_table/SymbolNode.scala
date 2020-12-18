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
    def default(x: Value): String = x match {
      case INT | SHORT | LONG => "0"
      case DOUBLE => "0.E0"
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
  case class Field(name: String, tpe: Type.Value, value: Any) extends SymbolNode

  /**
   * Тип объекта "Метод".
   */
  case class Method(name: String, tpe: Type.Value) extends SymbolNode

  /**
   * Тип объекта "Простая переменная".
   */
  case class Variable(name: String, tpe: Type.Value, value: Any) extends SymbolNode

  /**
   * Искусственный узел дерева.
   */
  class Synthetic() extends SymbolNode

  object Synthetic {
    def apply(): Synthetic = new Synthetic()
  }

  /**
   * Печатает дерево с корнем `node` в формате GraphViz.
   */
  def dotPrint(node: SymbolNode): Unit = {
    println("digraph G {")
    def _print(node: SymbolNode, id: Int = 0): Unit = {
      val q = '"'
      val root = s"\tn$id [${if (node.isInstanceOf[Synthetic]) "style=filled, fillcolor=black" else s"label=$q$node$q"}];"
      println(root)
      val leftId = 2*id+1
      val rightId = 2*id+2
      println(s"\tn$id -> n$leftId;")
      println(s"\tn$id -> n$rightId;")

      if (node.leftChild != null) _print(node.leftChild, leftId) else println(s"\tn$leftId [label=${q}left$q, style=filled, fillcolor=gray];")
      if (node.rightChild != null) _print(node.rightChild, rightId) else println(s"\tn$rightId [label=${q}right$q, style=filled, fillcolor=gray];")
    }
    _print(node)
    println("}")
  }
}