package com.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class PropertyFileExampleMain {
    public static void main(String[] args) throws IOException {
        final Path path = Paths.get("version.properties");
        if (Files.exists(path)) {
            final List<String> content = Files.readAllLines(path);
            System.out.println("""
                    +---------------------------+
                    | version.properties        |
                    +---------------------------+""");
            for (final String line : content) {
                System.out.println(line);
            }
        }
    }

    String getMessage() {
        return "Hello World!";
    }
}