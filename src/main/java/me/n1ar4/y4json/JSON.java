package me.n1ar4.y4json;

@SuppressWarnings("unused")
public class JSON implements JSONConst {
    /**
     * 序列化对象到 JSON 字符串
     *
     * @param object 对象
     * @return 字符串
     */
    public static String toJSONString(Object object) {
        StringBuilder sb = new StringBuilder();
        CoreWriter.deepParse(object, sb);
        return sb.toString();
    }

    /**
     * 反序列化 JSON 到 Map
     *
     * @param text 字符串
     * @return JSONObject
     */
    public static JSONObject parseObject(String text) {
        CoreReader reader = new CoreReader(text);
        return reader.readJSON();
    }

    public static JSONArray parseArray(String text) {
        CoreReader reader = new CoreReader(text);
        return reader.readJsonArray();
    }
}
