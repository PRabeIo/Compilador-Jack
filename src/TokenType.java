public enum TokenType {
	
    CLASS("keyword"),
    CONSTRUCTOR("keyword"),
    FUNCTION("keyword"),
    METHOD("keyword"),
    FIELD("keyword"),
    STATIC("keyword"),
    VAR("keyword"),
    INT("keyword"),
    CHAR("keyword"),
    BOOLEAN("keyword"),
    VOID("keyword"),
    TRUE("keyword"),
    FALSE("keyword"),
    NULL("keyword"),
    THIS("keyword"),
    LET("keyword"),
    DO("keyword"),
    IF("keyword"),
    ELSE("keyword"),
    WHILE("keyword"),
    RETURN("keyword"),
    
    LPAREN("symbol"),
    RPAREN("symbol"),
    LBRACE("symbol"),
    RBRACE("symbol"),
    LBRACKET("symbol"),
    RBRACKET("symbol"),
    COMMA("symbol"),
    SEMICOLON("symbol"),
    DOT("symbol"),
    PLUS("symbol"),
    MINUS("symbol"),
    ASTERISK("symbol"),
    SLASH("symbol"),
    AND("symbol"),
    OR("symbol"),
    NOT("symbol"),
    LT("symbol"),
    GT("symbol"),
    EQ("symbol"),

    NUMBER("integerConstant"),
    STRING("stringConstant"),
    IDENT("identifier"),
    EOF("eof");
    
    final String xmlTag;

    TokenType(String xmlTag) {
        this.xmlTag = xmlTag;
    }

    public String getXmlTag() { 
        return xmlTag;
    }
}
