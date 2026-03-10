package org.automationTool.analyzer;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.*;
import org.automationTool.model.ClassInfo;

import java.util.*;

public class DependencyExtractor {

    public static void extractDependencies(CompilationUnit cu, ClassInfo classInfo) {

        // Field dependencies
        cu.findAll(FieldDeclaration.class).forEach(field -> {
            String type = field.getElementType().asString();
            classInfo.addDependency(type);
        });

        // Constructor parameters
        cu.findAll(ConstructorDeclaration.class).forEach(cons -> {
            cons.getParameters().forEach(param ->
                    classInfo.addDependency(param.getType().asString())
            );
        });

        // Method parameters & return types
        cu.findAll(MethodDeclaration.class).forEach(method -> {
            classInfo.addDependency(method.getType().asString());

            method.getParameters().forEach(param ->
                    classInfo.addDependency(param.getType().asString())
            );
        });

        // Object creation (new ClassName())
        cu.findAll(ObjectCreationExpr.class).forEach(obj ->
                classInfo.addDependency(obj.getType().asString())
        );

        // Method call scope (service.method())
        cu.findAll(MethodCallExpr.class).forEach(call -> {
            call.getScope().ifPresent(scope ->
                    classInfo.addDependency(scope.toString())
            );
        });
    }
}