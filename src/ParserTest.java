import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes do analisador sintático (parser) da linguagem Jack.
 *
 * Estratégia: normaliza o XML removendo espaços no início de cada linha
 * antes de comparar, eliminando falsos negativos por indentação.
 *
 * Pré-requisito: os arquivos de referência devem estar em tests/expected/
 *   - tests/expected/MainP.xml
 *   - tests/expected/SquareP.xml
 *   - tests/expected/SquareGameP.xml
 *
 * Os arquivos de saída gerados ficam em tests/output/ (criado automaticamente).
 */
public class ParserTest {

    private static final String INPUT_DIR    = "tests/Square";
    private static final String EXPECTED_DIR = "tests/expected";
    private static final String OUTPUT_DIR   = "tests/output";

    @BeforeAll
    static void criarDiretorioSaida() {
        new File(OUTPUT_DIR).mkdirs();
    }

    // Lista dos arquivos temporários gerados pelos testes unitários
    private static final String[] ARQUIVOS_TEMPORARIOS = {
        OUTPUT_DIR + "/VaziaP.xml",
        OUTPUT_DIR + "/TesteVarP.xml",
        OUTPUT_DIR + "/TesteVoidP.xml",
        OUTPUT_DIR + "/TesteIfElseP.xml",
        OUTPUT_DIR + "/TesteWhileP.xml",
        OUTPUT_DIR + "/TesteExprP.xml",
        OUTPUT_DIR + "/TesteChamadaP.xml",
        OUTPUT_DIR + "/TesteArrayP.xml"
    };

    @AfterEach
    void limparArquivosTemporarios() {
        for (String path : ARQUIVOS_TEMPORARIOS) {
            File f = new File(path);
            if (f.exists()) f.delete();
        }
    }

    // -------------------------------------------------------------------------
    // Utilitários
    // -------------------------------------------------------------------------

    /**
     * Normaliza o XML removendo espaços iniciais de cada linha e
     * descartando linhas em branco — elimina diferenças de indentação.
     */
    private String normalizeXml(String content) {
        return content.lines()
                .map(String::stripLeading)
                .filter(line -> !line.isBlank())
                .collect(Collectors.joining("\n"));
    }

    /**
     * Executa o pipeline completo: scanner → parser → gera XxxP.xml.
     *
     * @param inputFile  Caminho do arquivo .jack de entrada.
     * @param outputFile Caminho do arquivo XML de saída gerado pelo parser.
     */
    private void runParser(String inputFile, String outputFile) throws Exception {
        String source = Files.readString(Paths.get(inputFile));
        JackScanner scanner = new JackScanner(source);
        List<Token> tokens  = scanner.tokenize();
        CompilationEngine engine = new CompilationEngine(tokens, outputFile);
        engine.compileClass();
    }

    /**
     * Lê e normaliza um arquivo de texto.
     */
    private String readNormalized(String path) throws IOException {
        return normalizeXml(Files.readString(Paths.get(path)));
    }

    // -------------------------------------------------------------------------
    // Testes unitários de construções isoladas
    // -------------------------------------------------------------------------

    @Test
    void testClassVazia() {
        // Uma classe sem campos nem métodos deve ser parseada sem erro
        String code = "class Vazia { }";
        assertDoesNotThrow(() -> {
            JackScanner scanner = new JackScanner(code);
            CompilationEngine engine = new CompilationEngine(
                    scanner.tokenize(), OUTPUT_DIR + "/VaziaP.xml");
            engine.compileClass();
        });
    }

    @Test
    void testVariavelDeClasse() {
        String code = "class Teste { field int x; static boolean flag; }";
        assertDoesNotThrow(() -> {
            JackScanner scanner = new JackScanner(code);
            CompilationEngine engine = new CompilationEngine(
                    scanner.tokenize(), OUTPUT_DIR + "/TesteVarP.xml");
            engine.compileClass();
        });
    }

    @Test
    void testFuncaoRetornoVoid() {
        String code = """
                class Teste {
                    function void nada() {
                        return;
                    }
                }
                """;
        assertDoesNotThrow(() -> {
            JackScanner scanner = new JackScanner(code);
            CompilationEngine engine = new CompilationEngine(
                    scanner.tokenize(), OUTPUT_DIR + "/TesteVoidP.xml");
            engine.compileClass();
        });
    }

