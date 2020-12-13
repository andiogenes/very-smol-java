package fun_test

import java.io.{File, FileOutputStream, PrintStream}

import cmd.App

/**
 * Лаунчер функциональных тестов.
 */
object Launcher extends App {
  /**
   * Получает список файлов в папке 'dir'.
   *
   * Взято с [[https://alvinalexander.com/scala/how-to-list-files-in-directory-filter-names-scala/]]
   */
  def getListOfFiles(dir: String): List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }

  // Сохраняем предыдущие stderr, stdout
  val err = System.err
  val out = System.out

  // Для всех файлов в текущей папке перенаправляем stdout и stdin и запускаем главную программу
  for (f <- getListOfFiles(".") if f.getName.endsWith(".java")) {
    val name = f.getName

    val testErr = new PrintStream(new FileOutputStream(s"$name.err"))
    val testOut = new PrintStream(new FileOutputStream(s"$name.out"))

    System.setErr(testErr)
    System.setOut(testErr)

    App.main(Array[String]("--source", name))

    testErr.close()
    testOut.close()
  }

  // Восстанавливаем stderr, stdout
  System.setErr(err)
  System.setOut(out)
}
