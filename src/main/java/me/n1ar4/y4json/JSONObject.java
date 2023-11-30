package me.n1ar4.y4json;

import me.n1ar4.y4json.token.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class JSONObject extends HashMap<String, Object> {
    private static final RuntimeException exp = new RuntimeException("invalid json object value");

    @Override
    public Object get(Object key) {
        Object val = super.get(key);
        if (val instanceof StringToken) {
            return ((StringToken) val).getValue();
        } else if (val instanceof NumberToken) {
            NumberToken num = (NumberToken) val;
            if (num.getType() == int.class) {
                return num.getIntValue();
            } else if (num.getType() == double.class) {
                return num.getDoubleValue();
            } else if (num.getType() == long.class) {
                return num.getLongValue();
            } else {
                throw exp;
            }
        } else if (val instanceof TrueToken) {
            return ((TrueToken) val).getValue();
        } else if (val instanceof FalseToken) {
            return ((FalseToken) val).getValue();
        } else if (val instanceof NullToken) {
            return ((NullToken) val).getValue();
        } else {
            if (val instanceof JSONObject) {
                return val;
            } else if (val instanceof JSONArray) {
                return val;
            } else {
                throw exp;
            }
        }
    }

    @Override
    public String toString() {
        Iterator<Entry<String, Object>> iterator = this.entrySet().iterator();
        if (!iterator.hasNext()) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        while (true) {
            Map.Entry<String, Object> entry = iterator.next();
            String key = entry.getKey();
            Object value = this.get(key);
            sb.append(key);
            sb.append('=');
            sb.append(value);
            if (!iterator.hasNext()) {
                return sb.append('}').toString();
            }
            sb.append(',').append(' ');
        }
    }
}
