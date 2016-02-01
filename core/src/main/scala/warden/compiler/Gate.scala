package warden.compiler

import warden.compiler.Ast.Program
import warden.compiler.IR.TypedProgram

import scala.language.implicitConversions

object Gate {
}

final case class Gate(name: String, program: Program)

final case class TypecheckedGate(name: String, program: TypedProgram)


final case class CompiledGate(name: String, code: Array[Byte])