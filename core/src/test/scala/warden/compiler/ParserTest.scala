package warden.compiler

import org.scalatest.{Matchers, FlatSpec}
import warden.compiler.Ast._

import scala.util.Failure

class ParserTest extends FlatSpec with Matchers {

  it should "parse an empty program" in {
    Parser("") match {
      case Left(e) => fail(e)
      case Right(_) =>
    }
  }

  it should "parse an OR expression" in {
    Parser("a OR b") match {
      case Left(e) => fail(e)
      case Right(p) => p.expressions.head match {
        case o: OrExpression =>
        case _ => fail()
      }
    }
  }


  it should "parse an AND expression" in {
    Parser("a AND b") match {
      case Left(e) => fail(e)
      case Right(p) => p.expressions.head match {
        case a: AndExpression =>
        case _ => fail()
      }
    }
  }


  it should "parse a call" in {
    Parser("(my-fn my-arg)") match {
      case Left(e) => fail(e)
      case Right(p) => p.expressions.head match {
        case a: CallExpression =>
        case _ => fail()
      }
    }
  }

  it should "parse identifiers" in {
    Parser("my-id") match {
      case Left(e) => fail(e)
      case Right(p) => p.expressions.head match {
        case p: Identifier => p.name should be("my-id")
        case e => fail(e.toString)
      }
    }
  }

  it should "parse properties" in {
    Parser("my/id/version") match {
      case Left(e) => fail(e)
      case Right(p) => p.expressions.head match {
        case p: Property => {
          val words: Seq[String] = p.path.map(_.name)
          words(0) should be("my")
          words(1) should be("id")
          words(2) should be("version")
        }
        case _ => fail()
      }
    }
  }




  it should "parse integers" in {
    Parser("356") match {
      case Left(e) => fail(e)
      case Right(p) => p.expressions.head match {
        case i: Ast.Integer => i.value should be(356)
        case _ => fail()
      }
    }
  }


  it should "parse 0" in {
    Parser("0") match {
      case Left(e) => fail(e)
      case Right(p) => p.expressions.head match {
        case i: Ast.Integer => i.value should be(0)
        case _ => fail()
      }
    }
  }

  it should "parse floats without a zero" in {
    Parser(".4") match {
      case Left(e) => fail(e)
      case Right(p) => p.expressions.head match {
        case i: Ast.Float => i.value should be(0.4f)
        case _ => fail()
      }
    }
  }

  it should "parse floats with a zero" in {
    Parser("0.0149") match {
      case Left(e) => fail(e)
      case Right(p) => p.expressions.head match {
        case i: Ast.Float => i.value should be(0.0149f)
        case _ => fail()
      }
    }
  }



  it should "parse a `in` expression" in {
    Parser("(user in $blacklist)") match {
      case Left(e) => fail(e)
      case Right(p) => p.expressions.head match {
        case i: Ast.Expression => println(i)
        case _ => fail()
      }
    }
  }


  it should "parse a `compare` expression" in {
    Parser("(app/version > $blacklist)") match {
      case Left(e) => fail(e)
      case Right(p) => p.expressions.head match {
        case i: Ast.Expression => println(i)
        case _ => fail()
      }
    }
  }



}
