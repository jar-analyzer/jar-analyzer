package com.n1ar4.agent.Utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class ReflectUtils {

    public static Object getStaticDeclaredField(Object obj, String fieldName) {
        Object value = null;
        Class<?> targetClass = obj.getClass();
        Field targetField = null;
        while (targetClass != null) {
            try {
                targetField = targetClass.getDeclaredField(fieldName);
                targetField.setAccessible(true);
                break;
            } catch (NoSuchFieldException e) {
                targetClass = targetClass.getSuperclass();
//                System.out.println();
//                OutputUtils.getPrintStream().println("e : " + e.toString());
            }
        }
        if (targetField == null) {
//            OutputUtils.getPrintStream().println("get DeclaredField "+ fieldName + " from " + obj.getClass().getName() + " failed");
            return null;
        }

        try {
            value = targetField.get(null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return value;
    }

    public static Object getDeclaredField(Object obj, String fieldName) {
        Class<?> targetClass = obj.getClass();
        Field targetField = null;
        while (targetClass != null) {
            targetField = FieldUtils.getDeclaredField(targetClass, fieldName, true);
            if (targetField != null)
                break;
            targetClass = targetClass.getSuperclass();
        }
        if (targetField == null) {
            return null;
        }
        try {
            return FieldUtils.readField(targetField, obj, true);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    public static Object getField(Object obj, String fieldName) {
        Object value = null;
        try {
            Field field = obj.getClass().getField(fieldName);
            value = field.get(obj);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return value;
    }

    public static <T> Method getTargetMethodByParameterType(Class objClass, String methodName, String parameterTypeStr) {
        Method targetMethod = null;
        Class<?> targetClass = objClass;
        while (targetClass != null && targetMethod == null) {
            Method[] declaredMethods = targetClass.getDeclaredMethods();
            for (Method declaredMethod : declaredMethods) {
                if (!declaredMethod.getName().equals(methodName)) {
                    continue;
                }
                if (!Arrays.toString(declaredMethod.getParameterTypes()).equals(parameterTypeStr)) {
                    continue;
                }
                targetMethod = declaredMethod;
                break;
            }
            if (targetMethod == null) {
                targetClass = targetClass.getSuperclass();
            }
        }

        return targetMethod;
    }

    @SuppressWarnings("all")
    public static <T> Method getTargetMethod(Class objClass, String methodName, Class[] classes, T... args) {
        Class[] argclass = null;
        if (classes == null && args != null) {
            argclass = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                argclass[i] = args[i].getClass();
            }
        }
        else if(classes != null) {
            argclass = classes;
        }else{
            argclass = new Class[0];
        }
        Method targetMethod = null;
        Class<?> targetClass = objClass;
        while (targetClass != null) {
            try {
                targetMethod = targetClass.getDeclaredMethod(methodName, argclass);
                targetMethod.setAccessible(true);
                break;
            } catch (NoSuchMethodException e) {
                targetClass = targetClass.getSuperclass();
            }
        }
        return targetMethod;
    }

    public static boolean hasTargetMethod(Object obj, String methodName, Class[] classes) {
        try {
            obj.getClass().getDeclaredMethod(methodName, classes);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    @SuppressWarnings("all")
    public static <T> Object callMethod(Object obj, String methodName, T... args) {
        return callMethod(obj, methodName, null, args);
    }

    @SuppressWarnings("all")
    public static <T> Object callMethod(Object obj, String methodName, Class[] classes, T... args) {
        Object result = null;
        if (obj == null) {
            return null;
        }
        Method method = getTargetMethod(obj.getClass(), methodName, classes, args);
        if (method == null) {
            return null;
        }
        try {
            result = method.invoke(obj, args);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static <T> Object callDeclaredMethod(Object obj, String methodName, T... args) {
        return callDeclaredMethod(obj, methodName, null, args);
    }

    public static <T> Object callDeclaredMethod(Object obj, String methodName, Class[] classes, T... args) {
        return callMethod(obj, methodName, classes, args);
    }

    public static <T> Object callStaticMethod(Class clz, String methodName, T... args) {
        return callStaticMethod(clz, methodName, null, args);
    }

    public static <T> Object callStaticMethod(Class clz, String methodName, Class[] classes, T... args) {
        Object result = null;
        Method method = getTargetMethod(clz, methodName, classes, args);
        if (method == null) {
            return null;
        }
        if ((method.getModifiers() & Modifier.STATIC) == 0) {
            return null;
        }

        try {
            method.setAccessible(true);
            result = method.invoke(null, args);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String getMethodInfoFromMethod(Method method) {
        return String.format("%s|%s|%s", method.getDeclaringClass().getName(), method.getName(), Arrays.toString(method.getParameterTypes()));
    }

    public static boolean isInstanceOf(Object obj, String className) {
        boolean result = false;
        try {
            ClassLoader loader = obj.getClass().getClassLoader();
            Class clazz = loader.loadClass(className);
            result = clazz.isInstance(obj);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean isTargetSubclassFromObj(Object obj, String className) {
        Class objClass = obj.getClass();
        while (objClass != null) {
            if (objClass.getClass().getName().equals(className)) {
                return true;
            }
            Class[] interfaces = objClass.getInterfaces();
            if (interfaces != null) {
                for (Class i : interfaces) {
                    if (i.getName().equals(className)) {
                        return true;
                    }
                }
            }
            objClass = objClass.getSuperclass();
        }
        return false;
    }

    public static String getClassAnnotations(Class filterClass) {
        StringBuilder filterDesc = new StringBuilder();
        if (filterClass != null) {
            Annotation[] annotations = filterClass.getAnnotations();
            if (annotations != null && annotations.length > 0) {
                filterDesc.append("Annotation : " + Arrays.toString(annotations));
            }
        }
        return filterDesc.toString();
    }

    public static String getMethodAnnotations(Method targetMethod) {
        Annotation[] annotations = targetMethod.getAnnotations();
        if (annotations == null || annotations.length == 0) {
            return "";
        }
        return Arrays.toString(annotations);
    }
}
