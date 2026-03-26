package org.automationTool.generator;

import org.automationTool.model.Microservice;
import org.automationTool.boundary.DependencyResolver;
import org.automationTool.analyzer.ComponentDetector;
import org.automationTool.util.ClassIndex;
import org.automationTool.util.Config;

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
        ControllerGenerator controllerGen = new ControllerGenerator();

        ClassIndex classIndex = new ClassIndex(
                org.automationTool.util.JavaFileScanner.scanJavaFiles(Config.MONOLITH_ROOT)
        );

        Path root = structureCreator.createStructure(service.getName());

        Path javaPath = root.resolve("src/main/java");
        Path resourcePath = root.resolve("src/main/resources");

        pomGenerator.generatePom(root, service.getName());
        configGenerator.generateApplicationYml(resourcePath, port);

        fileCopier.copyResourceDirectory(Config.MONOLITH_DB_H2, resourcePath.resolve("db/h2"));

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

        // -------- SEED CLASSES --------
        Set<String> seedClasses = new HashSet<>();

        service.getControllers().forEach(pathStr ->
                seedClasses.add(Path.of(pathStr).getFileName().toString().replace(".java", ""))
        );

        service.getEntities().forEach(pathStr ->
                seedClasses.add(Path.of(pathStr).getFileName().toString().replace(".java", ""))
        );

        Set<String> allRequired =
                DependencyResolver.resolveClosure(seedClasses, ComponentDetector.getClassMap());

        allRequired.addAll(seedClasses);

        // -------- CONTROLLERS LIST --------
        Set<String> controllerClasses = new HashSet<>();

        service.getControllers().forEach(pathStr -> {
            String cls = Path.of(pathStr).getFileName().toString().replace(".java", "");
            controllerClasses.add(cls);
        });

        // -------- ENTITY DETECTION --------
        Set<String> entityClasses = new HashSet<>();

        for (String cls : allRequired) {
            Path srcFile = classIndex.getClassFile(cls);
            if (srcFile == null) continue;

            String content = Files.readString(srcFile);

            if (content.contains("@Entity") ||
                    service.getEntities().stream().anyMatch(p -> p.contains(cls))) {
                entityClasses.add(cls);
            }
        }

        // -------- GENERATE SERVICE + REPOSITORY --------
        Map<String, Path> repoFiles = new HashMap<>();

        for (String cls : allRequired) {
            Path srcFile = classIndex.getClassFile(cls);
            if (srcFile == null) continue;

            String fileName = srcFile.getFileName().toString();

            if (fileName.endsWith("Repository.java")) {
                String entityName = fileName.replace("Repository.java", "");
                repoFiles.put(entityName, srcFile);
            }
        }

        for (String entity : entityClasses) {

            List<String> dynamicMethods = new ArrayList<>();

            if (repoFiles.containsKey(entity)) {
                String repoContent = Files.readString(repoFiles.get(entity));
                dynamicMethods = extractRepositoryMethods(repoContent);
            }

            repoGen.generateRepository(repoPath, basePackage, entity, dynamicMethods);
            serviceGen.generateService(servicePath, basePackage, entity, dynamicMethods);
        }

        // -------- COPY MODELS & SERVICES ONLY --------
        Map<Path, List<String>> groupedFiles = new HashMap<>();
        groupedFiles.put(servicePath, new ArrayList<>());
        groupedFiles.put(modelPath, new ArrayList<>());

        for (String cls : allRequired) {

            Path srcFile = classIndex.getClassFile(cls);
            if (srcFile == null) continue;

            String content = Files.readString(srcFile);
            String fileName = srcFile.getFileName().toString();

            if (fileName.endsWith("Repository.java")) continue;

            if (content.contains("@Service")) {
                groupedFiles.get(servicePath).add(srcFile.toString());
            } else if (!fileName.endsWith("Controller.java")) {
                groupedFiles.get(modelPath).add(srcFile.toString());
            }
        }

        fileCopier.copyFiles(groupedFiles.get(servicePath), servicePath, basePackage + ".service");
        fileCopier.copyFiles(groupedFiles.get(modelPath), modelPath, basePackage + ".model");

        // -------- GENERATE ENTITY CONTROLLERS --------
        for (String entity : entityClasses) {
            controllerGen.generateController(controllerPath, basePackage, entity);
        }

        // -------- GENERATE NON-ENTITY CONTROLLERS --------
        for (String controller : controllerClasses) {

            String entityName = controller.replace("Controller", "");

            if (!entityClasses.contains(entityName)) {
                controllerGen.generateBasicController(controllerPath, basePackage, controller);
            }
        }

        // -------- MAIN CLASS --------
        mainClassGenerator.generateMainClass(javaPath, service.getName());
    }

    // -------- METHOD EXTRACTION --------
    private List<String> extractRepositoryMethods(String content) {

        List<String> methods = new ArrayList<>();

        var pattern = java.util.regex.Pattern.compile(
                "(List<.*?>|Optional<.*?>|\\w+)\\s+(\\w+)\\s*\\((.*?)\\);"
        );

        var matcher = pattern.matcher(content);

        while (matcher.find()) {

            String methodName = matcher.group(2);

            if (methodName.matches("save|findAll|findById|deleteById|existsById|count")
                    || (!methodName.startsWith("findBy") && !methodName.startsWith("readBy"))) {
                continue;
            }

            methods.add(matcher.group());
        }

        return methods;
    }
}