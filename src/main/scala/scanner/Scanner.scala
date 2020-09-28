package scanner

import tokens.{Token, TokenType}

import scala.PartialFunction.condOpt
import scala.collection.immutable.HashMap

/**
 * Сканер исходного кода на языке Java.
 *
 * @param source Исходный код модуля
 */
class Scanner(private val source: String) extends Iterator[Token] {
  // Начало разбираемой лексемы
  private var start: Int = 0
  // Текущая позиция в тексте модуля
  private var current: Int = 0
  // Номер текущей строки
  private var line: Int = 1
  // Позиция конца предыдущей строки
  private var previousLineEnd = 0

  /**
   * Отображение лексем ключевых слов в тип лексемы.
   */
  private val keywords = HashMap(
    "class" -> TokenType.CLASS,
    "void" -> TokenType.VOID,
    "switch" -> TokenType.SWITCH,
    "case" -> TokenType.CASE,
    "default" -> TokenType.DEFAULT,
    "break" -> TokenType.BREAK,
    "int" -> TokenType.INT,
    "short" -> TokenType.SHORT,
    "long" -> TokenType.LONG,
    "double" -> TokenType.DOUBLE,
    "return" -> TokenType.RETURN,
  )

  /**
   * Возвращает следующую лексему модуля.
   *
   * Если произошла ошибка, возвращает лексему типа [TokenType.ERROR].
   * Если все лексемы прочитаны, возвращает лексему типа [TokenType.EOF].
   */
  def nextToken(): Token = {
    // Пропуск незначащих символов и комментариев. Если блочный комментарий не закрыт - возвращаем ошибку.
    ignoreWhitespaceAndComments().map(return _)

    // Установка указателя на начало текущей лексемы
    start = current
    current += 1

    // Если все лексемы прочитаны, возвращает лексему типа [TokenType.EOF].
    if (current > source.length) return Token(TokenType.EOF, "", "", line, start - previousLineEnd - 1)

    // Разбор лексем
    source(start) match {
      // Лексемы из одного символа можно сразу возвращать
      case ',' => emitToken(TokenType.COMMA)
      case ':' => emitToken(TokenType.COLON)
      case ';' => emitToken(TokenType.SEMICOLON)
      case '(' => emitToken(TokenType.LEFT_PAREN)
      case ')' => emitToken(TokenType.RIGHT_PAREN)
      case '{' => emitToken(TokenType.LEFT_BRACE)
      case '}' => emitToken(TokenType.RIGHT_BRACE)
      case '*' => emitToken(TokenType.STAR)
      case '%' => emitToken(TokenType.PERCENT)

      // Комментарии проверили ранее, поэтому возвращаем лексему "Косая черта".
      case '/' => emitToken(TokenType.SLASH)

      // Лексемы || и && разбираем символ за символом. Если второй символ не совпал - возвращаем ошибку.
      case '|' => accept('|', TokenType.OR) getOrElse emitToken(TokenType.ERROR, "unexpected")
      case '&' => accept('&', TokenType.AND) getOrElse emitToken(TokenType.ERROR, "unexpected")

      // Лексемы с '=' вторым символом. Если кроме первого символа удалось разобрать и '=', возвращаем
      // комбинированную лексему, иначе возвращаем лексему, соответствующую первому символу.
      case '!' => accept('=', TokenType.BANG_EQUAL) getOrElse emitToken(TokenType.BANG)
      case '=' => accept('=', TokenType.EQUAL_EQUAL) getOrElse emitToken(TokenType.EQUAL)
      case '>' => accept('=', TokenType.GREATER_EQUAL) getOrElse emitToken(TokenType.GREATER)
      case '<' => accept('=', TokenType.LESS_EQUAL) getOrElse emitToken(TokenType.LESS)

      // Лексемы +, ++, -, -- разбираются аналогично предыдущей группе лексем.
      case '+' => accept('+', TokenType.PLUS_PLUS) getOrElse emitToken(TokenType.PLUS)
      case '-' => accept('-', TokenType.MINUS_MINUS) getOrElse emitToken(TokenType.MINUS)

      // Если после точки стоит число, пробуем разобрать число начиная с точки.
      // Иначе возвращаем лексему типа "Точка".
      case '.' => source(current) match {
        case v if '0' <= v && v <= '9' => number()
        case _ => emitToken(TokenType.DOT)
      }
      // Если встретили числовой символ - пробуем разобрать число.
      case v if '0' <= v && v <= '9' => number()

      // Если встретили букву или нижнее подчеркивание, пробуем разобрать идентификатор (или ключевое слово).
      case v if v == '_' || ('a' <= v && v <= 'z') || ('A' <= v && v <= 'Z') => identifier()

      case _ => emitToken(TokenType.ERROR, "unexpected")
    }
  }

  /**
   * Разбирает идентификатор.
   *
   * @return Если лексема есть в списке ключевых слов, лексему с типом ключевого слова,
   *         иначе - лексему с типом идентификатора.
   */
  private def identifier(): Token = {
    while (current < source.length &&
      (source(current) == '_' ||
        ('a' <= source(current) && source(current) <= 'z') ||
        ('A' <= source(current) && source(current) <= 'Z') ||
        ('0' <= source(current) && source(current) <= '9'))) current += 1

    val lexeme = source.substring(start, current)

    keywords.get(lexeme)
      .map(emitToken(_))
      .getOrElse(emitToken(TokenType.IDENTIFIER, lexeme))
  }

