package org.rhm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String filePath = "test.rhl";
        String content = readFile(filePath);

        if (content != null) {
            Lexer lexer = new Lexer();
            List<Lexer.Token> tokens = lexer.tokenize(content);
            System.out.println("Tokens: [");
            for (Lexer.Token token : tokens) {
                System.out.println("  " + token + ",");
            }
            System.out.println("]");

            Parser parser = new Parser(tokens);
            Interpreter interpreter = new Interpreter(parser);
            interpreter.interpret();
        }
    }

    private static String readFile(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return null;
        }
    }
}
