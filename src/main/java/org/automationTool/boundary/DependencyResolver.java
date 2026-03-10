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

            for (String dep : ci.getDependencies()) {
                if (!resolved.contains(dep)) {
                    resolved.add(dep);
                    queue.add(dep);
                }
            }
        }

        return resolved;
    }
}