import java.util.Comparator;

public class Step {
    Node current;
    Step parent;
    Segment toParent;
    double g, h;

    Step(Node n, Node goal){
        this.current = n;
        g = 0;
        h = goal.location.distance(n.location);
    }

    Step(Node n, Step prev, Node goal, boolean isDistance){
        this.current = n;
        this.parent = prev;
        this.h = goal.location.distance(n.location);

        if (isDistance) {
            toParent = n.segments.stream()
                    .filter(x -> (x.start == n && x.end == prev.current && !x.road.isOneway)
                              || (x.start == prev.current && x.end == n))
                    .min(Comparator.comparingDouble(x -> x.length))
                    .orElse(null);

            // Should never happen
            if (toParent == null) {
                System.err.println("min dist segment is null for nodes:\n" + n + "\n\n" + prev.current + "\n");
            }

            g = prev.g + toParent.length;

        } else {
            // Get the time of all segments between the two nodes, then get the shortest time of that
            toParent = n.segments.stream()
                    .filter(x -> (x.start == n && x.end == prev.current && !x.road.isOneway)
                              || (x.start == prev.current && x.end == n))
                    .min(Comparator.comparingDouble(x -> x.length/x.road.speed))
                    .orElse(null);

            // Should never happen
            if (toParent == null) {
                System.err.println("min time segment is null for nodes:\n" + n + "\n" + prev.current);
            }

            // Get the time from the origin to the previous node + the time from the previous node to this one
            g = prev.g + (toParent.length / toParent.road.speed);
        }
    }

    double getF() {return g + h; }

    @Override
    public String toString() {
        String parentId = parent == null ? "null" : Integer.toString(parent.current.nodeID);

        return "Step{" +
                "current=" + current.nodeID +
                ", parent=" + parentId +
                ", g=" + g +
                ", h=" + h +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Step && ((Step) o).current == current;
    }

}
