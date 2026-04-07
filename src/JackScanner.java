import java.util.HashMap;
import java.util.Map;

public class JackScanner {

    private final String input;
    private int  pos  = 0;
    private char peek = ' ';
    private int  line = 1;
    private final Map<String, TokenType> keywords = new HashMap<>();

    private static final String SYMBOL_CHARS = "{}()[].,;+-*/&|<>=~";

    public JackScanner(String input) {
        this.input = input;

        keywords.put("class",       TokenType.CLASS);
        keywords.put("constructor", TokenType.CONSTRUCTOR);
        keywords.put("function",    TokenType.FUNCTION);
        keywords.put("method",      TokenType.METHOD);
        keywords.put("field",       TokenType.FIELD);
        keywords.put("static",      TokenType.STATIC);
        keywords.put("var",         TokenType.VAR);
        keywords.put("int",         TokenType.INT);
        keywords.put("char",        TokenType.CHAR);
        keywords.put("boolean",     TokenType.BOOLEAN);
        keywords.put("void",        TokenType.VOID);
        keywords.put("true",        TokenType.TRUE);
        keywords.put("false",       TokenType.FALSE);
        keywords.put("null",        TokenType.NULL);
        keywords.put("this",        TokenType.THIS);
        keywords.put("let",         TokenType.LET);
        keywords.put("do",          TokenType.DO);
        keywords.put("if",          TokenType.IF);
        keywords.put("else",        TokenType.ELSE);
        keywords.put("while",       TokenType.WHILE);
        keywords.put("return",      TokenType.RETURN);

        readch();
    }

    private void readch() {
        if (pos < input.length()) {
            if (peek == '\n') line++;
            peek = input.charAt(pos++);
        } else {
            peek = '\0';
        }
    }
}
