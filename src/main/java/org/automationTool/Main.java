package org.automationTool;

import org.automationTool.analyzer.ComponentDetector;
import org.automationTool.analyzer.ControllerAnalyzer;
import org.automationTool.analyzer.EntityDetector;
import org.automationTool.boundary.BoundaryInferer;
import org.automationTool.model.ControllerInfo;
import org.automationTool.util.ClassIndex;
import org.automationTool.util.Config;
import org.automationTool.util.JavaFileScanner;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {

        // 1️⃣ Scan monolith source files
        var javaFiles = JavaFileScanner.scanJavaFiles(Config.MONOLITH_ROOT);
        System.out.println("Scanning for Controllers...\n");

        // 2️⃣ Index classes for quick lookup
        ClassIndex classIndex = new ClassIndex(javaFiles);

        // 3️⃣ Collect ALL controller information first
        List<ControllerInfo> controllerInfos = new ArrayList<>();

        for (Path file : javaFiles) {

            ControllerAnalyzer.analyze(file).ifPresent(info -> {

                // 4️⃣ Filter only Spring-managed dependencies
                info.dependencies.removeIf(dep -> {
                    Path depFile = classIndex.getClassFile(dep);
                    return depFile == null
                            || !ComponentDetector.isSpringComponent(depFile);
                });

                // 5️⃣ Detect entities from repositories
                for (String repo : info.dependencies) {
                    Path repoFile = classIndex.getClassFile(repo);
                    if (repoFile != null) {
                        info.entities.addAll(EntityDetector.detectEntities(repoFile));
                    }
                }

                controllerInfos.add(info);
            });
        }

        // 6️⃣ Infer microservice boundaries ONCE
        System.out.println("\nInferred Microservices:\n");
        var microservices = BoundaryInferer.infer(controllerInfos);

        // 7️⃣ Print final result
        microservices.forEach(System.out::println);
    }
}
