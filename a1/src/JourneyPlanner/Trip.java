package JourneyPlanner;

import java.util.ArrayList;
import java.util.List;

public class Trip {
    private String ID;
    private List<Connection> connections = new ArrayList<Connection>();

    public Trip(String id, List<Connection> connections){
        this(id);
        this.connections = connections;
    }

    public Trip(String id){
        this.ID = id;
    }

    public void addConnection(Connection c) {
        connections.add(c);
    }

    public void highlight() {
        for (Connection c : connections) {
            c.setHighlight(true);
        }
    }
}
