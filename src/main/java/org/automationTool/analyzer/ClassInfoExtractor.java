package org.automationTool.analyzer;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import org.automationTool.model.ClassInfo;

import java.nio.file.Path;
import java.util.*;

public class ClassInfoExtractor {

    private static final Set<String> IGNORE_TYPES = Set.of(
            "String", "Integer", "Long", "Double", "Float",
            "Boolean", "List", "Set", "Map", "Optional", "void"
    );

    public static Map<String, ClassInfo> extract(Iterable<Path> javaFiles) {

        Map<String, ClassInfo> classMap = new HashMap<>();

        for (Path javaFile : javaFiles) {

            try {
                CompilationUnit cu = StaticJavaParser.parse(javaFile);

                for (ClassOrInterfaceDeclaration clazz :
                        cu.findAll(ClassOrInterfaceDeclaration.class)) {

                    String className = clazz.getNameAsString();

                    String packageName = cu.getPackageDeclaration()
                            .map(p -> p.getNameAsString())
                            .orElse("");

                    ClassInfo ci = new ClassInfo(className, packageName, javaFile);

                    // 1. Parent (extends)
                    if (!clazz.getExtendedTypes().isEmpty()) {
                        String parent = clazz.getExtendedTypes().get(0).getNameAsString();
                        ci.setParentClass(parent);
                        ci.addDependency(parent);
                    }

                    // 2. Interfaces
                    clazz.getImplementedTypes().forEach(i -> {
                        String iface = i.getNameAsString();
                        ci.addInterface(iface);
                        ci.addDependency(iface);
                    });

                    // 3. Fields
                    clazz.getFields().forEach(field -> {
                        String rawType = field.getElementType().asString();
                        String type = extractGenericType(rawType);

                        if (!IGNORE_TYPES.contains(type)) {
                            ci.addFieldType(type);
                            ci.addDependency(type);
                        }
                    });

                    // 4. Methods (return types + parameters)
                    for (MethodDeclaration method : clazz.getMethods()) {

                        String returnType = extractGenericType(method.getType().asString());
                        if (!IGNORE_TYPES.contains(returnType)) {
                            ci.addMethodReturnType(returnType);
                            ci.addDependency(returnType);
                        }

                        for (Parameter param : method.getParameters()) {
                            String paramType = extractGenericType(param.getType().asString());
                            if (!IGNORE_TYPES.contains(paramType)) {
                                ci.addMethodParameterType(paramType);
                                ci.addDependency(paramType);
                            }
                        }
                    }

                    // 5. Imports (fallback resolution)
                    cu.getImports().forEach(imp -> {
                        String fqcn = imp.getNameAsString();
                        String simple = fqcn.substring(fqcn.lastIndexOf(".") + 1);
                        ci.addImport(simple);
                    });

                    // Debug
                    System.out.println(
                            "Class: " + className +
                                    " | Parent: " + ci.getParentClass() +
                                    " | Deps: " + ci.getDependencies()
                    );

                    classMap.put(className, ci);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return classMap;
    }

    private static String extractGenericType(String type) {

        if (type == null) return null;

        if (type.contains("<") && type.contains(">")) {
            String inner = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">"));

            if (inner.contains(",")) {
                inner = inner.split(",")[0].trim();
            }

            return inner.trim();
        }

        return type.trim();
    }
}