package org.automationTool.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProjectStructureCreator {

    private static final String OUTPUT_DIR = "generated-services";

    public Path createStructure(String serviceName) throws IOException {

        String cleanName = serviceName.toLowerCase();

        Path root = Paths.get(OUTPUT_DIR, cleanName);

        Files.createDirectories(root.resolve("src/main/java"));
        Files.createDirectories(root.resolve("src/main/resources"));

        return root;
    }
}