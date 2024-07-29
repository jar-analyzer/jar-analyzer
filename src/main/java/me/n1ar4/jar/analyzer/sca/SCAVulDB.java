package me.n1ar4.jar.analyzer.sca;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import me.n1ar4.jar.analyzer.sca.dto.CVEData;
import me.n1ar4.jar.analyzer.utils.IOUtil;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SCAVulDB {
    private static final Logger logger = LogManager.getLogger();

    public static Map<String, CVEData> getCVEMap() {
        String path = "SCA/cve-database.json";
        InputStream is = SCAParser.class.getClassLoader().getResourceAsStream(path);
        String data = IOUtil.readString(is);
        JSONArray array = JSONArray.parse(data);
        if (array == null || array.isEmpty()) {
            return null;
        }
        Map<String, CVEData> cveMap = new HashMap<>();
        for (int i = 0; i < array.size(); i++) {
            JSONObject object = array.getJSONObject(i);
            String cve = (String) object.get("CVE");
            float cvss = object.getFloat("CVSS");
            String desc = (String) object.get("DESC");
            if (cve == null || cve.trim().isEmpty()) {
                continue;
            }
            CVEData cveData = new CVEData();
            cveData.setCve(cve);
            cveData.setCvss(cvss);
            cveData.setDesc(desc);
            cveMap.put(cve, cveData);
        }
        if (!cveMap.isEmpty()) {
            logger.info("cve data: {}", cveMap.size());
        }
        return cveMap;
    }
}
