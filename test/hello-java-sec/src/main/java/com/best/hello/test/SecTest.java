package com.best.hello.test;

import com.best.hello.util.Security;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SecTest {
    @Before
    public void init() {
        System.out.println("start test...");
    }

    @Test
    public void testXss() {
        String result =  Security.filterXss("<");
        Assert.assertEquals("&lt;", result);
    }

    @Test
    public void testHttp() {
        boolean result = Security.isHttp("http://www.baidu.com");
        Assert.assertTrue(result);
    }

    @Test
    public void testSql() {
        boolean result = Security.checkSql("fffselectafwe");
        Assert.assertTrue(result);
    }

    @Test
    public void testWhite() {
        boolean result = Security.isWhite("https://oa.baidu.com/index");
        Assert.assertTrue(result);
    }
}
