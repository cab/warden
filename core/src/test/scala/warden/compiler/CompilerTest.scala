package warden.compiler

import org.objectweb.asm.{Opcodes, ClassWriter, ClassReader}
import org.scalatest.{FlatSpec, Matchers}
import warden.{TextClassVisitor, User, ByteClassLoader}
import warden.compiler.Ast.{AndExpression, Identifier, OrExpression}


import scala.util.{Failure, Success}

class CompilerTest extends FlatSpec with Matchers {

  import warden.compiler.Compiler.{compile => compile_, _}
//  import Gate._


  def printClass(bytes: Array[Byte]) = {
    val reader = new ClassReader(bytes)
    val visitor =new TextClassVisitor(Opcodes.ASM5)
    reader.accept(visitor, ClassReader.SKIP_DEBUG)
  }

  def compile(gate: (String, String)): Either[Throwable, CompiledGate] = {
    val (name, input) = gate

    val program = Parser(input)
    program match {
      case Left(e) => Left(e)
      case Right(p) => {
        val typechecked = TypeChecker.check(Gate(name, p))
        typechecked match {
          case Failure(e) => Left(e)
          case Success(c) => compile_(c) match {
            case Success(co) => {
//              printClass(co.code)
              Right(co)
            }
            case Failure(e) => Left(e)
          }
        }
      }
    }
  }

  def run(gate: CompiledGate, expected: Boolean) = {
    val method = ByteClassLoader.loadClass(getClass.getClassLoader, gate.code).getMethod("gate", classOf[User])
    val testUser = new User {
      override def getId: String = "test"
    }
    val result: Boolean = method.invoke(null, testUser).asInstanceOf[Boolean]
    result should be(expected)
  }

  it should "compile an empty program" in {
    compile(("empty", "")) match {
      case Left(e) => fail(e)
      case Right(b) =>
    }
  }

  it should "compile a NOT" in {
    compile(("not", "(NOT 1)")) match {
      case Left(e) => fail(e)
      case Right(b) => run(b, false)
    }
    compile(("not", "(NOT 0)")) match {
      case Left(e) => fail(e)
      case Right(b) => run(b, true)
    }
    compile(("not", "(NOT 100)")) match {
      case Left(e) => fail(e)
      case Right(b) => run(b, false)
    }
  }


  it should "compile a IN" in {
    compile(("in",
      """
        $blacklist: ["you"]
        (user in $blacklist)
      """)) match {
      case Left(e) => fail(e)
      case Right(b) => run(b, false)
    }
  }

  it should "compile a less-than comparison" in {
    compile(("twoLessThanOne",
      """
        (2 < 1)
      """)) match {
      case Left(e) => fail(e)
      case Right(b) => run(b, false)
    }
  }


  it should "compile a greater-than comparison" in {
    compile(("twoGreaterThanOne",
      """
        (2 > 1)
      """)) match {
      case Left(e) => fail(e)
      case Right(b) => run(b, true)
    }
  }

  it should "compile a failing AND" in {
    compile(("and",
      """
        (1 > 2) AND (2 > 1)
      """)) match {
      case Left(e) => fail(e)
      case Right(b) => run(b, false)
    }
  }



  it should "compile a passing AND" in {
    compile(("and",
      """
        (1 < 2) AND (2 > 1)
      """)) match {
      case Left(e) => fail(e)
      case Right(b) => run(b, true)
    }
  }


  it should "compile a failing NOT" in {
    compile(("and",
      """
        (NOT (1 < 2)) AND (NOT (2 > 1))
      """)) match {
      case Left(e) => fail(e)
      case Right(b) => run(b, false)
    }
  }



  it should "compile a passing NOT" in {
    compile(("and",
      """
        (NOT (1 > 2)) AND (NOT (2 < 1))
      """)) match {
      case Left(e) => fail(e)
      case Right(b) => run(b, true)
    }
  }

  it should "compile a passing OR" in {
    compile(("or",
      """
        (1 > 2) OR (2 > 1)
      """)) match {
      case Left(e) => fail(e)
      case Right(b) => run(b, true)
    }
  }


  it should "compile a failing OR" in {
    compile(("or",
      """
        (1 > 2) OR (2 < 1)
      """)) match {
      case Left(e) => fail(e)
      case Right(b) => run(b, false)
    }
  }



}
