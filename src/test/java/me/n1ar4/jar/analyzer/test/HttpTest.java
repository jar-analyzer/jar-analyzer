package me.n1ar4.jar.analyzer.test;

import me.n1ar4.jar.analyzer.utils.http.Http;
import me.n1ar4.jar.analyzer.utils.http.HttpResponse;

public class HttpTest {
    public static void main(String[] args) {
        HttpResponse resp = Http.doGet("https://www.baidu.com");
        System.out.println(resp);
    }
}
