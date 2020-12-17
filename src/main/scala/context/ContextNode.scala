package context

import tokens.TokenType

sealed trait ContextNode {
  private var _parent: ContextNode = _
  private var _leftChild: ContextNode = _
  private var _rightChild: ContextNode = _

  def parent: ContextNode = _parent

  def leftChild: ContextNode = _leftChild
  def leftChild_=(node: ContextNode): Unit = { _leftChild = node; node._parent = this }

  def rightChild: ContextNode = _rightChild
  def rightChild_=(node: ContextNode): Unit = { _rightChild = node; node._parent = this }

  def isEmpty: Boolean = _leftChild == null && _rightChild == null
}

object ContextNode {
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

    def default(x: Value): Any = x match {
      case INT => 0
      case SHORT => 0.asInstanceOf[Short]
      case LONG => 0L
      case DOUBLE => 0.0
      case _ => throw new IllegalArgumentException("value conversion error")
    }
  }

  case class Class(name: String) extends ContextNode
  case class Field(name: String, tpe: Type.Value) extends ContextNode
  case class Method(name: String, tpe: Type.Value) extends ContextNode
  case class Variable(name: String, tpe: Type.Value) extends ContextNode
  case class Value(tpe: Type.Value, value: Any) extends ContextNode
  case class Synthetic() extends ContextNode

  def dotPrint(node: ContextNode): Unit = {
    println("digraph G {")
    def _print(node: ContextNode, id: Int = 0): Unit = {
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