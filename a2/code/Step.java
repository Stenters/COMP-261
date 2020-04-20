import java.util.Objects;

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
        h = goal.location.distance(n.location);
        g = prev.g + prev.current.location.distance(n.location);
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
