package org.automationTool.model;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class ClassInfo {

    private String className;
    private String packageName;
    private Path filePath;
    private Set<String> dependencies = new HashSet<>();

    public ClassInfo(String className, String packageName, Path filePath) {
        this.className = className;
        this.packageName = packageName;
        this.filePath = filePath;
    }

    public void addDependency(String dep) {
        dep = clean(dep);
        if (dep != null && !dep.equals(className)) {
            dependencies.add(dep);
        }
    }

    private String clean(String dep) {
        if (dep == null || dep.isBlank()) return null;

        // Remove generics
        if (dep.contains("<") && dep.contains(">")) {
            dep = dep.substring(dep.indexOf("<") + 1, dep.lastIndexOf(">"));
        }

        // Remove array brackets
        dep = dep.replace("[]", "");

        // Remove common junk words
        Set<String> ignore = Set.of(
                "void","int","long","double","float","char","byte","boolean",
                "String","Integer","Long","Double","Float","Boolean","Object",
                "List","Set","Map","Optional","Collection","Page","Pageable",
                "Model","ModelMap","BindingResult","RedirectAttributes",
                "WebDataBinder","Locale","Errors"
        );

        if (ignore.contains(dep)) return null;

        // Remove expressions like this.xxx or new Something()
        if (dep.contains(".") || dep.contains("(") || dep.contains(")")) return null;

        return dep.trim();
    }

    public Set<String> getDependencies() {
        return dependencies;
    }

    public String getClassName() { return className; }
    public String getPackageName() { return packageName; }
    public Path getFilePath() { return filePath; }
}