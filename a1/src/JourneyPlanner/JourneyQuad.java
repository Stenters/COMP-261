package JourneyPlanner;

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
//            System.out.printf("comparing (%f, %f) and (%f,%f)\n",
//                    clickLocation.x, clickLocation.y, current.x, current.y);
            listDepth(currentNode);

            if (clickLocation.y > current.y){
                if (clickLocation.x < current.x){
                    System.out.println("Choosing child one");
                    if (currentNode.childOne == null) return currentNode.getData();
                    currentNode = currentNode.getChildOne();
                } else {
                    System.out.println("Choosing child two");
                    if (currentNode.childTwo == null) return currentNode.getData();
                    currentNode = currentNode.getChildTwo();
                }
            } else {
                if (clickLocation.x < current.x){
                    System.out.println("Choosing child three");
                    if (currentNode.childThree == null) return currentNode.getData();
                    currentNode = currentNode.getChildThree();
                } else {
                    System.out.println("Choosing child four");
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

    private void listDepth(Node n) {
        int d1,d2,d3,d4;

        if (n.childOne == null) d1 = 0;
        else d1 = n.childOne.getDepth();
        if (n.childTwo == null) d2 = 0;
        else d2 = n.childTwo.getDepth();
        if (n.childThree == null) d3 = 0;
        else d3 = n.childThree.getDepth();
        if (n.childFour == null) d4 = 0;
        else d4 = n.childFour.getDepth();

        System.out.printf("\t(%d) (%d) (%d) (%d)\n", d1,d2,d3,d4);
    }

    private static class Node {
        private Stop data;
        private Node childOne, childTwo, childThree, childFour;

        public int getDepth() {
            int d1,d2,d3,d4;

            if (childOne == null) d1 = 0;
            else d1 = childOne.getDepth();
            if (childTwo == null) d2 = 0;
            else d2 = childTwo.getDepth();
            if (childThree == null) d3 = 0;
            else d3 = childThree.getDepth();
            if (childFour == null) d4 = 0;
            else d4 = childFour.getDepth();

            return Math.max(Math.max(d1,d2),Math.max(d3,d4)) + 1;
        }

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
