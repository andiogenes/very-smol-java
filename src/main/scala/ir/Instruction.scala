package ir

import symbol_table.SymbolNode

/**
 * Инструкция LLVM-биткода.
 *
 * [[https://llvm.org/docs/LangRef.html#instruction-reference]]
 */
sealed trait Instruction {
  val identifier: String
}

object Instruction {
  case class Value(override val identifier: String, tpe: SymbolNode.Type.Value, value: String) extends Instruction

  case class Add(override val identifier: String, tpe: SymbolNode.Type.Value, l: String, r: String) extends Instruction
  case class Sub(override val identifier: String, tpe: SymbolNode.Type.Value, l: String, r: String) extends Instruction
  case class Mul(override val identifier: String, tpe: SymbolNode.Type.Value, l: String, r: String) extends Instruction
  case class Div(override val identifier: String, tpe: SymbolNode.Type.Value, l: String, r: String) extends Instruction
  case class Minus(override val identifier: String, tpe: SymbolNode.Type.Value, value: String) extends Instruction

  case class And(override val identifier: String, tpe: SymbolNode.Type.Value, l: String, r: String) extends Instruction
  case class Or(override val identifier: String, tpe: SymbolNode.Type.Value, l: String, r: String) extends Instruction
  case class Neg(override val identifier: String, tpe: SymbolNode.Type.Value, value: String) extends Instruction

  case class Equal(override val identifier: String, tpe: SymbolNode.Type.Value, l: String, r: String) extends Instruction
  case class NotEqual(override val identifier: String, tpe: SymbolNode.Type.Value, l: String, r: String) extends Instruction
  case class Greater(override val identifier: String, tpe: SymbolNode.Type.Value, l: String, r: String) extends Instruction
  case class GreaterEqual(override val identifier: String, tpe: SymbolNode.Type.Value, l: String, r: String) extends Instruction
  case class Lower(override val identifier: String, tpe: SymbolNode.Type.Value, l: String, r: String) extends Instruction
  case class LowerEqual(override val identifier: String, tpe: SymbolNode.Type.Value, l: String, r: String) extends Instruction

  case class Call(override val identifier: String, tpe: SymbolNode.Type.Value, method: String) extends Instruction

  case class Alloca(override val identifier: String, tpe: SymbolNode.Type.Value) extends Instruction
  case class Load(override val identifier: String, tpe: SymbolNode.Type.Value, source: String) extends Instruction
  case class Store(tpe: SymbolNode.Type.Value, value: String, dst: String) extends Instruction {
    override val identifier: String = ""
  }

  case class Cast(override val identifier: String, from: SymbolNode.Type.Value, value: String, to: SymbolNode.Type.Value) extends Instruction
  case class ExtBool(override val identifier: String, value: String) extends Instruction

  case class Println(tpe: SymbolNode.Type.Value, value: String) extends Instruction {
    override val identifier: String = ""
  }
  case class PrintlnVoid() extends Instruction {
    override val identifier: String = ""
  }

  case class Fallthrough(override val identifier: String) extends Instruction
  case class GetFallthrough(override val identifier: String, source: String) extends Instruction
  case class SetFallthrough(dst: String) extends Instruction {
    override val identifier: String = ""
  }
  case class UnsetFallthrough(dst: String) extends Instruction {
    override val identifier: String = ""
  }
  case class EnsureFallthrough(override val identifier: String, l: String, r: String) extends Instruction
}

/**
 * Инструкция-терминатор базового блока.
 *
 * [[https://llvm.org/docs/LangRef.html#terminator-instructions]]
 */
sealed trait Terminator

object Terminator {
  case class Return(tpe: SymbolNode.Type.Value, value: String = "") extends Terminator
  case class Goto(dst: String) extends Terminator
  case class If(cond: String, trueBranch: String, falseBranch: String) extends Terminator
}