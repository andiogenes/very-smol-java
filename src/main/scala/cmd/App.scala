package cmd

import java.nio.charset.Charset
import java.nio.file.{Files, Paths}

import symbol_table.SymbolNode
import parser.Parser
import scanner.Scanner

object App extends App {
  val options = Cli.parse(args)
  val sourcePath = options(Cli.Source).toString

  val source = new String(Files.readAllBytes(Paths.get(sourcePath)), Charset.defaultCharset)
  val scanner = new Scanner(source)
  val parser = new Parser(scanner.buffered)

  parser.parse()
}
