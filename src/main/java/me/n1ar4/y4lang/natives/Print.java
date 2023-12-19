package me.n1ar4.y4lang.natives;

import me.n1ar4.y4lang.env.Environment;

public class Print {
    public static int print(Object obj) {
        if (obj == null) {
            System.out.println("null");
        }
        if (obj instanceof byte[]) {
            byte[] data = (byte[]) obj;
            StringBuilder sb = new StringBuilder();
            sb.append("bytes [");
            Byte[] newData = new Byte[data.length];
            for (int i = 0; i < data.length; i++) {
                newData[i] = data[i];
            }
            buildOutput(sb, newData);
            System.out.println(sb);
        } else if (obj instanceof Object[]) {
            Object[] data = (Object[]) obj;
            StringBuilder sb = new StringBuilder();
            sb.append("array [");
            buildOutput(sb, data);
            System.out.println(sb);
        } else {
            System.out.println(obj);
        }
        return Environment.TRUE;
    }

    private static void buildOutput(StringBuilder sb, Object[] data) {
        int length = data.length;
        for (int i = 0; i < length; i++) {
            sb.append(data[i]);
            if (i != length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
    }
}
