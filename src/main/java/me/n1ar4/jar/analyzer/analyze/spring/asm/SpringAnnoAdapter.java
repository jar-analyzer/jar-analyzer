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

package me.n1ar4.jar.analyzer.analyze.spring.asm;

import me.n1ar4.jar.analyzer.analyze.spring.SpringParam;
import org.objectweb.asm.AnnotationVisitor;

import java.util.List;

public class SpringAnnoAdapter extends AnnotationVisitor {
    private final List<SpringParam> params;
    private final int index;

    public SpringAnnoAdapter(int api, AnnotationVisitor annotationVisitor,
                             List<SpringParam> params, int parameter) {
        super(api, annotationVisitor);
        this.index = parameter;
        this.params = params;
    }

    @Override
    public void visit(String name, Object value) {
        if (name.equals("name") || name.equals("value")) {
            SpringParam param = this.params.get(this.index);
            param.setReqName(value.toString());
            param.setParamIndex(this.index);
            this.params.set(this.index, param);
        }
        super.visit(name, value);
    }
}
