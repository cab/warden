package warden.compiler

import org.objectweb.asm._
import org.objectweb.asm.Opcodes._

class MethodContext(visitor: MethodVisitor) extends MethodVisitor(ASM5) {
  override def visitLocalVariable(name: String, desc: String, signature: String, start: Label, end: Label, index: Int): Unit = super.visitLocalVariable(name, desc, signature, start, end, index)

  override def visitAttribute(attr: Attribute): Unit = visitor.visitAttribute(attr)

  override def visitCode(): Unit = visitor.visitCode()

  override def visitIincInsn(`var`: Int, increment: Int): Unit = visitor.visitIincInsn(`var`, increment)

  override def visitLdcInsn(cst: scala.Any): Unit = visitor.visitLdcInsn(cst)

  override def visitTypeAnnotation(typeRef: Int, typePath: TypePath, desc: String, visible: Boolean): AnnotationVisitor = visitor.visitTypeAnnotation(typeRef, typePath, desc, visible)

  override def visitTableSwitchInsn(min: Int, max: Int, dflt: Label, labels: Label*): Unit = visitor.visitTableSwitchInsn(min, max, dflt, labels: _*)

  override def visitInsnAnnotation(typeRef: Int, typePath: TypePath, desc: String, visible: Boolean): AnnotationVisitor = visitor.visitInsnAnnotation(typeRef, typePath, desc, visible)

  override def visitLookupSwitchInsn(dflt: Label, keys: Array[Int], labels: Array[Label]): Unit = visitor.visitLookupSwitchInsn(dflt, keys, labels)

  override def visitIntInsn(opcode: Int, operand: Int): Unit = visitor.visitIntInsn(opcode, operand)

  override def visitLineNumber(line: Int, start: Label): Unit = visitor.visitLineNumber(line, start)

  override def visitMultiANewArrayInsn(desc: String, dims: Int): Unit = visitor.visitMultiANewArrayInsn(desc, dims)

  override def visitFieldInsn(opcode: Int, owner: String, name: String, desc: String): Unit = visitor.visitFieldInsn(opcode, owner, name, desc)


  override def visitMethodInsn(opcode: Int, owner: String, name: String, desc: String, itf: Boolean): Unit = visitor.visitMethodInsn(opcode, owner, name, desc, itf)

  override def visitLocalVariableAnnotation(typeRef: Int, typePath: TypePath, start: Array[Label], end: Array[Label], index: Array[Int], desc: String, visible: Boolean): AnnotationVisitor = visitor.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible)

  override def visitJumpInsn(opcode: Int, label: Label): Unit = visitor.visitJumpInsn(opcode, label)

  override def visitVarInsn(opcode: Int, `var`: Int): Unit = visitor.visitVarInsn(opcode, `var`)

  override def visitFrame(`type`: Int, nLocal: Int, local: Array[AnyRef], nStack: Int, stack: Array[AnyRef]): Unit = visitor.visitFrame(`type`, nLocal, local, nStack, stack)

  override def visitEnd(): Unit = visitor.visitEnd()

  override def visitParameter(name: String, access: Int): Unit = visitor.visitParameter(name, access)

  override def visitTypeInsn(opcode: Int, `type`: String): Unit = visitor.visitTypeInsn(opcode, `type`)

  override def visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor = visitor.visitAnnotation(desc, visible)

  override def visitMaxs(maxStack: Int, maxLocals: Int): Unit = visitor.visitMaxs(maxStack, maxLocals)

  override def visitParameterAnnotation(parameter: Int, desc: String, visible: Boolean): AnnotationVisitor = visitor.visitParameterAnnotation(parameter, desc, visible)

  override def visitTryCatchAnnotation(typeRef: Int, typePath: TypePath, desc: String, visible: Boolean): AnnotationVisitor = visitor.visitTryCatchAnnotation(typeRef, typePath, desc, visible)

  override def visitTryCatchBlock(start: Label, end: Label, handler: Label, `type`: String): Unit = visitor.visitTryCatchBlock(start, end, handler, `type`)

  override def visitAnnotationDefault(): AnnotationVisitor = visitor.visitAnnotationDefault()

  override def visitInsn(opcode: Int): Unit = visitor.visitInsn(opcode)

  override def visitInvokeDynamicInsn(name: String, desc: String, bsm: Handle, bsmArgs: AnyRef*): Unit = visitor.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs: _*)

  override def visitLabel(label: Label): Unit = visitor.visitLabel(label)
}
