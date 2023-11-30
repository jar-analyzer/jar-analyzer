package me.n1ar4.jar.analyzer.test;


import me.n1ar4.http.HttpResponse;
import me.n1ar4.http.Y4Client;

public class HttpTest {
    public static void main(String[] args) {
        HttpResponse resp = new Y4Client().get("https://www.baidu.com");
        System.out.println(new String(resp.getBody()));
    }
}
