package org.automationTool.model;

import java.util.HashSet;
import java.util.Set;

public class Microservice {

    public String name;
    public Set<String> controllers = new HashSet<>();
    public Set<String> entities = new HashSet<>();

    // Constructor
    public Microservice(String name) {
        this.name = name;
    }

    // Getters
    public String getName() {
        return name;
    }

    public Set<String> getControllers() {
        return controllers;
    }

    public Set<String> getEntities() {
        return entities;
    }

    // Optional: Add helpers
    public void addController(String controllerPath) {
        controllers.add(controllerPath);
    }

    public void addEntity(String entityPath) {
        entities.add(entityPath);
    }

    @Override
    public String toString() {
        return name + " -> Controllers: " + controllers + ", Entities: " + entities;
    }
}