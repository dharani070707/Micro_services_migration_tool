package org.automationTool.generator;

import org.automationTool.model.Microservice;
import org.automationTool.boundary.DependencyResolver;
import org.automationTool.analyzer.ComponentDetector;
import org.automationTool.util.ClassIndex;
import java.nio.file.Path;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
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

        ProjectStructureCreator structureCreator = new ProjectStructureCreator();
        PomGenerator pomGenerator = new PomGenerator();
        ConfigGenerator configGenerator = new ConfigGenerator();
        SourceFileCopier fileCopier = new SourceFileCopier();
        MainClassGenerator mainClassGenerator = new MainClassGenerator();
        ClassIndex classIndex = new ClassIndex(
                org.automationTool.util.JavaFileScanner.scanJavaFiles(
                        org.automationTool.util.Config.MONOLITH_ROOT
                )
        );

        //  root folder FIRST
        Path root = structureCreator.createStructure(service.getName());

        //  resolve paths
        Path javaPath = root.resolve("src/main/java");
        Path resourcePath = root.resolve("src/main/resources");

        // Generate pom
        pomGenerator.generatePom(root, service.getName());

        // Generate application.yml
        configGenerator.generateApplicationYml(resourcePath, port);

        // Define new base package
        String basePackage = "org.generated." + service.getName().toLowerCase();

        // Compute dependency closure
        Set<String> seedClasses = new HashSet<>();

        service.getControllers().forEach(pathStr -> {
            Path path = Path.of(pathStr);
            String name = path.getFileName().toString().replace(".java", "");
            seedClasses.add(name);
        });

        service.getEntities().forEach(pathStr -> {
            Path path = Path.of(pathStr);
            String name = path.getFileName().toString().replace(".java", "");
            seedClasses.add(name);
        });

        Set<String> allRequired =
                DependencyResolver.resolveClosure(
                        seedClasses,
                        ComponentDetector.getClassMap()
                );

        System.out.println("Including classes: " + allRequired);

        Set<String> copied = new HashSet<>();

        for (String cls : allRequired) {
            Path srcFile = classIndex.getClassFile(cls);
            if (srcFile != null) {

                String pathStr = srcFile.toString();

                if (!copied.contains(pathStr)) {
                    copied.add(pathStr);

                    fileCopier.copyFiles(
                            Collections.singletonList(pathStr),
                            javaPath,
                            basePackage
                    );

                    System.out.println("Copied: " + srcFile.getFileName());
                }
            }
        }
        mainClassGenerator.generateMainClass(javaPath, service.getName());
    }

    private void copyResources(Path targetResourcePath) throws IOException {

        Path sourceResources = org.automationTool.util.Config.MONOLITH_ROOT
                .resolve("src/main/resources");

        if (!java.nio.file.Files.exists(sourceResources)) {
            return;
        }

        copyFolder(sourceResources, targetResourcePath);

        System.out.println("Copied all resource files");
    }

    private void copyFolder(Path source, Path target) throws IOException {

        java.nio.file.Files.walk(source).forEach(path -> {
            try {

                String name = path.getFileName().toString();

                // Skip all Spring config files
                if (name.startsWith("application") ||
                        name.startsWith("bootstrap")) {
                    return;
                }

                Path destination = target.resolve(source.relativize(path));

                if (java.nio.file.Files.isDirectory(path)) {
                    java.nio.file.Files.createDirectories(destination);
                } else {
                    java.nio.file.Files.copy(
                            path,
                            destination,
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING
                    );
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}