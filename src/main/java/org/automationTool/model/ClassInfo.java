package org.automationTool.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClassInfo {

    private String className;
    private String packageName;
    private Path filePath;
    public List<MethodInfo> methods = new ArrayList<>();
    public void addMethod(String name, String returnType, String params) {
        methods.add(new MethodInfo(name, returnType, params));
    }
    // EXISTING (keep)
    private String parentClass;

    //  NEW (needed for full resolution)
    private Set<String> interfaces = new HashSet<>();
    private Set<String> fieldTypes = new HashSet<>();
    private Set<String> methodReturnTypes = new HashSet<>();
    private Set<String> methodParameterTypes = new HashSet<>();
    private Set<String> imports = new HashSet<>();

    private Set<String> dependencies = new HashSet<>();

    //  KEEP YOUR EXISTING CONSTRUCTOR
    public ClassInfo(String className, String packageName, Path filePath) {
        this.className = className;
        this.packageName = packageName;
        this.filePath = filePath;
    }

    //  GETTERS

    public String getClassName() {
        return className;
    }

    public String getPackageName() {
        return packageName;
    }

    public Path getFilePath() {
        return filePath;
    }

    public String getParentClass() {
        return parentClass;
    }

    public Set<String> getDependencies() {
        return dependencies;
    }

    // NEW GETTERS

    public Set<String> getInterfaces() {
        return interfaces;
    }

    public Set<String> getFieldTypes() {
        return fieldTypes;
    }

    public Set<String> getMethodReturnTypes() {
        return methodReturnTypes;
    }

    public Set<String> getMethodParameterTypes() {
        return methodParameterTypes;
    }

    public Set<String> getImports() {
        return imports;
    }

    // SETTERS

    public void setParentClass(String parentClass) {
        this.parentClass = parentClass;
    }

    public void setDependencies(Set<String> dependencies) {
        this.dependencies = dependencies;
    }

    public void setFilePath(Path filePath) {
        this.filePath = filePath;
    }

    //  NEW ADDERS (important for extractor)

    public void addInterface(String iface) {
        iface = clean(iface);
        if (iface != null) interfaces.add(iface);
    }

    public void addFieldType(String type) {
        type = clean(type);
        if (type != null) fieldTypes.add(type);
    }

    public void addMethodReturnType(String type) {
        type = clean(type);
        if (type != null) methodReturnTypes.add(type);
    }

    public void addMethodParameterType(String type) {
        type = clean(type);
        if (type != null) methodParameterTypes.add(type);
    }

    public void addImport(String imp) {
        if (imp == null || imp.isBlank()) return;

        // convert full import → simple name
        if (imp.contains(".")) {
            imp = imp.substring(imp.lastIndexOf(".") + 1);
        }

        imp = clean(imp);
        if (imp != null) imports.add(imp);
    }

    //  CLEAN dependency logic (ONLY ONE METHOD)
    public void addDependency(String dep) {

        dep = clean(dep);

        if (dep != null && !dep.equals(className)) {
            dependencies.add(dep);
        }
    }

    private String clean(String dep) {

        if (dep == null || dep.isBlank()) return null;

        // Remove generics → List<Vet> → Vet
        if (dep.contains("<") && dep.contains(">")) {
            dep = dep.substring(dep.indexOf("<") + 1, dep.lastIndexOf(">"));
        }

        // Remove array brackets
        dep = dep.replace("[]", "");

        // Ignore common types
        Set<String> ignore = Set.of(
                "void","int","long","double","float","char","byte","boolean",
                "String","Integer","Long","Double","Float","Boolean","Object",
                "List","Set","Map","Optional","Collection","Page","Pageable",
                "Model","ModelMap","BindingResult","RedirectAttributes",
                "WebDataBinder","Locale","Errors"
        );

        if (ignore.contains(dep)) return null;

        // Ignore expressions
        if (dep.contains(".") || dep.contains("(") || dep.contains(")")) return null;

        return dep.trim();
    }
}