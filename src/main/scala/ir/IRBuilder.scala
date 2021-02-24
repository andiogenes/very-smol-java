package ir

import ir.Instruction._
import ir.ModuleEntry.FunctionDefinition
import ir.Terminator.{Goto, If, Return}
import semantic.Expr
import symbol_table.SymbolNode
import tokens.TokenType

import scala.collection.mutable

/**
 * Сборщик промежуточного представления для генерации в LLVM-биткод.
 */
trait IRBuilder {
  val module = new Module()

  var bblock: BasicBlock = _
  var definition: FunctionDefinition = _

  val fieldInitializationBlock: BasicBlock = BasicBlock("init", Return(SymbolNode.Type.VOID))
  val fieldInitialization: FunctionDefinition = FunctionDefinition("field_init", SymbolNode.Type.VOID, fieldInitializationBlock)

  private var isInitialization: Boolean = false
  def switchToInitialization(): Unit = isInitialization = true
  def switchToCommon(): Unit = isInitialization = false

  def currentBlock: BasicBlock = if (isInitialization) fieldInitializationBlock else bblock
  def currentDefinition: FunctionDefinition = if (isInitialization) fieldInitialization else definition

  def addBlock(linkWithPrevious: Boolean = false): BasicBlock = {
    val block = BasicBlock(s"block_${definition.basicBlockCounter}")
    definition.basicBlockCounter += 1

    if (linkWithPrevious) {
      definition.blocks.lastOption
        .filter(_.terminator == null)
        .foreach(_.terminator = Goto(block.identifier))
    }
    definition.blocks.append(block)

    block
  }

  def splitBlock(): BasicBlock = {
    addBlock(true)
  }

  def addInstruction(pf: PartialFunction[Int, Instruction], increment: Boolean = true): Instruction = {
    val instruction = pf(currentDefinition.localVariableCounter)
    if (increment) currentDefinition.localVariableCounter += 1

    currentBlock.instructions.append(instruction)

    instruction
  }

  def tryAddCast(from: SymbolNode.Type.Value, to: SymbolNode.Type.Value, value: String = ""): Option[Instruction] = {
    if (to != SymbolNode.Type.DOUBLE || from == to) return None
    val actualValue = if (value.isEmpty) s"%${currentDefinition.localVariableCounter - 1}" else value
    Some(addInstruction(id => Cast(s"$id", from, actualValue, to)))
  }

  def tryDereference(expr: Expr): Option[Instruction] = {
    expr match {
      case Expr.Reference(_, tpe, ref) =>
        ref match {
          case _: SymbolNode.Method => None
          case x => Some(addInstruction(id => Load(s"$id", tpe, x.identifier)))
        }
      case _ => None
    }
  }

  def addUnaryInstruction(opcode: TokenType.Value, tpe: SymbolNode.Type.Value, value: String): Option[Instruction] = {
    opcode match {
      case TokenType.PLUS => None
      case TokenType.MINUS => Some(addInstruction(id => Minus(s"$id", tpe, value)))
      case TokenType.BANG =>
        val cmp = addInstruction(id => Neg(s"$id", tpe, value)).identifier
        Some(addInstruction(id => ExtBool(s"$id", s"%$cmp")))
      case _ => throw new IllegalStateException("shouldn't reach here")
    }
  }

  def addCountingInstruction(opcode: TokenType.Value, expr: Expr, isPrefix: Boolean): Instruction = {
    val Expr.Reference(_, tpe, ref) = expr.asInstanceOf[Expr.Reference]
    val value = addInstruction(id => Load(s"$id", tpe, ref.identifier))
    val counting = opcode match {
      case TokenType.PLUS_PLUS =>
        addInstruction(id => Add(s"$id", tpe, s"%${value.identifier}", s"1${if (tpe == SymbolNode.Type.DOUBLE) ".0" else ""}"))
      case TokenType.MINUS_MINUS =>
        addInstruction(id => Sub(s"$id", tpe, s"%${value.identifier}", s"1${if (tpe == SymbolNode.Type.DOUBLE) ".0" else ""}"))
      case _ => throw new IllegalStateException("shouldn't reach here")
    }
    addInstruction(_ => Store(tpe, s"%${counting.identifier}", ref.identifier), increment = false)
    val result = if (isPrefix) counting else value
    addInstruction(id => Value(s"$id", tpe, s"%${result.identifier}"))
  }

