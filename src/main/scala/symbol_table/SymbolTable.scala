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
  def setCurrent(x: SymbolNode, root: SymbolNode = null): SymbolNode = {
    val prev = cur
    cur = x
    if (root != null) {
      if (prev == root) {
        prev.rightChild = cur
      } else {
        prev.leftChild = cur
      }
    }
    x
  }
}
