package org.automationTool.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ServiceGenerator {

    public void generateService(Path servicePath, String basePackage, String entity) throws IOException {

        String className = entity + "Service";

        String content =
                "package " + basePackage + ".service;\n\n" +

                        "import " + basePackage + ".model." + entity + ";\n" +
                        "import " + basePackage + ".repository." + entity + "Repository;\n" +

                        "import org.springframework.beans.factory.annotation.Autowired;\n" +
                        "import org.springframework.stereotype.Service;\n" +
                        "import org.springframework.data.domain.Page;\n" +
                        "import org.springframework.data.domain.Pageable;\n" +

                        "import java.util.List;\n\n" +

                        "@Service\n" +
                        "public class " + className + " {\n\n" +

                        "    @Autowired\n" +
                        "    private " + entity + "Repository repository;\n\n" +

                        "    // get all entities\n" +
                        "    public List<" + entity + "> findAll() {\n" +
                        "        return repository.findAll();\n" +
                        "    }\n\n" +

                        "    // paginated fetch\n" +
                        "    public Page<" + entity + "> findAll(Pageable pageable) {\n" +
                        "        return repository.findAll(pageable);\n" +
                        "    }\n" +

                        "}\n";

        Files.createDirectories(servicePath);

        Path file = servicePath.resolve(className + ".java");
        Files.writeString(file, content);

        System.out.println("Generated Service: " + className);
    }
}