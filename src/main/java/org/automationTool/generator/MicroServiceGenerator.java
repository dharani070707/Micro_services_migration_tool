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

        Path root = structureCreator.createStructure(service.getName());

        pomGenerator.generatePom(root, service.getName());

        configGenerator.generateApplicationYml(
                root.resolve("src/main/resources"),
                port
        );

        fileCopier.copyFiles(
                service.getControllers(),
                root.resolve("src/main/java")
        );

        fileCopier.copyFiles(
                service.getEntities(),
                root.resolve("src/main/java")
        );

        mainClassGenerator.generateMainClass(
                root.resolve("src/main/java"),
                service.getName()
        );
    }
}