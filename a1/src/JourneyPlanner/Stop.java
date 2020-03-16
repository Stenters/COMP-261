package JourneyPlanner;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Stop {

    private String ID, name;
    private Location location;
    List<Connection> incoming, outgoing;
    private boolean isHighlighted;
    public static final int CIRCLE_DIAMETER = 10;

    public Stop(String id, String name, double lat, double lon) {
        this(id, name, Location.newFromLatLon(lat, lon));
    }

    public Stop(String id, String name, Location loc){
        this.ID = id;
        this.name = name;
        this.location = loc;
        this.incoming = new ArrayList<Connection>();
        this.outgoing = new ArrayList<Connection>();
    }

    public void draw(Graphics g, Location origin, double scale) {
        Point loc = location.asPoint(origin, scale);

        if (isHighlighted) {
            g.setColor(Color.GREEN);

        } else {
            g.setColor(Color.BLUE);
        }

        g.fillOval( loc.x, loc.y, CIRCLE_DIAMETER, CIRCLE_DIAMETER);

        for(Connection c : outgoing) {
            c.draw(g, origin, scale);
        }
    }

    public void addIncomingConnection(Connection c) {
        incoming.add(c);
    }

    public void addOutgoingConnection(Connection c) {
        outgoing.add(c);
    }

    public String getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() { return location; }

    public void setHighlight(boolean isHighlighted) {
        this.isHighlighted = isHighlighted;
    }

    public void setTripHighlighted(boolean isHighlighted) {
        for(Trip t : getTrips()) { t.setHighlight(isHighlighted); }
    }

    public List<Trip> getTrips() {
        return Stream.concat(incoming.stream(), outgoing.stream())
                .map(c -> c.getParent()).distinct().collect(Collectors.toCollection(LinkedList::new));
    }
}
