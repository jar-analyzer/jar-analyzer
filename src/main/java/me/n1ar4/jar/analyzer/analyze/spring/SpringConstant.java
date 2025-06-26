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
    String ControllerAnno = "Lorg/springframework/stereotype/Controller;";
    String RestControllerAnno = ANNO_PREFIX + "RestController;";
    String RequestMappingAnno = ANNO_PREFIX + "RequestMapping;";
    String RequestParamAnno = ANNO_PREFIX + "RequestParam;";
    String MappingAnno = "Mapping";
}
