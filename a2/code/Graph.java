import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 * This represents the data structure storing all the roads, nodes, and
 * segments, as well as some information on which nodes and segments should be
 * highlighted.
 * 
 * @author tony
 */
public class Graph {
	// map node IDs to Nodes.
	Map<Integer, Node> nodes;
	// map road IDs to Roads.
	Map<Integer, Road> roads;
	// just some collection of Segments.
	Collection<Segment> segments;

	Collection<Node> highlightedNodes = new HashSet<>();
	Collection<Segment> highlightedSegments = new HashSet<>();

	public Graph(File nodes, File roads, File segments, File polygons, File restrictions) {
		this.nodes = Parser.parseNodes(nodes);
		this.roads = Parser.parseRoads(roads);
		this.segments = Parser.parseSegments(segments, this);
		Parser.parseRestrictions(restrictions, this);

	}

	public void draw(Graphics g, Dimension screen, Location origin, double scale) {
		// a compatibility wart on swing is that it has to give out Graphics
		// objects, but Graphics2D objects are nicer to work with. Luckily
		// they're a subclass, and swing always gives them out anyway, so we can
		// just do this.
		Graphics2D g2 = (Graphics2D) g;

		// draw all the segments.
		g2.setColor(Mapper.SEGMENT_COLOUR);
		for (Segment s : segments)
			s.draw(g2, origin, scale);

		g2.setColor(Mapper.HIGHLIGHT_COLOUR);
		// draw the highlighted segments
		for (Segment s : highlightedSegments) {
			s.draw(g2, origin, scale);
		}

		// draw all the nodes.
		g2.setColor(Mapper.NODE_COLOUR);
		for (Node n : nodes.values())
			n.draw(g2, screen, origin, scale);

		// draw the highlighted nodes
		g2.setColor(Mapper.HIGHLIGHT_COLOUR);
		for (Node n : highlightedNodes) {
			n.draw(g2, screen, origin, scale);
		}
	}

	public void addHighlightedNode(Node node) { this.highlightedNodes.add(node); }
	public void addHighlightedSegment(Segment segment) { this.highlightedSegments.add(segment); }
	public void unHighlight() {
		highlightedNodes = new HashSet<>();
		highlightedSegments = new HashSet<>();
	}
}

// code for COMP261 assignments