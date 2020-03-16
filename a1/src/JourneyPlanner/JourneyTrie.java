package JourneyPlanner;

import com.sun.istack.internal.Nullable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class JourneyTrie {
    private Node root = new Node((char) 0);

    public void add(Stop s) {

        Node currentNode = root;
        for (char c : s.getName().toLowerCase().toCharArray()) {
            if (currentNode.hasChild(c)) {
                currentNode = currentNode.getChild(c);
            } else {
                currentNode = currentNode.addChild(c);
            }
        }

        currentNode.addStop(s);
    }

    public List<Stop> allThatBeginWith(String prefix) {
        prefix = prefix.toLowerCase();
        Node currentNode = root;

        for (char c : prefix.toCharArray()) {
            if (currentNode.hasChild(c)) {
                currentNode = currentNode.getChild(c);
            } else {
                return new LinkedList<Stop>();
            }
        }

        return getAllChildStops(currentNode);
    }

    private List<Stop> getAllChildStops(Node currentNode) {
        if (currentNode.getAllStops().size() > 0) {
            return currentNode.getAllStops();
        }

        List<Stop> matches = new LinkedList<Stop>();

        for(Node n : currentNode.getAllChildren()) {
            matches.addAll(getAllChildStops(n));
        }

        return matches;
    }

    private static class Node {
        private char data;
        private List<Node> children;
        private List<Stop> stops;

        Node(char data){
            this.data = data;
            children = new ArrayList<>();
            stops = new ArrayList<>();
        }

        @Nullable
        public Node getChild(char data) {
            return children.stream().filter( x -> x.data == data ).findFirst().orElse(null);
        }

        public Node addChild(char c) {
            if (hasChild(c)) {
                return getChild(c);
            } else {
                Node n = new Node(c);
                children.add(n);
                return n;
            }
        }

        public void addStop(Stop s) { stops.add(s); }

//        @Nullable
//        public Stop getStop(String id) { return stops.stream().filter( x -> x.getID().equals(id) ).findFirst().orElse(null); }

        public boolean hasChild(char c) { return children.stream().anyMatch( x -> x.data == c ); }

        public List<Node> getAllChildren() { return children; }

        public List<Stop> getAllStops() { return stops; }
    }
}
