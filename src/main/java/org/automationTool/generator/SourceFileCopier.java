package org.automationTool.generator;

import java.io.IOException;
import java.nio.file.*;
import java.util.Collection;

public class SourceFileCopier {

    public void copyFiles(Collection<String> filePaths, Path destination) throws IOException {

        for (String filePath : filePaths) {

            Path source = Paths.get(filePath);

            if (!Files.exists(source)) {
                System.out.println("File not found: " + filePath);
                continue;
            }

            Path target = destination.resolve(source.getFileName());

            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Copied: " + source.getFileName());
        }
    }
}