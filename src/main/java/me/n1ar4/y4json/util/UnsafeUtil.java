package me.n1ar4.y4json.util;

import me.n1ar4.y4json.log.LogManager;
import me.n1ar4.y4json.log.Logger;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Unsafe 核心类
 * <p>
 * 为什么要使用 Unsafe 做反射：快速
 * </p>
 */
public class UnsafeUtil {
    private static final Logger logger = LogManager.getLogger();
    private static final Unsafe unsafe = getUnsafe();

    /**
     * 获得 Unsafe 对象
     *
     * @return 对象
     */
    private static Unsafe getUnsafe() {
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            return (Unsafe) unsafeField.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 不使用 Constructor 创建对象
     *
     * @param clazz 类型
     * @param <T>   泛型
     * @return 对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T newObject(Class<T> clazz) {
        try {
            return (T) unsafe.allocateInstance(clazz);
        } catch (Exception ex) {
            logger.error("unsafe error: " + ex);
            return null;
        }
    }

    /**
     * 获取任何一个 Object 的 Field 内容
     *
     * @param object 对象
     * @param field  字段
     * @param <T>    类型
     * @return 值
     */
    @SuppressWarnings("all")
    public static <T> T getFieldValue(Object object,
                                      Field field) {
        Class<?> fieldType = field.getType();
        long offset;
        if (Modifier.isStatic(field.getModifiers())) {
            offset = unsafe.staticFieldOffset(field);
            object = unsafe.staticFieldBase(field);
        } else {
            offset = unsafe.objectFieldOffset(field);
        }
        if (fieldType.equals(int.class)) {
            return (T) Integer.valueOf(unsafe.getInt(object, offset));
        } else if (fieldType.equals(long.class)) {
            return (T) Long.valueOf(unsafe.getLong(object, offset));
        } else if (fieldType.equals(byte.class)) {
            return (T) Byte.valueOf(unsafe.getByte(object, offset));
        } else if (fieldType.equals(short.class)) {
            return (T) Short.valueOf(unsafe.getShort(object, offset));
        } else if (fieldType.equals(char.class)) {
            return (T) Character.valueOf(unsafe.getChar(object, offset));
        } else if (fieldType.equals(float.class)) {
            return (T) Float.valueOf(unsafe.getFloat(object, offset));
        } else if (fieldType.equals(double.class)) {
            return (T) Double.valueOf(unsafe.getDouble(object, offset));
        } else if (fieldType.equals(boolean.class)) {
            return (T) Boolean.valueOf(unsafe.getBoolean(object, offset));
        } else {
            return (T) unsafe.getObject(object, offset);
        }
    }

    /**
     * 设置 Object 的 Field
     *
     * @param obj   对象
     * @param field 字段
     * @param val   内容
     */
    @SuppressWarnings("all")
    public static void setField(Object obj, Field field, Object val) {
        Class<?> fieldType = field.getType();
        long fieldOffset;
        if (Modifier.isStatic(field.getModifiers())) {
            fieldOffset = unsafe.staticFieldOffset(field);
            obj = unsafe.staticFieldBase(field);
        } else {
            fieldOffset = unsafe.objectFieldOffset(field);
        }
        if (fieldType.equals(int.class)) {
            unsafe.putInt(obj, fieldOffset, (Integer) val);
        } else if (fieldType.equals(long.class)) {
            long longVal = ((Number)val).longValue();
            unsafe.putLong(obj, fieldOffset, longVal);
        } else if (fieldType.equals(byte.class)) {
            unsafe.putByte(obj, fieldOffset, (Byte) val);
        } else if (fieldType.equals(short.class)) {
            unsafe.putShort(obj, fieldOffset, (Short) val);
        } else if (fieldType.equals(char.class)) {
            unsafe.putChar(obj, fieldOffset, (Character) val);
        } else if (fieldType.equals(float.class)) {
            unsafe.putFloat(obj, fieldOffset, (Float) val);
        } else if (fieldType.equals(double.class)) {
            unsafe.putDouble(obj, fieldOffset, (Double) val);
        } else if (fieldType.equals(boolean.class)) {
            unsafe.putBoolean(obj, fieldOffset, (Boolean) val);
        } else {
            unsafe.putObject(obj, fieldOffset, val);
        }
    }
}
