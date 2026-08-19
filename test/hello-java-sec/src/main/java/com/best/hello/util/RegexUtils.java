package com.best.hello.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils {
    /**
     * 手机号正则表达式
     */
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^(1[3|4|5|6|7|8|9][0-9])\\d{8}$");

    /**
     * 电信手机号正则表达式
     */
    private static final Pattern ELECOM_MOBILE_PATTERN = Pattern.compile("(133|1349|149|153|1700|1701|1702|173|1740|177|180|181|189|191|199)\\d{8}$");

    /**
     * 联通手机号正则表达式
     */
    private static final Pattern UNICON_MOBILE_PATTERN = Pattern.compile("(130|131|132|145|146|155|156|166|167|1704|1707|1708|1709|171|175|176|185|186)\\d{8}$");

    /**
     * 移动手机号正则表达式
     */
    private static final Pattern CHINA_MOBILE_PATTERN = Pattern.compile("(1340|1341|1342|1343|1344|1345|1346|1347|1348|135|136|137|138|139|147|148|150|151|152" +
            "|154|157|158|159|165|1703|1705|1706|172|178|182|183|184|187|188|198)\\d{8}$");

    /**
     * 国外手机号正则表达式
     */
    private static final Pattern FOREIGN_MOBILE_PATTERN = Pattern.compile("^(([0|00|\\+]\\d{1,4})-?(\\d{0,3})-?)(\\d{6,11})$");

    /**
     * 邮件正则表达式
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^([a-zA-Z0-9_\\.\\-])+\\@(([a-zA-Z0-9\\-])+\\.)+([a-zA-Z0-9]{2,4})+$");



    public static void main(String[] args) {
        Matcher phone = MOBILE_PATTERN.matcher("15898989898");
        Matcher mail = EMAIL_PATTERN.matcher("222.com");

        System.out.println(phone.matches());
        System.out.println(mail.matches());
    }
}
