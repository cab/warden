package warden.compiler

import org.parboiled2.{Parser => PParser, _}
import warden.compiler.Ast.{Identifier, Expression}


import scala.annotation.switch
import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}


object Parser {

  def apply(input: ParserInput): Either[Throwable, Ast.Program] = {
    val parser = new Parser(input)
    parser.Program.run() match {
      case Success(v) => Right[Throwable, Ast.Program](v)
      case Failure(e: ParseError) => Left[Throwable, Ast.Program](new Error(parser.formatError(e, new ErrorFormatter(showTraces = true)), e))
      case Failure(e) => Left[Throwable, Ast.Program](e)
    }
  }

  val WhiteSpaceChar = CharPredicate(" \n\r\t\f")
  val QuoteBackslash = CharPredicate("\"\\")
  val QuoteSlashBackSlash = QuoteBackslash ++ "/"

}

class Parser(val input: ParserInput) extends PParser with StringBuilding {

  import Parser._
  import CharPredicate.{Digit, Digit19, HexDigit}

  implicit def wspStr(s: String): Rule0 = rule {
    str(s) ~ zeroOrMore(' ')
  }

  def Program: Rule1[Ast.Program] = rule {
    WhiteSpace ~ zeroOrMore(WhiteSpace ~ Declaration) ~ zeroOrMore(WhiteSpace ~ Expression) ~ EOI ~> ((declarations: Seq[Ast.Declaration], exprs: Seq[Ast.Expression]) => Ast.Program(declarations, exprs))
  }

  def Declaration = rule {
    Var ~ ':' ~ WhiteSpace ~ Constant ~ oneOrMore('\n') ~> ((v: Ast.Var, c: Ast.Constant) => Ast.Declaration(v, c))
  }

  def Constant: Rule1[Ast.Constant] = rule {
    ConstantArray | Str
  }

  def ConstantArray = rule {
    '[' ~ zeroOrMore(Constant).separatedBy(",") ~ ']' ~> ((items: Seq[Ast.Constant]) => Ast.ConstantArray(items))
  }

  def Expression: Rule1[Ast.Expression] = rule {
    AndExpression
  }

  def AndExpression: Rule1[Ast.Expression] = rule {
    OrExpression ~ optional("AND" ~ AndExpression ~> ((left: Ast.Expression, right: Ast.Expression) => Ast.AndExpression(left, right)))
  }

  def OrExpression: Rule1[Ast.Expression] = rule {
    OpExpression ~ optional(WhiteSpace) ~ optional("OR" ~ OrExpression ~> ((left: Ast.Expression, right: Ast.Expression) => Ast.OrExpression(left, right)))
  }

  def OpExpression: Rule1[Expression] = rule {
    Atom | ('(' ~ (InExpression | CompareExpression | CallExpression) ~ ')')
  }

  def Op: Rule1[Ast.Op] = rule {
    (
      capture('>') ~> ((_: String) => Ast.Op.GreaterThan) |
        capture('<') ~> ((_: String) => Ast.Op.LessThan)
      )
  }

  def CompareExpression: Rule1[Ast.CompareExpression] = rule {
    OpExpression ~ WhiteSpace ~ Op ~ WhiteSpace ~ OpExpression ~> ((left: Ast.Expression, op: Ast.Op, right: Ast.Expression) => Ast.CompareExpression(left, right, op))
  }

  def InExpression: Rule1[Ast.InExpression] = rule {
    OpExpression ~ WhiteSpace ~ "in" ~ WhiteSpace ~ OpExpression ~> ((el: Ast.Expression, seq: Ast.Expression) => Ast.InExpression(el, seq))
  }


  def CallExpression: Rule1[Ast.CallExpression] = rule {
    Identifier ~ WhiteSpace ~ zeroOrMore(OpExpression).separatedBy(WhiteSpace) ~> ((id: Ast.Identifier, args: Seq[Ast.Expression]) => Ast.CallExpression(id, args))
  }

  def Atom: Rule1[Ast.Expression] = rule {
   Var | Property | Identifier | Float | Integer
  }

  def Property: Rule1[Ast.Property] = rule {
    ((Identifier ~ '/' ~ oneOrMore(Identifier).separatedBy('/')) ~> ((root: Identifier, path: Seq[Ast.Identifier]) => {
      Ast.Property(root +: path)
    }))
  }

//  def Call: Rule1[Ast.Call] = rule {
//    ('(' ~ Identifier ~ WhiteSpace  ~ zeroOrMore(Atom).separatedBy(WhiteSpace) ~ ')') ~> ((id: Ast.Identifier, args: Seq[Ast.Expression]) => Ast.Call(id, args))
//  }

  def Integer = rule {
    capture(CharPredicate.Digit19 ~ zeroOrMore(CharPredicate.Digit) | '0') ~> ((v: String) => Ast.Integer(java.lang.Integer.parseInt(v, 10)))
  }

  def Float: Rule1[Ast.Float] = rule {
    optional(capture((CharPredicate.Digit19 ~ zeroOrMore(CharPredicate.Digit)) | '0')) ~ '.' ~ capture(oneOrMore(CharPredicate.Digit)) ~> ((lead: Option[String], d: String) => {
      val str = lead.getOrElse("0") + "." + d
      val float = java.lang.Float.parseFloat(str)
      Ast.Float(float)
    })
  }

  def Identifier = rule {
    capture(CharPredicate.Alpha ~ zeroOrMore(CharPredicate.AlphaNum | '-')) ~> ((s: String) => Ast.Identifier(s))
  }

  def Var = rule {
    '$' ~ capture(CharPredicate.Alpha ~ zeroOrMore(CharPredicate.AlphaNum | '-')) ~> ((name: String) => Ast.Var(name))
  }

  def WhiteSpace = rule { zeroOrMore(WhiteSpaceChar) }


  def Str = rule { StrUnwrapped ~> (Ast.Str(_)) }

  def StrUnwrapped = rule { '"' ~ clearSB() ~ Characters ~ ws('"') ~ push(sb.toString) }

  def Characters = rule { zeroOrMore(NormalChar | '\\' ~ EscapedChar) }

  def NormalChar = rule { !QuoteBackslash ~ ANY ~ appendSB() }

  def EscapedChar = rule (
    QuoteSlashBackSlash ~ appendSB()
      | 'b' ~ appendSB('\b')
      | 'f' ~ appendSB('\f')
      | 'n' ~ appendSB('\n')
      | 'r' ~ appendSB('\r')
      | 't' ~ appendSB('\t')
      | Unicode ~> { code => sb.append(code.asInstanceOf[Char]); () }
  )

  def Unicode = rule { 'u' ~ capture(HexDigit ~ HexDigit ~ HexDigit ~ HexDigit) ~> (java.lang.Integer.parseInt(_, 16)) }


  def ws(c: Char) = rule { c ~ WhiteSpace }

}