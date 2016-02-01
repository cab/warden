package warden.compiler

import java.io.{StringWriter, PrintWriter}

import org.objectweb.asm.util.CheckClassAdapter
import org.objectweb.asm._
import warden.compiler.Ast._
import warden.compiler.IR.TypedExpression

import scala.language.{higherKinds, implicitConversions}
import scala.reflect.ClassTag
import scala.util.{Try, Failure, Success}


object Compiler {

  import IR._
  import org.objectweb.asm.Opcodes._
  import TypeChecker._



  def compile(gate: TypecheckedGate): Try[CompiledGate]  = Try {



    val classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES)

    classWriter.visit(V1_8, ACC_PUBLIC + ACC_SUPER, gate.name, null, "java/lang/Object", null)

    gate.program.declarations.foldLeft(classWriter)(compile)

    val gateMethod = new MethodContext(classWriter.visitMethod(ACC_PUBLIC + ACC_STATIC, "gate", "(Lwarden/User;)Z", null, null))

    gateMethod.visitCode()

    val writtenWriter = if(gate.program.expressions.isEmpty) {
      gateMethod.visitInsn(ICONST_0)
      gateMethod
    } else {
      gate.program.expressions.foldLeft(gateMethod)(compile)
    }


    gateMethod.visitInsn(IRETURN)
    writtenWriter.visitMaxs(0, 0)
    writtenWriter.visitEnd()

    val classBytes = classWriter.toByteArray
    val sw = new StringWriter()
    val pw = new PrintWriter(sw)
    CheckClassAdapter.verify(new ClassReader(classBytes), false, pw)

    if(sw.toString.length > 0) {
      throw new Error("Invalid bytecode generated: " + sw.toString)
//      throw new Error(sw.toString)
    }

    CompiledGate(gate.name, classBytes)
  }

  def compile(writer: MethodContext, expr: TypedExpression): MethodContext = {
    expr match {
      case a: TypedAndExpression => compile(writer, a)
      case o: TypedOrExpression => compile(writer, o)
      case o: TypedInExpression => compile(writer, o)
      case c: TypedCallExpression => compile(writer, c)
      case i: TypedInteger => compile(writer, i)
      case f: TypedFloat => compile(writer, f)
      case i: TypedIdentifier => compile(writer, i)
      case v: TypedVar => compile(writer, v)
      case p: TypedProperty => compile(writer, p)
      case c: TypedCompareExpression => compile(writer, c)
      case other => {
        println(s"TODO plz handle ${other}")
        writer
      }
    }
  }

  def compile(writer: MethodContext, call: TypedCallExpression): MethodContext = {
    call.args.foldLeft(writer)(compile)
    writer.visitMethodInsn(INVOKESTATIC, "warden/Stdlib", call.name.name, "(Z)Z", false)
    writer
  }

  def compile(writer: MethodContext, or: TypedOrExpression): MethodContext = {
    compile(writer, or.left)
    writer.visitVarInsn(ISTORE, 1)
    compile(writer, or.right)
    writer.visitVarInsn(ISTORE, 2)
    val label0 = new Label
    val label1 = new Label
    writer.visitVarInsn(ILOAD, 1)
    writer.visitJumpInsn(IFNE, label0)
    writer.visitVarInsn(ILOAD, 2)
    writer.visitJumpInsn(IFNE, label0)
    writer.visitInsn(ICONST_0)
    writer.visitJumpInsn(GOTO, label1)
    writer.visitLabel(label0)
    writer.visitInsn(ICONST_1)
    writer.visitLabel(label1)
    writer
  }


  def compile(writer: MethodContext, compare: TypedCompareExpression): MethodContext = {
    compile(writer, compare.left)
    compile(writer, compare.right)
    compare.op match {
      case gt: TypedOp.TypedGreaterThan.type => {
        val label0 = new Label
        val label1 = new Label
        writer.visitJumpInsn(IF_ICMPLE, label0)
        writer.visitInsn(ICONST_1)
        writer.visitJumpInsn(GOTO, label1)
        writer.visitLabel(label0)
        writer.visitInsn(ICONST_0)
        writer.visitLabel(label1)
      }
      case lt: TypedOp.TypedLessThan.type => {
        val label0 = new Label
        val label1 = new Label
        writer.visitJumpInsn(IF_ICMPGE, label0)
        writer.visitInsn(ICONST_1)
        writer.visitJumpInsn(GOTO, label1)
        writer.visitLabel(label0)
        writer.visitInsn(ICONST_0)
        writer.visitLabel(label1)
      }
      case other => throw new Error(s"Can not handle op: ${other}")
    }
    writer
  }


  def compile(writer: MethodContext, in: TypedInExpression): MethodContext = {
    compile(writer, in.seq)
    compile(writer, in.element)
    writer.visitMethodInsn(INVOKESTATIC, "warden/Stdlib", "in", "(Ljava/util/Collection;Ljava/lang/Object;)Z", false)
    writer
  }


  def compile(writer: MethodContext, and: TypedAndExpression): MethodContext = {
    compile(writer, and.left)
    writer.visitVarInsn(ISTORE, 1)
    compile(writer, and.right)
    writer.visitVarInsn(ISTORE, 2)
    val label0 = new Label
    val label1 = new Label
    writer.visitVarInsn(ILOAD, 1)
    writer.visitJumpInsn(IFEQ, label0)
    writer.visitVarInsn(ILOAD, 2)
    writer.visitJumpInsn(IFEQ, label0)
    writer.visitInsn(ICONST_1)
    writer.visitJumpInsn(GOTO, label1)
    writer.visitLabel(label0)
    writer.visitInsn(ICONST_0)
    writer.visitLabel(label1)
    writer
  }

  def compile(writer: MethodContext, float: TypedFloat): MethodContext = {
    writer
  }

  def compile(writer: MethodContext, int: TypedInteger): MethodContext = {
    int.value match {
      case 0 => writer.visitInsn(ICONST_0)
      case 1 => writer.visitInsn(ICONST_1)
      case 2 => writer.visitInsn(ICONST_2)
      case 3 => writer.visitInsn(ICONST_3)
      case 4 => writer.visitInsn(ICONST_4)
      case 5 => writer.visitInsn(ICONST_5)
      case x if x <= Short.MaxValue && x >= Short.MinValue => writer.visitIntInsn(BIPUSH, x)
      case _ => throw new Error("int too big") //TODO int + long
    }
    writer
  }

  def compile(writer: MethodContext, id: TypedIdentifier): MethodContext = {
    println(s"ID! ${id.name}")
    id.name match {
      case "user" => writer.visitVarInsn(ALOAD, 0);
    }
    writer
  }

  def nameOf(c: Class[_]): String = {
    org.objectweb.asm.Type.getInternalName(c)
  }

  def compile(writer: MethodContext, variable: TypedVar): MethodContext = {
    writer.visitLdcInsn(variable.name)
    val r = s"(L${nameOf(variable.`type`.javaClass)};)Ljava/util/List;"
    println(r)
    writer.visitMethodInsn(INVOKESTATIC, "warden/Stdlib", "list", r, false)
    writer
  }

  def compile(writer: ClassWriter, dec: TypedDeclaration): ClassWriter = {
    val fv = writer.visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, "blacklist", "Ljava/util/Set;", null, null)
    fv.visitEnd()
    writer
  }


}
