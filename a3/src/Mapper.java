import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.*;

/**
 * This is the main class for the mapping program. It extends the GUI abstract
 * class and implements all the methods necessary, as well as having a main
 * function.
 * 
 * @author tony
 */
public class Mapper extends GUI {
	public static final Color NODE_COLOUR = new Color(77, 113, 255);
	public static final Color SEGMENT_COLOUR = new Color(130, 130, 130);
	public static final Color HIGHLIGHT_COLOUR = new Color(255, 219, 77);

	// these two constants define the size of the node squares at different zoom
	// levels; the equation used is node size = NODE_INTERCEPT + NODE_GRADIENT *
	// log(scale)
	public static final int NODE_INTERCEPT = 1;
	public static final double NODE_GRADIENT = 0.8;

	// defines how much you move per button press, and is dependent on scale.
	public static final double MOVE_AMOUNT = 100;

	// defines how much you zoom in/out per button press, and the maximum and
	// minimum zoom levels.
	public static final double ZOOM_FACTOR = 1.3;
	public static final double MIN_ZOOM = 1, MAX_ZOOM = 200;

	// how far away from a node you can click before it isn't counted.
	public static final double MAX_CLICKED_DISTANCE = 0.15;

	// these two define the 'view' of the program, ie. where you're looking and
	// how zoomed in you are.
	private Location origin;
	private double scale;

	// our data structures.
	private Graph graph;

	private Map<Integer, Boolean> visitedNodes;
	private Map<Integer, Integer> nodeDepths;

	@Override
	protected void redraw(Graphics g) {
		if (graph != null)
			graph.draw(g, getDrawingAreaDimension(), origin, scale);
	}

	@Override
	protected void onClick(MouseEvent e) {
		Location clicked = Location.newFromPoint(e.getPoint(), origin, scale);
		// find the closest node.
		double bestDist = Double.MAX_VALUE;
		Node closest = null;

		for (Node node : graph.nodes.values()) {
			double distance = clicked.distance(node.location);
			if (distance < bestDist) {
				bestDist = distance;
				closest = node;
			}
		}

		// if it's close enough, highlight it and show some information.
		if (clicked.distance(closest.location) < MAX_CLICKED_DISTANCE) {
			graph.setHighlightedNode(closest);
			getTextOutputArea().setText(closest.toString());
		}
	}

	@Override
	protected void onSearch() {
		// Does nothing
	}

	@Override
	protected void onMove(Move m) {
		if (m == GUI.Move.NORTH) {
			origin = origin.moveBy(0, MOVE_AMOUNT / scale);
		} else if (m == GUI.Move.SOUTH) {
			origin = origin.moveBy(0, -MOVE_AMOUNT / scale);
		} else if (m == GUI.Move.EAST) {
			origin = origin.moveBy(MOVE_AMOUNT / scale, 0);
		} else if (m == GUI.Move.WEST) {
			origin = origin.moveBy(-MOVE_AMOUNT / scale, 0);
		} else if (m == GUI.Move.ZOOM_IN) {
			if (scale < MAX_ZOOM) {
				// yes, this does allow you to go slightly over/under the
				// max/min scale, but it means that we always zoom exactly to
				// the centre.
				scaleOrigin(true);
				scale *= ZOOM_FACTOR;
			}
		} else if (m == GUI.Move.ZOOM_OUT) {
			if (scale > MIN_ZOOM) {
				scaleOrigin(false);
				scale /= ZOOM_FACTOR;
			}
		}
	}

	@Override
	protected void onLoad(File nodes, File roads, File segments, File polygons) {
		graph = new Graph(nodes, roads, segments, polygons);
		origin = new Location(-250, 250); // close enough
		scale = 1;
	}

	/**
	 * This method does the nasty logic of making sure we always zoom into/out
	 * of the centre of the screen. It assumes that scale has just been updated
	 * to be either scale * ZOOM_FACTOR (zooming in) or scale / ZOOM_FACTOR
	 * (zooming out). The passed boolean should correspond to this, ie. be true
	 * if the scale was just increased.
	 */
	private void scaleOrigin(boolean zoomIn) {
		Dimension area = getDrawingAreaDimension();
		double zoom = zoomIn ? 1 / ZOOM_FACTOR : ZOOM_FACTOR;

		int dx = (int) ((area.width - (area.width * zoom)) / 2);
		int dy = (int) ((area.height - (area.height * zoom)) / 2);

		origin = Location.newFromPoint(new Point(dx, dy), origin, scale);
	}

