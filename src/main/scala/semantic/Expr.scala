package semantic

import symbol_table.SymbolNode

/**
 * Промежуточное представление выражений-операторов.
 */
sealed trait Expr {
  var tpe: SymbolNode.Type.Value
}

object Expr {

  /**
   * Неименованное значение.
   */
  case class Value(override var tpe: SymbolNode.Type.Value, value: Any) extends Expr

  /**
   * Именованное значение.
   */
  case class Reference(name: String, override var tpe: SymbolNode.Type.Value) extends Expr
}