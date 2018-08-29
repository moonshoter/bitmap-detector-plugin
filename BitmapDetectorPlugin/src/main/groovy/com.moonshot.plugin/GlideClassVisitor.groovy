package com.moonshot.plugin

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

class GlideClassVisitor extends ClassVisitor {


    GlideClassVisitor(ClassVisitor cv) {
        super(Opcodes.ASM5, cv)
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions);
        methodVisitor = new AdviceAdapter(Opcodes.ASM5, methodVisitor, access, name, desc) {

            private boolean isInject() {
                if (("setResource").equals(name)) {
                    return true
                }
                return false
            }

            @Override
            protected void onMethodExit(int i) {
                if (isInject()) {
                    //setResource 执行结束后调用 GlideHelper.detect
                    mv.visitVarInsn(ALOAD, 0)
                    mv.visitVarInsn(ALOAD, 1)
                    mv.visitMethodInsn(INVOKESTATIC, "com/moonshot/library/imagedetector/GlideHelper", "detect", "(Lcom/bumptech/glide/request/target/ImageViewTarget;Ljava/lang/Object;)V", false)

                }

            }
        }
        return methodVisitor
    }
}
