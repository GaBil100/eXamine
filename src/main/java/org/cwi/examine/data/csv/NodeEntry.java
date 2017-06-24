package org.cwi.examine.data.csv;

/**
 * Row node fields.
 */
public class NodeEntry extends ElementEntry {

    private String module;
    private String processes;
    private String functions;
    private String components;
    private String pathways;

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getProcesses() {
        return processes;
    }

    public void setProcesses(String processes) {
        this.processes = processes;
    }

    public String getFunctions() {
        return functions;
    }

    public void setFunctions(String functions) {
        this.functions = functions;
    }

    public String getComponents() {
        return components;
    }

    public void setComponents(String components) {
        this.components = components;
    }

    public String getPathways() {
        return pathways;
    }

    public void setPathways(String pathways) {
        this.pathways = pathways;
    }

    @Override
    public String toString() {
        return "NodeEntry{" +
                "module='" + module + '\'' +
                ", processes='" + processes + '\'' +
                ", functions='" + functions + '\'' +
                ", components='" + components + '\'' +
                ", pathways='" + pathways + '\'' +
                "} extends " + super.toString();
    }
}
