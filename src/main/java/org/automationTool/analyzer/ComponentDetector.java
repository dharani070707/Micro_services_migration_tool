package org.automationTool.analyzer;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import java.nio.file.Path;


public class ComponentDetector {

    public static boolean isSpringComponent(Path javaFile) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(javaFile);

            for (ClassOrInterfaceDeclaration clazz :
                    cu.findAll(ClassOrInterfaceDeclaration.class)) {

                boolean hasComponentAnnotation =
                        clazz.getAnnotations().stream().anyMatch(a -> {
                            String name = a.getNameAsString();
                            return name.equals("Service")
                                    || name.equals("Repository")
                                    || name.equals("Component");
                        });

                if (hasComponentAnnotation) {
                    return true;
                }

                if (clazz.isInterface()) {
                    boolean isSpringDataRepo = clazz.getExtendedTypes().stream()
                            .anyMatch(t -> {
                                String name = t.getNameAsString();
                                return name.equals("Repository")
                                        || name.equals("CrudRepository")
                                        || name.equals("JpaRepository")
                                        || name.equals("PagingAndSortingRepository");
                            });

                    if (isSpringDataRepo) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Component detection failed for " + javaFile.getFileName());
        }
        return false;
    }
}