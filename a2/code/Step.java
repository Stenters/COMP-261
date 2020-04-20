public class Step {
    Node current, parent;
    double g, h;

    Step(Node n, Node goal){
        this.current = n;
        g = 0;
        h = goal.location.distance(n.location);
    }

    Step(Node n, Step prev, Node goal, boolean isDistance){
        this.current = n;
        this.parent = prev.current;
        this.h = goal.location.distance(n.location);

        if (isDistance) {
            // Get the distance from the previous node + the distance from the origin to the previous node
            g = prev.g + prev.current.location.distance(n.location);

        } else {
            // Get the time of all segments between the two nodes, then get the shortest time of that
            double minTime = n.segments.stream()
                    .filter(x -> (x.start == n && x.end == prev.current) || (x.end == prev.current && x.end == n))
                    .mapToDouble(x -> x.length / x.road.speed)
                    .min()
                    .orElse(0);
            // Should never happen
            if (minTime == 0) {
                System.out.println("min time is 0 for nodes:\n" + n + "\n" + prev.current);
            }

            // Get the time from the origin to the previous node + the time from the previous node to this one
            g = prev.g + minTime;
        }
    }

    double getF() {return g + h; }

    @Override
    public String toString() {
        return "Step{" +
                "current=" + current +
                ", parent=" + parent +
                ", g=" + g +
                ", h=" + h +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Step && ((Step) o).current == current;
    }

}
