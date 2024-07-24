package me.n1ar4.jar.analyzer.sca;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import me.n1ar4.jar.analyzer.utils.IOUtil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SCAParser {
    public static List<SCARule> getApacheLog4j2Rules() {
        String path = "SCA/apache-log4j2-rule.json";
        InputStream is = SCAParser.class.getClassLoader().getResourceAsStream(path);
        String data = IOUtil.readString(is);
        JSONArray array = JSONArray.parse(data);
        if (array == null || array.isEmpty()) {
            return null;
        }
        String project = "Apache Log4j2";
        String keyClass = "org/apache/logging/log4j/core/lookup/JndiLookup";
        List<SCARule> result = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            JSONObject object = array.getJSONObject(i);
            String cveList = (String) object.get("CVE");
            String hash = object.getString("JndiLookupHash");
            String version = object.getString("MavenVersion");
            if (cveList.contains(",")) {
                String[] cveItems = cveList.split(",");
                for (String cve : cveItems) {
                    SCARule rule = new SCARule();
                    rule.setCVE(cve);
                    rule.setHash(hash);
                    rule.setKeyClassName(keyClass);
                    rule.setUuid(UUID.randomUUID().toString());
                    rule.setVersion(version);
                    rule.setProjectName(project);
                    result.add(rule);
                }
            } else {
                SCARule rule = new SCARule();
                rule.setCVE(cveList);
                rule.setHash(hash);
                rule.setKeyClassName(keyClass);
                rule.setUuid(UUID.randomUUID().toString());
                rule.setVersion(version);
                rule.setProjectName(project);
                result.add(rule);
            }
        }
        return result;
    }
}
