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
    JourneyQuad stopQuad = new JourneyQuad();
    List<Stop> stopList = new LinkedList<Stop>();
    List<Trip> tripList = new LinkedList<Trip>();

    public static int MOVE_FACTOR = 10, ZOOM_FACTOR = 2;
    private Location origin = new Location(0,0);
    private double scale = 10;

    /** GUI Methods **/
    @Override
    protected void redraw(Graphics g) {
        if (stopList == null) return;

        for (Stop s : stopList) {
            s.draw(g, origin, scale);
        }
    }

    @Override
    protected void onClick(MouseEvent e) {
        Stop s = findNearestStop(e.getX(), e.getY());
        unhighlight();
        s.setHighlight(true);
        getTextOutputArea().setText("Stop " + s.getName() + "\nTRIPS:\n");
        for (Trip t : s.getTrips()) {
            getTextOutputArea().append(t.getID() + "\n");
        }
    }

    @Override
    protected void onSearch() {
        String text = getSearchBox().getText();
        unhighlight();

        List<Stop> matchingStops = findMatchingRoutes(text);
        getTextOutputArea().setText(matchingStops.size() + " results found\n");

        for (Stop s : matchingStops) {
            getTextOutputArea().append(s.getName() + "\n");
            s.setHighlight(true);
            s.setTripHighlighted(true);
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
        // TODO: implement quad
        Point click = new Point(x, y);
        double minDist = Double.MAX_VALUE;
        Stop winner = null;

        for (Stop s : stopList) {
            double dist = s.getLocation().asPoint(origin, scale).distance(click);
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

    private void unhighlight() {
        for (Trip t : tripList) {
            t.setHighlight(false);
        }

        for (Stop s : stopList) {
            s.setHighlight(false);
        }
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
                tripList.add(t);
            }
        }
    }

    private void populateDataStructures() {

        for (Stop s : stopList) {
            stopQuad.add(s);
            stopTrie.add(s);
        }
//        int scores[] = new int[10000];
//        for (int i = -50; i < 50; ++i){
//            for (int j = -50; j < 50; ++j){
//                JourneyQuad jq = new JourneyQuad(i,j);
//
//                for (Stop s : stopList){
//                    jq.add(s);
//                }
//
//                scores[(i+50) * 100 + (j+50)] = jq.maxdepth;
//            }
//        }

//        int winner = Integer.MAX_VALUE, winningI = 0, winningJ = 0;
//        for (int i = 0; i < 100; ++i){
//            for (int j = 0; j < 100; ++j){
//                if (scores[i*100+j] < winner){
//                    winner = scores[i*100+j];
//                    winningI = i;
//                    winningJ = j;
//                }
//            }
//        }
//
//        System.out.printf("Best starting position is (%d, %d) with a max depth of %d\n", winningI, winningJ, winner);

    }

    public static void main(String[] args) {
        new JourneyPlanner();
    }

}
