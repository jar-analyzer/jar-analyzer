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
