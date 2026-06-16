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

    // ---- EL search workbench icons -------------------------------------
    // IDEA-flavoured glyphs used by the SPEL search panel toolbar,
    // template browser and output console.
    public static FlatSVGIcon ElRunIcon = new FlatSVGIcon("svg/elRun.svg", 16, 16);
    public static FlatSVGIcon ElStopIcon = new FlatSVGIcon("svg/elStop.svg", 16, 16);
    public static FlatSVGIcon ElCheckIcon = new FlatSVGIcon("svg/elCheck.svg", 16, 16);
    public static FlatSVGIcon ElExportIcon = new FlatSVGIcon("svg/elExport.svg", 16, 16);
    public static FlatSVGIcon ElFormatIcon = new FlatSVGIcon("svg/elFormat.svg", 16, 16);
    public static FlatSVGIcon ElClearIcon = new FlatSVGIcon("svg/elClear.svg", 16, 16);
    public static FlatSVGIcon ElCopyIcon = new FlatSVGIcon("svg/elCopy.svg", 16, 16);
    public static FlatSVGIcon ElClearLogIcon = new FlatSVGIcon("svg/elClearLog.svg", 16, 16);
    public static FlatSVGIcon ElTemplateIcon = new FlatSVGIcon("svg/elTemplate.svg", 16, 16);
    public static FlatSVGIcon ElGroupIcon = new FlatSVGIcon("svg/elGroup.svg", 16, 16);
    public static FlatSVGIcon ElSnippetIcon = new FlatSVGIcon("svg/elSnippet.svg", 16, 16);
    public static FlatSVGIcon ElConsoleIcon = new FlatSVGIcon("svg/elConsole.svg", 16, 16);
    public static FlatSVGIcon ElFilterIcon = new FlatSVGIcon("svg/elFilter.svg", 16, 16);

    // ---- AI menu icons -------------------------------------------------
    // Icons used by the top-level AI menu and its sub-items
    // (chat dialog / provider settings).
    public static FlatSVGIcon AiIcon = new FlatSVGIcon("svg/ai.svg", 16, 16);
    public static FlatSVGIcon AiChatIcon = new FlatSVGIcon("svg/aiChat.svg", 16, 16);
    public static FlatSVGIcon AiSettingsIcon = new FlatSVGIcon("svg/aiSettings.svg", 16, 16);
    public static FlatSVGIcon AiGenIcon = new FlatSVGIcon("svg/aiGen.svg", 16, 16);

    // ---- Workflow (DAG canvas) icons -----------------------------------
    // Per-node-type glyphs rendered inside a node card on the workflow
    // canvas (28x28 looks crisp on HiDPI without jagged edges).
    public static FlatSVGIcon WfTriggerIcon = new FlatSVGIcon("svg/wf/wfTrigger.svg", 28, 28);
    public static FlatSVGIcon WfConstantsIcon = new FlatSVGIcon("svg/wf/wfConstants.svg", 28, 28);
    public static FlatSVGIcon WfHttpIcon = new FlatSVGIcon("svg/wf/wfHttp.svg", 28, 28);
    public static FlatSVGIcon WfMergeIcon = new FlatSVGIcon("svg/wf/wfMerge.svg", 28, 28);
    public static FlatSVGIcon WfLoopIcon = new FlatSVGIcon("svg/wf/wfLoop.svg", 28, 28);
    public static FlatSVGIcon WfIfIcon = new FlatSVGIcon("svg/wf/wfIf.svg", 28, 28);
    public static FlatSVGIcon WfTransformIcon = new FlatSVGIcon("svg/wf/wfTransform.svg", 28, 28);
    public static FlatSVGIcon WfAgentIcon = new FlatSVGIcon("svg/wf/wfAgent.svg", 28, 28);
    public static FlatSVGIcon WfReportIcon = new FlatSVGIcon("svg/wf/wfReport.svg", 28, 28);
}
