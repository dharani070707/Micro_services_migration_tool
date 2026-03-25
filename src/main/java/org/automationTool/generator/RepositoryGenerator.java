package org.automationTool.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RepositoryGenerator {

    public void generateRepository(Path repoPath, String basePackage, String entity) throws IOException {

        String content = "package " + basePackage + ".repository;\n\n" +
                "import " + basePackage + ".model." + entity + ";\n" +
                "import org.springframework.data.jpa.repository.JpaRepository;\n" +
                "import org.springframework.stereotype.Repository;\n\n" +
                "@Repository\n" +
                "public interface " + entity + "Repository extends JpaRepository<" + entity + ", Integer> {\n" +
                "}\n";

        Files.createDirectories(repoPath);

        Path file = repoPath.resolve(entity + "Repository.java");
        Files.writeString(file, content);

        System.out.println("Generated Repository: " + entity + "Repository");
    }
}