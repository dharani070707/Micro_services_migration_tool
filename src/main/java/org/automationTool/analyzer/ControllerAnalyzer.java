package org.automationTool.analyzer;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import org.automationTool.model.ControllerInfo;

import java.nio.file.Path;
import java.util.Optional;

public class ControllerAnalyzer {

    public static Optional<ControllerInfo> analyze(Path javaFile) {

        try {
            CompilationUnit cu = StaticJavaParser.parse(javaFile);

            for (ClassOrInterfaceDeclaration clazz :
                    cu.findAll(ClassOrInterfaceDeclaration.class)) {

                // Accept BOTH Controller & RestController
                boolean isController = clazz.getAnnotations()
                        .stream()
                        .anyMatch(a -> {
                            String name = a.getNameAsString();
                            return name.equals("Controller") || name.equals("RestController");
                        });

                if (!isController) continue;

                ControllerInfo info = new ControllerInfo();

                info.controllerName = clazz.getNameAsString();

                info.packageName = cu.getPackageDeclaration()
                        .map(p -> p.getNameAsString())
                        .orElse("default");

                info.filePath = javaFile.toAbsolutePath().toString();

                info.setRestController(true);

                // Field injection
                for (FieldDeclaration field : clazz.getFields()) {
                    field.getVariables().forEach(v ->
                            info.dependencies.add(v.getType().asString()));
                }

                // Constructor injection
                for (ConstructorDeclaration constructor : clazz.getConstructors()) {
                    for (Parameter param : constructor.getParameters()) {
                        info.dependencies.add(param.getType().asString());
                    }
                }

                for (MethodDeclaration method : clazz.getMethods()) {

                    // Skip private methods (optional)
                    if (method.isPrivate()) continue;

                    String methodName = method.getNameAsString();
                    String returnType = method.getType().asString();

                    // Build parameter string safely
                    StringBuilder paramsBuilder = new StringBuilder();

                    for (Parameter param : method.getParameters()) {
                        paramsBuilder.append(param.getType().asString())
                                .append(" ")
                                .append(param.getNameAsString())
                                .append(", ");
                    }

                    String params = paramsBuilder.toString();

                    // Remove trailing comma safely
                    if (params.endsWith(", ")) {
                        params = params.substring(0, params.length() - 2);
                    }

                    // 🔥 SAFETY CHECKS (VERY IMPORTANT)
                    if (methodName == null || methodName.isBlank()) continue;
                    if (returnType == null || returnType.isBlank()) returnType = "void";

                    // DEBUG (optional)
                    System.out.println("Detected Method: "
                            + returnType + " " + methodName + "(" + params + ")");

                    // Save into ControllerInfo
                    info.addMethod(methodName, returnType, params);
                }

                return Optional.of(info);
            }

        } catch (Exception e) {
            System.err.println("Failed to parse: " + javaFile);
            e.printStackTrace();
        }

        return Optional.empty();
    }
}