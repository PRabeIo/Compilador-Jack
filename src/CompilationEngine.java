import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Analisador sintático da linguagem Jack.
 * Implementa recursive descent parsing — uma função por não-terminal da gramática.
 */
public class CompilationEngine {

    private final List<Token> tokens;
    private int cursor = 0;
    private final PrintWriter writer;
    private int indentLevel = 0;
    private static final int INDENT_SIZE = 2;

    public CompilationEngine(List<Token> tokens, String outputPath) throws IOException {
        this.tokens = tokens.stream()
                .filter(t -> t.tag != TokenType.EOF)
                .collect(Collectors.toList());
        this.writer = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outputPath), "UTF-8")));
    }

    // =========================================================================
    // Métodos auxiliares de navegação
    // =========================================================================

    private Token peek() {
        if (cursor >= tokens.size())
            throw new RuntimeException("Fim inesperado de tokens.");
        return tokens.get(cursor);
    }

    private Token peekNext() {
        if (cursor + 1 >= tokens.size()) return null;
        return tokens.get(cursor + 1);
    }

    private Token advance() {
        Token t = peek();
        cursor++;
        return t;
    }

    private boolean match(String value) {
        return cursor < tokens.size() && peek().value.equals(value);
    }

    private boolean matchType(String xmlTag) {
        return cursor < tokens.size() && peek().tag.getXmlTag().equals(xmlTag);
    }

    private void consume(String expectedValue) {
        Token t = advance();
        if (!t.value.equals(expectedValue)) {
            throw new RuntimeException(
                "Erro sintático: esperado '" + expectedValue +
                "', encontrado '" + t.value + "' (tipo: " + t.tag.getXmlTag() + ")" +
                " — token #" + cursor);
        }
        writeToken(t);
    }

    private void consumeType(String expectedXmlTag) {
        Token t = advance();
        if (!t.tag.getXmlTag().equals(expectedXmlTag)) {
            throw new RuntimeException(
                "Erro sintático: esperado tipo '" + expectedXmlTag +
                "', encontrado '" + t.value + "' (tipo: " + t.tag.getXmlTag() + ")" +
                " — token #" + cursor);
        }
        writeToken(t);
    }

    // =========================================================================
    // Métodos auxiliares de escrita XML
    // =========================================================================

    private void writeToken(Token t) {
        writeLine(t.toXML());
    }

    private void openTag(String tag) {
        writeLine("<" + tag + ">");
        indentLevel += INDENT_SIZE;
    }

    private void closeTag(String tag) {
        indentLevel -= INDENT_SIZE;
        writeLine("</" + tag + ">");
    }

    private void writeLine(String content) {
        writer.println(" ".repeat(indentLevel) + content);
    }

    private void close() {
        writer.flush();
        writer.close();
    }

    // =========================================================================
    // Regras da gramática Jack (a implementar nos próximos commits)
    // =========================================================================

    public void compileClass() {
        // TODO
        close();
    }
}
