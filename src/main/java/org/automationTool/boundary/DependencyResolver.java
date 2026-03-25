package org.automationTool.boundary;

import org.automationTool.model.ClassInfo;

import java.util.*;

public class DependencyResolver {

    public static Set<String> resolveClosure(
            Set<String> initialClasses,
            Map<String, ClassInfo> classMap) {

        Set<String> resolved = new HashSet<>(initialClasses);
        Queue<String> queue = new LinkedList<>(initialClasses);

        while (!queue.isEmpty()) {

            String cls = queue.poll();
            ClassInfo ci = classMap.get(cls);

            if (ci == null) continue;

            // 1. Full inheritance chain
            String parent = ci.getParentClass();
            while (isValid(parent) && !resolved.contains(parent)) {
                resolved.add(parent);
                queue.add(parent);

                ClassInfo parentInfo = classMap.get(parent);
                if (parentInfo != null) {
                    parent = parentInfo.getParentClass();
                } else {
                    break;
                }
            }

            // 2. Interfaces
            for (String iface : ci.getInterfaces()) {
                addIfValid(iface, resolved, queue, classMap);
            }

            // 3. Field types
            for (String field : ci.getFieldTypes()) {
                addIfValid(field, resolved, queue, classMap);
            }

            // 4. Method return types
            for (String ret : ci.getMethodReturnTypes()) {
                addIfValid(ret, resolved, queue, classMap);
            }

            // 5. Method parameters
            for (String param : ci.getMethodParameterTypes()) {
                addIfValid(param, resolved, queue, classMap);
            }

            // 6. Dependencies (existing fallback)
            for (String dep : ci.getDependencies()) {
                addIfValid(dep, resolved, queue, classMap);
            }

            // 7. Imports (last fallback)
            for (String imp : ci.getImports()) {
                addIfValid(imp, resolved, queue, classMap);
            }
        }

        return resolved;
    }

    private static void addIfValid(String type,
                                   Set<String> resolved,
                                   Queue<String> queue,
                                   Map<String, ClassInfo> classMap) {

        if (!isValid(type)) return;

        if (!resolved.contains(type) && classMap.containsKey(type)) {
            resolved.add(type);
            queue.add(type);
        }
    }

    private static boolean isValid(String type) {

        if (type == null || type.isBlank()) return false;

        return !Set.of(
                "String", "Integer", "Long", "Double", "Float",
                "Boolean", "List", "Set", "Map", "Date",
                "Optional", "BigDecimal", "void"
        ).contains(type);
    }
}