package symbol_table

/**
 * Таблица символов.
 */
class SymbolTable(var root: SymbolNode = null, private var cur: SymbolNode = null) {
  /**
   * Текущий узел таблицы.
   */
  def current: SymbolNode = cur

  /**
   * Устанавливает текущий узел таблицы.
   */
  def setCurrent[T <: SymbolNode](x: T, root: SymbolNode = null, isSameScope: Boolean = false): T = {
    val prev = cur
    cur = x
    if (root != null) {
      if (prev == root && !isSameScope) {
        prev.rightChild = cur
      } else {
        prev.leftChild = cur
      }
    }
    x
  }
}
