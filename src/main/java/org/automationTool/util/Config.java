package org.automationTool.util;

import java.nio.file.Path;

public class Config {

    // Monolith root
    public static final Path MONOLITH_ROOT =
            Path.of("/home/dharani-prasad-s/Downloads/spring-petclinic-monolithic-main");

    // Monolith resources
    public static final Path MONOLITH_RESOURCES =
            MONOLITH_ROOT.resolve("src/main/resources");

    // H2 DB folder (THIS is your screenshot path)
    public static final Path MONOLITH_DB_H2 =
            MONOLITH_RESOURCES.resolve("db/h2");

    // Generated services root
    public static final Path GENERATED_SERVICES_ROOT =
            Path.of("/home/dharani-prasad-s/Micro_services_Project/MigrationTool/generated-services");

    // Common subpaths
    public static final String JAVA_MAIN = "src/main/java";
    public static final String RESOURCES = "src/main/resources";
}