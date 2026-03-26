package org.automationTool.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ServiceGenerator {

    public void generateService(Path servicePath,
                                String basePackage,
                                String entity,
                                List<String> methods) throws IOException {

        String className = entity + "Service";
        String repoName = entity + "Repository";

        StringBuilder content = new StringBuilder();

        content.append("package ").append(basePackage).append(".service;\n\n");

        content.append("import ").append(basePackage).append(".model.").append(entity).append(";\n");
        content.append("import ").append(basePackage).append(".repository.").append(repoName).append(";\n");
        content.append("import org.springframework.stereotype.Service;\n");
        content.append("import java.util.*;\n\n");

        content.append("@Service\n");
        content.append("public class ").append(className).append(" {\n\n");

        content.append("    private final ").append(repoName).append(" repository;\n\n");

        content.append("    public ").append(className).append("(").append(repoName).append(" repository) {\n");
        content.append("        this.repository = repository;\n");
        content.append("    }\n\n");

        // DEFAULT METHODS
        content.append("    public List<").append(entity).append("> findAll() {\n");
        content.append("        return repository.findAll();\n");
        content.append("    }\n\n");

        content.append("    public ").append(entity).append(" findById(Integer id) {\n");
        content.append("        return repository.findById(id).orElse(null);\n");
        content.append("    }\n\n");

        content.append("    public ").append(entity).append(" save(").append(entity).append(" entity) {\n");
        content.append("        return repository.save(entity);\n");
        content.append("    }\n\n");

        content.append("    public void deleteById(Integer id) {\n");
        content.append("        repository.deleteById(id);\n");
        content.append("    }\n\n");

        // 🔥 DYNAMIC METHODS FIX
        for (String method : methods) {

            try {
                String signature = method.replace("Page<", "List<");

                String methodName = signature.substring(signature.indexOf(" ") + 1, signature.indexOf("("));

                content.append("    public ").append(signature).append(" {\n");
                content.append("        return repository.").append(methodName).append("();\n");
                content.append("    }\n\n");

            } catch (Exception ignored) {}
        }

        content.append("}\n");

        Files.createDirectories(servicePath);
        Files.writeString(servicePath.resolve(className + ".java"), content.toString());
    }
}