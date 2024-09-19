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

package com.n1ar4.agent.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectUtils {
    public static void set(Field field, Object value, Object target)
            throws IllegalArgumentException, IllegalAccessException {
        final boolean isAccessible = field.isAccessible();
        try {
            field.setAccessible(true);
            field.set(target, value);
        } finally {
            field.setAccessible(isAccessible);
        }
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

    @SuppressWarnings("all")
    public static <T> Method getTargetMethod(Class objClass, String methodName, Class[] classes, T... args) {
        Class[] argclass = null;
        if (classes == null) {
            argclass = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                argclass[i] = args[i].getClass();
            }
        } else {
            argclass = classes;
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
}
