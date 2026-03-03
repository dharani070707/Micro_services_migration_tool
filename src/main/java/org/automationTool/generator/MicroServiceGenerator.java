package org.automationTool.generator;

import org.automationTool.model.Microservice;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

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

        // 6️⃣ Copy files with package rewrite
        fileCopier.copyFiles(service.getControllers(), javaPath, basePackage);
        fileCopier.copyFiles(service.getEntities(), javaPath, basePackage);

        // 7️⃣ Generate main class
        mainClassGenerator.generateMainClass(javaPath, service.getName());
    }
}