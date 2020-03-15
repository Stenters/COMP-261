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
//        double startX = origin.x + (start.getLocation().x * scale) + (Stop.CIRCLE_DIAMETER / 2.0), startY = origin.y + (start.getLocation().y * scale) + (Stop.CIRCLE_DIAMETER / 2.0);
//        double endX = origin.x + (end.getLocation().x * scale) + (Stop.CIRCLE_DIAMETER / 2.0), endY = origin.y + (end.getLocation().y * scale) + (Stop.CIRCLE_DIAMETER / 2.0);

        g.drawLine((int) startX, (int) startY, (int) endX, (int) endY);
    }

    public Trip getParent() {
        return parent;
    }

    public void setHighlight(boolean isHighlighted) {
        this.isHighlighted = isHighlighted;
    }
}
