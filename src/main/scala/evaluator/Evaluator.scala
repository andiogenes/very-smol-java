package evaluator

import semantic.Expr
import tokens.TokenType

import scala.PartialFunction.condOpt

/**
 * Вычислитель выражений.
 */
trait Evaluator {
  /**
   * Вычисляет унарное выражение.
   *
   * @param prim Выражение, над которым выполняется операция
   * @param pref Префиксная операция
   * @param suf  Суффиксная операция
   */
  def evalUnary(prim: Option[Expr], pref: Option[TokenType.Value], suf: Option[TokenType.Value]): Option[Expr] = {
    prim.flatMap {
      case Expr.Value(tpe, value) =>
        condOpt(pref) {
          case Some(TokenType.PLUS) => Expr.Value(tpe, value)
          case Some(TokenType.MINUS) => Expr.Value(tpe, minus(value))
          case Some(TokenType.BANG) => Expr.Value(tpe, neg(value))
        }
      case Expr.Reference(_, tpe, ref) =>
        val p = condOpt(pref) {
          case Some(TokenType.PLUS) => Expr.Value(tpe, ref.value)
          case Some(TokenType.MINUS) => Expr.Value(tpe, minus(ref.value))
          case Some(TokenType.PLUS_PLUS) =>
            ref.value = inc(ref.value)
            Expr.Value(tpe, ref.value)
          case Some(TokenType.MINUS_MINUS) =>
            ref.value = dec(ref.value)
            Expr.Value(tpe, ref.value)
          case Some(TokenType.BANG) =>
            Expr.Value(tpe, neg(ref.value))
        }
        val s = condOpt(suf) {
          case Some(TokenType.PLUS_PLUS) =>
            val v = ref.value
            ref.value = inc(ref.value)
            Expr.Value(tpe, v)
          case Some(TokenType.MINUS_MINUS) =>
            val v = ref.value
            ref.value = dec(ref.value)
            Expr.Value(tpe, v)
        }
        p orElse s
    } orElse prim
  }

  /**
   * Вычисляет бинарное выражение.
   *
   * @param op Тип операции
   * @param l  Левый операнд
   * @param r  Правый операнд
   */
  def evalBinary(op: TokenType.Value, l: Any, r: Any): Any = op match {
    case TokenType.PLUS => add(l, r)
    case TokenType.MINUS => sub(l, r)
    case TokenType.STAR => mul(l, r)
    case TokenType.SLASH => div(l, r)
    case TokenType.LESS => if (less(l, r)) 1 else 0
    case TokenType.LESS_EQUAL => if (less(l, r) || l == r) 1 else 0
    case TokenType.GREATER => if (less(r, l)) 1 else 0
    case TokenType.GREATER_EQUAL => if (less(r, l) || l == r) 1 else 0
    case TokenType.EQUAL_EQUAL => if (l == r) 1 else 0
    case TokenType.BANG_EQUAL => if (l != r) 1 else 0
    case TokenType.AND => if (l != 0 && r != 0) 1 else 0
    case TokenType.OR => if (l != 0 || r != 0) 1 else 0
  }

  private def less(l: Any, r: Any): Boolean = {
    asInt(l).flatMap(l => asInt(r).map(l < _).orElse(asDouble(r).map(l < _)))
      .orElse(asDouble(l).flatMap(l => asInt(r).map(l < _).orElse(asDouble(r).map(l < _)))).get
  }

  private def add(l: Any, r: Any): Any = {
    asInt(l).flatMap(l => asInt(r).map(l + _).orElse(asDouble(r).map(l + _)))
      .orElse(asDouble(l).flatMap(l => asInt(r).map(l + _).orElse(asDouble(r).map(l + _)))).get
  }

  private def sub(l: Any, r: Any): Any = {
    asInt(l).flatMap(l => asInt(r).map(l - _).orElse(asDouble(r).map(l - _)))
      .orElse(asDouble(l).flatMap(l => asInt(r).map(l - _).orElse(asDouble(r).map(l - _)))).get
  }

  private def mul(l: Any, r: Any): Any = {
    asInt(l).flatMap(l => asInt(r).map(l * _).orElse(asDouble(r).map(l * _)))
      .orElse(asDouble(l).flatMap(l => asInt(r).map(l * _).orElse(asDouble(r).map(l * _)))).get
  }

  private def div(l: Any, r: Any): Any = {
    asInt(l).flatMap(l => asInt(r).map(l / _).orElse(asDouble(r).map(l / _)))
      .orElse(asDouble(l).flatMap(l => asInt(r).map(l / _).orElse(asDouble(r).map(l / _)))).get
  }

  private def minus(v: Any): Any = {
    asInt(v).map(-_).orElse(asDouble(v).map(-_)).get
  }

  private def neg(v: Any): Any = {
    if (v == 0) 1 else 0
  }

  private def inc(v: Any): Any = {
    asInt(v).map(_ + 1).orElse(asDouble(v).map(_ + 1)).get
  }

  private def dec(v: Any): Any = {
    asInt(v).map(_ + 1).orElse(asDouble(v).map(_ - 1)).get
  }

  private def asInt(v: Any): Option[Int] = v match {
    case _: Int | _: Short | _: Long => Some(v.asInstanceOf[Int])
    case _ => None
  }

  private def asDouble(v: Any): Option[Double] = v match {
    case x: Double => Some(x)
    case _ => None
  }
}
