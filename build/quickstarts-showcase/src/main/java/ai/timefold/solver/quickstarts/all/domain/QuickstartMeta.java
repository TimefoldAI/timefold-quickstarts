package ai.timefold.solver.quickstarts.all.domain;

import java.util.ArrayList;
import java.util.List;

public class QuickstartMeta {

    private String id;

    private List<Integer> ports = new ArrayList<>();

    public QuickstartMeta() {
    }

    public QuickstartMeta(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public List<Integer> getPorts() {
        return ports;
    }

    public void setPorts(List<Integer> ports) {
        this.ports = ports;
    }

}