	public static void main(String[] args) {
		new Mapper();
	}

	public void highlightArticulationPoints() {
		List<Node> APs = new LinkedList<>();
		for (Graph g: getComponents()) {
			Node start = g.nodes.entrySet().iterator().next().getValue();
			APs.addAll(DFS(start));
		}

		graph.setHighlightedNodes(APs);
	}

	public static void highlightMinimumSpanningTrees() {
		// TODO
	}

	public List<Node> DFS(Node root) {
		List<Node> APs = new LinkedList<>();
		int numSubTrees = 0;

		for (Node n : root.getNeighbors()) {
			if (isUnvisited(n.nodeID)){
				APs.addAll(getAPs(n, root));
				numSubTrees++;
			}
		}
		if (numSubTrees > 1) {
			APs.add(root);
		}

		return APs;
	}

	private List<Node> getAPs(Node startingNode, Node root) {
		int depth = 1;
		List<Node> APs = new LinkedList<>();
		Stack<NodeElement> nodesToCheck = new Stack<>();

		// Initialize the stack
		nodesToCheck.push(new NodeElement(startingNode, depth, root));

		// Iterate over all nodes in the subtree
		while (!nodesToCheck.empty()) {
			// Node to work with
			NodeElement element = nodesToCheck.peek();

			// First time visiting this node, initialize it
			if (isUnvisited(element.node.nodeID)) {
				// Set visited and reachback
				setVisited(element.node.nodeID);
				element.node.reachBack = depth;
				setDepth(element.node.nodeID, depth);
				// Set children
				Set<Node> neighbors = element.node.getNeighbors();
				neighbors.removeIf(e -> e.equals(element.parent));
				element.addChildren(neighbors);

			// Not done with this node yet, process one child
			} else if (!element.children.empty()) {
				Node child = element.popChild();
				// If you haven't visited a node, add it to the stack of nodes to check
				if (isUnvisited(child.nodeID)) {
					nodesToCheck.push(
							new NodeElement(child,
									getDepth(element.node.nodeID) +1,
									element.node));
				// You've already visited this node, your reachback cannot exceed its
				//	reachback
				} else {
					element.node.reachBack = Math.min(element.node.reachBack,
							getDepth(child.nodeID));
				}

			// All the node's children have been processed, process this node
			} else {
				// Ignore the starting node
				if (!element.node.equals(startingNode)){
					// Update the parent's reachback if this one's better
					element.parent.reachBack = Math.min(element.node.reachBack,
							element.parent.reachBack);
					// If you can't reach a node above the parent, you need the parent
					//	and it is an articulation point
					if (element.node.reachBack >= getDepth(element.parent.nodeID)) {
						APs.add(element.parent);
					}
				}
				// Finished processing this node
				nodesToCheck.pop();
			}

		}

		return APs;
	}

	private boolean isUnvisited(int id) {
		return visitedNodes.containsKey(id) && visitedNodes.get(id);
	}

	private void setVisited(int id) {
		visitedNodes.put(id, true);
	}

	private void setDepth(int id, int depth) {
		nodeDepths.put(id, depth);
	}

	private int getDepth(int id) {
		if (nodeDepths.containsKey(id)) {
			return nodeDepths.get(id);
		}
		return -1;
	}

	private static class NodeElement {
		Node node, parent;
		Stack<Node> children = new Stack<>();

		NodeElement(Node node, int depth, Node parent) {
			this.node = node;
			this.parent = parent;
		}

		public void addChildren(Set<Node> n) {
			children.addAll(n);
		}

		public Node popChild() {
			return children.pop();
		}
	}

	private List<Graph> getComponents() {
		LinkedList<Graph> components = new LinkedList<>();

		//TODO: Kosaraju

		return components;
	}

}

/*
TODO
	Articulation points
		Undirected
		No restrictions
		Can be disconnected
	Minimum spanning tree
		Undirected
		No restrictions
		Minimizes total length of tree
		Can be disconnected
	Minimum
		Node.getAdj()
		highlightArticulationPoints()
			recursive or iterative (ext pts)
			support disconnected
			pseudocode in report
	Core
		Node.getWeightedAdj()
		getMinSpanTree()
			Prim's algorithm vs Kruskal's algorithm (ext pts w/ disjoint set)
			support disconnected
			pseudocode in report
	Completion
		questions
	Challenge
		Use iterative
		use disjoint w/ Kruskal
 */

// code for COMP261 assignments