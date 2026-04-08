import java.io.*;
import java.util.List;

public class XMLGenerator {

    public static void write(List<Token> tokens, String outputPath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outputPath), "UTF-8"))) {

            writer.write("<tokens>");
            writer.newLine();

            for (Token token : tokens) {
                if (token.tag == TokenType.EOF) continue;
                writer.write(token.toXML());
                writer.newLine();
            }

            writer.write("</tokens>");
            writer.newLine();
        }
    }
}
