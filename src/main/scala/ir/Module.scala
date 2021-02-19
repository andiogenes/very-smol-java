package ir

import symbol_table.SymbolNode

import scala.collection.mutable

/**
 * Модуль LLVM.
 *
 * [[https://llvm.org/docs/LangRef.html#module-structure]]
 */
class Module {
  private val _entries: mutable.Buffer[ModuleEntry] = mutable.Buffer()
  def entries: Seq[ModuleEntry] = _entries.toSeq

  def add[E <: ModuleEntry](entry: E): E = {
    _entries.append(entry)
    entry
  }
}

sealed trait ModuleEntry {
  val identifier: String
  val tpe: SymbolNode.Type.Value
}

object ModuleEntry {

  /**
   * Глобальная переменная модуля.
   *
   * [[https://llvm.org/docs/LangRef.html#globalvars]]
   */
  case class GlobalVariable(override val identifier: String, override val tpe: SymbolNode.Type.Value, value: String) extends ModuleEntry

  /**
   * Функция модуля.
   *
   * [[https://llvm.org/docs/LangRef.html#functionstructure]]
   */
  class FunctionDefinition(override val identifier: String, override val tpe: SymbolNode.Type.Value) extends ModuleEntry {
    var localVariableCounter = 0
    var basicBlockCounter = 0
    var switchCounter = 0

    val blocks: mutable.Buffer[BasicBlock] = mutable.Buffer()
  }

  object FunctionDefinition {
    def apply(identifier: String, tpe: SymbolNode.Type.Value, blocks: BasicBlock*): FunctionDefinition = {
      val definition = new FunctionDefinition(identifier, tpe)
      definition.blocks.appendAll(blocks)
      definition
    }

    def unapply(d: FunctionDefinition): Option[(String, SymbolNode.Type.Value, Seq[BasicBlock])] = {
      Some(d.identifier, d.tpe, d.blocks.toSeq)
    }
  }
}