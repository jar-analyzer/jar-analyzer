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

package me.n1ar4.jar.analyzer.sca;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import me.n1ar4.jar.analyzer.sca.dto.SCARule;
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

    public static List<SCARule> getShiroRules() {
        String path = "SCA/apache-shiro-rule.json";
        InputStream is = SCAParser.class.getClassLoader().getResourceAsStream(path);
        String data = IOUtil.readString(is);
        JSONArray array = JSONArray.parse(data);
        if (array == null || array.isEmpty()) {
            return null;
        }
        String project = "Apache Shiro";
        String keyClass1 = "org/apache/shiro/mgt/AbstractRememberMeManager";
        String keyClass2 = "org/apache/shiro/util/AntPathMatcher";
        String keyClass3 = "org/apache/shiro/config/Ini";
        List<SCARule> result = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            JSONObject object = array.getJSONObject(i);
            String cveList = (String) object.get("CVE");
            if (cveList == null || cveList.trim().isEmpty()) {
                continue;
            }
            String hash1 = object.getString("AbstractRememberMeManagerHash");
            String hash2 = object.getString("AntPathMatcherHash");
            String hash3 = object.getString("IniHash");

            Map<String, String> hashMap = new HashMap<>();
            hashMap.put(keyClass1, hash1);
            hashMap.put(keyClass2, hash2);
            hashMap.put(keyClass3, hash3);

            buildManyRulesVersion(project, result, object, cveList, hashMap);
        }
        if (!result.isEmpty()) {
            logger.info("apache shiro sca rules: {}", result.size());
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

    private static void makeSCARules(String project,
                                     List<SCARule> result,
                                     String version,
                                     String cve,
                                     Map<String, String> hashMap) {
        SCARule rule = new SCARule();
        rule.setCVE(cve);
        rule.setHashMap(hashMap);
        rule.setUuid(UUID.randomUUID().toString());
        rule.setVersion(version);
        rule.setProjectName(project);
        result.add(rule);
    }

    private static void buildManyRulesVersion(String project,
                                              List<SCARule> result,
                                              JSONObject object,
                                              String cveList,
                                              Map<String, String> hashMap) {
        String version = object.getString("MavenVersion");
        if (cveList.contains(",")) {
            String[] cveItems = cveList.split(",");
            for (String cve : cveItems) {
                makeSCARules(project, result, version, cve, hashMap);
            }
        } else {
            makeSCARules(project, result, version, cveList, hashMap);
        }
    }
}
