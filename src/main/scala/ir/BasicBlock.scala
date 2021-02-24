package ir

import scala.collection.mutable

/**
 * Базовый блок LLVM IR.
 */
class BasicBlock(val identifier: String, var terminator: Terminator = null) {
  val instructions: mutable.Buffer[Instruction] = mutable.Buffer()
}

object BasicBlock {
  def apply(identifier: String, terminator: Terminator = null): BasicBlock = new BasicBlock(identifier, terminator)
}