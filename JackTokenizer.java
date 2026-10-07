package jackcompiler;

import java.io.File;
import java.io.IOException;

public class JackTokenizer {

    private final File inputFile;

    public JackTokenizer(String filePath) {
        this.inputFile = new File(filePath);
    }

    public void tokenize() throws IOException {
        if (!inputFile.exists()) {
            throw new IOException("Arquivo não encontrado: " + inputFile.getPath());
        }

        System.out.println("Arquivo carregado: " + inputFile.getPath());
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java JackTokenizer <arquivo.jack>");
            return;
        }

        try {
            JackTokenizer tokenizer = new JackTokenizer(args[0]);
            tokenizer.tokenize();
        } catch (IOException e) {
            System.err.println("Erro: " + e.getMessage());
        }
    }
}
:::
