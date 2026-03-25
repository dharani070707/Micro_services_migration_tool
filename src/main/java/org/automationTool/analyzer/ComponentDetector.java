package org.automationTool.analyzer;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.automationTool.model.ClassInfo;

import java.util.*;
import java.nio.file.Path;

public class ComponentDetector {

    private static final Map<String, ClassInfo> classMap = new HashMap<>();

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

    public static void analyzeDependencies(Path javaFile) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(javaFile);

            String className = javaFile.getFileName().toString().replace(".java", "");
            String packageName = cu.getPackageDeclaration()
                    .map(pd -> pd.getNameAsString())
                    .orElse("");

            // FIX: do not overwrite existing ClassInfo
            ClassInfo classInfo = classMap.getOrDefault(
                    className,
                    new ClassInfo(className, packageName, javaFile)
            );

            // Run existing dependency extractor
            DependencyExtractor.extractDependencies(cu, classInfo);

            // FIX: ensure inheritance is captured (critical)
            for (ClassOrInterfaceDeclaration clazz :
                    cu.findAll(ClassOrInterfaceDeclaration.class)) {

                if (!clazz.getExtendedTypes().isEmpty()) {
                    String parent = clazz.getExtendedTypes().get(0).getNameAsString();
                    classInfo.setParentClass(parent);
                    classInfo.addDependency(parent);
                }

                // FIX: interfaces
                clazz.getImplementedTypes().forEach(i -> {
                    String iface = i.getNameAsString();
                    classInfo.addInterface(iface);
                    classInfo.addDependency(iface);
                });
            }

            classMap.put(className, classInfo);

        } catch (Exception e) {
            System.err.println("Dependency analysis failed for " + javaFile.getFileName());
        }
    }

    public static Map<String, ClassInfo> getClassMap() {
        return classMap;
    }

    public static void cleanDependencies() {
        Set<String> projectClasses = classMap.keySet();

        for (ClassInfo ci : classMap.values()) {
            ci.getDependencies().removeIf(dep ->
                    dep == null ||
                            dep.isBlank() ||
                            dep.equals("this") ||
                            !projectClasses.contains(dep)
            );
        }
    }
}