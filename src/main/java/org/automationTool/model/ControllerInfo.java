package org.automationTool.model;

import java.util.HashSet;
import java.util.Set;

public class ControllerInfo {

    public String controllerName;
    public String packageName;

    public String filePath;

    // Repository / Service dependencies
    public Set<String> dependencies = new HashSet<>();

    // Entities used by this controller
    public Set<String> entities = new HashSet<>();

    @Override
    public String toString() {
        return packageName + "." + controllerName +
                " -> Repos/Services: " + dependencies +
                " -> Entities: " + entities +
                " -> FilePath: " + filePath;
    }
}