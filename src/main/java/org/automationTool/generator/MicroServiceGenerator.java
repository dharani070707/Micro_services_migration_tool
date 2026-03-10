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

        // 🆕 Build ClassIndex for lookup
        ClassIndex classIndex = new ClassIndex(
                org.automationTool.util.JavaFileScanner.scanJavaFiles(
                        org.automationTool.util.Config.MONOLITH_ROOT
                )
        );

        // 1️⃣ Create root folder FIRST
        Path root = structureCreator.createStructure(service.getName());

        // 2️⃣ Then resolve paths
        Path javaPath = root.resolve("src/main/java");
        Path resourcePath = root.resolve("src/main/resources");

        // 3️⃣ Generate pom
        pomGenerator.generatePom(root, service.getName());

        // 4️⃣ Generate application.yml
        configGenerator.generateApplicationYml(resourcePath, port);

        // 5️⃣ Define new base package
        String basePackage = "org.generated." + service.getName().toLowerCase();

        // 🧠 6️⃣ Compute dependency closure
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
}