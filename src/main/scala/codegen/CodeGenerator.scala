package codegen

import java.io.{File, PrintWriter}
import ir.{BasicBlock, IRBuilder, Instruction, ModuleEntry, Terminator}
import symbol_table.SymbolNode

/**
 * Генератор кода LLVM IR в формате ASCII (человеко-читаемое представление).
 */
trait CodeGenerator { _: IRBuilder =>
  val destination: String

  private val ll = new PrintWriter(new File(s"$destination.ll"))

  /**
   * Порождение строки кода.
   */
  private def emitln(line: String): Unit = ll.println(line)

  /**
   * Порождение кода всей программы.
   */
  def emitAll(): Unit = {
    emitStd()
    for (e <- module.entries) {
      emitEntry(e)
    }
    emitFieldInitialization()
    emitProgramEntry()
    ll.close()
  }

  /**
   * Порождение кода системных объявлений (printf, форматы вывода).
   */
  private def emitStd(): Unit = {
    emitln(
      """declare i32 @printf(i8*, ...)
        |
        |@digit_format = constant [4 x i8] c"%d\0A\00"
        |@float_format = constant [4 x i8] c"%g\0A\00"
        |@void_format = constant [4 x i8] c"\0A\00\00\00"
        |""".stripMargin)
  }

  /**
   * Порождение инициализации полей.
   */
  private def emitFieldInitialization(): Unit = {
    emitEntry(fieldInitialization)
  }

  /**
   * Порождение точки входа в программу.
   */
  private def emitProgramEntry(): Unit = {
    emitln(
      """define i32 @main() {
        | call void @field_init()
        | call void @Main_main()
        | ret i32 0
        |}""".stripMargin
    )
  }

  /**
   * Порождение кода элемента модуля - функций и глобальных переменных.
   * @param e текущий элемент модуля
   */
  private def emitEntry(e: ModuleEntry): Unit = {
    e match {
      case ModuleEntry.GlobalVariable(identifier, tpe, value) =>
        emitln(s"@$identifier = global ${llvmType(tpe)} $value\n")
      case ModuleEntry.FunctionDefinition(identifier, tpe, blocks) =>
        emitln(s"define ${llvmType(tpe)} @$identifier() {")
        for (b <- blocks) {
          emitBlock(b)
        }
        emitln("}\n")
    }
  }

  /**
   * Порождение кода для базового блока.
   * @param b базовый блок
   */
  private def emitBlock(b: BasicBlock): Unit = {
    if (b.terminator == null) return

    emitln(s"${b.identifier}:")
    for (i <- b.instructions) {
      emitInstruction(i)
    }
    emitTerminator(b.terminator)
  }

