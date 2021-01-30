package cmd

import interpreter.ParserInterpreter

import java.nio.charset.Charset
import java.nio.file.{Files, Paths}

object App extends App {
  val options = Cli.parse(args)

  val sourcePath = options(Cli.Source).toString
  val source = new String(Files.readAllBytes(Paths.get(sourcePath)), Charset.defaultCharset)

  val interpreter = new ParserInterpreter(source)
  interpreter.run()
}
