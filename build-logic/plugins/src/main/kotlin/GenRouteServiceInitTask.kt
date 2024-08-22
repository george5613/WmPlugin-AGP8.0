/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream


const val GENERATE_INIT: String = "GenerateInit: "
const val DOT_CLASS: String = ".class"

/**
 * Collect all ServiceInit_{*} classes & invoke init method
 *
 * Gen code into ServiceLoaderInit.class
 */
abstract class GenRouteServiceInitTask : DefaultTask() {

    // This property will be set to all Jar files available in scope
    @get:InputFiles
    abstract val allJars: ListProperty<RegularFile>

    // Gradle will set this property with all class directories that available in scope
    @get:InputFiles
    abstract val allDirectories: ListProperty<Directory>

    // Task will put all classes from directories and jars after optional modification into single jar
    @get:OutputFile
    abstract val output: RegularFileProperty

    private fun JarOutputStream.writeEntity(relativePath: String, byteArray: ByteArray) {
        putNextEntry(JarEntry(relativePath))
        write(byteArray)
        closeEntry()
    }

    // writeEntity methods check if the file has name that already exists in output jar
    private fun JarOutputStream.writeEntity(name: String, inputStream: InputStream) {
        putNextEntry(JarEntry(name))
        inputStream.copyTo(this)
        closeEntry()
    }

    @TaskAction
    fun taskAction() {
        println(GENERATE_INIT + "start...")
        val jarOutput = JarOutputStream(
            BufferedOutputStream(
                FileOutputStream(
                    output.get().asFile
                )
            )
        )

        val ms = System.currentTimeMillis()
        val initClasses = Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())


        allJars.get().forEach { jarInputFile ->
            val jarFile = JarFile(jarInputFile.asFile)
            jarFile.entries().iterator().forEach { jarEntry ->
                val classPathName = jarEntry.name
                //filter no need files
                if (classPathName.endsWith(DOT_CLASS) && !classPathName.contains("META-INF")) {
                    //copy classes from jar files without modification
                    jarOutput.writeEntity(classPathName, jarFile.getInputStream(jarEntry))
                    val className = classPathName.replace('/', '.')
                    if (className.startsWith(Const.GEN_PKG_SERVICE)) {
                        println("found class: $className")
                        initClasses.add(className)
                    }
                }
            }
            jarFile.close()
        }

        allDirectories.get().forEach { directory ->
            directory.asFile.walk().forEach { file ->
                if (file.isFile) {
                    val relativePath = directory.asFile.toURI().relativize(file.toURI()).path
                    val className = relativePath.replace('/', '.')
                    if (className.startsWith(Const.GEN_PKG_SERVICE)) {
                        println("found class: $className")
                        initClasses.add(className)
                    }
                    //copy service classes from directories without modification
                    jarOutput.writeEntity(relativePath, file.inputStream())
                }
            }
        }
        generateServiceInitClass(initClasses, jarOutput)
        jarOutput.close()
        println(String.format(GENERATE_INIT + "finished cost %sms", System.currentTimeMillis() - ms))
    }

    private fun generateServiceInitClass(classes: Set<String>, jarOutput: JarOutputStream) {
        val classWriter = ClassWriter(0)
        classWriter.visit(
            Opcodes.V1_8,
            Opcodes.ACC_PUBLIC,
            Const.SERVICE_LOADER_INIT.replace('.', '/'),
            null,
            "java/lang/Object",
            null
        )
        val mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC, "init", "()V", null, null)
        mv.visitCode()
        for (clazz in classes) {
            var input = clazz.replace(DOT_CLASS, "")
            input = input.replace(".", "/")
            mv.visitMethodInsn(
                Opcodes.INVOKESTATIC, input, "init", "()V", false
            )
        }
        mv.visitInsn(Opcodes.RETURN)
        mv.visitMaxs(0, 0)
        mv.visitEnd()
        classWriter.visitEnd()
        jarOutput.writeEntity("${Const.SERVICE_LOADER_INIT.replace('.', '/')}$DOT_CLASS", classWriter.toByteArray())
    }
}