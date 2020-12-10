package parser

import tokens.{Token, TokenType}

import scala.collection.BufferedIterator

class Parser(private val tokens: BufferedIterator[Token]) {
  class ParseError extends RuntimeException

  def parse(): Option[ParseError] = {
    program()
  }

  private def program(): Option[ParseError] = {
    lazy val klass = matchOrError(TokenType.CLASS)
    lazy val mainClassName = matchOrError() { case Token(TokenType.IDENTIFIER, "Main") => true }
    lazy val leftBrace = matchOrError(TokenType.LEFT_BRACE)

    lazy val classMembers1: Option[ParseError] = classMembers()

    lazy val void = matchOrError(TokenType.VOID)
    lazy val mainMethodName = matchOrError() { case Token(TokenType.IDENTIFIER, "main") => true }

    lazy val leftParen = matchOrError(TokenType.LEFT_PAREN)
    lazy val rightParen = matchOrError(TokenType.RIGHT_PAREN)

    lazy val mainBlock: Option[ParseError] = block()

    lazy val classMembers2: Option[ParseError] = classMembers()
    lazy val rightBrace = matchOrError(TokenType.RIGHT_BRACE)

    klass.orElse(mainClassName)
      .orElse(leftBrace)
      .orElse(classMembers1.toRight())
      .orElse(void)
      .orElse(mainMethodName)
      .orElse(leftParen)
      .orElse(rightParen)
      .orElse(mainBlock.toRight())
      .orElse(classMembers2.toRight())
      .orElse(rightBrace).toOption
  }

  private def classDeclaration(): Option[ParseError] = {
    lazy val klass = matchOrError(TokenType.CLASS)
    lazy val className = matchOrError(TokenType.IDENTIFIER)
    lazy val body: Option[ParseError] = classBody()

    klass.orElse(className).orElse(body.toRight()).toOption
  }

  private def classBody(): Option[ParseError] = {
    lazy val leftBrace = matchOrError(TokenType.LEFT_BRACE)
    lazy val members: Option[ParseError] = classMembers()
    lazy val rightBrace = matchOrError(TokenType.RIGHT_BRACE)

    leftBrace.orElse(members.toRight()).orElse(rightBrace).toOption
  }

  private def classMembers(): Option[ParseError] = {
    // TODO: написать
  }

  private def classMember(): Option[ParseError] = {
    // TODO: написать
  }

  private def field(): Option[ParseError] = {
    lazy val dataType = matchOrError() { case t =>
      t.tpe match {
        case TokenType.INT | TokenType.SHORT | TokenType.LONG | TokenType.DOUBLE => true
        case _ => false
      }
    }
    lazy val defOrDecl: Option[ParseError] = dataDefinitionOrDeclaration()
    lazy val semicolon = matchOrError(TokenType.SEMICOLON)

    dataType.orElse(defOrDecl.toRight()).orElse(semicolon).toOption
  }

  private def method(): Option[ParseError] = {
    lazy val returnType = matchOrError() { case t =>
      t.tpe match {
        case TokenType.INT | TokenType.SHORT | TokenType.LONG | TokenType.DOUBLE => true
        case _ => false
      }
    }
    lazy val methodName = matchOrError(TokenType.IDENTIFIER)
    lazy val leftParen = matchOrError(TokenType.LEFT_PAREN)
    lazy val rightParen = matchOrError(TokenType.RIGHT_PAREN)
    lazy val methodBlock: Option[ParseError] = block()

    returnType.orElse(methodName).orElse(leftParen).orElse(rightParen).orElse(methodBlock.toRight()).toOption
  }

  private def dataDefinitionOrDeclaration(): Option[ParseError] = {
    lazy val identifier = matchOrError(TokenType.IDENTIFIER)
    lazy val equals = matchOrError(TokenType.EQUAL)
    lazy val expr: Option[ParseError] = expression()

    val declaration = identifier
    (if (equals.isRight) declaration else declaration.orElse(equals).orElse(expr.toRight())).toOption
  }

  private def statementsAndDefinitions(): Option[ParseError] = {
    // TODO: написать
  }

  private def statement(): Option[ParseError] = {
    // TODO: написать
  }

  private def simpleStatement(): Option[ParseError] = {
    // TODO: написать
  }

  private def definition(): Option[ParseError] = {
    lazy val dataType = matchOrError() { case t =>
      t.tpe match {
        case TokenType.INT | TokenType.SHORT | TokenType.LONG | TokenType.DOUBLE => true
        case _ => false
      }
    }
    lazy val defOrDecl = dataDefinitionOrDeclaration()
    lazy val semicolon = matchOrError(TokenType.SEMICOLON)

    dataType.orElse(defOrDecl.toRight()).orElse(semicolon).toOption
  }

