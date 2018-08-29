package com.moonshot.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

import static org.objectweb.asm.ClassReader.EXPAND_FRAMES

class BitmapDetectorPlugin extends Transform implements Plugin<Project> {
    def isLibrary

    @Override
    void apply(Project project) {
        isLibrary = project.plugins.hasPlugin(LibraryPlugin)
        def android
        if (isLibrary) {
            android = project.extensions.getByType(LibraryExtension)
        } else {
            android = project.extensions.getByType(AppExtension)
        }
        android.registerTransform(this)
    }

    @Override
    String getName() {
        return "BitmapDetectorPlugin"
    }

    @Override
    Set getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set getScopes() {
        if (isLibrary) {
            return TransformManager.PROJECT_ONLY
        }
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }


    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
        String variantName = transformInvocation.getContext().getVariantName();
        if (!variantName.toLowerCase().contains("debug")) {
            return
            println "BitmapDetectorPlugin skip progress release transform"
        }

        //遍历inputs里的TransformInput
        transformInvocation.getInputs().each { TransformInput input ->
            //遍历input里边的DirectoryInput
            input.directoryInputs.each {
                DirectoryInput directoryInput ->
                    //是否是目录
                    if (directoryInput.file.isDirectory()) {
                        //遍历目录
                        directoryInput.file.eachFileRecurse {
                            File file ->
                                def name = file.name
                                //暂不需要Hook 项目代码
                        }
                    }
                    //处理完输入文件之后，要把输出给下一个任务
                    def dest = transformInvocation.getOutputProvider().getContentLocation(directoryInput.name,
                            directoryInput.contentTypes, directoryInput.scopes,
                            Format.DIRECTORY)
                    FileUtils.copyDirectory(directoryInput.file, dest)
            }
            input.jarInputs.each { JarInput jarInput ->
                //重名名输出文件,因为可能同名,会覆盖
                def jarName = jarInput.name
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                //
                File dest = transformInvocation.getOutputProvider().getContentLocation(jarName + md5Name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
                //处理Jar文件 主要是图片加载框架
                File modifyFile = processJar(jarInput.file)
                //覆盖要输入的文件
                FileUtils.copyFile(modifyFile, dest)
            }
        }

    }


    byte[] processCode(byte[] source) {
        ClassReader classReader = new ClassReader(source)
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
        ClassVisitor cv = new GlideClassVisitor(classWriter)
        classReader.accept(cv, EXPAND_FRAMES)

        return classWriter.toByteArray()

    }

    boolean isNeedProgress(String name) {
        if (name == null) {
            return false
        }
        if (name.contains("com/bumptech/glide/request/target/DrawableImageViewTarget.class")
                || name.contains("com/bumptech/glide/request/target/BitmapImageViewTarget.class")
                || name.contains("com/bumptech/glide/request/target/ThumbnailImageViewTarget.class")) {
            return true
        }
        return false
    }

    File processJar(File jarFile) {
        println "moonshot process jar file  " + jarFile.getAbsolutePath()
        JarFile jf = new JarFile(jarFile)
        Enumeration<JarEntry> je = jf.entries()
        File tempJar = new File(jarFile.parentFile, "temp.jar")
        JarOutputStream jos = new JarOutputStream(new FileOutputStream(tempJar))
        while (je.hasMoreElements()) {
            JarEntry jarEntry = je.nextElement()
            ZipEntry zipEntry = new ZipEntry(jarEntry.getName())
            InputStream originIns = jf.getInputStream(jarEntry)
            byte[] bytes = Utils.toByteArray(originIns)
            originIns.close()
            if (isNeedProgress(jarEntry.getName())) {
                bytes = processCode(bytes)
                jos.putNextEntry(zipEntry)
                jos.write(bytes)
                jos.closeEntry()
            } else {
                jos.putNextEntry(jarEntry)
                jos.write(bytes)
                jos.closeEntry()
            }

        }
        jos.close()
        jf.close()
        return tempJar
    }
}
