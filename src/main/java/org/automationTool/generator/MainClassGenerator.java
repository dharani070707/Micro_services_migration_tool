package org.automationTool.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MainClassGenerator {

    public void generateMainClass(Path javaPath, String serviceName) throws IOException {

        String className = capitalize(serviceName) + "Application";

        String content = """
                package org.generated.%s;

                import org.springframework.boot.SpringApplication;
                import org.springframework.boot.autoconfigure.SpringBootApplication;

                @SpringBootApplication
                public class %s {

                    public static void main(String[] args) {
                        SpringApplication.run(%s.class, args);
                    }
                }
                """.formatted(serviceName.toLowerCase(), className, className);

        Files.writeString(javaPath.resolve(className + ".java"), content);
    }

    private String capitalize(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}