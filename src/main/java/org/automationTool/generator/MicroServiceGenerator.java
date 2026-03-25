package org.automationTool.generator;

import org.automationTool.model.Microservice;
import org.automationTool.boundary.DependencyResolver;
import org.automationTool.analyzer.ComponentDetector;
import org.automationTool.util.ClassIndex;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class MicroServiceGenerator {

    private int basePort = 8081;

    public void generate(List<Microservice> services) throws IOException {

        int port = basePort;

        for (Microservice service : services) {
            System.out.println("Generating service: " + service.getName());
            generateService(service, port++);
        }
    }

    private void generateService(Microservice service, int port) throws IOException {

        SourceFileCopier fileCopier = new SourceFileCopier();
        ProjectStructureCreator structureCreator = new ProjectStructureCreator();
        PomGenerator pomGenerator = new PomGenerator();
        ConfigGenerator configGenerator = new ConfigGenerator();
        MainClassGenerator mainClassGenerator = new MainClassGenerator();
        RepositoryGenerator repoGen = new RepositoryGenerator();
        ServiceGenerator serviceGen = new ServiceGenerator();

        ClassIndex classIndex = new ClassIndex(
                org.automationTool.util.JavaFileScanner.scanJavaFiles(
                        org.automationTool.util.Config.MONOLITH_ROOT
                )
        );

        Path root = structureCreator.createStructure(service.getName());

        Path javaPath = root.resolve("src/main/java");
        Path resourcePath = root.resolve("src/main/resources");

        pomGenerator.generatePom(root, service.getName());
        configGenerator.generateApplicationYml(resourcePath, port);

        String basePackage = "org.generated." + service.getName().toLowerCase();

        Path basePackagePath = javaPath.resolve(basePackage.replace(".", "/"));

        Path modelPath = basePackagePath.resolve("model");
        Path controllerPath = basePackagePath.resolve("controller");
        Path servicePath = basePackagePath.resolve("service");
        Path repoPath = basePackagePath.resolve("repository");

        Files.createDirectories(modelPath);
        Files.createDirectories(controllerPath);
        Files.createDirectories(servicePath);
        Files.createDirectories(repoPath);

        Set<String> seedClasses = new HashSet<>();

        service.getControllers().forEach(pathStr -> {
            String name = Path.of(pathStr).getFileName().toString().replace(".java", "");
            seedClasses.add(name);
        });

        service.getEntities().forEach(pathStr -> {
            String name = Path.of(pathStr).getFileName().toString().replace(".java", "");
            seedClasses.add(name);
        });

        Set<String> allRequired =
                DependencyResolver.resolveClosure(
                        seedClasses,
                        ComponentDetector.getClassMap()
                );

        allRequired.addAll(seedClasses);

        Set<String> copied = new HashSet<>();
        Set<String> entityClasses = new HashSet<>();

        for (String cls : allRequired) {

            Path srcFile = classIndex.getClassFile(cls);
            if (srcFile == null) continue;

            String pathStr = srcFile.toString();
            if (copied.contains(pathStr)) continue;
            copied.add(pathStr);

            String content = Files.readString(srcFile);

            Path targetDir;
            String targetPackage;

            if (content.contains("@RestController") || content.contains("@Controller")) {

                targetDir = controllerPath;
                targetPackage = basePackage + ".controller";
                content = fixControllerContent(content, targetPackage);

            } else if (content.contains("@Repository")) {
                continue;

            } else if (content.contains("@Service")) {
                targetDir = servicePath;
                targetPackage = basePackage + ".service";

            } else {
                targetDir = modelPath;
                targetPackage = basePackage + ".model";

                if (content.contains("@Entity") ||
                        service.getEntities().stream().anyMatch(p -> p.contains(cls))) {
                    entityClasses.add(cls);
                }
            }

            fileCopier.copyFiles(
                    Collections.singletonList(pathStr),
                    targetDir,
                    targetPackage
            );
        }

        for (String entity : entityClasses) {
            repoGen.generateRepository(repoPath, basePackage, entity);
            serviceGen.generateService(servicePath, basePackage, entity);
        }

        mainClassGenerator.generateMainClass(javaPath, service.getName());
        copyResources(resourcePath);
    }

    private String fixControllerContent(String content, String newPackage) {

        String basePackage = newPackage.substring(0, newPackage.lastIndexOf("."));

        content = content.replaceAll(
                "package\\s+.*?;",
                "package " + newPackage + ";"
        );

        content = content.replace("@Controller", "@RestController");

        content = content.replaceAll("ResponseEntity\\s*<\\s*>", "ResponseEntity<?>");

        // Replace repository → service (class usage)
        content = content.replaceAll("\\b(\\w+)Repository\\b", "$1Service");

        // Fix variable names (vetRepository → vetService)
        content = content.replaceAll("repository", "service");

        // Fix constructor injection
        content = content.replaceAll(
                "(private\\s+final\\s+\\w+Service\\s+\\w+;)",
                "$1"
        );

        // Remove old repository imports
        content = content.replaceAll(
                "import\\s+.*Repository;",
                ""
        );

        // Add correct service import
        content = addImport(content, basePackage + ".service.*");

        // Add model imports (Vet, Vets, etc.)
        content = addImport(content, basePackage + ".model.*");

        // Add RestController import
        content = addImport(content, "org.springframework.web.bind.annotation.RestController");

        // Fix trailing commas
        content = content.replaceAll(",\\s*\\)", ")");

        return content;
    }

    private String addImport(String content, String importStmt) {

        if (content.contains("import " + importStmt)) return content;

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

    private void copyResources(Path targetResourcePath) throws IOException {

        Path sourceResources = org.automationTool.util.Config.MONOLITH_ROOT
                .resolve("src/main/resources");

        if (!Files.exists(sourceResources)) return;

        Files.walk(sourceResources).forEach(path -> {
            try {
                Path dest = targetResourcePath.resolve(sourceResources.relativize(path));

                if (Files.isDirectory(path)) {
                    Files.createDirectories(dest);
                } else {
                    Files.copy(path, dest, StandardCopyOption.REPLACE_EXISTING);
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}