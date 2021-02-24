package cmd

import scala.annotation.tailrec

object Cli {
  val usage =
    """
Usage: app [--source filename] [--destination filename] [--interpret] [--compile] [--just-emit] [--interpret-llvm]
"""

  val Source: Symbol = Symbol("source")
  val Destination: Symbol = Symbol("destination")
  val JustEmit: Symbol = Symbol("JustEmit")
  val Interpret: Symbol = Symbol("Interpret")
  val InterpretLLVM: Symbol = Symbol("InterpretLLVM")
  val Compile: Symbol = Symbol("Compile")

  val defaultOptions = Map(
    Source -> "",
    Destination -> "",
    JustEmit -> false,
    Interpret -> false,
    InterpretLLVM -> false,
    Compile -> false
  )

  type OptionMap = Map[Symbol, Any]

  def parse(args: Array[String]): OptionMap = {
    @tailrec
    def nextOption(map: OptionMap, list: List[String]): OptionMap = {
      list match {
        case Nil => map
        case v :: value :: tail if v == "--source" || v == "-s" =>
          nextOption(map ++ Map(Symbol("source") -> value), tail)
        case v :: value :: tail if v == "--destination" || v == "-d" =>
          nextOption(map ++ Map(Symbol("destination") -> value), tail)
        case v :: tail if v == "--interpret" =>
          nextOption(map ++ Map(Symbol("Interpret") -> true), tail)
        case v :: tail if v == "--interpret-llvm" =>
          nextOption(map ++ Map(Symbol("InterpretLLVM") -> true), tail)
        case v :: tail if v == "--compile" =>
          nextOption(map ++ Map(Symbol("Compile") -> true), tail)
        case v :: tail if v == "--just-emit" =>
          nextOption(map ++ Map(Symbol("JustEmit") -> true), tail)
        case option :: _ =>
          System.err.println(s"Unknown option $option")
          map
      }
    }

    nextOption(defaultOptions, args.toList)
  }
}
