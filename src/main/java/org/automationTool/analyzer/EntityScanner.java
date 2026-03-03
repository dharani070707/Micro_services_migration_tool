package org.automationTool.analyzer;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class EntityScanner {

    public static Map<String, String> scanEntities(Iterable<Path> javaFiles) {

        Map<String, String> entityNameToPath = new HashMap<>();

        for (Path javaFile : javaFiles) {

            try {
                CompilationUnit cu = StaticJavaParser.parse(javaFile);

                for (ClassOrInterfaceDeclaration clazz :
                        cu.findAll(ClassOrInterfaceDeclaration.class)) {

                    boolean isEntity = clazz.getAnnotations()
                            .stream()
                            .anyMatch(a -> a.getNameAsString().equals("Entity"));

                    if (isEntity) {

                        entityNameToPath.put(
                                clazz.getNameAsString(),
                                javaFile.toAbsolutePath().toString()
                        );
                    }
                }

            } catch (Exception ignored) {}
        }

        return entityNameToPath;
    }
}