package printer

import java.io.{FileOutputStream, PrintStream}

import parser.Parser
import symbol_table.SymbolNode

trait TreePrinter { _: Parser =>
  private var step: Int = 1
  private val isEnabled: Boolean = false

  def printTable(title: String): Unit = if (isEnabled) {
    val prevOut = System.out

    val out = new PrintStream(new FileOutputStream(s"step_$step.gv"))
    System.setOut(out)

    SymbolNode.dotPrint(symbolTable.root, title)

    out.close()
    System.setOut(prevOut)

    step += 1
  }
}
