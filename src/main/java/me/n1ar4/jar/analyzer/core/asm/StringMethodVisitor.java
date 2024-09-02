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

package me.n1ar4.jar.analyzer.core.asm;

import me.n1ar4.jar.analyzer.core.ClassReference;
import me.n1ar4.jar.analyzer.core.MethodReference;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StringMethodVisitor extends MethodVisitor {
    private final Map<MethodReference.Handle, List<String>> strMap;

    private MethodReference ownerHandle = null;

    public StringMethodVisitor(int api, MethodVisitor methodVisitor,
                               String owner, String methodName, String desc,
                               Map<MethodReference.Handle, List<String>> strMap,
                               Map<ClassReference.Handle, ClassReference> classMap,
                               Map<MethodReference.Handle, MethodReference> methodMap) {
        super(api, methodVisitor);
        this.strMap = strMap;
        ClassReference.Handle ch = new ClassReference.Handle(owner);
        if (classMap.get(ch) != null) {
            MethodReference m = methodMap.get(new MethodReference.Handle(ch, methodName, desc));
            if (m != null) {
                this.ownerHandle = m;
            }
        }
    }

    public static boolean isPrintable(String str) {
        return str.matches("\\A\\p{Print}+\\z");
    }

    @Override
    public void visitLdcInsn(Object o) {
        if (this.ownerHandle == null)
            return;
        if (o instanceof String) {
            String str = (String) o;
            if (str.trim().isEmpty()) {
                return;
            }
            if (!isPrintable(str)) {
                return;
            }
            List<String> mList = strMap.getOrDefault(this.ownerHandle.getHandle(), new ArrayList<>());
            mList.add(str);
            strMap.put(this.ownerHandle.getHandle(), mList);
        }
        super.visitLdcInsn(o);
    }
}
