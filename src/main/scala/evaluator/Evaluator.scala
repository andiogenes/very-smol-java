package evaluator

import parser.Parser

trait Evaluator { this : Parser =>
  def run(): Unit
}
