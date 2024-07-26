package me.n1ar4.jar.analyzer.sca;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import me.n1ar4.jar.analyzer.utils.IOUtil;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.io.InputStream;
import java.util.*;

public class SCAParser {
    private static final Logger logger = LogManager.getLogger();

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
            if (cveList == null || cveList.trim().isEmpty()) {
                continue;
            }
            String hash = object.getString("JndiLookupHash");
            buildOneRuleVersion(project, keyClass, result, object, cveList, hash);
        }
        if (!result.isEmpty()) {
            logger.info("apache log4j2 sca rules: {}", result.size());
        }
        return result;
    }

    public static List<SCARule> getFastjsonRules() {
        String path = "SCA/fastjson-rule.json";
        InputStream is = SCAParser.class.getClassLoader().getResourceAsStream(path);
        String data = IOUtil.readString(is);
        JSONArray array = JSONArray.parse(data);
        if (array == null || array.isEmpty()) {
            return null;
        }
        String project = "FASTJSON";
        String keyClass = "com/alibaba/fastjson/util/TypeUtils";
        List<SCARule> result = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            JSONObject object = array.getJSONObject(i);
            String cveList = (String) object.get("CVE");
            if (cveList == null || cveList.trim().isEmpty()) {
                continue;
            }
            String hash = object.getString("TypeUtilsHash");
            buildOneRuleVersion(project, keyClass, result, object, cveList, hash);
        }
        if (!result.isEmpty()) {
            logger.info("fastjson sca rules: {}", result.size());
        }
        return result;
    }

    private static void buildOneRuleVersion(String project,
                                            String keyClass,
                                            List<SCARule> result,
                                            JSONObject object,
                                            String cveList,
                                            String hash) {
        String version = object.getString("MavenVersion");
        if (cveList.contains(",")) {
            String[] cveItems = cveList.split(",");
            for (String cve : cveItems) {
                makeSCARule(project, keyClass, result, hash, version, cve);
            }
        } else {
            makeSCARule(project, keyClass, result, hash, version, cveList);
        }
    }

    private static void makeSCARule(String project,
                                    String keyClass,
                                    List<SCARule> result,
                                    String hash,
                                    String version,
                                    String cve) {
        SCARule rule = new SCARule();
        rule.setCVE(cve);

        Map<String, String> hashMap = new HashMap<>();
        hashMap.put(keyClass, hash);
        rule.setHashMap(hashMap);

        rule.setUuid(UUID.randomUUID().toString());
        rule.setVersion(version);
        rule.setProjectName(project);
        result.add(rule);
    }
}
