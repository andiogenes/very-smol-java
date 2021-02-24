package llvm

import sys.process._

/**
 * Набор инструментов для работы с утилитами инфраструктуры LLVM.
 */
trait LLVMToolbox {
  /**
   * Запускает интерпретатор LLVM-биткода для выбранного файла.
   */
  def lli(fileName: String): Int = {
    s"lli $fileName".!
  }

  /**
   * Запускает компилятор LLVM-биткода для выбранного файла.
   */
  def llc(fileName: String, fileType: String = "obj"): Int = {
    s"llc -filetype=$fileType $fileName".!
  }

  /**
   * Собирает исполняемый файл компилятором Clang.
   */
  def clang(source: String, destination: String): Int = {
    s"clang $source -o $destination".!
  }
}
