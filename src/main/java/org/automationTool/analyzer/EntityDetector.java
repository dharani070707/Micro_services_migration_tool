package org.automationTool.analyzer;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class EntityDetector {

    public static Set<String> detectEntities(Path repositoryFile) {
        Set<String> entities = new HashSet<>();

        try {
            CompilationUnit cu = StaticJavaParser.parse(repositoryFile);

            for (ClassOrInterfaceDeclaration clazz :
                    cu.findAll(ClassOrInterfaceDeclaration.class)) {

                if (!clazz.isInterface()) continue;

                clazz.getExtendedTypes().forEach(type -> {
                    if (type.getTypeArguments().isPresent()) {
                        if (type.getTypeArguments().isPresent()) {
                            var args = type.getTypeArguments().get();
                            if (!args.isEmpty()) {
                                entities.add(args.get(0).asString());
                            }
                        }
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Failed entity detection: " + repositoryFile.getFileName());
        }
        return entities;
    }
}
