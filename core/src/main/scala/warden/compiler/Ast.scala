package warden.compiler

import scala.reflect.ClassTag


object Ast {

  sealed trait Node {
  }
  sealed trait Expression extends Node {
  }
  sealed trait Constant extends Expression


  final case class Declaration(variable: Var, value: Constant) extends Node
  final case class Program(declarations: Seq[Declaration], expressions: Seq[Expression])
  sealed trait Op extends Expression

  object Op {
    case object GreaterThan extends Op
    case object LessThan extends Op
  }


  final case class Identifier(name: String) extends Expression
  final case class Property(path: Seq[Identifier]) extends Expression

  final case class Var(name: String) extends Expression

  final case class AndExpression(left: Expression, right: Expression) extends Expression
  final case class OrExpression(left: Expression, right: Expression) extends Expression
  final case class InExpression(element: Expression, seq: Expression) extends Expression

  final case class CompareExpression(left: Expression, right: Expression, op: Op) extends Expression


  final case class CallExpression(name: Identifier, args: Seq[Expression]) extends Expression


  final case class Integer(value: Int) extends Expression
  final case class Float(value: scala.Float) extends Expression

  final case class ConstantArray(items: Seq[Constant]) extends Constant
  final case class Str(value: String) extends Constant

}
