package org.automationTool;

import org.automationTool.analyzer.ComponentDetector;
import org.automationTool.analyzer.ControllerAnalyzer;
import org.automationTool.analyzer.EntityDetector;
import org.automationTool.analyzer.EntityScanner;
import org.automationTool.boundary.BoundaryInferer;
import org.automationTool.generator.MicroServiceGenerator;
import org.automationTool.model.ControllerInfo;
import org.automationTool.util.ClassIndex;
import org.automationTool.util.Config;
import org.automationTool.util.JavaFileScanner;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws Exception {

        //Scan monolith source files
        var javaFiles = JavaFileScanner.scanJavaFiles(Config.MONOLITH_ROOT);
        System.out.println("Analyzing class dependencies...\n");
        for (Path file : javaFiles) {
            ComponentDetector.analyzeDependencies(file);
        }
        ComponentDetector.cleanDependencies();
        ComponentDetector.getClassMap().values().forEach(ci -> {
            System.out.println("Class: " + ci.getClassName());
            ci.getDependencies().forEach(dep ->
                    System.out.println("   -> " + dep));
        });


        System.out.println("Scanning for Controllers...\n");

        // Build Entity Name → Path map
        Map<String, String> entityNameToPath =
                EntityScanner.scanEntities(javaFiles);

        // Index classes for quick lookup
        ClassIndex classIndex = new ClassIndex(javaFiles);

        // Collect ALL controller information
        List<ControllerInfo> controllerInfos = new ArrayList<>();

        for (Path file : javaFiles) {

            ControllerAnalyzer.analyze(file).ifPresent(info -> {

                // Filter only Spring-managed dependencies
                info.dependencies.removeIf(dep -> {
                    Path depFile = classIndex.getClassFile(dep);
                    return depFile == null
                            || !ComponentDetector.isSpringComponent(depFile);
                });

                // Detect entities from repositories
                for (String repo : info.dependencies) {
                    Path repoFile = classIndex.getClassFile(repo);
                    if (repoFile != null) {
                        info.entities.addAll(
                                EntityDetector.detectEntities(repoFile, entityNameToPath)
                        );
                    }
                }

                controllerInfos.add(info);
            });
        }

        // Infer microservice boundaries
        System.out.println("\nInferred Microservices:\n");
        var microservices = BoundaryInferer.infer(controllerInfos);

        microservices.forEach(System.out::println);

        // Generate services
        MicroServiceGenerator generator = new MicroServiceGenerator();
        generator.generate(microservices);
    }
}