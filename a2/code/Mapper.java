import javax.swing.*;
import java.awt.*;
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
public class  Mapper extends GUI {
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

	// To determine if routes should compute by distance (true) or
	// time (false)
	private boolean isDistance = true;

	// Nodes to search for using a*
	private Node start, end;

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

		if (closest == null) { return; }

		// if it's close enough, highlight it and show some information.
		if (clicked.distance(closest.location) < MAX_CLICKED_DISTANCE) {
			graph.setHighlight(closest);
			getTextOutputArea().setText(closest.toString());

			if (SwingUtilities.isRightMouseButton(e)) {
				JPopupMenu context = new JPopupMenu();
				JMenuItem startItem = new JMenuItem("Make start"), endItem = new JMenuItem("Make end");

				Node finalClosest = closest; // BC how Java handles variables in lambdas
				startItem.addActionListener(e1 -> start = finalClosest);
				endItem.addActionListener(e1 -> end = finalClosest);

				context.add(startItem);
				context.add(endItem);
				context.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	@Override
	protected void onSearch() {
		// TODO
	}

	@Override
	protected void onMove(Move m) {
		if (origin == null) {
			System.out.println("origin is null!");
		}
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

		JPanel tripPanel = new JPanel();
		tripPanel.setLayout(new BoxLayout(tripPanel, BoxLayout.LINE_AXIS));
		tripPanel.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));

		JLabel startLabel = new JLabel("Start"), endLabel = new JLabel("End");
		// TODO: select start and end
		JTextField startNode = new JTextField(), endNode = new JTextField();
		ButtonGroup distanceOrTime = new ButtonGroup();
		JRadioButton distance = new JRadioButton("Distance"), time = new JRadioButton("Time");
		JButton findRoute = new JButton("Search");

		startNode.setEnabled(false);
		endNode.setEnabled(false);

		findRoute.addActionListener(e ->
			listRoute(graph.nodes.get(Integer.parseInt(startNode.getText())),
					  graph.nodes.get(Integer.parseInt(endNode.getText()))));

		distance.addActionListener(e -> isDistance = true);
		time.addActionListener(e -> isDistance = false);

		distance.setSelected(true);
		distanceOrTime.add(distance);
		distanceOrTime.add(time);

		tripPanel.add(startLabel);
		tripPanel.add(Box.createRigidArea(new Dimension(5,0)));
		tripPanel.add(startNode);
		tripPanel.add(Box.createRigidArea(new Dimension(15,0)));
		tripPanel.add(endLabel);
		tripPanel.add(Box.createRigidArea(new Dimension(5,0)));
		tripPanel.add(endNode);

		tripPanel.add(Box.createRigidArea(new Dimension(15,0)));
		tripPanel.add(distance);
		tripPanel.add(time);

		tripPanel.add(Box.createHorizontalGlue());
		tripPanel.add(findRoute);

		frame.add(tripPanel, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}

	private Step aStar(Node start, Node end) {

		LinkedList<Step> open = new LinkedList<>(), closed = new LinkedList<>();
		open.add(new Step(start, end));

		while (open.size() > 0) {
			Step current = open.stream().min(Comparator.comparingDouble(Step::getF)).get();
//			System.out.printf("\n%s is minimum of \n%s\n\n", current, open);

			if (current.current == end) {
				return current;
			}

			for(Node n : current.current.getNeighbors()) {
				Step nextStep = new Step(n, current, end, isDistance);

				// Comparator is overridden, so only looks for Step objects with the same current node
				if (!open.contains(nextStep)) {
					if (!closed.contains(nextStep)) {
						open.add(nextStep);
					} else if (closed.get(closed.indexOf(nextStep)).g > nextStep.g) {
						closed.remove(nextStep);
						open.add(nextStep);
					}
				} else if (open.get(open.indexOf(nextStep)).g > nextStep.g) {
					open.set(open.indexOf(nextStep), nextStep);
				}
			}

			closed.add(current);
			open.remove(current);

		}

		return null;
	}

	private void listRoute(Node start, Node end) {
		Stack<Segment> segments = new Stack<>();
		Step curr = aStar(start, end);
		if (curr == null) {
			System.err.println("A* failed on nodes \n" + start + "\n\n\tand\n\n" + end);
			return;
		}

		while (curr.parent != null) {
			segments.push(curr.toParent);
			curr = curr.parent;
		}

		getTextOutputArea().setText("");

		while (!segments.empty()) {
			Segment s = segments.pop();
			getTextOutputArea().append(s.road.name + " (" + s.length + ")\n");
		}

	}

}

// code for COMP261 assignments