package org.automationTool.util;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassIndex {

    private final Map<String, Path> index = new HashMap<>();

    public ClassIndex(List<Path> javaFiles) {
        for (Path file : javaFiles) {
            String name = file.getFileName().toString();
            if (name.endsWith(".java")) {
                index.put(name.replace(".java", ""), file);
            }
        }
    }

    public Path getClassFile(String className) {
        return index.get(className);
    }
}