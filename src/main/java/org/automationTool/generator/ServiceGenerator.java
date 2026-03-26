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
        content.append("import org.springframework.data.domain.Page;\n");
        content.append("import org.springframework.data.domain.Pageable;\n");

        content.append("import java.util.List;\n");
        content.append("import java.util.Optional;\n\n");

        content.append("@Service\n");
        content.append("public class ").append(className).append(" {\n\n");

        content.append("    private final ").append(repoName).append(" repository;\n\n");

        content.append("    public ").append(className).append("(").append(repoName).append(" repository) {\n");
        content.append("        this.repository = repository;\n");
        content.append("    }\n\n");

        // -------- DEFAULT METHODS --------
        content.append("    public List<").append(entity).append("> findAll() {\n");
        content.append("        return repository.findAll();\n");
        content.append("    }\n\n");

        content.append("    public Page<").append(entity).append("> findAll(Pageable pageable) {\n");
        content.append("        return repository.findAll(pageable);\n");
        content.append("    }\n\n");

        content.append("    public Optional<").append(entity).append("> findById(Integer id) {\n");
        content.append("        return repository.findById(id);\n");
        content.append("    }\n\n");

        content.append("    public ").append(entity).append(" save(").append(entity).append(" entity) {\n");
        content.append("        return repository.save(entity);\n");
        content.append("    }\n\n");

        content.append("    public void deleteById(Integer id) {\n");
        content.append("        repository.deleteById(id);\n");
        content.append("    }\n\n");

        // -------- DYNAMIC METHODS --------
        for (String method : methods) {

            try {
                String signature = method.trim();

                // Example: List<PetType> findPetTypes()
                String returnType = signature.substring(0, signature.indexOf(" "));
                String rest = signature.substring(signature.indexOf(" ") + 1);

                String methodName = rest.substring(0, rest.indexOf("("));
                String params = rest.substring(rest.indexOf("(") + 1, rest.indexOf(")"));

                content.append("    public ").append(signature).append(" {\n");
                content.append("        return repository.").append(methodName).append("(");

                if (!params.isEmpty()) {
                    content.append(extractParamNames(params));
                }

                content.append(");\n");
                content.append("    }\n\n");

            } catch (Exception e) {
                System.out.println("Skipped invalid method: " + method);
            }
        }

        content.append("}\n");

        Files.createDirectories(servicePath);

        Path file = servicePath.resolve(className + ".java");
        Files.writeString(file, content.toString());

        System.out.println("Generated Service: " + className);
    }

    // -------- HELPER --------
    private String extractParamNames(String params) {

        String[] parts = params.split(",");
        StringBuilder names = new StringBuilder();

        for (String part : parts) {
            String[] tokens = part.trim().split(" ");
            names.append(tokens[tokens.length - 1]).append(",");
        }

        if (names.length() > 0) {
            names.setLength(names.length() - 1);
        }

        return names.toString();
    }
}