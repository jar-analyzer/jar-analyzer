/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.core;

import me.n1ar4.jar.analyzer.core.reference.AnnoReference;

import java.util.Objects;
import java.util.Set;

public class MethodReference {
    private final ClassReference.Handle classReference;
    private final Set<AnnoReference> annotations;
    private final String name;
    private final String desc;
    private final int access;
    private final boolean isStatic;
    private int lineNumber = -1;
    private final String jarName;
    private final Integer jarId;

    public MethodReference(ClassReference.Handle classReference,
                           String name, String desc, boolean isStatic,
                           Set<AnnoReference> annotations,
                           int access, int lineNumber, String jarName, Integer jarId) {
        this.classReference = classReference;
        this.name = name;
        this.desc = desc;
        this.isStatic = isStatic;
        this.annotations = annotations;
        this.access = access;
        this.lineNumber = lineNumber;
        this.jarName = jarName;
        this.jarId = jarId;
    }

    public int getAccess() {
        return access;
    }

    public ClassReference.Handle getClassReference() {
        return classReference;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getJarName() {
        return jarName;
    }

    public Integer getJarId() {
        return jarId;
    }

    public Set<AnnoReference> getAnnotations() {
        return annotations;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public Handle getHandle() {
        return new Handle(classReference, name, desc);
    }

    public MethodReference cloneObj() {
        return new MethodReference(new ClassReference.Handle(this.classReference.getName()),
                this.name, this.desc, this.isStatic,
                this.annotations, this.access, this.lineNumber, jarName, jarId);
    }

    public static class Handle {
        private final ClassReference.Handle classReference;
        private final Integer opcode;
        private final String name;
        private final String desc;

        public Handle(ClassReference.Handle classReference, String name, String desc) {
            this(classReference, -1, name, desc);
        }

        public Handle(ClassReference.Handle classReference, int opcode, String name, String desc) {
            this.classReference = classReference;
            this.opcode = opcode;
            this.name = name;
            this.desc = desc;
        }

        public ClassReference.Handle getClassReference() {
            return classReference;
        }

        public Integer getOpcode() {
            return opcode;
        }

        public String getName() {
            return name;
        }

        public String getDesc() {
            return desc;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Handle handle = (Handle) o;
            if (!Objects.equals(classReference, handle.classReference))
                return false;
            if (!Objects.equals(name, handle.name))
                return false;
            return Objects.equals(desc, handle.desc);
        }

        @Override
        public int hashCode() {
            int result = classReference != null ? classReference.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + (desc != null ? desc.hashCode() : 0);
            return result;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodReference handle = (MethodReference) o;
        if (!Objects.equals(classReference, handle.classReference))
            return false;
        if (!Objects.equals(name, handle.name))
            return false;
        return Objects.equals(desc, handle.desc);
    }

    @Override
    public int hashCode() {
        int result = classReference != null ? classReference.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (desc != null ? desc.hashCode() : 0);
        return result;
    }
}
