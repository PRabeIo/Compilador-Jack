import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Analisador sintático da linguagem Jack.
 * Implementa recursive descent parsing — uma função por não-terminal da gramática.
 * Consome a lista de tokens produzida pelo JackScanner e gera um arquivo XML
 * representando a árvore sintática, compatível com o validador oficial do nand2tetris.
 */
public class CompilationEngine {

    private final List<Token> tokens;
    private int cursor = 0;
    private final PrintWriter writer;
    private int indentLevel = 0;
    private static final int INDENT_SIZE = 2;

    /**
     * @param tokens     Lista de tokens produzida pelo JackScanner (incluindo EOF).
     * @param outputPath Caminho do arquivo XML de saída (ex: "tests/output/MainP.xml").
     */
    public CompilationEngine(List<Token> tokens, String outputPath) throws IOException {
        // Filtra o token EOF — o parser não precisa dele
        this.tokens = tokens.stream()
                .filter(t -> t.tag != TokenType.EOF)
                .collect(Collectors.toList());
        this.writer = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outputPath), "UTF-8")));
    }

    // =========================================================================
    // Métodos auxiliares de navegação
    // =========================================================================

    /** Retorna o token atual sem avançar. */
    private Token peek() {
        if (cursor >= tokens.size())
            throw new RuntimeException("Fim inesperado de tokens.");
        return tokens.get(cursor);
    }

    /** Retorna o próximo token (lookahead de 1) sem avançar. */
    private Token peekNext() {
        if (cursor + 1 >= tokens.size()) return null;
        return tokens.get(cursor + 1);
    }

    /** Avança e retorna o token atual. */
    private Token advance() {
        Token t = peek();
        cursor++;
        return t;
    }

    /** Verifica se o valor do token atual é igual a {@code value}. */
    private boolean match(String value) {
        return cursor < tokens.size() && peek().value.equals(value);
    }

    /** Verifica se a tag XML do token atual é igual a {@code xmlTag}. */
    private boolean matchType(String xmlTag) {
        return cursor < tokens.size() && peek().tag.getXmlTag().equals(xmlTag);
    }

    /**
     * Consome o token atual exigindo que seu valor seja {@code expectedValue}.
     * Lança RuntimeException com mensagem clara caso não corresponda.
     */
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

    /**
     * Consome o token atual exigindo que sua tag XML seja {@code expectedXmlTag}.
     * Lança RuntimeException com mensagem clara caso não corresponda.
     */
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

    /** Fecha o PrintWriter — deve ser chamado após compileClass(). */
    private void close() {
        writer.flush();
        writer.close();
    }

    // =========================================================================
    // Regras da gramática Jack (recursive descent)
    // =========================================================================

    /**
     * Ponto de entrada do parser.
     * class: 'class' className '{' classVarDec* subroutineDec* '}'
     */
    public void compileClass() {
        openTag("class");

        consume("class");
        consumeType("identifier");   // className
        consume("{");

        // zero ou mais declarações de variáveis de classe
        while (match("static") || match("field")) {
            compileClassVarDec();
        }

        // zero ou mais declarações de subrotinas
        while (match("constructor") || match("function") || match("method")) {
            compileSubroutine();
        }

        consume("}");

        closeTag("class");
        close();
    }

    /**
     * classVarDec: ('static' | 'field') type varName (',' varName)* ';'
     */
    private void compileClassVarDec() {
        openTag("classVarDec");

        // 'static' ou 'field'
        writeToken(advance());
        compileType();
        consumeType("identifier");   // varName

        while (match(",")) {
            consume(",");
            consumeType("identifier");
        }

        consume(";");
        closeTag("classVarDec");
    }

    /**
     * type: 'int' | 'char' | 'boolean' | className (identifier)
     * Aceita qualquer keyword de tipo primitivo ou identificador de classe.
     */
    private void compileType() {
        Token t = peek();
        if (t.value.equals("int") || t.value.equals("char") || t.value.equals("boolean")
                || t.tag.getXmlTag().equals("identifier")) {
            writeToken(advance());
        } else {
            throw new RuntimeException(
                "Erro sintático: tipo esperado, encontrado '" + t.value + "'");
        }
    }

    /**
     * subroutineDec: ('constructor' | 'function' | 'method')
     *                ('void' | type) subroutineName '(' parameterList ')' subroutineBody
     */
    private void compileSubroutine() {
        openTag("subroutineDec");

        // 'constructor', 'function' ou 'method'
        writeToken(advance());

        // tipo de retorno: 'void' ou type
        if (match("void")) {
            writeToken(advance());
        } else {
            compileType();
        }

        consumeType("identifier");   // subroutineName
        consume("(");
        compileParameterList();
        consume(")");
        compileSubroutineBody();

        closeTag("subroutineDec");
    }

    /**
     * parameterList: ((type varName) (',' type varName)*)?
     * Pode ser vazia.
     */
    private void compileParameterList() {
        openTag("parameterList");

        if (!match(")")) {
            compileType();
            consumeType("identifier");   // varName

            while (match(",")) {
                consume(",");
                compileType();
                consumeType("identifier");
            }
        }

        closeTag("parameterList");
    }

    /**
     * subroutineBody: '{' varDec* statements '}'
     */
    private void compileSubroutineBody() {
        openTag("subroutineBody");

        consume("{");

        while (match("var")) {
            compileVarDec();
        }

        compileStatements();
        consume("}");

        closeTag("subroutineBody");
    }

    /**
     * varDec: 'var' type varName (',' varName)* ';'
     */
    private void compileVarDec() {
        openTag("varDec");

        consume("var");
        compileType();
        consumeType("identifier");   // varName

        while (match(",")) {
            consume(",");
            consumeType("identifier");
        }

        consume(";");
        closeTag("varDec");
    }

    /**
     * statements: statement*
     * statement: letStatement | ifStatement | whileStatement | doStatement | returnStatement
     */
    private void compileStatements() {
        openTag("statements");

        while (true) {
            if      (match("let"))    compileLet();
            else if (match("if"))     compileIf();
            else if (match("while"))  compileWhile();
            else if (match("do"))     compileDo();
            else if (match("return")) compileReturn();
            else break;
        }

        closeTag("statements");
    }

    /**
     * letStatement: 'let' varName ('[' expression ']')? '=' expression ';'
     */
    private void compileLet() {
        openTag("letStatement");

        consume("let");
        consumeType("identifier");   // varName

        // acesso a array opcional
        if (match("[")) {
            consume("[");
            compileExpression();
            consume("]");
        }

        consume("=");
        compileExpression();
        consume(";");

        closeTag("letStatement");
    }

    /**
     * ifStatement: 'if' '(' expression ')' '{' statements '}'
     *              ('else' '{' statements '}')?
     */
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

    /**
     * whileStatement: 'while' '(' expression ')' '{' statements '}'
     */
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

    /**
     * doStatement: 'do' subroutineCall ';'
     */
    private void compileDo() {
        openTag("doStatement");

        consume("do");
        compileSubroutineCall();
        consume(";");

        closeTag("doStatement");
    }

    /**
     * returnStatement: 'return' expression? ';'
     */
    private void compileReturn() {
        openTag("returnStatement");

        consume("return");

        if (!match(";")) {
            compileExpression();
        }

        consume(";");

        closeTag("returnStatement");
    }

    /**
     * expression: term (op term)*
     * op: '+' | '-' | '*' | '/' | '&' | '|' | '<' | '>' | '='
     */
    private void compileExpression() {
        openTag("expression");

        compileTerm();

        while (isOp()) {
            writeToken(advance());   // operador binário
            compileTerm();
        }

        closeTag("expression");
    }

    /** Verifica se o token atual é um operador binário. */
    private boolean isOp() {
        if (cursor >= tokens.size()) return false;
        Token t = peek();
        if (!t.tag.getXmlTag().equals("symbol")) return false;
        return "+-*/&|<>=".contains(t.value);
    }

    /**
     * term: integerConstant | stringConstant | keywordConstant
     *     | varName | varName '[' expression ']'
     *     | subroutineCall
     *     | '(' expression ')'
     *     | unaryOp term
     *
     * keywordConstant: 'true' | 'false' | 'null' | 'this'
     * unaryOp: '-' | '~'
     */
    private void compileTerm() {
        openTag("term");

        Token t = peek();
        String xmlTag = t.tag.getXmlTag();

        if (xmlTag.equals("integerConstant") || xmlTag.equals("stringConstant")) {
            // constante numérica ou string
            writeToken(advance());

        } else if (t.value.equals("true") || t.value.equals("false")
                || t.value.equals("null") || t.value.equals("this")) {
            // keywordConstant
            writeToken(advance());

        } else if (t.value.equals("(")) {
            // expressão entre parênteses
            consume("(");
            compileExpression();
            consume(")");

        } else if (t.value.equals("-") || t.value.equals("~")) {
            // operador unário
            writeToken(advance());
            compileTerm();

        } else if (xmlTag.equals("identifier")) {
            // varName, varName[expr] ou subroutineCall
            Token next = peekNext();

            if (next != null && next.value.equals("[")) {
                // acesso a array: varName '[' expression ']'
                consumeType("identifier");
                consume("[");
                compileExpression();
                consume("]");

            } else if (next != null && (next.value.equals("(") || next.value.equals("."))) {
                // chamada de subrotina
                compileSubroutineCall();

            } else {
                // simples varName
                consumeType("identifier");
            }

        } else {
            throw new RuntimeException(
                "Erro sintático em term: token inesperado '" + t.value +
                "' (tipo: " + xmlTag + ") — token #" + cursor);
        }

        closeTag("term");
    }

    /**
     * subroutineCall: subroutineName '(' expressionList ')'
     *               | (className | varName) '.' subroutineName '(' expressionList ')'
     */
    private void compileSubroutineCall() {
        consumeType("identifier");   // subroutineName ou className/varName

        if (match(".")) {
            consume(".");
            consumeType("identifier");   // subroutineName
        }

        consume("(");
        compileExpressionList();
        consume(")");
    }

    /**
     * expressionList: (expression (',' expression)*)?
     * Pode ser vazia.
     */
    private void compileExpressionList() {
        openTag("expressionList");

        if (!match(")")) {
            compileExpression();

            while (match(",")) {
                consume(",");
                compileExpression();
            }
        }

        closeTag("expressionList");
    }
}
