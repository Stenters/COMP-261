package JourneyPlanner;

import com.sun.istack.internal.Nullable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class JourneyTrie {
    private Node root = new Node((char) 0);

    public void add(Stop s) {
        Node currentNode = root;
        for (char c : s.getName().toCharArray()) {
            if (currentNode.hasChild(c)) {
                currentNode = currentNode.getChild(c);
            } else {
                currentNode = currentNode.addChild(c);
            }
        }

        currentNode.addStop(s);
    }

    public List<Stop> allThatBeginWith(String prefix) {
        Node currentNode = root;
        for (char c : prefix.toCharArray()) {
            if (currentNode.hasChild(c)) {
                currentNode = currentNode.getChild(c);
            } else {
                return null;
            }
        }

        return getAllChildStops(currentNode);
    }

    private List<Stop> getAllChildStops(Node currentNode) {
        List<Stop> matches = new LinkedList<Stop>(currentNode.getAllStops());

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
