package JourneyPlanner;

import java.awt.*;

public class Connection {
    private Stop start, end;
    private Trip parent;
    private boolean isHighlighted;

    public Connection(Stop start, Stop end, Trip parent){
        this.start = start;
        this.end = end;
        this.parent = parent;
    }

    public void draw(Graphics g, Location origin, double scale) {
        if (isHighlighted) {
            g.setColor(Color.GREEN);
        } else {
            g.setColor(Color.CYAN);
        }
        double startX = start.getLocation().asPoint(origin, scale).x, startY = start.getLocation().asPoint(origin, scale).y;
        double endX = end.getLocation().asPoint(origin, scale).x, endY = end.getLocation().asPoint(origin, scale).y;

        g.drawLine((int) startX, (int) startY, (int) endX, (int) endY);
    }

    public Trip getParent() {
        return parent;
    }

    public void setHighlight(boolean isHighlighted) {
//        if (isHighlighted) System.out.println("Highlighting connection");
        if (!isHighlighted) System.out.println("Unhighlighting connection");;
        this.isHighlighted = isHighlighted;
        start.setHighlight(true);
    }
}
