package org.automationTool.generator;

import org.automationTool.model.Microservice;
import org.automationTool.boundary.DependencyResolver;
import org.automationTool.analyzer.ComponentDetector;
import org.automationTool.util.ClassIndex;
import org.automationTool.util.Config;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

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

        Set<String> copiedClasses = new HashSet<>();
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

        // -------- GENERATE REPO + SERVICE --------
        for (String entity : entityClasses) {

            List<String> dynamicMethods = new ArrayList<>();

            if (repoFiles.containsKey(entity)) {
                String repoContent = Files.readString(repoFiles.get(entity));
                dynamicMethods = extractRepositoryMethods(repoContent);
            }

            repoGen.generateRepository(repoPath, basePackage, entity, dynamicMethods);
            serviceGen.generateService(servicePath, basePackage, entity, dynamicMethods);
        }

        // -------- COPY & TRANSFORM FILES --------
        for (String cls : allRequired) {

            Path srcFile = classIndex.getClassFile(cls);
            if (srcFile == null) continue;

            String fileName = srcFile.getFileName().toString();

            if (copiedClasses.contains(fileName)) continue;
            copiedClasses.add(fileName);

            String content = Files.readString(srcFile);

            Path targetDir;
            String targetPackage;

            if (content.contains("@RestController") || content.contains("@Controller")) {

                targetDir = controllerPath;
                targetPackage = basePackage + ".controller";
                content = fixControllerContent(content, targetPackage);

            } else if (fileName.endsWith("Repository.java")) {
                continue;

            } else if (content.contains("@Service")) {
                targetDir = servicePath;
                targetPackage = basePackage + ".service";
                content = fixPackage(content, targetPackage);

            } else {
                targetDir = modelPath;
                targetPackage = basePackage + ".model";
                content = fixPackage(content, targetPackage);
            }

            // ✅ safer import replacement
            content = content.replaceAll(
                    "org\\.springframework\\.samples\\.petclinic\\.\\w+",
                    basePackage + ".model"
            );

            Path targetFile = targetDir.resolve(srcFile.getFileName());
            Files.writeString(targetFile, content);
        }

        mainClassGenerator.generateMainClass(javaPath, service.getName());
    }

    // -------- METHOD EXTRACTION --------
    private List<String> extractRepositoryMethods(String content) {

        List<String> methods = new ArrayList<>();

        Pattern pattern = Pattern.compile(
                "(List<.*?>|Page<.*?>|Optional<.*?>|\\w+)\\s+(\\w+)\\s*\\((.*?)\\);"
        );

        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {

            String returnType = matcher.group(1);
            String methodName = matcher.group(2);
            String params = matcher.group(3);

            if (methodName.matches("save|findAll|findById|deleteById|existsById|count")
                    || (!methodName.startsWith("findBy") && !methodName.startsWith("readBy"))) {
                continue;
            }

            String fullMethod = returnType + " " + methodName + "(" + params + ")";
            methods.add(fullMethod);
        }

        return methods;
    }

    // -------- CONTROLLER FIX --------
    private String fixControllerContent(String content, String newPackage) {

        String basePackage = newPackage.substring(0, newPackage.lastIndexOf("."));

        content = content.replaceAll("package\\s+.*?;", "package " + newPackage + ";");

        content = content.replace("@Controller", "@RestController");

        content = content.replaceAll("\\b(\\w+)Repository\\b", "$1Service");
        content = content.replaceAll("repository", "service");

        // ✅ generic fix for invalid find methods
        content = content.replaceAll("\\.find(?!By)[A-Z]\\w*\\s*\\(", ".findAll(");

        content = content.replaceAll("import\\s+.*Repository;", "");

        content = addImport(content, basePackage + ".service.*");
        content = addImport(content, basePackage + ".model.*");
        content = addImport(content, "org.springframework.web.bind.annotation.RestController");

        content = content.replaceAll(",\\s*\\)", ")");

        return content;
    }

    private String addImport(String content, String importStmt) {

        if (content.contains("import " + importStmt)) return content;

        if (content.contains("import ")) {
            return content.replaceFirst("(import .*?;)", "$1\nimport " + importStmt + ";");
        } else {
            return content.replaceFirst("(package .*?;)", "$1\nimport " + importStmt + ";");
        }
    }

    private String fixPackage(String content, String newPackage) {
        return content.replaceAll("package\\s+.*?;", "package " + newPackage + ";");
    }
}