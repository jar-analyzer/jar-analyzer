package me.n1ar4.y4json;

@SuppressWarnings("unused")
public interface JSONConst {
    int EOF = -1;
    // ###################################################
    char prefix = '{';
    char suffix = '}';
    char arrayLeft = '[';
    char arrayRight = ']';
    char colon = ':';
    char comma = ',';
    char quotationMark = '"';
    char reverseSolidus = '\\';
    char solidus = '/';
    char backSpace = '\b';
    char formFeed = '\f';
    char lineFeed = '\n';
    char carriageReturn = '\r';
    char horizontalTab = '\t';
    char space = ' ';
    char negative = '-';
    // ####################### TYPE #######################
    String INT_TYPE = "int";
    String BYTE_TYPE = "byte";
    String SHORT_TYPE = "short";
    String LONG_TYPE = "long";
    String BOOL_TYPE = "boolean";
    String CHAR_TYPE = "char";
    String DOUBLE_TYPE = "double";
    String FLOAT_TYPE = "float";
    String STRING_TYPE = "java.lang.String";
}
