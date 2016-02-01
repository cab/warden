package warden;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.PrintWriter;

public class TextClassVisitor extends ClassVisitor {
    public TextClassVisitor(int api) {
        super(api, new TraceClassVisitor(new PrintWriter(System.out)));
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        Printer p = new Textifier(Opcodes.ASM5) {
            @Override
            public void visitMethodEnd() {
                print(new PrintWriter(System.out)); // print it after it has been visited
            }
        };
        return new TraceMethodVisitor(mv, p);
    }
}