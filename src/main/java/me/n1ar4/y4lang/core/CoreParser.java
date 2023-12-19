package me.n1ar4.y4lang.core;

import me.n1ar4.y4lang.ast.*;
import me.n1ar4.y4lang.exception.ParseException;
import me.n1ar4.y4lang.lexer.Lexer;
import me.n1ar4.y4lang.parser.Operators;
import me.n1ar4.y4lang.parser.Parser;
import me.n1ar4.y4lang.token.Token;

import java.util.HashSet;

import static me.n1ar4.y4lang.parser.Parser.rule;

/**
 * SYNTAX ANALYSIS USING BNF
 */
public class CoreParser {
    /**
     * START BASIC RULE
     */
    HashSet<String> reserved = new HashSet<>();
    Operators operators = new Operators();
    Parser expr0 = Parser.rule();
    // primary : "(" expr ")" | number | identifier | string
    Parser primary = Parser.rule(PrimaryExpr.class)
            .or(Parser.rule().sep("(").ast(expr0).sep(")"),
                    Parser.rule().number(NumberLiteral.class),
                    Parser.rule().identifier(Name.class, reserved),
                    Parser.rule().string(StringLiteral.class));
    // factor : - primary | primary
    Parser factor = Parser.rule()
            .or(Parser.rule(NegativeExpr.class).sep("-").ast(primary), primary);
    // expr : factor { operator factor }
    Parser expr = expr0.expression(BinaryExpr.class, factor, operators);
    Parser statement0 = Parser.rule();
    // block: "{" [ statement ] { ( ";" | EOL ) [statement] } "}"
    Parser block = Parser.rule(BlockStmt.class)
            .sep("{").option(statement0)
            .repeat(Parser.rule().sep(";", Token.EOL).option(statement0))
            .sep("}");
    // simple: expr
    Parser simple = Parser.rule(PrimaryExpr.class).ast(expr);
    // statement : "if" expr block [ "else" block ]
    //             | "while" expr block | simple
    //             | "return" factor
    //             | "go" factor
    Parser statement = statement0.or(
            Parser.rule(IfStmt.class).sep("if").ast(expr).ast(block)
                    .option(Parser.rule().sep("else").ast(block)),
            Parser.rule(WhileStmt.class).sep("while").ast(expr).ast(block),
            Parser.rule(ReturnStmt.class).sep("return").ast(factor),
            Parser.rule(GoStmt.class).sep("go").ast(factor),
            simple);
    // include : "#include" identifier
    Parser include = Parser.rule(IncludeStmt.class).sep("#include")
            .string(StringLiteral.class);
    // program : ( include | statement | null ) ( ";" | EOL )
    Parser program = Parser.rule().or(include, statement, Parser.rule(NullStmt.class))
            .sep(";", Token.EOL);

    /**
     * START FUNCTION RULE
     */
    Parser param = rule().identifier(reserved);
    // params : param { ","  param }
    Parser params = rule(ParameterList.class)
            .ast(param).repeat(rule().sep(",").ast(param));
    // param_list : params "(" [ params ] ")"
    Parser paramList = rule().sep("(").maybe(params).sep(")");
    // def : "def" identifier param_list block
    Parser def = rule(DefStmt.class)
            .sep("def").identifier(reserved).ast(paramList).ast(block);
    // args : expr { "," expr }
    Parser args = rule(Arguments.class)
            .ast(expr).repeat(rule().sep(",").ast(expr));
    // postfix : "(" [ args ] ")"
    Parser postfix = rule().sep("(").maybe(args).sep(")");

    /**
     * START ARRAY RULE
     */
    // element : expr { "," expr }
    Parser elements = rule(ArrayLiteral.class)
            .ast(expr).repeat(rule().sep(",").ast(expr));

    /**
     * BUILD RULE
     */
    public CoreParser() {
        addReserved();
        addOperators();
        arrayRule();
        functionRule();
    }

    /**
     * BUILD ARRAY RULE
     */
    private void arrayRule() {
        // primary : "[" [element] "]"
        //           | "(" expr ")" | number | identifier | string
        primary.insertChoice(rule().sep("[").maybe(elements).sep("]"));
        // postfix : "(" [ args ] ")" | "[" expr "]"
        postfix.insertChoice(rule(ArrayRef.class).sep("[").ast(expr).sep("]"));
    }

    /**
     * BUILD FUNCTION RULE
     */
    private void functionRule() {
        // primary : ( "[" [element] "]" | "(" expr ")"
        //           | number | identifier | string ) { postfix }
        primary.repeat(postfix);
        // simple : expr [args]
        simple.option(args);
        // program : [ def | statement ] ( ";" | EOL )
        program.insertChoice(def);
        // primary : ( "[" [element] "]" | "(" expr ")"
        //           | number | identifier | string | "fun" ) { postfix }
        primary.insertChoice(rule(Fun.class)
                .sep("fun").ast(paramList).ast(block));
    }

    private void addOperators() {
        operators.add("=", 1, Operators.RIGHT);
        operators.add("==", 2, Operators.LEFT);
        operators.add("!=", 2, Operators.LEFT);
        operators.add(">", 2, Operators.LEFT);
        operators.add(">=", 2, Operators.LEFT);
        operators.add("<", 2, Operators.LEFT);
        operators.add("<=", 2, Operators.LEFT);
        operators.add("+", 3, Operators.LEFT);
        operators.add("-", 3, Operators.LEFT);
        operators.add("*", 4, Operators.LEFT);
        operators.add("/", 4, Operators.LEFT);
        operators.add("%", 4, Operators.LEFT);
    }

    private void addReserved() {
        reserved.add(";");
        reserved.add("}");
        reserved.add("]");
        reserved.add(")");
        reserved.add(Token.EOL);
    }

    public ASTree parse(Lexer lexer) throws ParseException {
        return program.parse(lexer);
    }
}
