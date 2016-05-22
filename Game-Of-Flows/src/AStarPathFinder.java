
import java.util.ArrayList;
import java.util.Collections;

/**
 * A path finder implementation that uses the AStar heuristic based algorithm to
 * determine a path.
 */
public class AStarPathFinder {

    //The set of points that have been searched through
    private final ArrayList CLOSED = new ArrayList();
    //The set of points that we do not yet consider fully searched
    private final ArrayList OPEN = new ArrayList();
    //The map being searched
    private final GameMap MAP;
    //The maximum depth of search to accept before giving up
    private final int MAX_DEPTH;
    //The complete set of points across the map
    private final Node[][] NODES;

    /**
     * Create a path finder
     *
     * @param map The map to be searched
     * @param maxSearchDistance The maximum depth to search before giving up
     */
    public AStarPathFinder(GameMap map, int maxSearchDistance) {
        this.MAP = map;
        this.MAX_DEPTH = maxSearchDistance;

        NODES = new Node[map.getWidthInTiles()][map.getHeightInTiles()];
        for (int x = 0; x < map.getWidthInTiles(); x++) {
            for (int y = 0; y < map.getHeightInTiles(); y++) {
                NODES[x][y] = new Node(x, y);
            }
        }
    }

    public Path findPath(int startX, int startY, int targetX, int targetY) {
        // If the destination is blocked it can't get there so there is no path
        if (MAP.blocked(targetX, targetY)) {
            return null;
        }

        // Initial state for A*. The closed group is empty. Only the starting
        // tile is in the open list
        NODES[startX][startY].cost = 0;
        NODES[startX][startY].depth = 0;
        CLOSED.clear();
        OPEN.clear();
        addOpenAndSort(NODES[startX][startY]);

        NODES[targetX][targetY].parent = null;

        // While the search hasn't exceeded the max search depth andthere are still open points
        int maxDepth = 0;
        while ((maxDepth < MAX_DEPTH) && !OPEN.isEmpty()) {

            // Pull out the first point in the open list, this is determined to 
            // be the most likely to be the next step based on the heuristic
            Node current = (Node) OPEN.get(0);
            // Finish the loop if the target is hit, a path has been found
            if (current == NODES[targetX][targetY]) break;
            //After this iteration is done the current point will be closed
            OPEN.remove(current);
            CLOSED.add(current);

            int[][] neighbors = {{0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1},{2,2}};
            // Search through all the neighbours of the current point evaluating
            // them as next steps
            // TODO: This needs to be properly modified to account for all possible excavator steps
            for (int[] change : neighbors) {
                int x = change[0];
                int y = change[1];
                // Don't use a tile if it is the current tile
                if ((x == 0) && (y == 0)) continue;
                // Determine the location of the neighbour and evaluate it
                int xp = x + current.x;
                int yp = y + current.y;
                if (isValidLocation(startX, startY, xp, yp)) {
                    // The cost to get to this point is cost the current plus the movement
                    // cost to reach this point. Note that the heursitic value is only used
                    // in the sorted open list
                    float nextStepCost = current.cost + MAP.getCost(current.x, current.y, xp, yp);
                    Node neighbour = NODES[xp][yp];
                    MAP.pathFinderVisited(xp, yp);
                    if (nextStepCost < neighbour.cost) {
                        if (OPEN.contains(neighbour)) OPEN.remove(neighbour);
                        if (CLOSED.contains(neighbour)) CLOSED.remove(neighbour);
                    }
                    // If the point hasn't already been processed and discarded then
                    // reset it's cost to the current cost and add it as a next possible
                    // step (to the open list)
                    if (!OPEN.contains(neighbour) && !(CLOSED.contains(neighbour))) {
                        neighbour.cost = nextStepCost;
                        neighbour.heuristic = getCost(xp, yp, targetX, targetY);
                        maxDepth = Math.max(maxDepth, neighbour.setParent(current));
                        addOpenAndSort(neighbour);
                    }
                }
            }
        }
        // There was no path. Return null
        if (NODES[targetX][targetY].parent == null) return null;
        // Path found. Use the parent
        // references of the points to find out way from the target location back
        // to the start recording the point on the way.
        Path path = new Path();
        Node target = NODES[targetX][targetY];
        while (target != NODES[startX][startY]) {
            path.prependStep(target.x, target.y);
            target = target.parent;
        }
        path.prependStep(startX, startY);
        // Return the generated path
        return path;
    }

    public float getCost(int x, int y, int tx, int ty) {
        float dx = tx - x;
        float dy = ty - y;
        float result = (float) (Math.sqrt((dx * dx) + (dy * dy)));
        return result;
    }

    /**
     * Check if a given location is valid for the supplied mover
     *
     * @param startX The starting x coordinate
     * @param startY The starting y coordinate
     * @param x The x coordinate of the location to check
     * @param y The y coordinate of the location to check
     * @return true if the location is valid for the given mover
     */
    protected boolean isValidLocation(int startX, int startY, int x, int y) {
        boolean invalid = (x < 0) || (y < 0) || (x >= MAP.getWidthInTiles()) || (y >= MAP.getHeightInTiles());
        if ((!invalid)) {
            invalid = MAP.blocked(x, y);
        }
        return !invalid;
    }

    public void addOpenAndSort(Node p) {
        this.OPEN.add(p);
        Collections.sort(this.OPEN);
    }

    private class Node implements Comparable {

        // X coordinate

        private int x;
        // Y coordinate
        private int y;
        // The cost for moving to this point
        private float cost;
        // The parent point for this point
        private Node parent;
        // How close this point is to the target
        private float heuristic;
        // How far from the start this point is
        private int depth;

        public Node(int x, int y) {
            this.x = x;
            this.y = y;
        }

        /**
         * Set the parent of this point
         *
         * @param parent The parent point which lead us to this point
         * @return The depth reached in searching
         */
        public int setParent(Node parent) {
            depth = parent.depth + 1;
            this.parent = parent;

            return depth;
        }

        @Override
        public int compareTo(Object other) {
            Node o = (Node) other;

            float f = heuristic + cost;
            float of = o.heuristic + o.cost;

            if (f < of) {
                return -1;
            } else if (f > of) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
