package org.automationTool.analyzer;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EntityDetector {

    public static Set<String> detectEntities(Path repositoryFile,
                                             Map<String, String> entityNameToPath) {

        Set<String> entityPaths = new HashSet<>();

        try {
            CompilationUnit cu = StaticJavaParser.parse(repositoryFile);

            for (ClassOrInterfaceDeclaration clazz :
                    cu.findAll(ClassOrInterfaceDeclaration.class)) {

                if (!clazz.isInterface()) continue;

                clazz.getExtendedTypes().forEach(type -> {

                    if (type.getTypeArguments().isPresent()) {

                        var args = type.getTypeArguments().get();

                        if (!args.isEmpty()) {

                            String entityName = args.get(0).asString();

                            // 🔥 Convert entity name → file path
                            String entityPath = entityNameToPath.get(entityName);

                            if (entityPath != null) {
                                entityPaths.add(entityPath);
                            }
                        }
                    }
                });
            }

        } catch (Exception e) {
            System.err.println("Failed entity detection: " + repositoryFile);
        }

        return entityPaths;
    }
}