    @Test
    void testIfElse() {
        String code = """
                class Teste {
                    function void checagem(int x) {
                        if (x) {
                            return;
                        } else {
                            return;
                        }
                    }
                }
                """;
        assertDoesNotThrow(() -> {
            JackScanner scanner = new JackScanner(code);
            CompilationEngine engine = new CompilationEngine(
                    scanner.tokenize(), OUTPUT_DIR + "/TesteIfElseP.xml");
            engine.compileClass();
        });
    }

    @Test
    void testWhile() {
        String code = """
                class Teste {
                    function void loop(int n) {
                        while (n) {
                            let n = n;
                        }
                        return;
                    }
                }
                """;
        assertDoesNotThrow(() -> {
            JackScanner scanner = new JackScanner(code);
            CompilationEngine engine = new CompilationEngine(
                    scanner.tokenize(), OUTPUT_DIR + "/TesteWhileP.xml");
            engine.compileClass();
        });
    }

    @Test
    void testExpressaoComOperadores() {
        String code = """
                class Teste {
                    function int calc(int a, int b) {
                        var int resultado;
                        let resultado = a + b;
                        return resultado;
                    }
                }
                """;
        assertDoesNotThrow(() -> {
            JackScanner scanner = new JackScanner(code);
            CompilationEngine engine = new CompilationEngine(
                    scanner.tokenize(), OUTPUT_DIR + "/TesteExprP.xml");
            engine.compileClass();
        });
    }

    @Test
    void testChamadaDeMetodo() {
        String code = """
                class Teste {
                    function void chamar() {
                        do Output.printString("oi");
                        return;
                    }
                }
                """;
        assertDoesNotThrow(() -> {
            JackScanner scanner = new JackScanner(code);
            CompilationEngine engine = new CompilationEngine(
                    scanner.tokenize(), OUTPUT_DIR + "/TesteChamadaP.xml");
            engine.compileClass();
        });
    }

    @Test
    void testAcessoArray() {
        String code = """
                class Teste {
                    function void arr(Array a) {
                        var int x;
                        let x = a[0];
                        return;
                    }
                }
                """;
        assertDoesNotThrow(() -> {
            JackScanner scanner = new JackScanner(code);
            CompilationEngine engine = new CompilationEngine(
                    scanner.tokenize(), OUTPUT_DIR + "/TesteArrayP.xml");
            engine.compileClass();
        });
    }

    // -------------------------------------------------------------------------
    // Testes de validação contra arquivos oficiais do nand2tetris
    // -------------------------------------------------------------------------

    @Test
    void testMainParserContraOficial() throws Exception {
        String output   = OUTPUT_DIR   + "/MainP.xml";
        String expected = EXPECTED_DIR + "/MainP.xml";

        runParser(INPUT_DIR + "/Main.jack", output);

        assertTrue(new File(expected).exists(),
                "Arquivo esperado não encontrado: " + expected);

        assertEquals(readNormalized(expected), readNormalized(output),
                "Saída do parser para Main.jack não corresponde ao arquivo oficial.");
    }

    @Test
    void testSquareParserContraOficial() throws Exception {
        String output   = OUTPUT_DIR   + "/SquareP.xml";
        String expected = EXPECTED_DIR + "/SquareP.xml";

        runParser(INPUT_DIR + "/Square.jack", output);

        assertTrue(new File(expected).exists(),
                "Arquivo esperado não encontrado: " + expected);

        assertEquals(readNormalized(expected), readNormalized(output),
                "Saída do parser para Square.jack não corresponde ao arquivo oficial.");
    }

    @Test
    void testSquareGameParserContraOficial() throws Exception {
        String output   = OUTPUT_DIR   + "/SquareGameP.xml";
        String expected = EXPECTED_DIR + "/SquareGameP.xml";

        runParser(INPUT_DIR + "/SquareGame.jack", output);

        assertTrue(new File(expected).exists(),
                "Arquivo esperado não encontrado: " + expected);

        assertEquals(readNormalized(expected), readNormalized(output),
                "Saída do parser para SquareGame.jack não corresponde ao arquivo oficial.");
    }
}
