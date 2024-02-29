package org.vidar.rules;

import lombok.Data;

import java.util.List;

/**
 * @author zhchen
 */
@Data
public class ClazzRule {
    private String name;
    private Boolean isInterface;
    private List<String> extendsList;
    private List<String> implementsList;
    private List<String> annotations;
    private List<Field> fields;
    private List<Method> methods;


    @Data
    public static class Field {
        private String name;
        private Integer access;
        private String type;
    }

    @Data
    public static class Method {
        private String clazz;
        private String name;
        private String desc;
        private String access;
        private Boolean isStatic;
        private List<Call> calls;
    }

    @Data
    public static class Call {
        private String classRef;
        private String name;
        private String desc;
    }
}
