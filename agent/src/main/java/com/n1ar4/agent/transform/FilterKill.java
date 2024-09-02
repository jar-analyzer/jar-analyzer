/*
 * MIT License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.n1ar4.agent.transform;


import com.n1ar4.agent.asm.FilterKillClassVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class FilterKill implements ClassFileTransformer {
    private final String className;

    public FilterKill(String className) {
        this.className = className;
    }

    @Override
    public byte[] transform(ClassLoader loader,
                            String className, Class<?> clsMemShell,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {
        try {
            className = className.replace("/", ".");
            if (className.equals(this.className)) {
                ClassReader cr = new ClassReader(classfileBuffer);
                ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
                int api = Opcodes.ASM9;
                ClassVisitor cv = new FilterKillClassVisitor(api, cw);
                int parsingOptions = ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES;
                cr.accept(cv, parsingOptions);
                return cw.toByteArray();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new byte[0];
    }
}
