package org.automationTool.generator;

import java.io.IOException;
import java.nio.file.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SourceFileCopier {

    private final Set<String> copiedClasses = new HashSet<>();

    public void copyFiles(Collection<String> filePaths,
                          Path destination,
                          String targetPackage) throws IOException {

        String basePackage = targetPackage.substring(0, targetPackage.lastIndexOf("."));

        for (String filePath : filePaths) {

            Path source = Paths.get(filePath);

            if (!Files.exists(source)) {
                System.out.println("File not found: " + filePath);
                continue;
            }

            String fileName = source.getFileName().toString();

            if (copiedClasses.contains(fileName)) continue;
            copiedClasses.add(fileName);

            if (fileName.equals("application.properties")) continue;

            String content = Files.readString(source);

            String finalPackage;

            if (fileName.endsWith(".java")) {

                if (fileName.endsWith("Repository.java")) continue;

                // -------- PACKAGE --------
                if (fileName.endsWith("Controller.java")) {
                    finalPackage = basePackage + ".controller";
                } else if (fileName.endsWith("Service.java") || fileName.endsWith("ServiceImpl.java")) {
                    finalPackage = basePackage + ".service";
                } else {
                    finalPackage = basePackage + ".model";
                }

                content = content.replaceFirst(
                        "package\\s+[^;]+;",
                        "package " + finalPackage + ";"
                );

                content = fixImports(content, basePackage);

                // -------- ENTITY FIX --------
                if (content.contains("@Table"))
                    content = addImport(content, "jakarta.persistence.Table");

                if (content.contains("@EmbeddedId"))
                    content = addImport(content, "jakarta.persistence.EmbeddedId");

                if (content.contains("@Embeddable"))
                    content = addImport(content, "jakarta.persistence.Embeddable");

                if (content.contains("@MappedSuperclass"))
                    content = addImport(content, "jakarta.persistence.MappedSuperclass");

                if (content.contains("@Pattern"))
                    content = addImport(content, "jakarta.validation.constraints.Pattern");

                // ================= CONTROLLER FIX =================
                if (finalPackage.contains(".controller")) {

                    // Convert to REST controller
                    content = content.replace("@Controller", "@RestController");

                    content = content.replaceAll(
                            "import\\s+org\\.springframework\\.stereotype\\.Controller;",
                            ""
                    );

                    content = addImport(content, "org.springframework.web.bind.annotation.RestController");

                    // Replace repository → service
                    content = content.replaceAll("\\b(\\w+)Repository\\b", "$1Service");

                    // Remove repository imports
                    content = content.replaceAll("import\\s+.*Repository;", "");

                    // Add required imports
                    content = addImport(content, basePackage + ".service.*");
                    content = addImport(content, basePackage + ".model.*");

                    // Remove UI imports
                    content = content.replaceAll(
                            "import\\s+org\\.springframework\\.ui\\.[^;]+;",
                            ""
                    );

                    // ADD RequestBody import
                    content = addImport(content, "org.springframework.web.bind.annotation.RequestBody");

                    // FIX POST METHODS (SAFE)
                    content = content.replaceAll(
                            "@PostMapping\\s*\\n\\s*public\\s+(\\w+)\\s+(\\w+)\\((\\w+)\\s+(\\w+)\\)",
                            "@PostMapping\npublic $1 $2(@RequestBody $3 $4)"
                    );
                }

                // -------- ANNOTATIONS --------
                if (content.contains("@GetMapping"))
                    content = addImport(content, "org.springframework.web.bind.annotation.GetMapping");

                if (content.contains("@PostMapping"))
                    content = addImport(content, "org.springframework.web.bind.annotation.PostMapping");

                if (content.contains("@PutMapping"))
                    content = addImport(content, "org.springframework.web.bind.annotation.PutMapping");

                if (content.contains("@DeleteMapping"))
                    content = addImport(content, "org.springframework.web.bind.annotation.DeleteMapping");

                if (content.contains("@Autowired"))
                    content = addImport(content, "org.springframework.beans.factory.annotation.Autowired");

                // -------- CLEAN JAXB --------
                content = content.replaceAll(
                        "import\\s+jakarta\\.xml\\.bind\\.annotation\\.[^;]+;",
                        ""
                );

                content = content.replaceAll("@Xml[^\\n]*", "");
                content = content.replaceAll("@NotBlank", "");

                // -------- AUTO ENTITY --------
                if (finalPackage.contains(".model")
                        && content.contains("class")
                        && !content.contains("@Entity")
                        && !content.contains("@MappedSuperclass")
                        && !content.contains("@Embeddable")
                        && (content.contains("@Id") || content.contains("@EmbeddedId"))) {

                    content = content.replaceFirst(
                            "(public class)",
                            "@Entity\n$1"
                    );

                    content = addImport(content, "jakarta.persistence.Entity");
                }

                validate(content, fileName);

                if (!content.trim().endsWith("}")) {
                    content += "\n}";
                }
            }

            Path target = destination.resolve(fileName);

            Files.createDirectories(target.getParent());
            Files.writeString(target, content);

            System.out.println("Copied: " + target);
        }
    }

    private void validate(String content, String fileName) {
        long open = content.chars().filter(c -> c == '{').count();
        long close = content.chars().filter(c -> c == '}').count();

        if (open != close) {
            throw new RuntimeException("Brace mismatch in: " + fileName);
        }
    }

    private String fixImports(String content, String basePackage) {

        content = content.replaceAll(
                "org\\.springframework\\.samples\\.petclinic\\.model\\.",
                basePackage + ".model."
        );

        content = content.replaceAll(
                "org\\.springframework\\.samples\\.petclinic\\.repository\\.",
                basePackage + ".repository."
        );

        content = content.replaceAll(
                "org\\.springframework\\.samples\\.petclinic\\.service\\.",
                basePackage + ".service."
        );

        return content;
    }

    private String addImport(String content, String importStmt) {

        if (content.contains("import " + importStmt + ";")) return content;

        if (content.contains("import ")) {
            return content.replaceFirst("(import .*?;)", "$1\nimport " + importStmt + ";");
        } else {
            return content.replaceFirst("(package .*?;)", "$1\nimport " + importStmt + ";");
        }
    }

    public void copyResourceDirectory(Path sourceDir, Path targetDir) throws IOException {

        if (!Files.exists(sourceDir)) return;

        Files.walk(sourceDir).forEach(sourcePath -> {
            try {
                Path targetPath = targetDir.resolve(sourceDir.relativize(sourcePath));

                if (Files.isDirectory(sourcePath)) {
                    Files.createDirectories(targetPath);
                } else {
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }

            } catch (IOException ignored) {}
        });
    }
}