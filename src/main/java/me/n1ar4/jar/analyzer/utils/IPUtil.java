package me.n1ar4.jar.analyzer.utils;

import java.util.regex.Pattern;

public class IPUtil {
    private static final String IPV4_PATTERN =
            "^(25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})\\." +
                    "(25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})\\." +
                    "(25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})\\." +
                    "(25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})$";
    private static final String IPV6_PATTERN =
            "(([0-9a-fA-F]{1,4}:){7}([0-9a-fA-F]{1,4}|:))|" +
                    "(([0-9a-fA-F]{1,4}:){1,7}:)|" +
                    "(([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4})|" +
                    "(([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2})|" +
                    "(([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3})|" +
                    "(([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4})|" +
                    "(([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5})|" +
                    "([0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6}))|" +
                    "(::([0-9a-fA-F]{1,4}:){0,5}([0-9a-fA-F]{1,4}))|" +
                    "(::([0-9a-fA-F]{1,4}:){0,6}[0-9a-fA-F]{1,4})";
    private static final Pattern IPv4_PATTERN_COMPILED = Pattern.compile(IPV4_PATTERN);
    private static final Pattern IPv6_PATTERN_COMPILED = Pattern.compile(IPV6_PATTERN);

    public static boolean isValidIPAddress(String ipAddress) {
        return isValidIPv4(ipAddress) || isValidIPv6(ipAddress);
    }

    private static boolean isValidIPv4(String ipAddress) {
        return IPv4_PATTERN_COMPILED.matcher(ipAddress).matches();
    }

    private static boolean isValidIPv6(String ipAddress) {
        return IPv6_PATTERN_COMPILED.matcher(ipAddress).matches();
    }
}
