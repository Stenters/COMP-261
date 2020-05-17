import javax.swing.*;
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

	private Collection<Integer> visitedNodes;
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

		if (SwingUtilities.isRightMouseButton(e)) {
			JPopupMenu context = new JPopupMenu();
			JMenuItem artPtItem = new JMenuItem("Highlight Articulation Points" +
					" connected to this node"),
					minTreeItem = new JMenuItem("Highlight the minimum spanning tree" +
							" connected to this node");

			Node finalClosest = closest; // BC how Java handles variables in lambdas
			artPtItem.addActionListener(e1 -> highlightArticulationPoints(finalClosest));
			minTreeItem.addActionListener(e1 -> highlightMinimumSpanningTrees());

			context.add(artPtItem);
			context.add(minTreeItem);
			context.show(e.getComponent(), e.getX(), e.getY());
		}

		// if it's close enough, highlight it and show some information.
		if (closest != null && clicked.distance(closest.location) < MAX_CLICKED_DISTANCE) {
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

	@Override
	protected void initialise() {
		super.initialise();

		getTextOutputArea().setText("Right click to highlight articulation points"
			+ " or minimum spanning tree");

		onLoad(new File("./data/small/nodeID-lat-lon.tab"),
				new File("./data/small/roadID-roadInfo.tab"),
				new File("./data/small/roadSeg-roadID-length-nodeID-nodeID-coords.tab"),
				null);
		redraw();
	}

	/* GUI Methods */

	public void highlightArticulationPoints(Node root) {
		visitedNodes = new HashSet<>();
		nodeDepths = new HashMap<>();
		List<Node> APs = new LinkedList<>(DFS(root));
		graph.setHighlightedNodes(APs);

		getTextOutputArea().setText("Number of Articulation points: " + APs.size() + "\n");

		for(Node n : APs) {
			getTextOutputArea().append(n.toString() + "\n");
		}

		redraw();

	}

	public void highlightMinimumSpanningTrees() {
		graph.setHighlightedSegments(getMST());
		redraw();
	}

//	public List<List<Node>> getComponents() {
//		visitedNodes = new HashSet<>();
//		LinkedList<List<Node>> components = new LinkedList<>();
//		for (Node n : graph.nodes.values()) {
//			components.add(getComponent(n));
//		}
//
//		return components;
//	}
//
//	private List<Node> getComponent(Node n) {
//		LinkedList<Node> component = new LinkedList<>();
//
//		if (isUnvisited(n.nodeID)) {
//			setVisited(n.nodeID);
//			component.add(n);
//			for(Node m : n.getNeighbors()) {
//				component.addAll(getComponent(m));
//			}
//		}
//
//		return component;
//	}

	/* DFS & APs */

	public List<Node> DFS(Node root) {
		List<Node> APs = new LinkedList<>();
		int numSubTrees = 0;

		for (Node n : root.getNeighbors()) {
			if (isUnvisited(n.nodeID)){
				APs.addAll(getAPs(n, root));
				++numSubTrees;
			}
		}

		if (numSubTrees > 1) {
			APs.add(root);
		}

		return APs;
	}

	private List<Node> getAPs(Node startingNode, Node root) {
		List<Node> APs = new LinkedList<>();
		Stack<NodeElement> nodesToCheck = new Stack<>();

		// Initialize the stack
		nodesToCheck.push(new NodeElement(startingNode,1, root));

		// Iterate over all nodes in the subtree
		while (!nodesToCheck.empty()) {
			// Node to work with
			NodeElement element = nodesToCheck.peek();

			// First time visiting this node, initialize it
			if (isUnvisited(element.node.nodeID)) {
				// Set visited and reachback
				setVisited(element.node.nodeID);
				element.node.reachBack = element.depth;
				setDepth(element.node.nodeID, element.depth);

				// Set children
				Set<Node> neighbors = element.node.getNeighbors();
				neighbors.removeIf(e -> e.equals(element.parent));
				element.addChildren(neighbors);

			// Not done with this node yet, process one child
			} else if (!element.children.empty()) {
				Node child = element.popChild();

				// If you haven't visited a node, add it to the stack of nodes to check
				if (isUnvisited(child.nodeID)) {
					nodesToCheck.push(new NodeElement(child, element.depth + 1, element.node));

				// You've already visited this node, your reachback cannot exceed its depth
				} else {
					element.node.reachBack = Math.min(element.node.reachBack, getDepth(child.nodeID));
				}

			// All the node's children have been processed, process this node
			} else {
				// Ignore the starting node
				if (!element.node.equals(startingNode)){

					// Update the parent's reachback if this one's better
					element.parent.reachBack = Math.min(element.node.reachBack, element.parent.reachBack);

					// If you can't reach a node above the parent, you need the parent
					//	and it is an articulation point
					if (element.node.reachBack >= getDepth(element.parent.nodeID) && !APs.contains(element.parent)) {

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
		return !visitedNodes.contains(id);
	}

	private void setVisited(int id) {
		visitedNodes.add(id);
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
		int depth;
		Node node, parent;
		Stack<Node> children = new Stack<>();

		NodeElement(Node node, int depth, Node parent) {
			this.node = node;
			this.depth = depth;
			this.parent = parent;
		}

		public void addChildren(Set<Node> n) {
			children.addAll(n);
		}

		public Node popChild() {
			return children.pop();
		}
	}

	/* MST */
	private Set<Segment> getMST() {
		PriorityQueue<FringeElement> fringe = new PriorityQueue<>();
		Set<Segment> tree = new HashSet<>();
		HashMap<Integer, TreeNode> forest = new HashMap<>();

		for (int k : graph.nodes.keySet()) {
			forest.put(k, new TreeNode(graph.nodes.get(k)));
		}

		for(Segment s : graph.segments) {
			fringe.add(new FringeElement(forest.get(s.start.nodeID), forest.get(s.end.nodeID), s.start.getShortestSegment(s.end)));
		}

		System.out.printf("Starting sizes:\t\tforest: %d\t\tfringe: %d\t\ttree: %d\n", forest.size(), fringe.size(), tree.size());
		FringeElement element;

		while (forest.size() > 1 && (element = fringe.poll()) != null) {
			/*
		while ( (n = queue.poll()) != null ) {
            Set a1 = n.a.getParent(), b1 = n.b.getParent();

            if (a1 == b1) { continue; }

            if (a1.depth < b1.depth) {
                a1.setParent(b1);
                b1.updateDepth(a1.depth);
            } else {
                b1.setParent(a1);
                a1.updateDepth(b1.depth);
            }
        }
		*/

			TreeNode parentA = element.a.getParent(), parentB = element.b.getParent();
			if (parentA.depth != 0 || parentB.depth != 0) {
				System.out.println("Working!");
			}

			if (parentA.equals(parentB)) {
				continue;
			}

			tree.add(element.edge);

			if (parentA.depth < parentB.depth) {
				parentA.setParent(parentB);
				parentB.updateDepth(parentA);
				forest.remove(element.a.data.nodeID);
			} else {
				parentB.setParent(parentA);
				parentA.updateDepth(parentB);
				forest.remove(element.b.data.nodeID);
			}
		}

		System.out.printf("Ending sizes:\t\tforest: %d\t\tfringe: %d\t\ttree:%d\n", forest.size(), fringe.size(), tree.size());
		return tree;
	}

	private static class FringeElement implements Comparable<FringeElement> {
		TreeNode a, b;
		Segment edge;

		FringeElement(TreeNode a, TreeNode b, Segment edge) {
			this.a = a;
			this.b = b;
			this.edge = edge;
		}

		@Override
		public int compareTo(FringeElement o) {
			return Double.compare(edge.length, o.edge.length);
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof FringeElement && ((FringeElement) o).edge == this.edge;
		}
	}

	private static class TreeNode {
		private TreeNode parent;
		int depth = 0;
		Node data;

		TreeNode(Node n) {
			data = n;
			parent = this;
		}

		public TreeNode getParent() {
			if (parent == this) {
				return this;
			}
			return parent.getParent();
		}

		public void setParent(TreeNode parent) {
			this.parent = parent;
		}

		public void updateDepth(TreeNode child) {
			if (depth == child.depth) {
				depth++;
			}
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof TreeNode && ((TreeNode) o).data.equals(data);
		}

		@Override
		public int hashCode() {
			return data.nodeID;
		}

	}

}

/*
TODO
	Report
		including pseudocode
	Minimum
		Finds articulation points in one component
		Displays art pts
		report w/ pseudocode
		all components
	Core
		MST 1 component
		report w/ pseudocode
		all components
	Completion
		questions
	Challenge
		question abt fft
  */

// code for COMP261 assignments