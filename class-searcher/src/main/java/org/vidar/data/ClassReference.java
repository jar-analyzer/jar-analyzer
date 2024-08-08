package org.vidar.data;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * @author zhchen
 */
@Data
public class ClassReference {
    private final String name;
    private final String superClass;
    private final String[] interfaces;
    private final boolean isInterface;
    private final Member[] members;
    private final Set<String> annotations;

    @Data
    public static class Member {
        private final String name;
        private final int modifiers;
        private final ClassReference.Handle type;

        public Member(String name, int modifiers, Handle type) {
            this.name = name;
            this.modifiers = modifiers;
            this.type = type;
        }

    }

    public ClassReference(String name, String superClass, String[] interfaces, boolean isInterface, Member[] members, Set<String> annotations) {
        this.name = name;
        this.superClass = superClass;
        this.interfaces = interfaces;
        this.isInterface = isInterface;
        this.members = members;
        this.annotations = annotations;
    }

    public Handle getHandle() {
        return new Handle(name);
    }

    @Data
    public static class Handle {
        private final String name;

        public Handle(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            return ((Handle) o).getName().equals(name);
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }
    }

    public static class Factory implements DataFactory<ClassReference> {

        @Override
        public ClassReference parse(String[] fields) {
            String[] interfaces;
            if (fields[2].equals("")) {
                interfaces = new String[0];
            } else {
                interfaces = fields[2].split(",");
            }

            String[] memberEntries = fields[4].split("!");
            Member[] members = new Member[memberEntries.length / 3];
            for (int i = 0; i < members.length; i++) {
                members[i] = new Member(memberEntries[3 * i], Integer.parseInt(memberEntries[3 * i + 1]),
                        new ClassReference.Handle(memberEntries[3 * i + 2]));
            }
            String[] tmpAnnotations = fields[5].split(",");
            Set<String> annotations = new HashSet<>();
            for (int i = 0; i < tmpAnnotations.length; i++) {
                if (tmpAnnotations.length > 0) {
                    annotations.add(tmpAnnotations[i]);
                }
            }

            return new ClassReference(
                    fields[0],
                    fields[1].equals("") ? null : fields[1],
                    interfaces,
                    Boolean.parseBoolean(fields[3]),
                    members,
                    annotations);
        }

        @Override
        public String[] serialize(ClassReference obj) {
            String interfaces;
            if (obj.interfaces.length > 0) {
                StringBuilder interfacesSb = new StringBuilder();
                for (String iface : obj.interfaces) {
                    interfacesSb.append(",").append(iface);
                }
                interfaces = interfacesSb.substring(1);
            } else {
                interfaces = "";
            }

            StringBuilder members = new StringBuilder();
            for (Member member : obj.members) {
                members.append("!").append(member.getName())
                        .append("!").append(Integer.toString(member.getModifiers()))
                        .append("!").append(member.getType().getName());
            }

            StringBuilder annotations = new StringBuilder();
            for (String a : obj.annotations) {
                annotations.append(a).append(",");
            }

            return new String[]{
                    obj.name,
                    obj.superClass,
                    interfaces,
                    Boolean.toString(obj.isInterface),
                    members.length() == 0 ? null : members.substring(1),
                    annotations.length() > 0 ? annotations.substring(0, annotations.length() - 1) : ""
            };
        }
    }
}
