package org.automationTool.analyzer;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import org.automationTool.model.ClassInfo;

public class DependencyExtractor {

    public static void extractDependencies(CompilationUnit cu, ClassInfo classInfo) {

        // 1. Inheritance (VERY IMPORTANT)
        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(clazz -> {

            if (!clazz.getExtendedTypes().isEmpty()) {
                String parent = clazz.getExtendedTypes(0).getNameAsString();
                classInfo.setParentClass(parent);
                classInfo.addDependency(parent);
            }

            clazz.getImplementedTypes().forEach(i -> {
                String iface = i.getNameAsString();
                classInfo.addInterface(iface);
                classInfo.addDependency(iface);
            });
        });

        // 2. Field dependencies
        cu.findAll(FieldDeclaration.class).forEach(field -> {
            String type = clean(field.getElementType().asString());
            if (type != null) {
                classInfo.addFieldType(type);
                classInfo.addDependency(type);
            }
        });

        // 3. Constructor parameters
        cu.findAll(ConstructorDeclaration.class).forEach(cons -> {
            cons.getParameters().forEach(param -> {
                String type = clean(param.getType().asString());
                if (type != null) {
                    classInfo.addMethodParameterType(type);
                    classInfo.addDependency(type);
                }
            });
        });

        // 4. Method parameters & return types
        cu.findAll(MethodDeclaration.class).forEach(method -> {

            String returnType = clean(method.getType().asString());
            if (returnType != null) {
                classInfo.addMethodReturnType(returnType);
                classInfo.addDependency(returnType);
            }

            method.getParameters().forEach(param -> {
                String type = clean(param.getType().asString());
                if (type != null) {
                    classInfo.addMethodParameterType(type);
                    classInfo.addDependency(type);
                }
            });
        });

        // 5. Object creation (new ClassName())
        cu.findAll(ObjectCreationExpr.class).forEach(obj -> {
            String type = clean(obj.getType().asString());
            if (type != null) {
                classInfo.addDependency(type);
            }
        });

        // 6. Imports (fallback - IMPORTANT)
        cu.getImports().forEach(imp -> {
            String fqcn = imp.getNameAsString();
            String simple = fqcn.substring(fqcn.lastIndexOf(".") + 1);
            classInfo.addImport(simple);
        });

        // NOTE: Removed noisy MethodCallExpr scope dependency
    }

    // Helper: clean generics + arrays
    private static String clean(String type) {

        if (type == null || type.isBlank()) return null;

        // Handle generics: List<Vet> → Vet
        if (type.contains("<") && type.contains(">")) {
            type = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">"));

            // Handle multiple generics Map<K,V>
            if (type.contains(",")) {
                type = type.split(",")[0].trim();
            }
        }

        // Remove array brackets
        type = type.replace("[]", "");

        return type.trim();
    }
}