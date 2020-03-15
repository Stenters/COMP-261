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
    List<Stop> stopList = new LinkedList<Stop>(); // TODO: which data structure?
    List<Trip> tripList = new LinkedList<Trip>();

    public static int MOVE_FACTOR = 10, ZOOM_FACTOR = 2;
    private Location origin;
    private double scale;

    /** GUI Methods **/
    @Override
    protected void redraw(Graphics g) {
        for (Stop s : stopList) {
            s.draw(g, origin, scale);
        }
    }

    @Override
    protected void onClick(MouseEvent e) {
        Stop s = findNearestStop(e.getX(), e.getY());
        s.setHighlight(true);
        // TODO: display info
    }

    @Override
    protected void onSearch() {
        String text = getSearchBox().getText();
        List<Stop> matchingStops = findMatchingRoutes(text);
        for (Stop s : matchingStops) {
            s.setHighlight(true);
        }
    }

    @Override
    protected void onMove(Move m) {
        double dY = 0, dX = 0;
        switch (m){
            case EAST:
                dX = MOVE_FACTOR;
                break;
            case SOUTH:
                dY = MOVE_FACTOR;
                break;
            case WEST:
                dX = -MOVE_FACTOR;
                break;
            case NORTH:
                dY = -MOVE_FACTOR;
                break;
            case ZOOM_IN:
                scale *= ZOOM_FACTOR;
                dX = (getDrawingAreaDimension().width - getDrawingAreaDimension().width / scale) / 2;
                dY = (getDrawingAreaDimension().height - getDrawingAreaDimension().height / scale) / 2;
                break;
            case ZOOM_OUT:
                dX = (-1 * (getDrawingAreaDimension().width - getDrawingAreaDimension().width / scale)) / 4;
                dY = (-1 * (getDrawingAreaDimension().height - getDrawingAreaDimension().height / scale)) / 4;
                scale /= ZOOM_FACTOR;
                break;
            default:
                getTextOutputArea().setText("Illegal Direction!");
                break;
        }

//        for (Stop s : stopList) {
//            s.move(dX , dY );
//        }
        Point originPoint = origin.asPoint(origin, scale);
        originPoint.x += dX;
        originPoint.y += dY;
        origin = Location.newFromPoint(originPoint, origin, scale);
//        origin = origin.moveBy(dX, dY);
        redraw();
    }

    @Override
    protected void onLoad(File stopFile, File tripFile) {
        try {
            BufferedReader stopReader = new BufferedReader(new FileReader(stopFile)), tripReader = new BufferedReader(new FileReader(tripFile));

            getTextOutputArea().setText("Loading files");

            stopList = new LinkedList<Stop>();
            tripList = new LinkedList<Trip>();

            stopTrie = new JourneyTrie();
            stopHashMap = new HashMap<String, Stop>();
            stopQuad = new JourneyQuad();
            stopList = new ArrayList<Stop>();
            tripList = new ArrayList<Trip>();

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
                System.out.printf("%s is the new winner (dist %f)\n", s.getID(), dist);
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
        origin = new Location(0, 0);
        scale = 10;
    }
}
