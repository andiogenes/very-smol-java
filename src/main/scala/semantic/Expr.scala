package semantic

import symbol_table.SymbolNode

/**
 * Промежуточное представление выражений-операторов.
 */
sealed trait Expr {
  var tpe: SymbolNode.Type.Value
  def isLiteral: Boolean
}

object Expr {

  /**
   * Неименованное значение.
   */
  case class Value(override var tpe: SymbolNode.Type.Value, value: Any) extends Expr {
    override def isLiteral: Boolean = value != SymbolNode.Undefined
  }

  /**
   * Именованное значение.
   */
  case class Reference(name: String, override var tpe: SymbolNode.Type.Value) extends Expr {
    override def isLiteral: Boolean = false
  }
}