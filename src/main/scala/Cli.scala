import scala.annotation.tailrec

object Cli {
  val usage =
    """
Usage: app [--source filename]
"""

  val Source: Symbol = Symbol("source")

  val defaultOptions = Map(
    Source -> ""
  )

  type OptionMap = Map[Symbol, Any]

  def parse(args: Array[String]): OptionMap = {
    @tailrec
    def nextOption(map: OptionMap, list: List[String]): OptionMap = {
      list match {
        case Nil => map
        case v :: value :: tail if v == "--source" || v == "-s" =>
          nextOption(map ++ Map(Symbol("source") -> value), tail)
        case option :: _ =>
          println(s"Unknown option $option")
          map
      }
    }

    nextOption(defaultOptions, args.toList)
  }
}