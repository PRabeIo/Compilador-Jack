public class Token {
    public final TokenType tag; 
    public final String value;

    public Token(TokenType tag, String value) {
        this.tag   = tag;
        this.value = value;
    }

    public TokenType getType()  { return tag; }
    public String    getValue() { return value; }

    public String toXML() {
        return "<" + tag.getXmlTag() + "> " + escapeXml(value) + " </" + tag.getXmlTag() + ">";
    }

    private static String escapeXml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    @Override
    public String toString() {
        return "<" + tag + ", " + value + ">";
    }
}
