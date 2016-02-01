package warden.compiler

import warden.compiler.TypeChecker.Type

object IR {

  sealed trait TypedNode {
    def `type`: Type
  }
  sealed trait TypedConstant extends TypedExpression

  sealed trait TypedExpression extends TypedNode {

  }

  final case class TypedDeclaration(variable: TypedVar, value: TypedConstant) extends TypedNode {
    override def `type`: Type = value.`type`
  }
  final case class TypedProgram(declarations: Seq[TypedDeclaration], expressions: Seq[TypedExpression])
  sealed trait TypedOp extends TypedExpression

  object TypedOp {
    case object TypedGreaterThan extends TypedOp {
      override def `type`: Type = TypeChecker.BoolType
    }
    case object TypedLessThan extends TypedOp {
      override def `type`: Type = TypeChecker.BoolType
    }
  }


  final case class TypedIdentifier(name: String, `type`: Type) extends TypedExpression
  final case class TypedProperty(path: Seq[TypedIdentifier], `type`: Type) extends TypedExpression

  final case class TypedVar(name: String, `type`: Type) extends TypedExpression

  final case class TypedAndExpression(left: TypedExpression, right: TypedExpression) extends TypedExpression {
    override def `type`: Type = TypeChecker.BoolType
  }
  final case class TypedOrExpression(left: TypedExpression, right: TypedExpression) extends TypedExpression {
    override def `type`: Type = TypeChecker.BoolType
  }
  final case class TypedInExpression(element: TypedExpression, seq: TypedExpression) extends TypedExpression {
    override def `type`: Type = TypeChecker.BoolType
  }

  final case class TypedCompareExpression(left: TypedExpression, right: TypedExpression, op: TypedOp) extends TypedExpression {
    override def `type`: Type = op.`type`
  }

  final case class TypedCallExpression(name: TypedIdentifier, args: Seq[TypedExpression], `type`: Type) extends TypedExpression


  final case class TypedInteger(value: Int) extends TypedExpression {
    override def `type`: Type = TypeChecker.IntType
  }
  final case class TypedFloat(value: scala.Float) extends TypedExpression {
    override def `type`: Type = TypeChecker.FloatType
  }

  final case class TypedConstantArray(items: Seq[TypedConstant]) extends TypedConstant {
    override def `type`: Type = items.headOption.map(_.`type`).getOrElse(TypeChecker.BoolType) // TODO Empty arrays aren't allowed, but
  }
  final case class TypedStr(value: String) extends TypedConstant {
    override def `type`: Type = TypeChecker.StringType
  }


  sealed trait AllocatedNode extends TypedNode {
    def `type`: Type
  }
  sealed trait AllocatedConstant extends AllocatedExpression

  sealed trait AllocatedExpression extends AllocatedNode {

  }

  final case class AllocatedDeclaration(variable: AllocatedVar, value: AllocatedConstant) extends AllocatedNode {
    override def `type`: Type = value.`type`
  }
  final case class AllocatedProgram(declarations: Seq[AllocatedDeclaration], expressions: Seq[AllocatedExpression])
  sealed trait AllocatedOp extends AllocatedExpression

  object AllocatedOp {
    case object AllocatedGreaterThan extends AllocatedOp {
      override def `type`: Type = TypeChecker.BoolType
    }
    case object AllocatedLessThan extends AllocatedOp {
      override def `type`: Type = TypeChecker.BoolType
    }
  }


  final case class AllocatedIdentifier(name: String, `type`: Type) extends AllocatedExpression
  final case class AllocatedProperty(path: Seq[AllocatedIdentifier], `type`: Type) extends AllocatedExpression

  final case class AllocatedVar(name: String, `type`: Type) extends AllocatedExpression

  final case class AllocatedAndExpression(left: AllocatedExpression, right: AllocatedExpression) extends AllocatedExpression {
    override def `type`: Type = TypeChecker.BoolType
  }
  final case class AllocatedOrExpression(left: AllocatedExpression, right: AllocatedExpression) extends AllocatedExpression {
    override def `type`: Type = TypeChecker.BoolType
  }
  final case class AllocatedInExpression(element: AllocatedExpression, seq: AllocatedExpression) extends AllocatedExpression {
    override def `type`: Type = TypeChecker.BoolType
  }

  final case class AllocatedCompareExpression(left: AllocatedExpression, right: AllocatedExpression, op: AllocatedOp) extends AllocatedExpression {
    override def `type`: Type = op.`type`
  }

  final case class AllocatedCallExpression(name: AllocatedIdentifier, args: Seq[AllocatedExpression], `type`: Type) extends AllocatedExpression


  final case class AllocatedInteger(value: Int) extends AllocatedExpression {
    override def `type`: Type = TypeChecker.IntType
  }
  final case class AllocatedFloat(value: scala.Float) extends AllocatedExpression {
    override def `type`: Type = TypeChecker.FloatType
  }

  final case class AllocatedConstantArray(items: Seq[AllocatedConstant]) extends AllocatedConstant {
    override def `type`: Type = items.headOption.map(_.`type`).getOrElse(TypeChecker.BoolType) // TODO Empty arrays aren't allowed, but
  }
  final case class AllocatedStr(value: String) extends AllocatedConstant {
    override def `type`: Type = TypeChecker.StringType
  }


}
