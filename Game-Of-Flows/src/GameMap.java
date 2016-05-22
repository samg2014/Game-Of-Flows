public class GameMap {
	/** The map width in tiles */
	public static final int WIDTH = 31;
	/** The map height in tiles */
	public static final int HEIGHT = 31;
	
	/** Indicate grass terrain at a given location */
	public static final int GRASS = 0;
	/** Indicate water terrain at a given location */
	public static final int WATER = 1;
	/** Indicate trees terrain at a given location */
	public static final int TREES = 2;
	/** Indicate a plane is at a given location */
	public static final int PLANE = 3;
	/** Indicate a boat is at a given location */
	public static final int BOAT = 4;
	/** Indicate a tank is at a given location */
	public static final int TANK = 5;
	
	/** The terrain settings for each tile in the map */
	private int[][] terrain = new int[WIDTH][HEIGHT];
	/** The unit in each tile of the map */
	private int[][] units = new int[WIDTH][HEIGHT];
	/** Indicator if a given tile has been visited during the search */
	private boolean[][] visited = new boolean[WIDTH][HEIGHT];
	
	public GameMap() {
		units[15][15] = TANK;
		units[2][7] = BOAT;
		units[20][25] = PLANE;
	}
	
	/**
	 * Clear the array marking which tiles have been visted by the path 
	 * finder.
	 */
	public void clearVisited() {
		for (int x=0;x<getWidthInTiles();x++) {
			for (int y=0;y<getHeightInTiles();y++) {
				visited[x][y] = false;
			}
		}
	}
	
	public boolean visited(int x, int y) {
		return visited[x][y];
	}
	
	/**
	 * Get the terrain at a given location
	 * 
	 * @param x The x coordinate of the terrain tile to retrieve
	 * @param y The y coordinate of the terrain tile to retrieve
	 * @return The terrain tile at the given location
	 */
	public int getTerrain(int x, int y) {
		return terrain[x][y];
	}
	
	/**
	 * Get the unit at a given location
	 * 
	 * @param x The x coordinate of the tile to check for a unit
	 * @param y The y coordinate of the tile to check for a unit
	 * @return The ID of the unit at the given location or 0 if there is no unit 
	 */
	public int getUnit(int x, int y) {
		return units[x][y];
	}
	
	/**
	 * Set the unit at the given location
	 * 
	 * @param x The x coordinate of the location where the unit should be set
	 * @param y The y coordinate of the location where the unit should be set
	 * @param unit The ID of the unit to be placed on the map, or 0 to clear the unit at the
	 * given location
	 */
	public void setUnit(int x, int y, int unit) {
		units[x][y] = unit;
	}

	public boolean blocked(int x, int y) {
//		// if theres a unit at the location, then it's blocked
//
//		if (getUnit(x,y) != 0) {
//			return true;
//		}
            
            // NEEDS TO BE IMPLEMENTED
		
                //
//		int unit = ((UnitMover) mover).getType();
//		
//		// planes can move anywhere
//
//		if (unit == PLANE) {
//			return false;
//		}
//		// tanks can only move across grass
//
//		if (unit == TANK) {
//			return terrain[x][y] != GRASS;
//		}
//		// boats can only move across water
//
//		if (unit == BOAT) {
//			return terrain[x][y] != WATER;
//		}
//		
//		// unknown unit so everything blocks

		return false;
	}

	public float getCost(int sx, int sy, int tx, int ty) {
		return 1;
	}
        
	public int getHeightInTiles() {
		return WIDTH;
	}

	public int getWidthInTiles() {
		return HEIGHT;
	}

	public void pathFinderVisited(int x, int y) {
		visited[x][y] = true;
	}
}