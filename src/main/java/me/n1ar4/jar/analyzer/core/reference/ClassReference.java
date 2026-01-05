/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.core.reference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class ClassReference {
    private final Integer version;
    private final Integer access;
    private final String name;
    private final String superClass;
    private final List<String> interfaces;
    private final boolean isInterface;
    private final List<Member> members;
    private final Set<AnnoReference> annotations;
    private final String jarName;
    private final Integer jarId;

    public static class Member {
        private final String name;
        private final int modifiers;
        private final String value;
        private final String desc;
        private final String signature;
        private final Handle type;

        public Member(String name, int modifiers, String realValue, String desc, String signature, Handle type) {
            this.name = name;
            this.modifiers = modifiers;
            this.value = realValue;
            this.desc = desc;
            this.signature = signature;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public int getModifiers() {
            return modifiers;
        }

        public String getValue() {
            return value;
        }

        public String getDesc() {
            return desc;
        }

        public String getSignature() {
            return signature;
        }

        public Handle getType() {
            return type;
        }
    }

    public ClassReference(String name, String superClass, List<String> interfaces,
                          boolean isInterface, List<Member> members,
                          ArrayList<String> annotations, String jarName, Integer jarId) {
        this(-1, -1, name, superClass, interfaces, isInterface, members,
                annotations.stream().map(AnnoReference::new).collect(Collectors.toSet()), jarName, jarId);
    }

    public ClassReference(Integer version, Integer access, String name, String superClass,
                          List<String> interfaces, boolean isInterface, List<Member> members,
                          Set<AnnoReference> annotations, String jarName, Integer jarId) {
        this.version = version;
        this.access = access;
        this.name = name;
        this.superClass = superClass;
        this.interfaces = interfaces;
        this.isInterface = isInterface;
        this.members = members;
        this.annotations = annotations;
        this.jarName = jarName;
        this.jarId = jarId;
    }

    public Integer getVersion() {
        return version;
    }

    public Integer getAccess() {
        return access;
    }

    public String getJarName() {
        return jarName;
    }

    public String getName() {
        return name;
    }

    public String getSuperClass() {
        return superClass;
    }

    public List<String> getInterfaces() {
        return interfaces;
    }

    public boolean isInterface() {
        return isInterface;
    }

    public List<Member> getMembers() {
        return members;
    }

    public Handle getHandle() {
        return new Handle(name);
    }

    public Set<AnnoReference> getAnnotations() {
        return annotations;
    }

    public Integer getJarId() {
        return jarId;
    }

    public static class Handle {
        private final String name;

        public Handle(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Handle handle = (Handle) o;
            return Objects.equals(name, handle.name);
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }

        public Handle cloneObj() {
            return new Handle(this.name);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClassReference c = (ClassReference) o;
        return Objects.equals(name, c.name);
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
