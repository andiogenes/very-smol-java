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
  def setCurrent(x: SymbolNode): SymbolNode = {
    cur = x
    x
  }
}