  /**
   * Разбирает число.
   *
   * @return Если у числа нет экспоненциальной части, возвращает лексему типа "Целое число",
   *         иначе, если эксопненциальная часть полностью описана - возвращает лексему "Число в экспоненциальной
   *         форме", иначе возвращает ошибку.
   */
  private def number(): Token = {
    // Разбираем числа диапазона.
    while (current < source.length && '0' <= source(current) && source(current) <= '9') current += 1
    if (current >= source.length) return emitToken(TokenType.NUMBER_INT)

    // Если точки после чисел нет - возвращаем целое число.
    // Иначе - продолжаем разбор
    source(current) match {
      case '.' => current += 1
      case _ => return emitToken(TokenType.NUMBER_INT)
    }

    // Разбираем порядок экспоненты.
    while (current < source.length && '0' <= source(current) && source(current) <= '9') current += 1
    if (current >= source.length) return emitToken(TokenType.ERROR, "unfinished exponential number literal")

    // Если есть E после порядка - продолжаем разбор. Иначе - ошибка.
    source(current) match {
      case 'E' => current += 1
      case _ => return emitToken(TokenType.ERROR, "unfinished exponential number literal")
    }

    if (current >= source.length) return emitToken(TokenType.ERROR, "Unfinished exponential number literal")

    // Если есть знак или начало числа степени - продолжаем разбор.
    // Иначе - ошибка.
    source(current) match {
      case '+' | '-' =>
        current += 1
        if (current < source.length && ('0' > source(current) || source(current) > '9')) return emitToken(TokenType.ERROR)
      case v if '0' <= v && v <= '9' =>
      case _ => return emitToken(TokenType.ERROR, "unfinished exponential number literal")
    }

    // Разбираем числа степени.
    while (current < source.length && '0' <= source(current) && source(current) <= '9') current += 1

    emitToken(TokenType.NUMBER_EXP, source.substring(start, current))
  }

  /**
   * Если в позиции [current] стоит символ [char], принимает лексему с типом [tpe].
   *
   * Иначе, возвращает [None].
   */
  private def accept(char: Char, tpe: TokenType.Value) = if (current < source.length) condOpt(source(current)) {
    case v if v == char =>
      current += 1
      emitToken(tpe)
  } else None

  /**
   * Пропускает незначащие символы и комментарии.
   *
   * Если комментарий не закрыт, возвращает ошибку.
   */
  private def ignoreWhitespaceAndComments(): Option[Token] = {
    var error: Option[Token] = None

    while (current < source.length && (source(current) match {
      // пробелы, символы табуляции
      case ' ' | '\t' =>
        current += 1
        true

      // переносы строк
      case '\n' =>
        line += 1
        current += 1
        previousLineEnd = current
        true

      // комментарии
      case '/' => current + 1 < source.length && (source(current + 1) match {
        // Однострочный комментарий
        case '/' =>
          current += 1
          // Пропускаем символы до переноса строки
          while (current < source.length && source(current) != '\n') current += 1
          true

        // Многострочный комментарий
        case '*' =>
          start = current
          current += 1
          error = error orElse blockComment()
          true

        case _ => false
      })

      case _ => false
    })) {}

    error
  }

  /**
   * Пропускает многострочный комментарий.
   *
   * Если комментарий не закрыт, возвращает ошибку.
   */
  private def blockComment(): Option[Token] = {
    // На начало разбора есть один комментарий, который надо закрыть.
    var isEnclosed = false

    while (!isEnclosed && current < source.length) {
      source(current) match {
        // */ -> закрываем комментарий
        case '*' =>
          if (current + 1 < source.length && source(current + 1) == '/') {
            isEnclosed = true
            current += 1
          }

        // Учитываем переносы строк
        case '\n' =>
          line += 1
          previousLineEnd = current + 1

        case _ =>
      }

      current += 1
    }

    // Если дошли до конца модуля и не "закрыли" комментарий - возвращаем ошибку
    Option.when(!isEnclosed && current >= source.length)(emitToken(TokenType.ERROR, "unclosed comment"))
  }

  /**
   * Создаёт лексему типа [tpe], со значением [extra] с началом в некоторой позиции на некоторой строке.
   */
  private def emitToken(tpe: TokenType.Value, extra: String = ""): Token = {
    val text = source.substring(start, current)
    Token(tpe, text, extra, line, start - previousLineEnd)
  }

  override def hasNext: Boolean = current <= source.length

  override def next(): Token = nextToken()
}

/**
 * Объект-команьон класса [Scanner].
 */
object Scanner {
  /**
   * Печатает сообщение об ошибке, если тип [token] - [TokenType.ERROR].
   */
  def printError(token: Token): Unit = token match {
    case Token(TokenType.ERROR, lexeme, extra, line, pos) =>
      val dq = '"'
      System.err.println(s"Error: $extra $dq$lexeme$dq at $line:${pos + 1}")
    case _ =>
  }
}