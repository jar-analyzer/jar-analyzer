package me.n1ar4.y4json;

import me.n1ar4.y4json.log.LogManager;
import me.n1ar4.y4json.log.Logger;
import me.n1ar4.y4json.util.UnsafeUtil;
import me.n1ar4.y4json.util.QuickSort;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

public class CoreWriter implements JSONConst {
    private static final Logger logger = LogManager.getLogger();

    private static void writeInternal(Object object,
                                      StringBuilder sb,
                                      String typeName,
                                      Object nextObject) {
        switch (typeName) {
            case STRING_TYPE:
                sb.append(quotationMark);
                String s = (String) object;
                // 参考规范：JSON 中应该转义以下内容
                s = s.replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\b", "\\b")
                        .replace("\f", "\\f")
                        .replace("\n", "\\n")
                        .replace("\r", "\\r")
                        .replace("\t", "\\t");
                sb.append(s);
                sb.append(quotationMark);
                break;
            case DOUBLE_TYPE:
                sb.append((double) object);
                break;
            case FLOAT_TYPE:
                sb.append((float) object);
                break;
            case BYTE_TYPE:
                sb.append((byte) object);
                break;
            case SHORT_TYPE:
                sb.append((short) object);
                break;
            case LONG_TYPE:
                long val = (long) object;
                if (val < Integer.MAX_VALUE) {
                    sb.append((int) val);
                } else {
                    sb.append(quotationMark);
                    sb.append(val);
                    sb.append(quotationMark);
                }
                break;
            case CHAR_TYPE:
                sb.append((char) object);
                break;
            case BOOL_TYPE:
                boolean boolVal = (boolean) object;
                sb.append(boolVal ? "true" : "false");
                break;
            case INT_TYPE:
                sb.append((int) object);
                break;
            default:
                if (nextObject != null) {
                    deepParse(nextObject, sb);
                } else {
                    sb.append("null");
                }
                break;
        }
    }


    /**
     * 解析 Object 到 JSON 字符串
     *
     * @param object 对象
     * @param sb     StringBuilder
     */
    static void deepParse(Object object, StringBuilder sb) {
        // 数组类型
        if (object.getClass().isArray()) {
            // 数组：开始
            sb.append(arrayLeft);
            int length = Array.getLength(object);
            // 遍历写入
            for (int i = 0; i < length; i++) {
                Object element = Array.get(object, i);
                if (element == null) {
                    continue;
                }
                String typeName = element.getClass().getName();
                // 处理value内部
                writeInternal(element, sb, typeName, element);
                // 非最后一个元素应添加逗号
                if (i != length - 1) {
                    sb.append(comma);
                }
            }
            // 数组：结束
            sb.append(arrayRight);
        } else {
            // 一般情况：开始
            sb.append(prefix);
            Field[] fields = object.getClass().getDeclaredFields();
            // 对象的属性按照字母序进行序列化写入
            QuickSort.quickSort(fields, 0, fields.length - 1);
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                // key一定是字符串
                sb.append(quotationMark);
                String fieldName = field.getName();
                sb.append(fieldName);
                sb.append(quotationMark);
                // 分隔符
                sb.append(colon);
                // value可能是多种类型
                String typeName = field.getType().getName();
                // 尝试反射获取值
                Object val = null;
                try {
                    val = UnsafeUtil.getFieldValue(object, field);
                } catch (Exception ex) {
                    logger.error("reflect get val error: " + ex);
                    // 显示错误
                    sb.append(quotationMark);
                    sb.append("json parse error");
                    sb.append(quotationMark);
                }
                // 处理value内部
                writeInternal(val, sb, typeName, val);
                // 非最后一个元素应添加逗号
                if (i != fields.length - 1) {
                    sb.append(comma);
                }
            }
            //一般情况：结束
            sb.append(suffix);
        }
    }
}
