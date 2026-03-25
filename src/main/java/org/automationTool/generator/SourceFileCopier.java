package org.automationTool.generator;

import java.io.IOException;
import java.nio.file.*;
import java.util.Collection;

public class SourceFileCopier {

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

            // FINAL FIX: skip ANY repository file by name
            if (fileName.endsWith("Repository.java")) {
                continue;
            }
            if (source.getFileName().toString().equals("application.properties")) {
                continue;
            }

            String content = Files.readString(source);

            if (source.toString().endsWith(".java")) {

                // fix package
                content = content.replaceFirst(
                        "package\\s+[^;]+;",
                        "package " + targetPackage + ";"
                );

                // fix imports
                content = fixImports(content, basePackage, targetPackage);

                // controller transformation
                if (targetPackage.contains(".controller")) {

                    content = content.replace("@Controller", "@RestController");

                    content = content.replaceAll("\\b(\\w+)Repository\\b", "$1Service");
                    content = content.replaceAll("\\brepository\\b", "service");

                    content = content.replaceAll("import\\s+.*Repository;", "");

                    content = content.replaceAll(
                            "import\\s+org\\.springframework\\.stereotype\\.Controller;",
                            ""
                    );

                    content = addImport(content, basePackage + ".service.*");
                    content = addImport(content, basePackage + ".model.*");
                    content = addImport(content, "org.springframework.web.bind.annotation.RestController");

                    content = content.replaceAll(
                            "import\\s+org\\.springframework\\.ui\\.[^;]+;",
                            ""
                    );

                    content = content.replaceAll(
                            "import\\s+org\\.springframework\\.ui\\.Model;",
                            ""
                    );

                    content = content.replaceAll(
                            "(?s)(public|private)\\s+String\\s+\\w+\\([^)]*Model[^)]*\\)\\s*\\{(?:[^{}]*|\\{[^{}]*\\})*\\}",
                            ""
                    );

                    content = content.replaceAll(
                            "(?s)\\w+\\s+\\w+\\([^)]*Model[^)]*\\)\\s*\\{.*?\\}",
                            ""
                    );

                    content = content.replaceAll(
                            "return\\s+\"[^\"]*\";",
                            ""
                    );

                    content = content.replaceAll(
                            "return\\s+addPaginationModel\\([^;]+;",
                            "return \"\";"
                    );

                    content = content.replaceAll(
                            "import\\s+org\\.springframework\\.ui\\.;",
                            ""
                    );

                    // prevent Model compile error
                    content = content.replaceAll("\\bModel\\b", "Object");
                }

                if (content.contains("@GetMapping"))
                    content = addImport(content, "org.springframework.web.bind.annotation.GetMapping");

                if (content.contains("@PostMapping"))
                    content = addImport(content, "org.springframework.web.bind.annotation.PostMapping");

                if (content.contains("@Autowired"))
                    content = addImport(content, "org.springframework.beans.factory.annotation.Autowired");

                // remove JAXB
                content = content.replaceAll(
                        "import\\s+jakarta\\.xml\\.bind\\.annotation\\.[^;]+;",
                        ""
                );
                content = content.replaceAll("@Xml[^\\n]*", "");

                // remove validation
                content = content.replaceAll(
                        "import\\s+jakarta\\.validation\\.constraints\\.[^;]+;",
                        ""
                );
                content = content.replaceAll("@NotBlank", "");

                // ensure entity
                if (targetPackage.contains(".model")
                        && content.contains("class")
                        && !content.contains("@Entity")) {

                    content = content.replaceFirst(
                            "(public class)",
                            "@Entity\n$1"
                    );

                    content = addImport(content, "jakarta.persistence.Entity");
                }

                if (!content.trim().endsWith("}")) {
                    content = content + "\n}";
                }
            }

            Path target = destination.resolve(source.getFileName());
            Files.createDirectories(target.getParent());
            Files.writeString(target, content);

            System.out.println("Copied: " + source.getFileName());
        }
    }

    private String fixImports(String content, String basePackage, String targetPackage) {

        content = content.replaceAll(
                "import\\s+org\\.springframework\\.samples\\.petclinic\\.model\\.(\\w+);",
                "import " + basePackage + ".model.$1;"
        );

        content = content.replaceAll(
                "import\\s+org\\.springframework\\.samples\\.petclinic\\.service\\.(\\w+);",
                "import " + basePackage + ".service.$1;"
        );

        content = content.replaceAll(
                "import\\s+org\\.springframework\\.samples\\.petclinic\\.repository\\.(\\w+);",
                "import " + basePackage + ".repository.$1;"
        );

        content = content.replaceAll(
                "import\\s+org\\.springframework\\.samples\\.petclinic\\.vet\\.(\\w+);",
                "import " + basePackage + ".model.$1;"
        );

        content = content.replaceAll(
                "org\\.springframework\\.samples\\.petclinic\\.model\\.",
                basePackage + ".model."
        );

        content = content.replaceAll(
                "org\\.springframework\\.samples\\.petclinic\\.vet\\.",
                basePackage + ".model."
        );

        content = content.replaceAll(
                "import\\s+org\\.generated\\.model\\.(\\w+);",
                "import " + basePackage + ".model.$1;"
        );

        if (targetPackage.contains(".controller")) {
            content = content.replaceAll(
                    "import\\s+" + basePackage + "\\.model\\.(\\w+Service);",
                    "import " + basePackage + ".service.$1;"
            );
        }

        return content;
    }

    private String addImport(String content, String importStmt) {

        if (content.contains("import " + importStmt + ";")) {
            return content;
        }

        if (content.contains("import ")) {
            return content.replaceFirst(
                    "(import .*?;)",
                    "$1\nimport " + importStmt + ";"
            );
        } else {
            return content.replaceFirst(
                    "(package .*?;)",
                    "$1\nimport " + importStmt + ";"
            );
        }
    }
}