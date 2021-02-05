package evaluation

import semantic.Expr

/**
 * Контекст вычисления.
 */
trait EvaluationContext {
  /**
   * Состояние вычисления.
   */
  case class State(isInterpreting: Boolean, switchCondition: Option[Expr], isBreakExecuted: Boolean, returnValue: Option[Expr])

  private val context = new collection.mutable.Stack[State]()

  /**
   * Флаг интерпретации.
   */
  var isInterpreting = true

  /**
   * Условие switch
   */
  var switchCondition: Option[Expr] = None

  /**
   * Флаг вызова break.
   */
  var isBreakExecuted = false

  /**
   * Возвращаемое значение функции.
   */
  var returnValue: Option[Expr] = None

  def saveContext(): Unit = {
    context.push(State(isInterpreting, switchCondition, isBreakExecuted, returnValue))
  }

  def restoreContext(): Unit = {
    val ctx = context.pop()

    isInterpreting = ctx.isInterpreting
    switchCondition = ctx.switchCondition
    isBreakExecuted = ctx.isBreakExecuted
    returnValue = ctx.returnValue
  }

  def peekContext(): State = context.top
}