  def addBinaryInstruction(opcode: TokenType.Value, tpe: SymbolNode.Type.Value, l: String, r: String): Instruction = {
    opcode match {
      case TokenType.STAR => addInstruction(id => Mul(s"$id", tpe, l, r))
      case TokenType.SLASH => addInstruction(id => Div(s"$id", tpe, l, r))
      case TokenType.PLUS => addInstruction(id => Add(s"$id", tpe, l, r))
      case TokenType.MINUS => addInstruction(id => Sub(s"$id", tpe, l, r))
      case TokenType.EQUAL_EQUAL =>
        val cmp = addInstruction(id => Equal(s"$id", tpe, l, r)).identifier
        addInstruction(id => ExtBool(s"$id", s"%$cmp"))
      case TokenType.BANG_EQUAL =>
        val cmp = addInstruction(id => NotEqual(s"$id", tpe, l, r)).identifier
        addInstruction(id => ExtBool(s"$id", s"%$cmp"))
      case TokenType.GREATER =>
        val cmp = addInstruction(id => Greater(s"$id", tpe, l, r)).identifier
        addInstruction(id => ExtBool(s"$id", s"%$cmp"))
      case TokenType.GREATER_EQUAL =>
        val cmp = addInstruction(id => GreaterEqual(s"$id", tpe, l, r)).identifier
        addInstruction(id => ExtBool(s"$id", s"%$cmp"))
      case TokenType.LESS =>
        val cmp = addInstruction(id => Lower(s"$id", tpe, l, r)).identifier
        addInstruction(id => ExtBool(s"$id", s"%$cmp"))
      case TokenType.LESS_EQUAL =>
        val cmp = addInstruction(id => LowerEqual(s"$id", tpe, l, r)).identifier
        addInstruction(id => ExtBool(s"$id", s"%$cmp"))
      case TokenType.AND =>
        val leftId = {
          val cmp = addInstruction(id => NotEqual(s"$id", tpe, l, "0")).identifier
          addInstruction(id => ExtBool(s"$id", s"%$cmp")).identifier
        }
        val rightId = {
          val cmp = addInstruction(id => NotEqual(s"$id", tpe, r, "0")).identifier
          addInstruction(id => ExtBool(s"$id", s"%$cmp")).identifier
        }
        addInstruction(id => And(s"$id", SymbolNode.Type.INT, s"%$leftId", s"%$rightId"))
      case TokenType.OR =>
        val leftId = {
          val cmp = addInstruction(id => NotEqual(s"$id", tpe, l, "0")).identifier
          addInstruction(id => ExtBool(s"$id", s"%$cmp")).identifier
        }
        val rightId = {
          val cmp = addInstruction(id => NotEqual(s"$id", tpe, r, "0")).identifier
          addInstruction(id => ExtBool(s"$id", s"%$cmp")).identifier
        }
        addInstruction(id => Or(s"$id", SymbolNode.Type.INT, s"%$leftId", s"%$rightId"))
      case _ => throw new IllegalStateException("shouldn't reach here")
    }
  }

  def startSwitchInstruction(): Unit = {
    val d = currentDefinition
    switchContext.push((switchIdentifier, switchValue, switchType, switchBlockCounter, switchHasDefault, switchFallthrough))
    switchIdentifier = d.switchCounter
    d.switchCounter += 1
    switchBlockCounter = 0
    switchHasDefault = false
    switchFallthrough = s"%${addInstruction(id => Fallthrough(s"$id")).identifier}"
    addInstruction(_ => UnsetFallthrough(switchFallthrough), increment = false)
  }

  def refillSwitchInstruction(exprOpt: Option[Expr]): Unit = {
    exprOpt.foreach { expr =>
      switchValue = s"%${currentDefinition.localVariableCounter - 1}"
      switchType = expr.tpe
    }
  }

  def addCaseBlock(caseExprOpt: Option[Expr], tpe: SymbolNode.Type.Value): BasicBlock = {
    val l = caseExprOpt
      .flatMap(expr => tryAddCast(from = expr.tpe, to = tpe))
      .map(_.identifier)
      .getOrElse(s"%${currentDefinition.localVariableCounter - 1}")

    val r = tryAddCast(from = switchType, to = tpe, value = switchValue)
      .map(_.identifier)
      .getOrElse(switchValue)

    val cmp = addInstruction(id => Equal(s"$id", tpe, l, r)).identifier
    val fallthrough = addInstruction(id => GetFallthrough(s"$id", switchFallthrough)).identifier
    val condition = addInstruction(id => EnsureFallthrough(s"$id", s"%$fallthrough", s"%$cmp")).identifier

    val block = BasicBlock(s"switch_${switchIdentifier}_$switchBlockCounter")
    switchBlockCounter += 1

    definition.blocks.lastOption
      .filter(_.terminator == null)
      .foreach(_.terminator = If(s"%$condition", block.identifier, s"switch_${switchIdentifier}_$switchBlockCounter"))
    definition.blocks.append(block)

    block
  }

  def addDefaultBlock(refill: Boolean = false): BasicBlock = {
    val block = BasicBlock(s"switch_${switchIdentifier}_$switchBlockCounter")
    if (refill) switchBlockCounter += 1

    definition.blocks.lastOption
      .filter(_.terminator == null)
      .foreach(_.terminator = Goto(block.identifier))
    definition.blocks.append(block)

    if (!refill) {
      switchHasDefault = true
    }

    block
  }

  def endSwitchInstruction(): BasicBlock = {
    val block = BasicBlock(s"switch_${switchIdentifier}_end")

    definition.blocks.lastOption
      .filter(_.terminator == null)
      .foreach(_.terminator = Goto(block.identifier))
    definition.blocks.append(block)

    val ctx = switchContext.pop()
    switchIdentifier = ctx._1
    switchValue = ctx._2
    switchType = ctx._3
    switchBlockCounter = ctx._4
    switchHasDefault = ctx._5
    switchFallthrough = ctx._6

    block
  }

  var switchIdentifier = 0
  var switchValue: String = ""
  var switchType: SymbolNode.Type.Value = SymbolNode.Type.VOID
  var switchBlockCounter = 0
  var switchHasDefault: Boolean = false
  var switchFallthrough: String = ""
  val switchContext: mutable.Stack[(Int, String, SymbolNode.Type.Value, Int, Boolean, String)] = mutable.Stack()

  val namespacePrefix: mutable.Stack[String] = mutable.Stack()

  def formatEntryName(name: String): String = s"${namespacePrefix.reverseIterator.mkString("_")}_$name"
}
