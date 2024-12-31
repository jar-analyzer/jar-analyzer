/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
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
