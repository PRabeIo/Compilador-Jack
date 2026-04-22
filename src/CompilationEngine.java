import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

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

    private Token peek() {
        if (cursor >= tokens.size()) throw new RuntimeException("Fim inesperado de tokens.");
        return tokens.get(cursor);
    }

    private Token peekNext() {
        if (cursor + 1 >= tokens.size()) return null;
        return tokens.get(cursor + 1);
    }

    private Token advance() { Token t = peek(); cursor++; return t; }
    private boolean match(String value) { return cursor < tokens.size() && peek().value.equals(value); }
    private boolean matchType(String xmlTag) { return cursor < tokens.size() && peek().tag.getXmlTag().equals(xmlTag); }

    private void consume(String expectedValue) {
        Token t = advance();
        if (!t.value.equals(expectedValue))
            throw new RuntimeException("Erro sintático: esperado '" + expectedValue + "', encontrado '" + t.value + "' — token #" + cursor);
        writeToken(t);
    }

    private void consumeType(String expectedXmlTag) {
        Token t = advance();
        if (!t.tag.getXmlTag().equals(expectedXmlTag))
            throw new RuntimeException("Erro sintático: esperado tipo '" + expectedXmlTag + "', encontrado '" + t.value + "' — token #" + cursor);
        writeToken(t);
    }

    private void writeToken(Token t) { writeLine(t.toXML()); }
    private void openTag(String tag) { writeLine("<" + tag + ">"); indentLevel += INDENT_SIZE; }
    private void closeTag(String tag) { indentLevel -= INDENT_SIZE; writeLine("</" + tag + ">"); }
    private void writeLine(String content) { writer.println(" ".repeat(indentLevel) + content); }
    private void close() { writer.flush(); writer.close(); }

    public void compileClass() {
        openTag("class");
        consume("class");
        consumeType("identifier");
        consume("{");
        while (match("static") || match("field")) compileClassVarDec();
        while (match("constructor") || match("function") || match("method")) compileSubroutine();
        consume("}");
        closeTag("class");
        close();
    }

    private void compileClassVarDec() {
        openTag("classVarDec");
        writeToken(advance());
        compileType();
        consumeType("identifier");
        while (match(",")) { consume(","); consumeType("identifier"); }
        consume(";");
        closeTag("classVarDec");
    }

    private void compileType() {
        Token t = peek();
        if (t.value.equals("int") || t.value.equals("char") || t.value.equals("boolean")
                || t.tag.getXmlTag().equals("identifier")) writeToken(advance());
        else throw new RuntimeException("Erro sintático: tipo esperado, encontrado '" + t.value + "'");
    }

    private void compileSubroutine() {
        openTag("subroutineDec");
        writeToken(advance());
        if (match("void")) writeToken(advance()); else compileType();
        consumeType("identifier");
        consume("(");
        compileParameterList();
        consume(")");
        compileSubroutineBody();
        closeTag("subroutineDec");
    }

    private void compileParameterList() {
        openTag("parameterList");
        if (!match(")")) {
            compileType(); consumeType("identifier");
            while (match(",")) { consume(","); compileType(); consumeType("identifier"); }
        }
        closeTag("parameterList");
    }

    private void compileSubroutineBody() {
        openTag("subroutineBody");
        consume("{");
        while (match("var")) compileVarDec();
        compileStatements();
        consume("}");
        closeTag("subroutineBody");
    }

    private void compileVarDec() {
        openTag("varDec");
        consume("var");
        compileType();
        consumeType("identifier");
        while (match(",")) { consume(","); consumeType("identifier"); }
        consume(";");
        closeTag("varDec");
    }

    private void compileStatements() {
        openTag("statements");
        while (true) {
            if      (match("let"))    compileLet();
            else if (match("if"))     compileIf();
            else if (match("while"))  compileWhile();
            else if (match("do"))     break; // do/return — próximo commit
            else if (match("return")) break;
            else break;
        }
        closeTag("statements");
    }

    private void compileLet() {
        openTag("letStatement");
        consume("let");
        consumeType("identifier");
        if (match("[")) { consume("["); compileExpression(); consume("]"); }
        consume("=");
        compileExpression();
        consume(";");
        closeTag("letStatement");
    }

    // ifStatement: 'if' '(' expression ')' '{' statements '}' ('else' '{' statements '}')?
    private void compileIf() {
        openTag("ifStatement");
        consume("if");
        consume("(");
        compileExpression();
        consume(")");
        consume("{");
        compileStatements();
        consume("}");
        if (match("else")) {
            consume("else");
            consume("{");
            compileStatements();
            consume("}");
        }
        closeTag("ifStatement");
    }

    // whileStatement: 'while' '(' expression ')' '{' statements '}'
    private void compileWhile() {
        openTag("whileStatement");
        consume("while");
        consume("(");
        compileExpression();
        consume(")");
        consume("{");
        compileStatements();
        consume("}");
        closeTag("whileStatement");
    }

    // expression — placeholder até o commit 13
    private void compileExpression() {
        openTag("expression");
        openTag("term");
        writeToken(advance());
        closeTag("term");
        closeTag("expression");
    }
}