  /**
   * Порождение кода для инструкций.
   * @param i инструкция
   */
  private def emitInstruction(i: Instruction): Unit = {
    i match {
      case Instruction.Value(identifier, tpe, value) =>
        emitln(s" %$identifier = ${fpaPrefix(tpe)}add ${llvmType(tpe)} $value, ${SymbolNode.Type.default(tpe)}")
      case Instruction.Add(identifier, tpe, l, r) =>
        emitln(s" %$identifier = ${fpaPrefix(tpe)}add ${llvmType(tpe)} $l, $r")
      case Instruction.Sub(identifier, tpe, l, r) =>
        emitln(s" %$identifier = ${fpaPrefix(tpe)}sub ${llvmType(tpe)} $l, $r")
      case Instruction.Mul(identifier, tpe, l, r) =>
        emitln(s" %$identifier = ${fpaPrefix(tpe)}mul ${llvmType(tpe)} $l, $r")
      case Instruction.Div(identifier, tpe, l, r) =>
        emitln(s" %$identifier = ${if (tpe == SymbolNode.Type.DOUBLE) "f" else "u"}div ${llvmType(tpe)} $l, $r")
      case Instruction.Minus(identifier, tpe, value) =>
        emitln(s" %$identifier = ${fpaPrefix(tpe)}sub ${llvmType(tpe)} ${SymbolNode.Type.default(tpe)}, $value")
      case Instruction.And(identifier, tpe, l, r) =>
        emitln(s" %$identifier = and ${llvmType(tpe)} $l, $r")
      case Instruction.Or(identifier, tpe, l, r) =>
        emitln(s" %$identifier = or ${llvmType(tpe)} $l, $r")
      case Instruction.Neg(identifier, tpe, value) =>
        emitln(s" %$identifier = icmp eq ${llvmType(tpe)} $value, ${SymbolNode.Type.default(tpe)}")
      case Instruction.Equal(identifier, tpe, l, r) =>
        emitln(s" %$identifier = ${cmpPrefix(tpe)}cmp ${if (tpe == SymbolNode.Type.DOUBLE) "o" else ""}eq ${llvmType(tpe)} $l, $r")
      case Instruction.NotEqual(identifier, tpe, l, r) =>
        emitln(s" %$identifier = ${cmpPrefix(tpe)}cmp ${if (tpe == SymbolNode.Type.DOUBLE) "o" else ""}ne ${llvmType(tpe)} $l, $r")
      case Instruction.Greater(identifier, tpe, l, r) =>
        emitln(s" %$identifier = ${cmpPrefix(tpe)}cmp ${if (tpe == SymbolNode.Type.DOUBLE) "o" else "s"}gt ${llvmType(tpe)} $l, $r")
      case Instruction.GreaterEqual(identifier, tpe, l, r) =>
        emitln(s" %$identifier = ${cmpPrefix(tpe)}cmp ${if (tpe == SymbolNode.Type.DOUBLE) "o" else "s"}ge ${llvmType(tpe)} $l, $r")
      case Instruction.Lower(identifier, tpe, l, r) =>
        emitln(s" %$identifier = ${cmpPrefix(tpe)}cmp ${if (tpe == SymbolNode.Type.DOUBLE) "o" else "s"}lt ${llvmType(tpe)} $l, $r")
      case Instruction.LowerEqual(identifier, tpe, l, r) =>
        emitln(s" %$identifier = ${cmpPrefix(tpe)}cmp ${if (tpe == SymbolNode.Type.DOUBLE) "o" else "s"}le ${llvmType(tpe)} $l, $r")
      case Instruction.Alloca(identifier, tpe) =>
        emitln(s" %$identifier = alloca ${llvmType(tpe)}")
      case Instruction.Load(identifier, tpe, source) =>
        val t = llvmType(tpe)
        emitln(s" %$identifier = load $t, $t* $source")
      case Instruction.Store(tpe, value, dst) =>
        val t = llvmType(tpe)
        emitln(s" store $t $value, $t* $dst")
      case Instruction.Cast(identifier, from, value, to) =>
        emitln(s" %$identifier = sitofp ${llvmType(from)} $value to ${llvmType(to)}")
      case Instruction.ExtBool(identifier, value) =>
        emitln(s" %$identifier = zext i1 $value to i32")
      case Instruction.Println(tpe, value) =>
        val formatter = if (tpe == SymbolNode.Type.DOUBLE) "float_format" else "digit_format"
        emitln(s" call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @$formatter, i32 0, i32 0), ${llvmType(tpe)} %$value)")
      case Instruction.PrintlnVoid() =>
        emitln(s" call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @void_format, i32 0, i32 0))")
      case Instruction.Call(identifier, tpe, method) =>
        emitln(s" ${if (tpe != SymbolNode.Type.VOID) s"%$identifier = " else ""}call ${llvmType(tpe)} () $method()")
      case Instruction.Fallthrough(identifier) =>
        emitln(s" %$identifier = alloca i1")
      case Instruction.GetFallthrough(identifier, source) =>
        emitln(s" %$identifier = load i1, i1* $source")
      case Instruction.SetFallthrough(dst) =>
        emitln(s" store i1 true, i1* $dst")
      case Instruction.UnsetFallthrough(dst) =>
        emitln(s" store i1 false, i1* $dst")
      case Instruction.EnsureFallthrough(identifier, l, r) =>
        emitln(s" %$identifier = or i1 $l, $r")
    }
  }

  /**
   * Порождение кода для завершающей инструкции (терминатора) базового блока.
   * @param t терминатор блока
   */
  private def emitTerminator(t: Terminator): Unit = {
    if (t == null) return

    t match {
      case Terminator.Return(tpe, value) =>
        emitln(s" ret ${llvmType(tpe)} $value")
      case Terminator.Goto(dst) =>
        emitln(s" br label %$dst")
      case Terminator.If(cond, trueBranch, falseBranch) =>
        emitln(s" br i1 $cond, label %$trueBranch, label %$falseBranch")
    }
  }

  /**
   * Переводит системный тип в тип LLVM.
   */
  private def llvmType(tpe: SymbolNode.Type.Value): String = tpe match {
    case symbol_table.SymbolNode.Type.VOID => "void"
    case symbol_table.SymbolNode.Type.INT => "i32"
    case symbol_table.SymbolNode.Type.SHORT => "i32"
    case symbol_table.SymbolNode.Type.LONG => "i32"
    case symbol_table.SymbolNode.Type.DOUBLE => "double"
  }

  /**
   * Опциональный префикс для инструкций арифметических операций с плавающей точкой.
   */
  private def fpaPrefix(tpe: SymbolNode.Type.Value): String = if (tpe == SymbolNode.Type.DOUBLE) "f" else ""

  /**
   * Префикс инструкций операций сравнения.
   */
  private def cmpPrefix(tpe: SymbolNode.Type.Value): String = if (tpe == SymbolNode.Type.DOUBLE) "f" else "i"
}
