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
            username: dharani
            password: dharani

          jpa:
            hibernate:
              ddl-auto: none
            show-sql: true
            properties:
              hibernate:
                format_sql: true

          h2:
            console:
              enabled: true
              path: /h2-console

          sql:
            init:
              mode: always
              schema-locations: classpath:db/h2/schema.sql
              data-locations: classpath:db/h2/data.sql
        """.formatted(port);

        Files.createDirectories(resourcePath);
        Files.writeString(resourcePath.resolve("application.yml"), content);
    }
}