package org.automationTool.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ControllerGenerator {

    public void generateController(Path controllerPath, String basePackage, String entity) throws IOException {

        String className = entity + "Controller";
        String serviceName = entity + "Service";
        String varName = Character.toLowerCase(entity.charAt(0)) + entity.substring(1) + "Service";

        StringBuilder content = new StringBuilder();

        content.append("package ").append(basePackage).append(".controller;\n\n");

        content.append("import ").append(basePackage).append(".model.").append(entity).append(";\n");
        content.append("import ").append(basePackage).append(".service.").append(serviceName).append(";\n\n");

        content.append("import org.springframework.web.bind.annotation.*;\n");
        content.append("import java.util.List;\n\n");

        content.append("@RestController\n");
        content.append("@RequestMapping(\"/").append(entity.toLowerCase()).append("\")\n");
        content.append("public class ").append(className).append(" {\n\n");

        content.append("    private final ").append(serviceName).append(" ").append(varName).append(";\n\n");

        content.append("    public ").append(className).append("(")
                .append(serviceName).append(" ").append(varName).append(") {\n");
        content.append("        this.").append(varName).append(" = ").append(varName).append(";\n");
        content.append("    }\n\n");

        // ✅ GET ALL
        content.append("    @GetMapping\n");
        content.append("    public List<").append(entity).append("> getAll() {\n");
        content.append("        return ").append(varName).append(".findAll();\n");
        content.append("    }\n\n");

        // ✅ GET BY ID
        content.append("    @GetMapping(\"/{id}\")\n");
        content.append("    public ").append(entity).append(" getById(@PathVariable Integer id) {\n");
        content.append("        return ").append(varName).append(".findById(id);\n");
        content.append("    }\n\n");

        // ✅ CREATE (THIS WAS MISSING — NOW FIXED)
        content.append("    @PostMapping\n");
        content.append("    public ").append(entity).append(" create(@RequestBody ").append(entity).append(" obj) {\n");
        content.append("        return ").append(varName).append(".save(obj);\n");
        content.append("    }\n\n");

        // ✅ DELETE
        content.append("    @DeleteMapping(\"/{id}\")\n");
        content.append("    public void delete(@PathVariable Integer id) {\n");
        content.append("        ").append(varName).append(".deleteById(id);\n");
        content.append("    }\n");

        content.append("}\n");

        Files.createDirectories(controllerPath);
        Files.writeString(controllerPath.resolve(className + ".java"), content.toString());
    }

    public void generateBasicController(Path controllerPath, String basePackage, String controllerName) throws IOException {

        String content = "package " + basePackage + ".controller;\n\n" +
                "import org.springframework.web.bind.annotation.*;\n\n" +
                "@RestController\n" +
                "@RequestMapping(\"/" + controllerName.toLowerCase().replace("controller", "") + "\")\n" +
                "public class " + controllerName + " {\n\n" +

                "    @GetMapping\n" +
                "    public String home() {\n" +
                "        return \"" + controllerName + " is working\";\n" +
                "    }\n" +
                "}";

        Files.createDirectories(controllerPath);
        Files.writeString(controllerPath.resolve(controllerName + ".java"), content);
    }
}