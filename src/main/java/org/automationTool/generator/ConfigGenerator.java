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
                    driverClassName: org.h2.Driver
                    username: sa
                    password:
                  jpa:
                    database-platform: org.hibernate.dialect.H2Dialect
                    hibernate:
                      ddl-auto: update
                """.formatted(port);

        Files.writeString(resourcePath.resolve("application.yml"), content);
    }
}