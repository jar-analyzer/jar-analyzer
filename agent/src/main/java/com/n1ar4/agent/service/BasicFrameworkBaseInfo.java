package com.n1ar4.agent.service;

import com.n1ar4.agent.dto.UrlInfo;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BasicFrameworkBaseInfo {
    private static class TrustAnyTrustManager implements X509TrustManager{

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        }
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
    private static class TrustAnyHostnameVerifier implements HostnameVerifier{
        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    }
    public abstract Object getInstance();
    public abstract ArrayList<UrlInfo> getUrlInfos();
    public abstract String getRequestUrl();
    public boolean SendRequest(){
        String req = getRequestUrl();
        if(req == null){return false;}
        if(req.endsWith("/")){
            req += "a";
        }
        String host = "127.0.0.1";
        Matcher matcher = Pattern.compile("//.*:").matcher(req);
        if(matcher.find()){
            String matched = matcher.group(0);
            if(!matched.equals("127.0.0.1")){
                host = matched.substring(2, matched.length()-1);
                host = host.equals("0.0.0.0")?"127.0.0.1":host;
                req = req.replaceAll(matched, "//127.0.0.1:");
            }
        }

        if(req.startsWith("https")){
            try {
                SSLContext ssl = SSLContext.getInstance("SSL");
                ssl.init(null, new TrustManager[]{new TrustAnyTrustManager()}, new java.security.SecureRandom());
                URL url = new URL(req);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection.setRequestProperty("Host", host);
                connection.setSSLSocketFactory(ssl.getSocketFactory());
                connection.setHostnameVerifier(new TrustAnyHostnameVerifier());
                connection.setDoOutput(true);
                connection.getResponseCode();
            }catch (Exception e){

                return false;
            }
        }else {
            try {
                URL url = new URL(req);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection.setRequestProperty("Host", host);
                connection.setDoOutput(true);
                connection.setRequestMethod("GET");
                connection.getResponseCode();
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }
}
