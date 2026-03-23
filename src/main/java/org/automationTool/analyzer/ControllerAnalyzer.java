package org.automationTool.analyzer;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.Parameter;
import org.automationTool.model.ControllerInfo;

import java.nio.file.Path;
import java.util.Optional;

public class ControllerAnalyzer {

    public static Optional<ControllerInfo> analyze(Path javaFile) {

        try {
            CompilationUnit cu = StaticJavaParser.parse(javaFile);

            for (ClassOrInterfaceDeclaration clazz :
                    cu.findAll(ClassOrInterfaceDeclaration.class)) {

                //Accept BOTH Controller & RestController
                boolean isController = clazz.getAnnotations()
                        .stream()
                        .anyMatch(a -> {
                            String name = a.getNameAsString();
                            return name.equals("Controller") || name.equals("RestController");
                        });

                if (!isController) continue;

                //Create info AFTER filtering
                ControllerInfo info = new ControllerInfo();

                info.controllerName = clazz.getNameAsString();

                info.packageName = cu.getPackageDeclaration()
                        .map(p -> p.getNameAsString())
                        .orElse("default");

                info.filePath = javaFile.toAbsolutePath().toString();

                // Treat everything as backend controller
                info.setRestController(true);

                // Field injection dependencies
                for (FieldDeclaration field : clazz.getFields()) {
                    field.getVariables().forEach(v ->
                            info.dependencies.add(v.getType().asString()));
                }

                // Constructor injection dependencies
                for (ConstructorDeclaration constructor : clazz.getConstructors()) {
                    for (Parameter param : constructor.getParameters()) {
                        info.dependencies.add(param.getType().asString());
                    }
                }

                return Optional.of(info);
            }

        } catch (Exception e) {
            System.err.println("Failed to parse: " + javaFile);
        }

        return Optional.empty();
    }
}