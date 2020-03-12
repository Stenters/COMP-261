package JourneyPlanner;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class JourneyQuad {
    // TODO: what data type?
    private Node<String> root;

    JourneyQuad(){
        root = new Node<String>("I am a stub");
    }

    public void add(Stop s) {
        // TODO
        throw new NotImplementedException();
    }

    private static class Node<E> {
        E data;
        Node<E> childOne, childTwo, childThree, childFour;

        Node(E data) {
            this.data = data;
        }

        public Node<E> getChildOne() {
            return childOne;
        }

        public Node<E> getChildTwo() {
            return childTwo;
        }

        public Node<E> getChildThree() {
            return childThree;
        }

        public Node<E> getChildFour() {
            return childFour;
        }

        public E getData() {
            return data;
        }
    }
}
