
import java.util.ArrayList;
import java.util.Scanner;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Sam
 */
public class Field {

    private Tile[][] tiles;
    private Excavator[] excavators;

    public Field() {
        tiles = new Tile[31][31];
        for (int i = 0; i < 31; i++) {
            for (int j = 0; j < 31; j++) {
                tiles[i][j] = new Tile(i, j);
            }
        }
        excavators = new Excavator[6];
        for (int i = 0; i < 6; i++) {
            excavators[i] = new Excavator(i);
            excavators[i].setColor(i < 3 ? Color.RED : Color.BLUE);
        }
    }

    // Executes the turn for all RED excavators
    public void executeTurn() {
        for (Excavator ex : excavators) {
            if (ex.getColor() == Color.RED) {
                ex.execute();
            }
        }
    }

    public Excavator getExcavator(int index) {
        return excavators[index];
    }

    public Tile getTile(int x, int y) {
        return tiles[x][y];
    }

    // Turns the board provided by the engine into a machine friendly array
    public void decodeField() {
        for (int i = 0; i < 31; i++) {
            for (int j = 0; j < 31; j++) {

                // Grab the pair of character encoding this tile and get this tile to edit
                String code = Player.in.next();
                //Player.out.println(code);
                Tile tile = tiles[i][j];

                //Reset this tile
                tile.toDefault();

                //Interpret the height of the dirt on this tile
                int height = Integer.parseInt(code.substring(0, 1));
                tile.setDirtHeight(height);

                //Get the second character for this tile
                String second = code.substring(1, 2);

                // Interpret the second part
                if (second.equals(".")) {
                    // a '.' means the space is holding only dirt.
                    // Nothing needs to be done
                }
                if (second.equals("N")) {
                    // this space holds a neutral (on land) boat
                    //Player.out.println("Neutral boat at: " + tile.x + ", " + tile.y);
                    tile.setHasBoat(true);
                    tile.setBoatColor(Color.NEUTRAL);
                }
                if (second.equals("R")) {
                    // this space holds the red water source
                    tile.setIsWaterSource(true);
                    tile.setSourceColor(Color.RED);
                }
                if (second.equals("B")) {
                    // this space holds the blue water source
                    tile.setIsWaterSource(true);
                    tile.setSourceColor(Color.BLUE);
                }
                if (second.equals("H")) {
                    // this space holds a water hole
                    tile.setIsWaterHole(true);
                }
                if (second.equals("F")) {
                    // this space has flowing red water
                    tile.setHasWater(true);
                    tile.setWaterColor(Color.RED);
                    tile.setDirtHeight(0);
                    tile.setWaterFlow(Integer.parseInt(code.substring(0, 1)));
                }
                if (second.equals("G")) {
                    // this space has flowing blue water
                    tile.setHasWater(true);
                    tile.setWaterColor(Color.BLUE);
                    tile.setDirtHeight(0);
                    tile.setWaterFlow(Integer.parseInt(code.substring(0, 1)));
                }
                if (second.equals("r")) {
                    // this space has a red boat (on red water)
                    tile.setHasBoat(true);
                    tile.setBoatColor(Color.RED);
                    tile.setDirtHeight(0);
                    tile.setWaterFlow(Integer.parseInt(code.substring(0, 1)));
                }
                if (second.equals("b")) {
                    // this space has a blue boat (on blue water)
                    tile.setHasBoat(true);
                    tile.setBoatColor(Color.BLUE);
                    tile.setDirtHeight(0);
                    tile.setWaterFlow(Integer.parseInt(code.substring(0, 1)));
                }

            }
        }

        for (int e = 0; e < 6; e++) {
            // Grab all data regarding this excavator
            int xLoc = Player.in.nextInt();
            int yLoc = Player.in.nextInt();
            String content = Player.in.next();

            // Set the excavator's location
            this.excavators[e].setxLoc(xLoc);
            this.excavators[e].setyLoc(yLoc);

            // Update the tile this excavator is on to hold this excavator
            this.tiles[xLoc][yLoc].setExcavator(this.excavators[e]);
            this.tiles[xLoc][yLoc].setHasExcavator(true);

            // Set the excavator's data regarding what it holds
            this.excavators[e].setIsHoldingBoat(content.equals("b"));
            this.excavators[e].setIsHoldingDirt(content.equals("d"));
        }
    }

