/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.analyze.spring;

public interface SpringConstant {
    String SBApplication = "Lorg/springframework/boot/autoconfigure/SpringBootApplication;";
    String ControllerAnno = "Lorg/springframework/stereotype/Controller;";
    String RestControllerAnno = "Lorg/springframework/web/bind/annotation/RestController;";
    String RequestMappingAnno = "Lorg/springframework/web/bind/annotation/RequestMapping;";
    String GetMappingAnno = "Lorg/springframework/web/bind/annotation/GetMapping;";
    String PostMappingAnno = "Lorg/springframework/web/bind/annotation/PostMapping;";
    String ResponseBodyAnno = "Lorg/springframework/web/bind/annotation/ResponseBody;";
    String RequestParamAnno = "Lorg/springframework/web/bind/annotation/RequestParam;";
}
