package warden.compiler

import warden.compiler.Ast.{InExpression, Str, ConstantArray, CallExpression}

import scala.language.{existentials, implicitConversions}
import scala.util.{Failure, Success, Try}

object TypeChecker {

  import IR._

  trait Type {
    def javaClass: Class[_]
  }

  case object UnknownType extends Type {
    override def javaClass: Class[_] = throw new NotImplementedError()
  }

  case object StringType extends Type {
    override def javaClass: Class[_] = classOf[java.lang.String]
  }

  case object BoolType extends Type {
    override def javaClass: Class[_] = classOf[java.lang.Boolean]
  }

  case object IntType extends Type {
    override def javaClass: Class[_] = classOf[java.lang.Integer]
  }

  case object FloatType extends Type {
    override def javaClass: Class[_] = classOf[java.lang.Float]
  }


  case object UserType extends Type {
    override def javaClass: Class[_] = classOf[warden.User]
  }

  final case class ArrayType(subtype: Type) extends Type {
    override def javaClass: Class[_] = throw new NotImplementedError()
  }

  def check(gate: Gate): Try[TypecheckedGate] = {
    val checkedProgram = check(gate.program)
    checkedProgram.map(p => TypecheckedGate(gate.name, p))
  }



  class Env {

    private var declarations = Map.empty[String, TypedDeclaration]

    def register(node: TypedNode): Unit = {
      node match {
        case declaration: TypedDeclaration => declarations = declarations + (declaration.variable.name -> declaration)
        case other => {
          println(s"PLEASE HANDLE ${node}")
        }
      }
    }

    def getIdType(name: String): Option[Type] = {
      name match {
        case "user" => Some(UserType)
        case _ => None
      }
    }

    def getVariableType(name: String): Option[Type] = {
      declarations.lift(name).map(_.`type`)
    }
  }

  def envify(nodes: Seq[TypedNode]): Env = {
    nodes.foldLeft(new Env)((env, node) => {
      env.register(node)
      env
    })
  }

  def check(program: Ast.Program): Try[TypedProgram] = {
    val checkedDeclarations = program.declarations.foldLeft(Seq.empty[TypedDeclaration])((seq, dec) => {
      val env = envify(seq)
      val typed = analyse(env, dec) match {
        case Success(d) => d
        case Failure(e) => throw e
      }
      seq :+ typed
    })
    val checkedExpressions = program.expressions.foldLeft(Seq.empty[TypedExpression])((seq, expr) => {
      val allTypes = checkedDeclarations ++ seq
      val env = envify(allTypes)
      val typed = analyse(env, expr) match {
        case Success(d) => d
        case Failure(e) => throw e
      }
      seq :+ typed
    })
    Try {
      TypedProgram(checkedDeclarations, checkedExpressions)
    }
  }

  def analyse(env: Env, node: Ast.Expression): Try[TypedExpression] = Try {
    node match {
      case call: CallExpression => {
        val `type` = getTypeOfStdlibFn(call.name.name)
        val args = call.args.map(i => analyse(env, i)).map {
          case Success(t) => t
          case Failure(e) => throw e
        }
        TypedCallExpression(TypedIdentifier(call.name.name, `type`), args, `type`)
      }
      case str: Str => {
        TypedStr(str.value)
      }
      case int: Ast.Integer => {
        TypedInteger(int.value)
      }

      case id: Ast.Identifier => {
        val envType = env.getIdType(id.name)
        envType match {
          case Some(t) => TypedIdentifier(id.name, t)
          case None => throw new Error(s"Could not resolve ${id.name}")
        }
      }

      case variable: Ast.Var => {
        val envType = env.getVariableType(variable.name)
        envType match {
          case Some(t) => TypedVar(variable.name, t)
          case None => throw new Error(s"Could not resolve $$${variable.name}")
        }
      }
      case in: InExpression => {
        val typedElement = analyse(env, in.element)
        val typedSeq = analyse(env, in.seq)
        (typedElement, typedSeq) match {
          case (Success(e), Success(s)) => TypedInExpression(e, s)
          case (Failure(e), _) => throw e
          case (_, Failure(e)) => throw e
        }
      }
      case compare: Ast.CompareExpression => {
        val typedLeft = analyse(env, compare.left)
        val typedRight = analyse(env, compare.right)
        val typedOp = analyse(env, compare.op).map(_.asInstanceOf[TypedOp]) //TODO possible without asInstanceOf?
        (typedLeft, typedRight, typedOp) match {
          case (Success(l), Success(r), Success(o)) => TypedCompareExpression(l, r, o)
          case (Failure(e), _, _) => throw e
          case (_, Failure(e), _) => throw e
          case (_, _, Failure(e)) => throw e
        }
      }
      case gt: Ast.Op.GreaterThan.type => TypedOp.TypedGreaterThan
      case lt: Ast.Op.LessThan.type => TypedOp.TypedLessThan
      case and: Ast.AndExpression => {
        val typedLeft = analyse(env, and.left)
        val typedRight = analyse(env, and.right)
        (typedLeft, typedRight) match {
          case (Success(l), Success(r)) => TypedAndExpression(l, r)
          case (Failure(e), _) => throw e
          case (_, Failure(e)) => throw e
        }
      }
      case or: Ast.OrExpression => {
        val typedLeft = analyse(env, or.left)
        val typedRight = analyse(env, or.right)
        (typedLeft, typedRight) match {
          case (Success(l), Success(r)) => TypedOrExpression(l, r)
          case (Failure(e), _) => throw e
          case (_, Failure(e)) => throw e
        }
      }
      case array: ConstantArray => {
        val typedItems = array.items.map(i => analyse(env, i)).map {
          case Success(t: TypedConstant) => t
          case Failure(e) => throw e
          case _ => throw new Error("Expected constant")
        }
        TypedConstantArray(typedItems)
      }

      case x => {
        println(s"plz handle ${x}")
        throw new Error(s"plz handle ${x.getClass}")
      }
    }
  }

  def analyse(env: Env, node: Ast.Declaration): Try[TypedDeclaration] = Try {
    val typedValue = analyse(env, node.value) match {
      case Success(c: TypedConstant) => c
      case Failure(e) => throw e
      case _ => throw new Error("BAD TYPING TODO")
    }
    val typedVar = TypedVar(node.variable.name, typedValue.`type`)
    TypedDeclaration(typedVar, typedValue)
  }

  def getTypeOfStdlibFn(name: String): Type = {
    IntType
  }

}
