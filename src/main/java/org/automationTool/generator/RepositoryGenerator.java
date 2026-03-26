package org.automationTool.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class RepositoryGenerator {

    public void generateRepository(Path repoPath,
                                   String basePackage,
                                   String entity,
                                   List<String> methods) throws IOException {

        String repoName = entity + "Repository";

        StringBuilder content = new StringBuilder();

        content.append("package ").append(basePackage).append(".repository;\n\n");

        content.append("import ").append(basePackage).append(".model.").append(entity).append(";\n");
        content.append("import org.springframework.data.jpa.repository.JpaRepository;\n");
        content.append("import org.springframework.stereotype.Repository;\n");

        content.append("import org.springframework.data.domain.Page;\n");
        content.append("import org.springframework.data.domain.Pageable;\n");

        content.append("import java.util.*;\n\n");

        content.append("@Repository\n");
        content.append("public interface ").append(repoName)
                .append(" extends JpaRepository<")
                .append(entity).append(", Integer> {\n\n");

        // ✅ ADD DYNAMIC METHODS HERE
        for (String method : methods) {
            content.append("    ").append(method).append(";\n");
        }

        content.append("}\n");

        Files.createDirectories(repoPath);

        Path file = repoPath.resolve(repoName + ".java");
        Files.writeString(file, content.toString());

        System.out.println("Generated Repository: " + repoName);
    }
}