public class Token {
    public final TokenType tag;
    public final String value;

    public Token(TokenType tag, String value) {
        this.tag   = tag;
        this.value = value;
    }

    public TokenType getType()  { return tag; }
    public String    getValue() { return value; }

    @Override
    public String toString() {
        return "<" + tag + ", " + value + ">";
    }
}
