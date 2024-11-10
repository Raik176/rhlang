package org.rhm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        if (args.length > 0) {
            String filePath = args[0];
            String content = readFile(filePath);

            if (content != null) {
                Lexer lexer = new Lexer();
                List<Lexer.Token<?>> tokens = lexer.tokenize(content);
                displayTokens(tokens);

                Parser parser = new Parser(tokens);
                Interpreter interpreter = new Interpreter(parser);
                interpreter.interpret();
            }
        } else {
            enterInteractiveMode();
        }
    }

    private static String readFile(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            logger.error("Error reading file: {}", filePath, e);  // Using SLF4J for error logging
            return null;
        }
    }

    private static void enterInteractiveMode() {
        Scanner scanner = new Scanner(System.in);
        logger.info("Entering interactive mode. Type 'exit' to quit.");  // Using SLF4J for info logging

        while (true) {
            System.out.print(">>> ");
            String input = scanner.nextLine().trim();

            if ("exit".equalsIgnoreCase(input)) {
                logger.info("Exiting interactive mode.");  // Using SLF4J for info logging
                break;
            }

            if (!input.isEmpty()) {
                Lexer lexer = new Lexer();
                List<Lexer.Token<?>> tokens = lexer.tokenize(input);
                displayTokens(tokens);

                Parser parser = new Parser(tokens);
                Interpreter interpreter = new Interpreter(parser);
                interpreter.interpret();
            }
        }

        scanner.close();
    }

    private static void displayTokens(List<Lexer.Token<?>> tokens) {
        final int indexWidth = 10;
        final int typeWidth = 25;
        final int valueWidth = 25;
        final int valueTypeWidth = 25;
        final String separator = String.format("%0" + (indexWidth + typeWidth + valueWidth + valueTypeWidth + 3) + "d", 0).replace('0', '-');
        final String RESET = "\u001B[0m";

        logger.debug("Displaying {} tokens.", tokens.size());  // Using SLF4J for debug logging
        System.out.println("\nTokens:");
        System.out.printf("%-" + indexWidth + "s %-" + typeWidth + "s %-" + valueWidth + "s %-" + valueTypeWidth + "s%n", "Index", "Type", "Value", "Value Type");

        System.out.println(separator);

        for (int i = 0; i < tokens.size(); i++) {
            Lexer.Token<?> token = tokens.get(i);
            Object value = token.getAs(Object.class);
            String valueType = value != null ? value.getClass().getName() : "null";

            String color = value != null ? generateColorFromString(value) : "";

            System.out.printf("%-" + indexWidth + "d %-" + typeWidth + "s %-" + valueWidth + "s %-" + valueTypeWidth + "s%n",
                    i,
                    token.type,
                    token.getAs(Object.class),
                    color + valueType + RESET);
        }

        System.out.println(separator);
        System.out.println();
    }

    private static String generateColorFromString(Object input) {
        int hash = input.getClass().getName().hashCode();

        int red = (hash >> 16) & 0xFF;
        int green = (hash >> 8) & 0xFF;
        int blue = hash & 0xFF;

        red = (red + 256) % 256;
        green = (green + 256) % 256;
        blue = (blue + 256) % 256;

        return String.format("\u001B[38;2;%d;%d;%dm", red, green, blue);
    }
}