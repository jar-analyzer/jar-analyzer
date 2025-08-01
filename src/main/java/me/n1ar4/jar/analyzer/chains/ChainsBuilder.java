/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.chains;

import javax.swing.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class ChainsBuilder {
    public static final Map<String, SinkModel> sinkData = new LinkedHashMap<>();

    static {
        SinkModel sink0 = new SinkModel();
        sink0.setBoxName("Runtime.exec(String)");
        sink0.setClassName("java/lang/Runtime");
        sink0.setMethodName("exec");
        sink0.setMethodDesc("(Ljava/lang/String;)Ljava/lang/Process;");
        sinkData.put(sink0.getBoxName(), sink0);

        SinkModel sink1 = new SinkModel();
        sink1.setBoxName("ProcessBuilder.start");
        sink1.setClassName("java/lang/ProcessBuilder");
        sink1.setMethodName("start");
        sink1.setMethodDesc("()Ljava/lang/Process;");
        sinkData.put(sink1.getBoxName(), sink1);

        SinkModel sink2 = new SinkModel();
        sink2.setBoxName("ScriptEngine.eval");
        sink2.setClassName("javax/script/ScriptEngine");
        sink2.setMethodName("eval");
        sink2.setMethodDesc("(Ljava/lang/String;)Ljava/lang/Object;");
        sinkData.put(sink2.getBoxName(), sink2);

        SinkModel sink3 = new SinkModel();
        sink3.setBoxName("BCEL Classloader.loadClass");
        sink3.setClassName("com/sun/org/apache/bcel/internal/util/ClassLoader");
        sink3.setMethodName("loadClass");
        sink3.setMethodDesc("(Ljava/lang/String;)Ljava/lang/Class;");
        sinkData.put(sink3.getBoxName(), sink3);

        SinkModel sink4 = new SinkModel();
        sink4.setBoxName("InitialContext.lookup");
        sink4.setClassName("javax/naming/InitialContext");
        sink4.setMethodName("lookup");
        sink4.setMethodDesc("(Ljava/lang/String;)Ljava/lang/Object;");
        sinkData.put(sink4.getBoxName(), sink4);

        SinkModel sink5 = new SinkModel();
        sink5.setBoxName("Context.lookup");
        sink5.setClassName("javax/naming/Context");
        sink5.setMethodName("lookup");
        sink5.setMethodDesc("(Ljava/lang/String;)Ljava/lang/Object;");
        sinkData.put(sink5.getBoxName(), sink5);

        SinkModel sink6 = new SinkModel();
        sink6.setBoxName("DirContext.search");
        sink6.setClassName("javax/naming/directory/DirContext");
        sink6.setMethodName("search");
        sink6.setMethodDesc("(Ljava/lang/String;Ljava/lang/String;Ljavax/naming/directory/SearchControls;)Ljavax/naming/NamingEnumeration;");
        sinkData.put(sink6.getBoxName(), sink6);

        SinkModel sink7 = new SinkModel();
        sink7.setBoxName("LdapContext.search");
        sink7.setClassName("javax/naming/ldap/LdapContext");
        sink7.setMethodName("search");
        sink7.setMethodDesc("(Ljava/lang/String;Ljava/lang/String;Ljavax/naming/directory/SearchControls;)Ljavax/naming/NamingEnumeration;");
        sinkData.put(sink7.getBoxName(), sink7);

        SinkModel sink8 = new SinkModel();
        sink8.setBoxName("Statement.execute");
        sink8.setClassName("java/sql/Statement");
        sink8.setMethodName("execute");
        sink8.setMethodDesc("(Ljava/lang/String;)Z");
        sinkData.put(sink8.getBoxName(), sink8);

        SinkModel sink9 = new SinkModel();
        sink9.setBoxName("Statement.executeQuery");
        sink9.setClassName("java/sql/Statement");
        sink9.setMethodName("executeQuery");
        sink9.setMethodDesc("(Ljava/lang/String;)Ljava/sql/ResultSet;");
        sinkData.put(sink9.getBoxName(), sink9);

        SinkModel sink10 = new SinkModel();
        sink10.setBoxName("Statement.executeUpdate");
        sink10.setClassName("java/sql/Statement");
        sink10.setMethodName("executeUpdate");
        sink10.setMethodDesc("(Ljava/lang/String;)I");
        sinkData.put(sink10.getBoxName(), sink10);

        SinkModel sink11 = new SinkModel();
        sink11.setBoxName("Connection.prepareStatement");
        sink11.setClassName("java/sql/Connection");
        sink11.setMethodName("prepareStatement");
        sink11.setMethodDesc("(Ljava/lang/String;)Ljava/sql/PreparedStatement;");
        sinkData.put(sink11.getBoxName(), sink11);

        SinkModel sink12 = new SinkModel();
        sink12.setBoxName("Connection.prepareCall");
        sink12.setClassName("java/sql/Connection");
        sink12.setMethodName("prepareCall");
        sink12.setMethodDesc("(Ljava/lang/String;)Ljava/sql/CallableStatement;");
        sinkData.put(sink12.getBoxName(), sink12);

        SinkModel sink13 = new SinkModel();
        sink13.setBoxName("ObjectInputStream.readObject");
        sink13.setClassName("java/io/ObjectInputStream");
        sink13.setMethodName("readObject");
        sink13.setMethodDesc("()Ljava/lang/Object;");
        sinkData.put(sink13.getBoxName(), sink13);

        SinkModel sink14 = new SinkModel();
        sink14.setBoxName("ObjectInputStream.readUnshared");
        sink14.setClassName("java/io/ObjectInputStream");
        sink14.setMethodName("readUnshared");
        sink14.setMethodDesc("()Ljava/lang/Object;");
        sinkData.put(sink14.getBoxName(), sink14);

        SinkModel sink15 = new SinkModel();
        sink15.setBoxName("XMLDecoder.readObject");
        sink15.setClassName("java/beans/XMLDecoder");
        sink15.setMethodName("readObject");
        sink15.setMethodDesc("()Ljava/lang/Object;");
        sinkData.put(sink15.getBoxName(), sink15);

        SinkModel sink16 = new SinkModel();
        sink16.setBoxName("Yaml.load");
        sink16.setClassName("org/yaml/snakeyaml/Yaml");
        sink16.setMethodName("load");
        sink16.setMethodDesc("(Ljava/lang/String;)Ljava/lang/Object;");
        sinkData.put(sink16.getBoxName(), sink16);

        SinkModel sink17 = new SinkModel();
        sink17.setBoxName("Yaml.loadAs");
        sink17.setClassName("org/yaml/snakeyaml/Yaml");
        sink17.setMethodName("loadAs");
        sink17.setMethodDesc("(Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;");
        sinkData.put(sink17.getBoxName(), sink17);

        SinkModel sink18 = new SinkModel();
        sink18.setBoxName("JSON.parseObject");
        sink18.setClassName("com/alibaba/fastjson/JSON");
        sink18.setMethodName("parseObject");
        sink18.setMethodDesc("(Ljava/lang/String;)Lcom/alibaba/fastjson/JSONObject;");
        sinkData.put(sink18.getBoxName(), sink18);

        SinkModel sink19 = new SinkModel();
        sink19.setBoxName("JSON.parse");
        sink19.setClassName("com/alibaba/fastjson/JSON");
        sink19.setMethodName("parse");
        sink19.setMethodDesc("(Ljava/lang/String;)Ljava/lang/Object;");
        sinkData.put(sink19.getBoxName(), sink19);

        SinkModel sink20 = new SinkModel();
        sink20.setBoxName("ObjectMapper.readValue");
        sink20.setClassName("com/fasterxml/jackson/databind/ObjectMapper");
        sink20.setMethodName("readValue");
        sink20.setMethodDesc("(Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;");
        sinkData.put(sink20.getBoxName(), sink20);

        SinkModel sink21 = new SinkModel();
        sink21.setBoxName("HessianInput.readObject");
        sink21.setClassName("com/caucho/hessian/io/HessianInput");
        sink21.setMethodName("readObject");
        sink21.setMethodDesc("()Ljava/lang/Object;");
        sinkData.put(sink21.getBoxName(), sink21);

        SinkModel sink22 = new SinkModel();
        sink22.setBoxName("AbstractHessianInput.readObject");
        sink22.setClassName("com/caucho/hessian/io/AbstractHessianInput");
        sink22.setMethodName("readObject");
        sink22.setMethodDesc("()Ljava/lang/Object;");
        sinkData.put(sink22.getBoxName(), sink22);

        SinkModel sink23 = new SinkModel();
        sink23.setBoxName("XStream.fromXML");
        sink23.setClassName("com/thoughtworks/xstream/XStream");
        sink23.setMethodName("fromXML");
        sink23.setMethodDesc("(Ljava/lang/String;)Ljava/lang/Object;");
        sinkData.put(sink23.getBoxName(), sink23);

        SinkModel sink24 = new SinkModel();
        sink24.setBoxName("Input.readObject");
        sink24.setClassName("com/esotericsoftware/kryo/io/Input");
        sink24.setMethodName("readObject");
        sink24.setMethodDesc("(Ljava/lang/Class;)Ljava/lang/Object;");
        sinkData.put(sink24.getBoxName(), sink24);

        SinkModel sink25 = new SinkModel();
        sink25.setBoxName("FileInputStream.new");
        sink25.setClassName("java/io/FileInputStream");
        sink25.setMethodName("<init>");
        sink25.setMethodDesc("(Ljava/lang/String;)V");
        sinkData.put(sink25.getBoxName(), sink25);

        SinkModel sink26 = new SinkModel();
        sink26.setBoxName("FileOutputStream.new");
        sink26.setClassName("java/io/FileOutputStream");
        sink26.setMethodName("<init>");
        sink26.setMethodDesc("(Ljava/lang/String;)V");
        sinkData.put(sink26.getBoxName(), sink26);

        SinkModel sink27 = new SinkModel();
        sink27.setBoxName("RandomAccessFile.new");
        sink27.setClassName("java/io/RandomAccessFile");
        sink27.setMethodName("<init>");
        sink27.setMethodDesc("(Ljava/lang/String;Ljava/lang/String;)V");
        sinkData.put(sink27.getBoxName(), sink27);

        SinkModel sink28 = new SinkModel();
        sink28.setBoxName("File.delete");
        sink28.setClassName("java/io/File");
        sink28.setMethodName("delete");
        sink28.setMethodDesc("()Z");
        sinkData.put(sink28.getBoxName(), sink28);

        SinkModel sink29 = new SinkModel();
        sink29.setBoxName("URL.openConnection");
        sink29.setClassName("java/net/URL");
        sink29.setMethodName("openConnection");
        sink29.setMethodDesc("()Ljava/net/URLConnection;");
        sinkData.put(sink29.getBoxName(), sink29);

        SinkModel sink30 = new SinkModel();
        sink30.setBoxName("HttpURLConnection.connect");
        sink30.setClassName("java/net/HttpURLConnection");
        sink30.setMethodName("connect");
        sink30.setMethodDesc("()V");
        sinkData.put(sink30.getBoxName(), sink30);
    }

    public static void buildBox(
            JComboBox<String> sinkBox,
            JTextField sinkClassText,
            JTextField sinkMethodText,
            JTextField sinkDescText) {
        for (String sink : sinkData.keySet()) {
            sinkBox.addItem(sink);
        }
        sinkBox.setSelectedIndex(0);
        sinkBox.addActionListener(e -> {
            String key = (String) sinkBox.getSelectedItem();
            SinkModel model = sinkData.get(key);
            sinkClassText.setText(model.getClassName());
            sinkClassText.setCaretPosition(0);
            sinkMethodText.setText(model.getMethodName());
            sinkMethodText.setCaretPosition(0);
            sinkDescText.setText(model.getMethodDesc());
            sinkDescText.setCaretPosition(0);
        });
    }
}