    public GameMap convertToMap() {
        GameMap map = new GameMap();
        for (int y = 0; y < tiles.length; y++) {
            for (int x = 0; x < tiles[y].length; x++) {
                Tile tile = tiles[x][y];
                //Player.out.println(tile);
                if (tile.getDirtHeight() != 1) {
                    map.terrain[x][y] = GameMap.BLOCKED;
                } else if (tile.hasBoat()) {
                    map.terrain[x][y] = GameMap.BLOCKED;
                } else if (tile.hasExcavator()) {
                    map.terrain[x][y] = GameMap.BLOCKED;
                } else if (tile.hasWater()) {
                    map.terrain[x][y] = GameMap.BLOCKED;
                } else if (tile.isWaterHole()) {
                    map.terrain[x][y] = GameMap.BLOCKED;
                } else if (tile.isWaterSource()) {
                    map.terrain[x][y] = GameMap.BLOCKED;
                } else {
                    map.terrain[x][y] = GameMap.OPEN;
                }
            }
        }
        return map;
    }

    public int[] findNearestBoat(int x, int y) {
        double distance = Double.MAX_VALUE;
        int[] loc = null;
        for (int i = tiles.length - 1; i >= 0; i--) {
            for (int j = tiles[i].length - 1; j >= 0; j--) {
                Tile tile = tiles[i][j];
                if (tile.hasBoat() && tile.getBoatColor() == Color.NEUTRAL) {
                    if (Math.sqrt(Math.pow(Math.abs(tile.x - x), 2) + Math.pow(Math.abs(tile.y - y), 2)) < distance) {
                        loc = new int[]{tile.x, tile.y};
                        distance = Math.sqrt(Math.pow(Math.abs(tile.x - x), 2) + Math.pow(Math.abs(tile.y - y), 2));
                    }
                }
            }
        }
        return loc;
    }

    public double findNearestBoatDistance(int x, int y) {
        double distance = Double.MAX_VALUE;
        for (int i = tiles.length - 1; i >= 0; i--) {
            for (int j = tiles[i].length - 1; j >= 0; j--) {
                Tile tile = tiles[i][j];
                if (tile.hasBoat() && tile.getBoatColor() == Color.NEUTRAL) {
                    if (Math.sqrt(Math.pow(Math.abs(tile.x - x), 2) + Math.pow(Math.abs(tile.y - y), 2)) < distance) {
                        distance = Math.sqrt(Math.pow(Math.abs(tile.x - x), 2) + Math.pow(Math.abs(tile.y - y), 2));
                    }
                }
            }
        }
        return distance;
    }

    int[][] dontDig = new int[][]{{9, 9}, {9, 8}, {9, 7}, {9, 7}, {11, 9}, {11, 8}, {11, 7}, {11, 7}};

    public int[] findAdjacentDirt(int x, int y) {
        int[] coordinates = new int[2];
        for (int i = -1; i <= 1; i++) {
            two:
            for (int j = -1; j <= 1; j++) {
                int newX = x + i;
                int newY = y + j;
                if (newX > -1 && (newY) > -1 && newX < 31 && (newY) < 31 && !(i == 0 && j == 0) && tiles[newX][newY].getDirtHeight() > 0 && !tiles[newX][newY].hasWater() && !tiles[newX][newY].isWaterSource()) {
                    for (int[] c : dontDig) {
                        if (c[0] == newX && c[1] == newY) {
                            continue two;
                        }
                    }
                    coordinates = new int[]{newX, newY};
                    return coordinates;
                }
            }
        }
        return coordinates;
    }

