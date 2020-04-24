1. describe what your code does and doesn’t do (e.g., which stages and extensions above
did you do).

    My code implemented every requirement except taking traffic lights into consideration

2. give a detailed pseudocode algorithm for the main search.

    open = new List
    closed = new List

    while open isn't empty:
        current = get element from open with lowest f value

        if current is goal: return current

        for each Neighbor of current:
            if Neighbor is restricted from current or
                Neighbor is in the wrong direction of a one way street:
                    skip

             // Note: this code is in the Step constructor
             Neighbor.parent = current
             Neighbor.bestSegment = lowest cost segment between Neighbor and current (time or dist)
             Neighbor.gVal = current.gVal + bestSegment.cost
             Neighbor.hVal = distanceFrom(goal)

             if Neighbor is in open:
                if Neighbor.gVal < open[Neighbor].gVal:
                    open[Neighbor] = Neighbor
             else if Neighbor is in closed:
                if Neighbor.gVal < closed[Neighbor].gVal:
                    open.add(Neighbor)
                    closed.remove(Neighbor)
             else:
                open.add(Neighbor)

        open.remove(current)
        closed.add(Current)

    // If we get here, return null so we can handle the error
    return null;


3. describe your path cost and heuristic estimate

    I used the euclidean distance from a node to the goal to determine the heuristic, for
        distance cost I used the length of a segment, for time cost I used length / speed

4. outline how you tested that your program worked.

    For general route finding: I utilized the same section of road during development in order to check for accuracy.
    Once I had that implemented, I added time to the equation. For one way roads, I did the same thing, only using a
    road that was oneway. For restricted turns, I found a set of nodes that were restricted and tested the route going
    through them.

5. Questions:

    5.1. Show how to use A* search algorithm to search for the shortest path from node D to node
    H. You should show (1) at each step, the elements in the fringe and the element to be
    visited next, and (2) the final shortest path as a sequence of nodes.
    Each element is represented in the format of <node, fromNode, g, f>. Below is the information
    in step 0 for you to start.

        Step 0:
            Fringe elements: {<D, null, 0, 25>}
            Element to visit next: <D, null, 0, 25>

        Step 1:
            Fringe elements: {<A,D,15,53>,<C,D,14,51>,<E,D,10,26>,<F,D,8,27>}
            Element to visit next: <E,D,10,26>

        Step 2:
            Fringe elements: {<A,D,15,53>,<C,D,14,51>,<F,D,8,27>,<H,E,31,37>}
            Element to visit next: <F,D,8,27>

        Step 3:
            Fringe elements: {<A,D,15,53>,<C,D,14,51>,<G,F,18,19>,<H,E,31,37>,<I,F,23,39>}
            Element to visit next: <G,F,18,19>

        Step 4:
            Fringe elements: {<A,D,15,53>,<C,D,14,51>,<H,G,28,28>,<I,F,23,39>}
            Element to visit next: <H,G,28,28>

        Step 5: Done


    5.2. Show how to use 1-to-1 Dijkstra’s algorithm to search for the shortest path from node D
    to node H. You should show (1) at each step, the elements in the fringe and the element
    to be visited next, and (2) the final shortest path as a sequence of nodes.
    Each element is represented in the format of hnode; fromNode; costSoFari. Below is the
    information in step 0 for you to start.

        Step 0:
            Fringe elements: {<D; null; 0>}
            Element to visit next: <D; null; 0>

        Step 1:
            Fringe elements: {<A,D,15>,<C,D,14>,<E,D,10>,<F,D,8>}
            Element to visit next: <F,D,8>

        Step 2:
            Fringe elements: {<A,D,15>,<C,D,14>,<E,D,10>,<G,F,18>,<I,F,23>}
            Element to visit next: <E,D,10>

        Step 3:
            Fringe elements: {<A,D,15>,<C,D,14>,<G,F,18>,<H,E,31>,<I,F,23>}
            Element to visit next: <C,D,14>

        Step 4:
            Fringe elements: {<A,D,15>,<B,C,22>,<G,F,18>,<H,E,31>,<I,F,23>}
            Element to visit next: <A,D,15>

        Step 5:
            Fringe elements: {<B,C,22>,<G,F,18>,<H,E,31>,<I,F,23>}
            Element to visit next: <G,F,18>

        Step 6:
            Fringe elements: {<B,C,22>,<H,G,28>,<I,F,23>}
            Element to visit next: <B,C,22>

        Step 7:
            Fringe elements: {<H,G,28>,<I,F,23>}
            Element to visit next: <I,F,23>

        Step 8:
            Fringe elements: {<H,G,28>}
            Element to visit next: <H,G,28>

    Step 9: Done


    5.3. A* search takes fewer steps than 1-to-1 Dijkstra’s algorithm. Briefly describe the reason.

        A* utilizes heuristics to predict the value of a node, whereas Dijkstra's simply looks for the
    shortest one. Thus with a suitably goof heuristic, A* will have to expand fewer extraneous nodes