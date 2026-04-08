import java.io.*;
import java.nio.file.*;
import java.util.*;

public class JackAnalyzer {

    public static void main(String[] args) throws Exception {

        System.out.println("Diretório atual: " + new File(".").getAbsolutePath());

        if (args.length < 1) {
            System.out.println("Nenhum argumento informado.");
            System.out.println("Uso: JackAnalyzer <arquivo.jack | diretório>");
            System.out.println("Exemplo de argumento: tests");
            System.exit(1);
        }

        File target = new File(args[0]);

        if (!target.exists()) {
            target = new File(new File(".").getAbsolutePath() + File.separator + args[0]);
        }

        if (!target.exists()) {
            System.err.println("Erro: '" + args[0] + "' não encontrado.");
            System.exit(1);
        }

        List<File> jackFiles = new ArrayList<>();

        if (target.isDirectory()) {
            File[] listed = target.listFiles(f -> f.getName().endsWith(".jack"));
            if (listed != null) jackFiles.addAll(Arrays.asList(listed));
        } else if (target.getName().endsWith(".jack")) {
            jackFiles.add(target);
        } else {
            System.err.println("Erro: informe um arquivo .jack ou um diretório.");
            System.exit(1);
        }

        if (jackFiles.isEmpty()) {
            System.err.println("Nenhum arquivo .jack encontrado em: " + target.getAbsolutePath());
            System.exit(1);
        }

        for (File jackFile : jackFiles) {
            processFile(jackFile);
        }
    }

    private static void processFile(File jackFile) {
        try {
            String source = Files.readString(jackFile.toPath());

            JackScanner scanner = new JackScanner(source);
            List<Token> tokens = scanner.tokenize();

            String baseName   = jackFile.getName().replace(".jack", "");
            String outputPath = jackFile.getParent() + File.separator + baseName + "T.xml";

            XMLGenerator.write(tokens, outputPath);
            System.out.println("✓ " + jackFile.getName() + " → " + baseName + "T.xml");

        } catch (IOException e) {
            System.err.println("✗ Erro: " + e.getMessage());
        }
    }
}
