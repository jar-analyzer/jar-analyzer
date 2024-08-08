package org.vidar.data;

import lombok.Data;

/**
 * @author zhchen
 */
@Data
public class MethodReference {
    private final ClassReference.Handle classReference;
    private final String name;
    private final String desc;
    private final boolean isStatic;
    private final String accessModifier;

    public MethodReference(ClassReference.Handle classReference, String name, String desc, boolean isStatic, String accessModifier) {
        this.classReference = classReference;
        this.name = name;
        this.desc = desc;
        this.isStatic = isStatic;
        this.accessModifier = accessModifier;
    }

    public Handle getHandle() {
        return new Handle(classReference, name, desc);
    }

    @Data
    public static class Handle {
        private final ClassReference.Handle classReference;
        private final String name;
        private final String desc;

        public Handle(ClassReference.Handle classReference, String name, String desc) {
            this.classReference = classReference;
            this.name = name;
            this.desc = desc;
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
            return handle.classReference.equals(classReference)
                    && handle.name.equals(name)
                    && handle.desc.equals(desc);
        }

        @Override
        public int hashCode() {
            int result = classReference != null ? classReference.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + (desc != null ? desc.hashCode() : 0);
            return result;
        }
    }

    public static class Factory implements DataFactory<MethodReference> {

        @Override
        public MethodReference parse(String[] fields) {
            return new MethodReference(
                    new ClassReference.Handle(fields[0]),
                    fields[1],
                    fields[2],
                    Boolean.parseBoolean(fields[3]),
                    fields[4]);
        }

        @Override
        public String[] serialize(MethodReference obj) {
            return new String[]{
                    obj.classReference.getName(),
                    obj.name,
                    obj.desc,
                    Boolean.toString(obj.isStatic),
                    obj.accessModifier
            };
        }
    }
}
