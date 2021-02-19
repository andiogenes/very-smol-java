package semantic

import symbol_table.{IdentifiedInIR, SymbolNode, ValueContainer}

/**
 * Промежуточное представление выражений-операторов.
 */
sealed trait Expr {
  var tpe: SymbolNode.Type.Value
  def value: Any
  def isLiteral: Boolean
}

object Expr {

  /**
   * Неименованное значение.
   */
  case class Value(override var tpe: SymbolNode.Type.Value, value: Any) extends Expr {
    override def isLiteral: Boolean = value != SymbolNode.Undefined
    override def toString: String = value.toString
  }

  /**
   * Именованное значение.
   */
  case class Reference(name: String, override var tpe: SymbolNode.Type.Value, ref: ValueContainer with IdentifiedInIR) extends Expr {
    override def value: Any = ref.value
    override def isLiteral: Boolean = false
    override def toString: String = ref.value.toString
  }
}