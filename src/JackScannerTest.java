import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class JackScannerTest {

    private static final String INPUT_DIR    = "tests/Scanner";
    private static final String EXPECTED_DIR = "tests/expected";
    private static final String OUTPUT_DIR   = "tests/output";

    @BeforeAll
    static void criarDiretorioSaida() {
        new File(OUTPUT_DIR).mkdirs();
    }

    @AfterEach
    void limparArquivosTemporarios() {
        // Nenhum arquivo temporário gerado pelos testes unitários — nada a limpar
    }

    // -------------------------------------------------------------------------
    // Utilitários
    // -------------------------------------------------------------------------

    private String normalizeXml(String content) {
        return content.lines()
                .map(String::stripLeading)
                .filter(line -> !line.isBlank())
                .collect(Collectors.joining("\n"));
    }

    private void runScanner(String inputFile, String outputFile) throws Exception {
        String source = Files.readString(Paths.get(inputFile));
        JackScanner scanner = new JackScanner(source);
        List<Token> tokens  = scanner.tokenize();
        XMLGenerator.write(tokens, outputFile);
    }

    private String readNormalized(String path) throws IOException {
        return normalizeXml(Files.readString(Paths.get(path)));
    }

    // -------------------------------------------------------------------------
    // Testes unitários (strings em memória)
    // -------------------------------------------------------------------------

    @Test
    void testNumeroBasico() {
        JackScanner scanner = new JackScanner("444");
        List<Token> tokens = scanner.tokenize();
        assertEquals("<integerConstant> 444 </integerConstant>", tokens.get(0).toXML());
    }

    @Test
    void testNumerosComXml() {
        String[][] casos = {
            { "2",     "<integerConstant> 2 </integerConstant>"   },
            { "444",   "<integerConstant> 444 </integerConstant>" },
            { "46",    "<integerConstant> 46 </integerConstant>"  },
            { " 132 ", "<integerConstant> 132 </integerConstant>" },
        };
        for (String[] caso : casos) {
            JackScanner scanner = new JackScanner(caso[0]);
            List<Token> tokens = scanner.tokenize();
            assertEquals(caso[1], tokens.get(0).toXML(), "Falhou para: " + caso[0]);
        }
    }

    @Test
    void testStringBasica() {
        JackScanner scanner = new JackScanner("\"ola\"");
        List<Token> tokens = scanner.tokenize();
        assertEquals(TokenType.STRING, tokens.get(0).tag);
        assertEquals("ola",           tokens.get(0).value);
        assertEquals("<stringConstant> ola </stringConstant>", tokens.get(0).toXML());
    }

    @Test
    void testStringComEspacos() {
        JackScanner scanner = new JackScanner("\"hello world\"");
        List<Token> tokens = scanner.tokenize();
        assertEquals("<stringConstant> hello world </stringConstant>", tokens.get(0).toXML());
    }

    @Test
    void testIdentificadoresEKeywords() {
        JackScanner scanner = new JackScanner("minhaVar123");
        List<Token> tokens = scanner.tokenize();
        assertEquals(TokenType.IDENT, tokens.get(0).tag);
        assertEquals("minhaVar123",   tokens.get(0).value);
        assertEquals("<identifier> minhaVar123 </identifier>", tokens.get(0).toXML());

        scanner = new JackScanner("function");
        tokens  = scanner.tokenize();
        assertEquals(TokenType.FUNCTION, tokens.get(0).tag);
        assertEquals("function",         tokens.get(0).value);
        assertEquals("<keyword> function </keyword>", tokens.get(0).toXML());
    }

    @Test
    void testTodasAsKeywords() {
        String[] kws = {
            "class", "constructor", "function", "method", "field", "static",
            "var", "int", "char", "boolean", "void", "true", "false", "null",
            "this", "let", "do", "if", "else", "while", "return"
        };
        for (String kw : kws) {
            JackScanner scanner = new JackScanner(kw);
            List<Token> tokens = scanner.tokenize();
            assertEquals("keyword", tokens.get(0).tag.getXmlTag(), "Deveria ser keyword: " + kw);
        }
    }

    @Test
    void testIdentificadorComUnderscore() {
        JackScanner scanner = new JackScanner("minha_var");
        List<Token> tokens = scanner.tokenize();
        assertEquals(TokenType.IDENT, tokens.get(0).tag);
        assertEquals("minha_var",     tokens.get(0).value);
    }

    @Test
    void testSimbolosXml() {
        JackScanner scanner = new JackScanner("x + y;");
        List<Token> tokens = scanner.tokenize();
        List<Token> semEof = tokens.stream().filter(t -> t.tag != TokenType.EOF).toList();
        String[] esperado = {
            "<identifier> x </identifier>",
            "<symbol> + </symbol>",
            "<identifier> y </identifier>",
            "<symbol> ; </symbol>",
        };
        for (int i = 0; i < esperado.length; i++) {
            assertEquals(esperado[i], semEof.get(i).toXML(), "Token " + i + " não corresponde");
        }
    }

    @Test
    void testEscapeXml() {
        JackScanner scanner = new JackScanner("a < b");
        List<Token> tokens = scanner.tokenize();
        Token lt = tokens.stream().filter(t -> t.value.equals("<")).findFirst().orElseThrow();
        assertEquals("<symbol> &lt; </symbol>", lt.toXML());

        scanner = new JackScanner("a > b");
        tokens  = scanner.tokenize();
        Token gt = tokens.stream().filter(t -> t.value.equals(">")).findFirst().orElseThrow();
        assertEquals("<symbol> &gt; </symbol>", gt.toXML());

        scanner = new JackScanner("a & b");
        tokens  = scanner.tokenize();
        Token amp = tokens.stream().filter(t -> t.value.equals("&")).findFirst().orElseThrow();
        assertEquals("<symbol> &amp; </symbol>", amp.toXML());
    }

    @Test
    void testComentarioLinhaIgnorado() {
        JackScanner scanner = new JackScanner("let x = 5; // isto some");
        List<Token> tokens = scanner.tokenize().stream().filter(t -> t.tag != TokenType.EOF).toList();
        boolean temComentario = tokens.stream().anyMatch(t -> t.value.contains("//") || t.value.contains("isto"));
        assertFalse(temComentario, "Comentário não deveria gerar token");
        List<String> values = tokens.stream().map(t -> t.value).toList();
        assertTrue(values.contains("let"));
        assertTrue(values.contains("x"));
        assertTrue(values.contains("5"));
    }

    @Test
    void testComentarioBlocoIgnorado() {
        JackScanner scanner = new JackScanner("let /* ignora tudo aqui */ x = 1;");
        List<Token> tokens = scanner.tokenize().stream().filter(t -> t.tag != TokenType.EOF).toList();
        List<String> values = tokens.stream().map(t -> t.value).toList();
        assertTrue(values.contains("let"));
        assertTrue(values.contains("x"));
        assertTrue(values.contains("1"));
        assertFalse(values.contains("ignora"));
    }

    @Test
    void testComentarioJavadocIgnorado() {
        String code = "/** Descrição da classe */\nclass Main {}";
        JackScanner scanner = new JackScanner(code);
        List<Token> tokens = scanner.tokenize();
        assertEquals(TokenType.CLASS, tokens.get(0).tag);
        assertEquals("class",         tokens.get(0).value);
    }

    @Test
    void testCodigoJackCompleto() {
        String code = """
            class Main {
                function void main() {
                    let x = 5;
                    return;
                }
            }
            """;
        JackScanner scanner = new JackScanner(code);
        List<Token> tokens = scanner.tokenize();
        List<String> tags   = tokens.stream().map(t -> t.tag.getXmlTag()).toList();
        List<String> values = tokens.stream().map(t -> t.value).toList();
        assertTrue(tags.contains("keyword"));
        assertTrue(tags.contains("identifier"));
        assertTrue(tags.contains("symbol"));
        assertTrue(tags.contains("integerConstant"));
        assertTrue(values.contains("Main"));
        assertTrue(values.contains("x"));
        assertTrue(values.contains("5"));
    }

    @Test
    void testExpressaoComEscape() {
        String code = "if (x < 0) { let sign = \"negative\"; }";
        JackScanner scanner = new JackScanner(code);
        List<Token> tokens = scanner.tokenize().stream().filter(t -> t.tag != TokenType.EOF).toList();
        assertEquals("<keyword> if </keyword>",                      tokens.get(0).toXML());
        assertEquals("<symbol> ( </symbol>",                         tokens.get(1).toXML());
        assertEquals("<identifier> x </identifier>",                tokens.get(2).toXML());
        assertEquals("<symbol> &lt; </symbol>",                      tokens.get(3).toXML());
        assertEquals("<integerConstant> 0 </integerConstant>",      tokens.get(4).toXML());
        assertEquals("<symbol> ) </symbol>",                         tokens.get(5).toXML());
        assertEquals("<symbol> { </symbol>",                         tokens.get(6).toXML());
        assertEquals("<keyword> let </keyword>",                     tokens.get(7).toXML());
        assertEquals("<identifier> sign </identifier>",             tokens.get(8).toXML());
        assertEquals("<symbol> = </symbol>",                         tokens.get(9).toXML());
        assertEquals("<stringConstant> negative </stringConstant>",  tokens.get(10).toXML());
        assertEquals("<symbol> ; </symbol>",                         tokens.get(11).toXML());
        assertEquals("<symbol> } </symbol>",                         tokens.get(12).toXML());
    }

    // -------------------------------------------------------------------------
    // Testes de validação contra arquivos oficiais do nand2tetris
    // -------------------------------------------------------------------------

    @Test
    void testMainScannerContraOficial() throws Exception {
        String output   = OUTPUT_DIR   + "/MainT.xml";
        String expected = EXPECTED_DIR + "/MainT.xml";

        runScanner(INPUT_DIR + "/Main.jack", output);

        assertTrue(new File(expected).exists(),
                "Arquivo esperado não encontrado: " + expected);

        assertEquals(readNormalized(expected), readNormalized(output),
                "Saída do scanner para Main.jack não corresponde ao arquivo oficial.");
    }

    @Test
    void testSquareScannerContraOficial() throws Exception {
        String output   = OUTPUT_DIR   + "/SquareT.xml";
        String expected = EXPECTED_DIR + "/SquareT.xml";

        runScanner(INPUT_DIR + "/Square.jack", output);

        assertTrue(new File(expected).exists(),
                "Arquivo esperado não encontrado: " + expected);

        assertEquals(readNormalized(expected), readNormalized(output),
                "Saída do scanner para Square.jack não corresponde ao arquivo oficial.");
    }

    @Test
    void testSquareGameScannerContraOficial() throws Exception {
        String output   = OUTPUT_DIR   + "/SquareGameT.xml";
        String expected = EXPECTED_DIR + "/SquareGameT.xml";

        runScanner(INPUT_DIR + "/SquareGame.jack", output);

        assertTrue(new File(expected).exists(),
                "Arquivo esperado não encontrado: " + expected);

        assertEquals(readNormalized(expected), readNormalized(output),
                "Saída do scanner para SquareGame.jack não corresponde ao arquivo oficial.");
    }
}
