import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.util.*;

/**
 * Node represents an intersection in the road graph. It stores its ID and its
 * location, as well as all the segments that it connects to. It knows how to
 * draw itself, and has an informative toString method.
 * 
 * @author tony
 */
public class Node {

	public final int nodeID;
	public final Location location;
	public final Collection<Segment> segments;
	public int reachBack = 0;

	public Node(int nodeID, double lat, double lon) {
		this.nodeID = nodeID;
		this.location = Location.newFromLatLon(lat, lon);
		this.segments = new HashSet<>();
	}

	public void addSegment(Segment seg) {
		segments.add(seg);
	}

	public void draw(Graphics g, Dimension area, Location origin, double scale) {
		Point p = location.asPoint(origin, scale);

		// for efficiency, don't render nodes that are off-screen.
		if (p.x < 0 || p.x > area.width || p.y < 0 || p.y > area.height)
			return;

		int size = (int) (Mapper.NODE_GRADIENT * Math.log(scale) + Mapper.NODE_INTERCEPT);
		g.fillRect(p.x - size / 2, p.y - size / 2, size, size);
	}

	public String toString() {
		Set<String> edges = new HashSet<>();
		for (Segment s : segments) {
			edges.add(s.road.name);
		}

		StringBuilder str =
				new StringBuilder("ID: " + nodeID + "  loc: " + location + "\nroads: ");
		for (String e : edges) {
			str.append(e).append(", ");
		}
		return str.substring(0, str.length() - 2);
	}

	public Set<Node> getNeighbors() {
		Set<Node> neighbors = new HashSet<>();

		for(Segment s : segments) {
			if (s.start.equals(this)) {
				neighbors.add(s.end);
			} else {
				neighbors.add(s.start);
			}
		}

		return neighbors;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Node && this.nodeID == ((Node) o).nodeID;
	}

}

// code for COMP261 assignments