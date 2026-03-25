package org.automationTool.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigGenerator {

    public void generateApplicationYml(Path resourcePath, int port) throws IOException {

        String content = """
                server:
                  port: %d

                spring:
                  datasource:
                    url: jdbc:h2:mem:testdb
                    driver-class-name: org.h2.Driver
                    username: sa
                    password:

                  jpa:
                    hibernate:
                      ddl-auto: update
                    show-sql: true

                  h2:
                    console:
                      enabled: true
                """.formatted(port);

        Files.createDirectories(resourcePath);
        Files.writeString(resourcePath.resolve("application.yml"), content);
    }
}