  private def assignment(): Option[ParseError] = {
    lazy val dest: Option[ParseError] = assignmentDestination()
    lazy val equals = matchOrError(TokenType.EQUAL)
    lazy val expr: Option[ParseError] = expression()
    lazy val semicolon = matchOrError(TokenType.SEMICOLON)

    dest.toRight().orElse(equals).orElse(expr.toRight()).orElse(semicolon).toOption
  }

  private def assignmentDestination(): Option[ParseError] = {
    // TODO: написать
  }

  private def switch(): Option[ParseError] = {
    lazy val switch = matchOrError(TokenType.SWITCH)

    lazy val leftParen = matchOrError(TokenType.LEFT_PAREN)
    lazy val expr: Option[ParseError] = expression()
    lazy val rightParen = matchOrError(TokenType.RIGHT_PAREN)

    lazy val leftBrace = matchOrError(TokenType.LEFT_BRACE)
    lazy val branches: Option[ParseError] = switchBranches()
    lazy val rightBrace = matchOrError(TokenType.RIGHT_BRACE)

    switch.orElse(leftParen)
      .orElse(expr.toRight()).orElse(rightParen)
      .orElse(leftBrace).orElse(branches.toRight()).orElse(rightBrace).toOption
  }

  private def switchBranches(): Option[ParseError] = {
    // TODO: написать
  }

  private def switchBranch(): Option[ParseError] = {
    lazy val label: Option[ParseError] = switchLabel()
    lazy val body = statementsAndDefinitions()

    label.orElse(body)
  }

  private def switchLabel(): Option[ParseError] = {
    // TODO: написать
  }

  private def expression(): Option[ParseError] = {
    // TODO: написать
  }

  private def or(): Option[ParseError] = {
    // TODO: написать
  }

  private def and(): Option[ParseError] = {
    // TODO: написать
  }

  private def comparison(): Option[ParseError] = {
    // TODO: написать
  }

  private def add(): Option[ParseError] = {
    // TODO: написать
  }

  private def mul(): Option[ParseError] = {
    // TODO: написать
  }

  private def unary(): Option[ParseError] = {
    // TODO: написать
  }

  private def elementary(): Option[ParseError] = {
    // TODO: написать
  }

  private def parenthesizedExpression(): Option[ParseError] = {
    lazy val leftParen = matchOrError(TokenType.LEFT_PAREN)
    lazy val expr = expression()
    lazy val rightParen = matchOrError(TokenType.RIGHT_PAREN)

    leftParen.orElse(expr.toRight()).orElse(rightParen).toOption
  }

  private def objekt(): Option[ParseError] = {
    lazy val identifier = matchOrError(TokenType.IDENTIFIER)
    lazy val leftParen = matchOrError(TokenType.LEFT_PAREN)
    lazy val rightParen = matchOrError(TokenType.RIGHT_PAREN)

    val directAccess = identifier
    (if (leftParen.isRight) directAccess else directAccess.orElse(leftParen).orElse(rightParen)).toOption
  }

  private def fieldAccess(): Option[ParseError] = {
    lazy val obj = objekt()
    lazy val dot = matchOrError(TokenType.DOT)
    lazy val objAccess: Option[ParseError] = objectFieldAccess()

    obj.toRight().orElse(dot).orElse(objAccess.toRight()).toOption
  }

  private def objectFieldAccess(): Option[ParseError] = {
    // TODO: написать
  }

  private def methodCall(): Option[ParseError] = {
    lazy val objAccess: Option[ParseError] = objectAccess()
    // inline "Вызов метода объекта"
    lazy val identifier = matchOrError(TokenType.IDENTIFIER)
    lazy val leftParen = matchOrError(TokenType.LEFT_PAREN)
    lazy val rightParen = matchOrError(TokenType.RIGHT_PAREN)

    objAccess.toRight().orElse(identifier).orElse(leftParen).orElse(rightParen).toOption
  }

  private def objectAccess(): Option[ParseError] = {
    // TODO: написать
  }

  def matchOrError()(pf: PartialFunction[Token, Boolean]): Either[Token, ParseError] = {
    tokens.headOption.toLeft(new ParseError()) match {
      case Left(token) => if (pf(token)) Left(tokens.next()) else Right(new ParseError())
      case error => error
    }
  }

  def matchOrError(tpe: TokenType.Value*): Either[Token, ParseError] = matchOrError() { t => tpe.contains(t.tpe) }

}
