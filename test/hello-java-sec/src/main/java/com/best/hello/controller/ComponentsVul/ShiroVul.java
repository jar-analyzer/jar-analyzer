package com.best.hello.controller.ComponentsVul;

import com.alibaba.fastjson.JSON;
import org.apache.shiro.codec.Base64;
import io.swagger.annotations.Api;
import org.apache.shiro.crypto.AesCipherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.web.util.WebUtils.getCookie;

@Api("Shiro <1.2.5 默认密钥致反序列化")
@RestController
@RequestMapping("/vulnapi/shiro")
public class ShiroVul {
    private static final Logger log = LoggerFactory.getLogger(ShiroVul.class);
    private static final String shirokey = "kPH+bIxk5D2deZiIxcaaaA==";
    private static final byte[] DEFAULT_CIPHER_KEY_BYTES = Base64.decode(shirokey);
    private final AesCipherService acs = new AesCipherService();

    private Cookie createDeleteCookie(String name) {
        Cookie cookie = new Cookie(name, "deleteMe");
        // 设置过期时间为0，立即删除
        cookie.setMaxAge(0);
        return cookie;
    }

    @GetMapping("/vul")
    public String vul(HttpServletRequest request, HttpServletResponse response) {
        /*
         处理cookie的流程：得到rememberMe的cookie值 -> Base64解码 –> AES解密 –> readObject反序列化
         */
        Map<String, String> m = new HashMap<>();
        log.info("[vul] Shiro <1.2.5 默认密钥致反序列化");
        Cookie cookie = getCookie(request, "rememberMe");
        if (cookie == null) {
            m.put("message", "Need rememberMe Cookie");
            return JSON.toJSONString(m);
        }

        try {
            String rememberMe = cookie.getValue();
            byte[] b64DecodeRememberMe = Base64.decode(rememberMe);
            byte[] aesDecrypt = acs.decrypt(b64DecodeRememberMe, DEFAULT_CIPHER_KEY_BYTES).getBytes();
            ByteArrayInputStream bytes = new ByteArrayInputStream(aesDecrypt);
            ObjectInputStream in = new ObjectInputStream(bytes);
            in.readObject();
            in.close();
        } catch (Exception e) {
            response.addCookie(createDeleteCookie("rememberMe"));
            m.put("message", "Shiro decrypt error");
            return JSON.toJSONString(m);
        }
        m.put("message", "Shiro550 deserialize success");
        m.put("flag", "4412b13458511405e1ac81b287f35fbb");
        return JSON.toJSONString(m);
    }

    /**
     * 使用官方生成的方法提供密钥
     */
    @GetMapping("/genkey")
    public String genkey() {
        KeyGenerator keygen = null;
        try {
            keygen = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        SecretKey deskey = keygen.generateKey();
        return Base64.encodeToString(deskey.getEncoded());
    }

}
