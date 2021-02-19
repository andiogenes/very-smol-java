package cmd

import java.nio.charset.Charset
import java.nio.file.{Files, Paths}

import compiler.ParserCompiler

object App extends App {
  val options = Cli.parse(args)

  val sourcePath = options(Cli.Source).toString
  val source = new String(Files.readAllBytes(Paths.get(sourcePath)), Charset.defaultCharset)

  val destinationPath = {
    val path = options(Cli.Destination).toString
    if (path.nonEmpty) path else sourcePath.split('.').head
  }

  val compile = options(Cli.Compile).asInstanceOf[Boolean]
  val interpret = !compile && options(Cli.Interpret).asInstanceOf[Boolean]

  val compiler = new ParserCompiler(source, destinationPath, interpret = interpret, compile = compile)
  compiler.run()
}
