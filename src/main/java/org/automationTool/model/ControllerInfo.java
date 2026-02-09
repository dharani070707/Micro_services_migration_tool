package org.automationTool.model;
import java.util.HashSet;
import java.util.Set;

public class ControllerInfo {
    public String controllerName;
    public String packageName;
    public Set<String> dependencies = new HashSet<>();
    public Set<String> entities = new HashSet<>();
    @Override
    public String toString() {
        return packageName + "." + controllerName +
                " -> Repos: " + dependencies +
                " -> Entities: " + entities;
    }
}