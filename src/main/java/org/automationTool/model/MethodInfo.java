package org.automationTool.model;

public class MethodInfo {
    public String name;
    public String returnType;
    public String params;

    public MethodInfo(String name, String returnType, String params) {
        this.name = name;
        this.returnType = returnType;
        this.params = params;
    }
}