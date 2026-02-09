package org.automationTool.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class JavaFileScanner {
    public static List<Path> scanJavaFiles(Path root) throws IOException {
        return Files.walk(root)
                .filter(p -> p.toString().endsWith(".java"))
                .filter(p -> !p.toString().contains("/src/test/"))
                .collect(Collectors.toList());
    }
}
