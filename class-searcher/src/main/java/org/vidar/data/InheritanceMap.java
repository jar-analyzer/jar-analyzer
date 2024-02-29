package org.vidar.data;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author zhchen
 */
public class InheritanceMap {
    //子-父关系集合
    private final Map<ClassReference.Handle, Set<ClassReference.Handle>> inheritanceMap;
    //父-子关系集合
    private final Map<ClassReference.Handle, Set<ClassReference.Handle>> subClassMap;

    public InheritanceMap(Map<ClassReference.Handle, Set<ClassReference.Handle>> inheritanceMap) {
        this.inheritanceMap = inheritanceMap;
        subClassMap = new HashMap<>();
        for (Map.Entry<ClassReference.Handle, Set<ClassReference.Handle>> entry : inheritanceMap.entrySet()) {
            ClassReference.Handle child = entry.getKey();
            for (ClassReference.Handle parent : entry.getValue()) {
                subClassMap.computeIfAbsent(parent, k -> new HashSet<>()).add(child);
            }
        }
    }

    public Set<Map.Entry<ClassReference.Handle, Set<ClassReference.Handle>>> entrySet() {
        return inheritanceMap.entrySet();
    }

    public Set<ClassReference.Handle> getSuperClasses(ClassReference.Handle clazz) {
        Set<ClassReference.Handle> parents = inheritanceMap.get(clazz);
        if (parents == null) {
            return null;
        }
        return Collections.unmodifiableSet(parents);
    }

    public boolean isSubclassOf(ClassReference.Handle clazz, ClassReference.Handle superClass) {
        Set<ClassReference.Handle> parents = inheritanceMap.get(clazz);
        if (parents == null) {
            return false;
        }
        return parents.contains(superClass);
    }

    public Set<ClassReference.Handle> getSubClasses(ClassReference.Handle clazz) {
        Set<ClassReference.Handle> subClasses = subClassMap.get(clazz);
        if (subClasses == null) {
            return null;
        }
        return Collections.unmodifiableSet(subClasses);
    }

    public void save() throws IOException {
        //inheritanceMap.dat数据格式：
        //类名 父类或超类或接口类1 父类或超类或接口类2 父类或超类或接口类3 ...
        DataLoader.saveData(Paths.get("inheritanceMap.dat"), new InheritanceMapFactory(), inheritanceMap.entrySet());
    }

    public static InheritanceMap load() throws IOException {
        Map<ClassReference.Handle, Set<ClassReference.Handle>> inheritanceMap = new HashMap<>();
        for (Map.Entry<ClassReference.Handle, Set<ClassReference.Handle>> entry : DataLoader.loadData(
                Paths.get("inheritanceMap.dat"), new InheritanceMapFactory())) {
            inheritanceMap.put(entry.getKey(), entry.getValue());
        }
        return new InheritanceMap(inheritanceMap);
    }

    private static class InheritanceMapFactory implements DataFactory<Map.Entry<ClassReference.Handle, Set<ClassReference.Handle>>> {
        @Override
        public Map.Entry<ClassReference.Handle, Set<ClassReference.Handle>> parse(String[] fields) {
            ClassReference.Handle clazz = new ClassReference.Handle(fields[0]);
            Set<ClassReference.Handle> superClasses = new HashSet<>();
            for (int i = 1; i < fields.length; i++) {
                superClasses.add(new ClassReference.Handle(fields[i]));
            }
            return new AbstractMap.SimpleEntry<>(clazz, superClasses);
        }

        @Override
        public String[] serialize(Map.Entry<ClassReference.Handle, Set<ClassReference.Handle>> obj) {
            final String[] fields = new String[obj.getValue().size()+1];
            fields[0] = obj.getKey().getName();
            int i = 1;
            for (ClassReference.Handle handle : obj.getValue()) {
                fields[i++] = handle.getName();
            }
            return fields;
        }
    }
}
