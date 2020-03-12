package JourneyPlanner;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class JourneyPlanner extends GUI {
    /** Variables **/
    JourneyTrie stopTrie = new JourneyTrie();
    HashMap<String,Stop> stopHashMap = new HashMap<String, Stop>();
    JourneyQuad stopQuad = new JourneyQuad();
    List<Stop> stopList = new ArrayList<Stop>(); // TODO: which data structure?
    List<Trip> tripList = new ArrayList<Trip>();

    public static int MOVE_FACTOR = 10, ZOOM_FACTOR = 2;
    private Location origin;
    private double scale;

    /** GUI Methods **/
    @Override
    protected void redraw(Graphics g) {
        g.setColor(Color.BLACK);
        g.drawOval( getDrawingAreaDimension().width / 2, getDrawingAreaDimension().height / 2, 10, 10);

        if (stopList.size() > 1)
            stopList.get(1).setHighlight(true);

        for (Stop s : stopList) {
            s.draw(g, origin, scale);
        }
    }

    @Override
    protected void onClick(MouseEvent e) {
        Stop s = findNearestStop(e.getX(), e.getY());
        s.setHighlight(true);
        // TODO: display info?
    }

    @Override
    protected void onSearch() {
        String text = getSearchBox().getText();
        List<Stop> matchingStops = findMatchingRoutes(text);
        for (Stop s : matchingStops) {
            s.setHighlight(true);
            for (Connection c : s.incoming) {
                c.getParent().highlight();
            }
            for (Connection c : s.outgoing){
                c.getParent().highlight();
            }
        }
    }

    @Override
    protected void onMove(Move m) {
        double dY = 0, dX = 0;
        switch (m){
            case EAST:
                dX = -1 * MOVE_FACTOR;
                break;
            case SOUTH:
                dY = -1 * MOVE_FACTOR;
                break;
            case WEST:
                dX = MOVE_FACTOR;
                break;
            case NORTH:
                dY = MOVE_FACTOR;
                break;
            case ZOOM_IN:
                scale *= ZOOM_FACTOR;
                break;
            case ZOOM_OUT:
                scale /= ZOOM_FACTOR;
                break;
            default:
                getTextOutputArea().setText("Illegal Direction!");
                break;
        }

        origin = origin.moveBy(dX, dY);
        redraw();
    }

    @Override
    protected void onLoad(File stopFile, File tripFile) {
        try {
            BufferedReader stopReader = new BufferedReader(new FileReader(stopFile)), tripReader = new BufferedReader(new FileReader(tripFile));

            getTextOutputArea().setText("Loading files");

            stopList = new LinkedList<Stop>();
            tripList = new LinkedList<Trip>();

            parseStopFile(stopReader);
            parseTripFile(tripReader);
            populateDataStructures();

        } catch (IOException e) {
            getTextOutputArea().setText("Invalid files passed in! \n\tDetails:\n" + e);
        }
    }

    /** Helper Methods **/

    private Stop findNearestStop(int x, int y) {
        // TODO: implement Trie
        Location click = new Location(x,y);
        double minDist = Double.MAX_VALUE;
        Stop winner = null;

        for (Stop s : stopList) {
            double dist = s.getLocation().distance(click);
            if (dist < minDist) {
                minDist = dist;
                winner = s;
            }
        }

        return winner;
    }

    private List<Stop> findMatchingRoutes(String text) {
        return stopTrie.allThatBeginWith(text);
    }

    private void parseStopFile(BufferedReader stopReader) throws IOException {
        String line = stopReader.readLine(); // Trow out first line
        while((line = stopReader.readLine()) != null) {
            String[] elements = line.split("\t");

            if (elements.length != 4) {
                getTextOutputArea().append("Invalid Stop, skipping line of length " + elements.length + " (" + line + ")\n");

            } else {
                // Each line = id, name, lat, lon
                String id = elements[0];
                String name = elements[1];
                double latitude = Double.parseDouble(elements[2]);
                double longitude = Double.parseDouble(elements[3]);

                Stop s = new Stop(id,name,latitude,longitude);
                stopList.add(s);
            }
        }
    }

    private void parseTripFile(BufferedReader tripReader) throws IOException {
        String line = tripReader.readLine(); // Throw out first line
        while((line = tripReader.readLine()) != null) {
            String[] elements = line.split("\t");

            if (elements.length < 3) {
                getTextOutputArea().append("Invalid Trip, skipping line of length " + elements.length + " (" + line + ")\n");

            } else {
                // Each line = id, list of stops
                Trip t = new Trip(elements[0]);
                List<Stop> stopsInTrip = stopList.stream().filter(x-> Arrays.asList(elements).contains(x.getID())).collect(Collectors.toList());

                for(int i = 1; i < stopsInTrip.size(); ++i) {
                    Stop incoming = stopsInTrip.get(i-1), outgoing = stopsInTrip.get(i);
                    Connection c = new Connection(incoming, outgoing, t);
                    incoming.addOutgoingConnection(c);
                    outgoing.addIncomingConnection(c);
                    t.addConnection(c);
                }
            }
        }
    }

    private void populateDataStructures() {

        for (Stop s : stopList) {
            stopHashMap.put(s.getName(), s);
            // stopQuad.add(s); TODO
            stopTrie.add(s);
        }
    }

    public static void main(String[] args) {
        new JourneyPlanner().getTextOutputArea().setText("TODO: Implement a lot of things");
    }

    public JourneyPlanner(){
        Dimension d = getDrawingAreaDimension();
        origin = new Location(d.width / 2.0, d.height / 2.0);
        scale = 1;
    }
}
