/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui.util;

import com.formdev.flatlaf.extras.FlatSVGIcon;

public class SvgManager {
    public static FlatSVGIcon TomcatIcon = new FlatSVGIcon("svg/tomcat.svg", 16, 16);
    public static FlatSVGIcon AdvanceIcon = new FlatSVGIcon("svg/advance.svg", 16, 16);
    public static FlatSVGIcon ConnectIcon = new FlatSVGIcon("svg/connect.svg", 16, 16);
    public static FlatSVGIcon InheritIcon = new FlatSVGIcon("svg/inherit.svg", 16, 16);
    public static FlatSVGIcon LeakIcon = new FlatSVGIcon("svg/leak.svg", 16, 16);
    public static FlatSVGIcon NoteIcon = new FlatSVGIcon("svg/note.svg", 16, 16);
    public static FlatSVGIcon ScaIcon = new FlatSVGIcon("svg/sca.svg", 16, 16);
    public static FlatSVGIcon SearchIcon = new FlatSVGIcon("svg/search.svg", 16, 16);
    public static FlatSVGIcon StartIcon = new FlatSVGIcon("svg/start.svg", 16, 16);
    public static FlatSVGIcon SpringIcon = new FlatSVGIcon("svg/spring.svg", 16, 16);
    public static FlatSVGIcon GadgetIcon = new FlatSVGIcon("svg/gadget.svg", 16, 16);
    public static FlatSVGIcon DirIcon = new FlatSVGIcon("svg/dir.svg", 16, 16);
    public static FlatSVGIcon DogIcon = new FlatSVGIcon("svg/dog.svg", 16, 16);

    // ---- file-tree class kind icons -------------------------------------
    // One badge per semantic category of *.class entries in the tree, so
    // interface / abstract / enum / annotation / record / exception
    // classes can be told apart at a glance. See ClassKindResolver.
    public static FlatSVGIcon ClassIcon = new FlatSVGIcon("svg/class.svg", 16, 16);
    public static FlatSVGIcon AbstractClassIcon = new FlatSVGIcon("svg/abstractClass.svg", 16, 16);
    public static FlatSVGIcon InterfaceIcon = new FlatSVGIcon("svg/interface.svg", 16, 16);
    public static FlatSVGIcon AnnotationIcon = new FlatSVGIcon("svg/annotation.svg", 16, 16);
    public static FlatSVGIcon EnumIcon = new FlatSVGIcon("svg/enum.svg", 16, 16);
    public static FlatSVGIcon RecordIcon = new FlatSVGIcon("svg/record.svg", 16, 16);
    public static FlatSVGIcon ExceptionIcon = new FlatSVGIcon("svg/exception.svg", 16, 16);

    // ---- file-tree resource icons --------------------------------------
    // Per-extension icons for non-class entries (config / web / scripts /
    // archives / binary assets). The default fallback when no extension
    // rule matches is FileIcon.
    public static FlatSVGIcon FileIcon = new FlatSVGIcon("svg/file.svg", 16, 16);
    public static FlatSVGIcon XmlIcon = new FlatSVGIcon("svg/xml.svg", 16, 16);
    public static FlatSVGIcon YamlIcon = new FlatSVGIcon("svg/yaml.svg", 16, 16);
    public static FlatSVGIcon PropertiesIcon = new FlatSVGIcon("svg/properties.svg", 16, 16);
    public static FlatSVGIcon JsonIcon = new FlatSVGIcon("svg/json.svg", 16, 16);
    public static FlatSVGIcon ManifestIcon = new FlatSVGIcon("svg/manifest.svg", 16, 16);
    public static FlatSVGIcon TextIcon = new FlatSVGIcon("svg/text.svg", 16, 16);
    public static FlatSVGIcon HtmlIcon = new FlatSVGIcon("svg/html.svg", 16, 16);
    public static FlatSVGIcon CssIcon = new FlatSVGIcon("svg/css.svg", 16, 16);
    public static FlatSVGIcon JsIcon = new FlatSVGIcon("svg/js.svg", 16, 16);
    public static FlatSVGIcon SqlIcon = new FlatSVGIcon("svg/sql.svg", 16, 16);
    public static FlatSVGIcon ShellIcon = new FlatSVGIcon("svg/shell.svg", 16, 16);
    public static FlatSVGIcon ImageFileIcon = new FlatSVGIcon("svg/image.svg", 16, 16);
    public static FlatSVGIcon FontIcon = new FlatSVGIcon("svg/font.svg", 16, 16);
    public static FlatSVGIcon ArchiveIcon = new FlatSVGIcon("svg/archive.svg", 16, 16);
    public static FlatSVGIcon NestedJarIcon = new FlatSVGIcon("svg/nestedJar.svg", 16, 16);
    public static FlatSVGIcon NativeIcon = new FlatSVGIcon("svg/native.svg", 16, 16);
    public static FlatSVGIcon JavaSourceIcon = new FlatSVGIcon("svg/javaSource.svg", 16, 16);
}