    public int[] findAdjacentDump(int x, int y, int notX, int notY) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int newX = x + i;
                int newY = y + j;
                if (newX > -1 && newY > -1 && newX < 31 && newY < 31 && !(i == 0 && j == 0) && tiles[newX][newY].getDirtHeight() < 4 && tiles[i + x][j + y].getDirtHeight() > 0 && !tiles[i + x][j + y].hasWater() && !tiles[i + x][j + y].isWaterSource()) {
                    if (newX != notX || newY != notY && !(newX == 10 && (newY) == 6)) {
                        return new int[]{newX, newY};
                    }
                }
            }
        }

        return null;
    }

    public int[] findOpponentStart() {
        if (tiles[19][20].getWaterColor() == Color.BLUE || tiles[19][20].getDirtHeight() == 0) {
            return (new int[]{19, 20});
        }
        if (tiles[20][19].getWaterColor() == Color.BLUE || tiles[20][19].getDirtHeight() == 0) {
            return (new int[]{20, 19});
        }
        if (tiles[21][20].getWaterColor() == Color.BLUE || tiles[21][20].getDirtHeight() == 0) {
            return (new int[]{21, 20});
        }
        if (tiles[20][21].getWaterColor() == Color.BLUE || tiles[20][21].getDirtHeight() == 0) {
            return (new int[]{20, 21});
        }
        return null;
    }

    public ArrayList<Tile> findOpponentCanal() {
        ArrayList<Tile> ret = new ArrayList<>();
        for (Tile[] row : tiles) {
            for (Tile t : row) {
                if (t.hasWater() && t.getWaterColor().equals(Color.BLUE) && !t.hasBoat()) {
                    ret.add(t);
                }
            }
        }
        return ret;
    }

    public ArrayList<Tile> getTilesAroundBlueSource() {
        ArrayList<Tile> ret = new ArrayList<>();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (j != 0 || i != 0) {
                    Tile t = tiles[20 + i][20 + j];
                    if (t.getDirtHeight() > 0 && !t.hasBoat() && !t.hasExcavator() && !t.isWaterHole() && !t.hasWater() && !t.isWaterSource()) {
                        ret.add(t);
                    }
                }
            }
        }
        return ret;
    }

    public int[] optimize(int x, int y, int targetX, int targetY) {
        double distance = Double.MAX_VALUE;
        int[] coordinates = new int[2];
        GameMap map = this.convertToMap();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i != 0 || j != 0) {
                    int newX = targetX + i;
                    int newY = targetY + j;
                    if (newX == x && newY == y) {
                        coordinates[0] = newX;
                        coordinates[1] = newY;
                        return coordinates;
                    }
                    if (newX > -1 && newY > -1 && newX < 31 && newY < 31 && map.terrain[newX][newY] != GameMap.BLOCKED) {
                        if (Math.sqrt(Math.pow(Math.abs(newX - x), 2) + Math.pow(Math.abs(newY - y), 2)) < distance) {
                            distance = Math.sqrt(Math.pow(Math.abs(newX - x), 2) + Math.pow(Math.abs(newY - y), 2));
                            coordinates[0] = newX;
                            coordinates[1] = newY;
                        }
                    }
                }
            }

        }
        return coordinates;
    }

    public Tile getNextCanalTarget(int x, int y) {
        Tile ret = null;
        if (Player.out != null) {
            Player.out.println("NT: " + x + ", " + y);
        }
        int[][] check = new int[][]{{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
        for (int[] change : check) {
            Tile t;
            try {
                t = getTile(x + change[0], y + change[1]);
            } catch (IndexOutOfBoundsException e) {
                continue;
            }
            if (t.getDirtHeight() < 1) {
                continue;
            }
//            if(t.hasWater() || t.getDirtHeight() < 1){
//                return t;
//            }
            if (Player.out != null) {
                Player.out.println(t);
            }
            int count = 0;
            for (int[] p : check) {
                Tile i;
                try {
                    i = getTile(x + p[0], y + p[1]);
                } catch (IndexOutOfBoundsException e) {
                    continue;
                }
                if (i.hasWater() || i.getDirtHeight() < 1) {
                    count++;
                }
            }
            if (count < 2 && ret == null) {
                return t;
            }
        }
        if (Player.out != null) {
            Player.out.println("Next tile: " + ret);
        }
        return ret;
    }
}
