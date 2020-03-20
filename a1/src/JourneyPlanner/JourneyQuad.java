package JourneyPlanner;

import java.awt.*;

public class JourneyQuad {
    private Node root;

    JourneyQuad(){
        root = new Node(new Stop("", "", new Location(51,61)));
    }

    public void add(Stop s) {
        Node currNode = root;
        Location target = s.getLocation();
        boolean isDone = false;

        while (!isDone) {
            Location current = currNode.data.getLocation();

            if (target.y > current.y) {
                if (target.x < current.x) {
                    if (currNode.getChildOne() == null) {
                        currNode.childOne = new Node(s);
                        isDone = true;
                    }
                    currNode = currNode.getChildOne();
                } else {
                    if (currNode.getChildTwo() == null) {
                        currNode.childTwo = new Node(s);
                        isDone = true;
                    }
                    currNode = currNode.getChildTwo();
                }
            } else {
                if (target.x < current.x) {
                    if (currNode.getChildThree() == null) {
                        currNode.childThree = new Node(s);
                        isDone = true;
                    }
                    currNode = currNode.getChildThree();
                } else {
                    if (currNode.getChildFour() == null) {
                        currNode.childFour = new Node(s);
                        isDone = true;
                    }
                    currNode = currNode.getChildFour();
                }
            }
        }
    }

    public Stop findClosest(Location clickLocation) {
        Node currentNode = root;

        while (true){
            Location current = currentNode.getData().getLocation();

            if (clickLocation.y > current.y){
                if (clickLocation.x < current.x){
                    if (currentNode.childOne == null) return currentNode.getData();
                    currentNode = currentNode.getChildOne();
                } else {
                    if (currentNode.childTwo == null) return currentNode.getData();
                    currentNode = currentNode.getChildTwo();
                }
            } else {
                if (clickLocation.x < current.x){
                    if (currentNode.childThree == null) return currentNode.getData();
                    currentNode = currentNode.getChildThree();
                } else {
                    if (currentNode.childFour == null) return currentNode.getData();
                    currentNode = currentNode.getChildFour();
                }
            }
        }
    }

    @Override
    public String toString() {
        return getAllStrings(root, 0);
    }

    private String getAllStrings(Node node, int indent) {
        if (node == null) return "";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; ++i) { sb.append("-"); }
        sb.append(node.data.getName()).append('\n');
        sb.append(getAllStrings(node.childOne, indent + 1));
        sb.append(getAllStrings(node.childTwo, indent + 1));
        sb.append(getAllStrings(node.childThree, indent + 1));
        sb.append(getAllStrings(node.childFour, indent + 1));

        return sb.toString();
    }

    private static class Node {
        private Stop data;
        private Node childOne, childTwo, childThree, childFour;

        Node(Stop data) {
            this.data = data;
        }

        public Node getChildOne() {
            return childOne;
        }

        public Node getChildTwo() {
            return childTwo;
        }

        public Node getChildThree() {
            return childThree;
        }

        public Node getChildFour() {
            return childFour;
        }

        public Stop getData() {
            return data;
        }

    }
}
