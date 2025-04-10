/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.analyze.spring;

public interface SpringConstant {
    String ANNO_PREFIX = "Lorg/springframework/web/bind/annotation/";
    String SBApplication = "Lorg/springframework/boot/autoconfigure/SpringBootApplication;";
    String ControllerAnno = "Lorg/springframework/stereotype/Controller;";
    String RestControllerAnno = ANNO_PREFIX + "RestController;";
    String RequestMappingAnno = ANNO_PREFIX + "RequestMapping;";
    String GetMappingAnno = ANNO_PREFIX + "GetMapping;";
    String PostMappingAnno = ANNO_PREFIX + "PostMapping;";
    String ResponseBodyAnno = ANNO_PREFIX + "ResponseBody;";
    String RequestParamAnno = ANNO_PREFIX + "RequestParam;";
    String MappingAnno = "Mapping;";
}
