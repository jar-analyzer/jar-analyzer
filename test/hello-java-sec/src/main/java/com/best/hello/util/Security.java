package com.best.hello.util;

import org.apache.http.conn.util.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 存放安全类
 *
 * @date 2021/08/02
 */
public class Security {
    static Logger log = LoggerFactory.getLogger(Security.class);

    /**
     * 判断是否为内网IP
     *
     * @return True or False
     */
    public static boolean isIntranet(String ip) {
        log.info("isIntranet: " + ip);
        Pattern reg = Pattern.compile("^(127\\.0\\.0\\.1)|(localhost)|^(10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})|^(172\\.((1[6-9])|(2\\d)|(3[01]))\\.\\d{1,3}\\.\\d{1,3})|^(192\\.168\\.\\d{1,3}\\.\\d{1,3})$");
        Matcher match = reg.matcher(ip);
        return match.find();
    }

    /**
     * 判断是否是http类型
     *
     * @return True or False
     */
    public static boolean isHttp(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }

    /**
     * 判断url是否在白名单内
     */
    public static boolean isWhite(String url) {
        List<String> url_list = new ArrayList<String>();
        url_list.add("baidu.com");
        url_list.add("www.baidu.com");
        url_list.add("oa.baidu.com");

        // 从url转换host
        URI uri = null;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            System.out.print(e);
        }
        assert uri != null;
        String host = uri.getHost().toLowerCase();

        return url_list.contains(host);
    }

    /**
     * url转IP
     */
    public static String urltoIp(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost().toLowerCase();
            // 判断 URL 是否是 IP 地址
            if (InetAddressUtils.isIPv4Address(host)) {
                return host;
            } else {
                InetAddress ip = Inet4Address.getByName(host);
                return ip.getHostAddress();
            }
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }

    /**
     * XSS黑名单过滤
     */
    public static String filterXss(String content) {
        content = StringUtils.replace(content, "&", "&amp;");
        content = StringUtils.replace(content, "<", "&lt;");
        content = StringUtils.replace(content, ">", "&gt;");
        content = StringUtils.replace(content, "\"", "&quot;");
        content = StringUtils.replace(content, "'", "&#x27;");
        content = StringUtils.replace(content, "/", "&#x2F;");
        return content;
    }

    public static String filterXss2(String content) {
        String[] black_list = {"&", "<", ">", "\"", "'", "/"};
        String[] white_list = {"&amp;", "&lt;", "&gt;", "&quot;", "&#x27;", "&#x2F;"};
        // 遍历非法字符集合，并使用replace 方法替换
        for (int i = 0; i < black_list.length; i++) {
            content = content.replace(black_list[i], white_list[i]);
        }
        return content;
    }

    /**
     * SQL注入检测
     */
    public static boolean checkSql(String content) {
        String[] black_list = {"'", ";", "--", "+", ",", "%", "=", ">", "<", "*", "(", ")", "and", "or", "exec", "insert", "select", "delete", "update", "count", "drop", "chr", "mid", "master", "truncate", "char", "declare"};
        for (String s : black_list) {
            if (content.toLowerCase().contains(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 白名单正则字符串过滤
     */
    public static boolean isValidOrder(String content) {
        return content.matches("[0-9a-zA-Z+_]+");
    }

    public static boolean isValidSort(String sort) {
        return "desc".equalsIgnoreCase(sort) || "asc".equalsIgnoreCase(sort);
    }

    /**
     * 目录遍历检测
     */
    public static boolean checkTraversal(String content) {
        return content.contains("..") || content.contains("/");
    }

    /**
     * 命令执行检测
     */
    public static boolean checkOs(String content) {
        String[] black_list = {"|", ",", "&", "&&", ";", "||"};
        for (String s : black_list) {
            if (content.contains(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * XXE检测
     */
    public static boolean checkXXE(String content) {
        String[] black_list = {"ENTITY", "DOCTYPE"};
        for (String s : black_list) {
            if (content.toUpperCase().contains(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 文件上传检测
     */
    public static boolean isValidFileType(String fileName) {
        String[] allowedTypes = {"jpg", "jpeg", "png", "gif", "bmp", "ico"};
        String extension = StringUtils.getFilenameExtension(fileName);
        if (extension != null) {
            for (String allowedType : allowedTypes) {
                if (allowedType.equalsIgnoreCase(extension)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * RCE黑名单正则表达式检测
     */
    public static boolean checkRCE(String content) {
        String[] black_list = {"java.+lang", "Runtime", "getRuntime", "ProcessBuilder", "exec.*\\("};

        for (String regex : black_list) {
            if (Pattern.compile(regex).matcher(content).find()) {
                return true;
            }
        }
        return false;
    }

}