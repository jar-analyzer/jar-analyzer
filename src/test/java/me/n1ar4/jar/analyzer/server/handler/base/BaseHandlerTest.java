package me.n1ar4.jar.analyzer.server.handler.base;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BaseHandlerTest extends BaseHandler {

    @Test
    public void testExtractMethodCodeComplex() {
        String classCode = "package me.test;\n" +
                "import java.util.List;\n" +
                "import java.util.Map;\n" +
                "\n" +
                "public class ComplexTest {\n" +
                "    // Overload 1: Simple\n" +
                "    public void test(String s) {\n" +
                "        System.out.println(\"test(String)\");\n" +
                "    }\n" +
                "\n" +
                "    // Overload 2: Primitive\n" +
                "    public void test(int i) {\n" +
                "        System.out.println(\"test(int)\");\n" +
                "    }\n" +
                "\n" +
                "    // Overload 3: Multiple args\n" +
                "    public void test(String s, int i) {\n" +
                "        System.out.println(\"test(String, int)\");\n" +
                "    }\n" +
                "\n" +
                "    // Overload 4: Array\n" +
                "    public void test(byte[] b) {\n" +
                "        System.out.println(\"test(byte[])\");\n" +
                "    }\n" +
                "\n" +
                "    // Overload 5: Generics (Decompiled usually shows generics)\n" +
                "    public void test(Map<String, Integer> map) {\n" +
                "        System.out.println(\"test(Map)\");\n" +
                "    }\n" +
                "\n" +
                "    // Overload 6: Varargs\n" +
                "    public void test(String... args) {\n" +
                "        System.out.println(\"test(String...)\");\n" +
                "    }\n" +
                "}\n";

        // 1. Test String param
        // (Ljava/lang/String;)V
        String code1 = extractMethodCode(classCode, "test", "(Ljava/lang/String;)V");
        assertNotNull(code1);
        assertTrue(code1.contains("\"test(String)\""));

        // 2. Test int param
        // (I)V
        String code2 = extractMethodCode(classCode, "test", "(I)V");
        assertNotNull(code2);
        assertTrue(code2.contains("\"test(int)\""));

        // 3. Test multiple params
        // (Ljava/lang/String;I)V
        String code3 = extractMethodCode(classCode, "test", "(Ljava/lang/String;I)V");
        assertNotNull(code3);
        assertTrue(code3.contains("\"test(String, int)\""));

        // 4. Test array param
        // ([B)V
        String code4 = extractMethodCode(classCode, "test", "([B)V");
        assertNotNull(code4);
        assertTrue(code4.contains("\"test(byte[])\""));

        // 5. Test Generics (Map)
        // (Ljava/util/Map;)V
        String code5 = extractMethodCode(classCode, "test", "(Ljava/util/Map;)V");
        assertNotNull(code5);
        assertTrue(code5.contains("\"test(Map)\""));

        // 6. Test Varargs (treated as array in bytecode)
        // ([Ljava/lang/String;)V
        String code6 = extractMethodCode(classCode, "test", "([Ljava/lang/String;)V");
        assertNotNull(code6);
        assertTrue(code6.contains("\"test(String...)\""));
    }

    @Test
    public void testExtractMethodCodeComplexReturn() {
        String classCode = "package me.test;\n" +
                "import java.util.List;\n" +
                "import java.util.Map;\n" +
                "\n" +
                "public class ComplexReturnTest {\n" +
                "    public Map<String, List<Integer>> getMap(String key) {\n" +
                "        return null;\n" +
                "    }\n" +
                "\n" +
                "    public ComplexReturnTest[] getArray() {\n" +
                "        return null;\n" +
                "    }\n" +
                "}\n";

        // Test Map<String, List<Integer>> return type
        // (Ljava/lang/String;)Ljava/util/Map;
        String code1 = extractMethodCode(classCode, "getMap", "(Ljava/lang/String;)Ljava/util/Map;");
        assertNotNull(code1);
        assertTrue(code1.contains("public Map<String, List<Integer>> getMap(String key)"));

        // Test Array return type
        // ()[Lme/test/ComplexReturnTest;
        String code2 = extractMethodCode(classCode, "getArray", "()[Lme/test/ComplexReturnTest;");
        assertNotNull(code2);
        assertTrue(code2.contains("public ComplexReturnTest[] getArray()"));
    }
}
