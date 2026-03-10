package org.automationTool.generator;

import java.io.IOException;
import java.nio.file.*;
import java.util.Collection;

public class SourceFileCopier {

    public void copyFiles(Collection<String> filePaths,
                          Path destination,
                          String newBasePackage) throws IOException {

        for (String filePath : filePaths) {

            Path source = Paths.get(filePath);

            if (!Files.exists(source)) {
                System.out.println("File not found: " + filePath);
                continue;
            }

            String content = Files.readString(source);
            content = content.replaceAll(
                    "package .*?;",
                    "package " + newBasePackage + ";"
            );

            Path target = destination.resolve(source.getFileName());

            Files.writeString(target, content);
        }
    }
}