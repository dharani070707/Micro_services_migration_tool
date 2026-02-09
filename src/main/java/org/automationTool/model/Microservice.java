package org.automationTool.model;

import java.util.HashSet;
import java.util.Set;

public class Microservice {
    public String name;
    public Set<String> controllers = new HashSet<>();
    public Set<String> entities = new HashSet<>();

    @Override
    public String toString() {
        return name + " -> Controllers: " + controllers + ", Entities: " + entities;
    }
}
