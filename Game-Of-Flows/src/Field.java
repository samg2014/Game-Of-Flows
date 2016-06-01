
import java.util.ArrayList;

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
            this.tiles[xLoc][yLoc].assigned = false;

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
                if (tile.getDirtHeight() != 1) {
                    map.terrain[x][y] = GameMap.BLOCKED;
                    tile.blocked = true;
                } else if (tile.hasBoat()) {
                    map.terrain[x][y] = GameMap.BLOCKED;
                    tile.blocked = true;
                } else if (tile.hasExcavator()) {
                    map.terrain[x][y] = GameMap.BLOCKED;
                    tile.blocked = true;
                } else if (tile.hasWater()) {
                    map.terrain[x][y] = GameMap.BLOCKED;
                    tile.blocked = true;
                } else if (tile.isWaterHole()) {
                    map.terrain[x][y] = GameMap.BLOCKED;
                    tile.blocked = true;
                } else if (tile.isWaterSource()) {
                    map.terrain[x][y] = GameMap.BLOCKED;
                    tile.blocked = true;
                } else if (tile.assigned) {
                    map.terrain[x][y] = GameMap.BLOCKED;
                    tile.blocked = true;
                } else {
                    map.terrain[x][y] = GameMap.OPEN;
                    tile.blocked = false;
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

    public int[] findFarthestBoat(int x, int y) {
        double distance = Double.MIN_VALUE;
        int[] loc = null;
        for (int i = tiles.length - 1; i >= 0; i--) {
            for (int j = tiles[i].length - 1; j >= 0; j--) {
                Tile tile = tiles[i][j];
                if (tile.hasBoat() && tile.getBoatColor() == Color.NEUTRAL) {
                    if (Math.sqrt(Math.pow(Math.abs(tile.x - x), 2) + Math.pow(Math.abs(tile.y - y), 2)) > distance) {
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

    //int[][] dontDig = new int[][]{{9, 9}, {9, 8}, {9, 7}, {9, 6}, {11, 9}, {11, 8}, {11, 7}, {11, 6}, {9, 10}, {9, 11}, {10, 11}, {11, 11}, {11, 10}, {10, 9}, {10, 8}, {10, 7}, {10, 6}};
    ArrayList<Tile> dontDig = null;//getFillList(this.getPathToWaterHolePartTwo());

    public int[] findAdjacentDirt(int x, int y) {
        int[] coordinates = new int[2];
        for (int i = -1; i <= 1; i++) {
            two:
            for (int j = -1; j <= 1; j++) {
                int newX = x + i;
                int newY = y + j;
                if (newX > -1 && (newY) > -1 && newX < 31 && (newY) < 31 && !(i == 0 && j == 0) && tiles[newX][newY].getDirtHeight() > 0 && !tiles[newX][newY].hasWater() && !tiles[newX][newY].isWaterSource()) {
                    for (Tile c : dontDig) {
                        if (c.x == newX && c.y == newY) {
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

    public ArrayList<int[]> dontDump;

    public int[] findAdjacentDump(int x, int y, int notX, int notY) {
        ArrayList<int[]> possible = new ArrayList<>();
        for (int i = -1; i <= 1; i++) {
            two:
            for (int j = -1; j <= 1; j++) {
                int newX = x + i;
                int newY = y + j;
                if (newX > -1 && newY > -1 && newX < 31 && newY < 31 && !(i == 0 && j == 0) && tiles[newX][newY].getDirtHeight() < 4 && !tiles[newX][newY].hasWater() && !tiles[newX][newY].isWaterSource() && !tiles[newX][newY].isWaterHole()) {
                    if (newX != notX || newY != notY && !(newX == 10 && (newY) == 6)) {
                        for (int[] c : dontDump) {
                            if (c[0] == newX && c[1] == newY) {
                                continue two;
                            }
                        }
                        possible.add(new int[]{newX, newY});
                    }
                }
            }
        }
        for (int[] p : possible) {
            if (Player.fillList.contains(getTile(p[0], p[1]))) {
                return p;
            }
        }
        if (possible.size() > 0) {
            return possible.get(0);
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
                        //tiles[newX][newY].assigned = true;
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
        //tiles[coordinates[0]][coordinates[1]].assigned = true;
        return coordinates;
    }

    public Tile getNextCanalTarget(int x, int y) {
        Tile ret = null;
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
        return ret;
    }

    public Tile getNearestWaterHole() {
        ArrayList<Tile> waterHoles = new ArrayList<>();
        int next = 0;
        for (Tile[] i : tiles) {
            for (Tile j : i) {
                if (j.isWaterHole()) {
                    waterHoles.add(j);
                }
            }
        }

        Tile minDistance = waterHoles.get(0);
        for (int i = 1; i < waterHoles.size(); i++) {
            if (Math.sqrt(Math.pow(waterHoles.get(i).x - 10, 2) + Math.pow(waterHoles.get(i).y - 10, 2))
                    < Math.sqrt(Math.pow(minDistance.x - 10, 2) + Math.pow(minDistance.y - 10, 2))) {
                minDistance = waterHoles.get(i);
            }
        }
        return minDistance;
    }

    public Tile getFarthestWaterHole() {
        if (Player.out != null) {
            Player.out.println("getFarthestWaterHole()");
        }
        ArrayList<Tile> waterHoles = new ArrayList<>();
        for (Tile[] i : tiles) {
            for (Tile j : i) {
                if (j.isWaterHole()) {
                    waterHoles.add(j);
                }
            }
        }

        Tile maxDistance = waterHoles.get(0);
        for (Tile waterHole : waterHoles) {
            if (Player.out != null) {
                Player.out.println(maxDistance);
            }
            if (Math.sqrt(Math.pow(waterHole.x - 10, 2) + Math.pow(waterHole.y - 10, 2)) > Math.sqrt(Math.pow(maxDistance.x - 10, 2) + Math.pow(maxDistance.y - 10, 2))) {
                maxDistance = waterHole;
            }
        }
        return maxDistance;
    }

    public Tile get2ndNearestWaterHole() {
        ArrayList<Tile> waterHoles = new ArrayList<>();
        int next = 0;
        for (Tile[] i : tiles) {
            for (Tile j : i) {
                if (j.isWaterHole()) {
                    waterHoles.add(j);
                }
            }
        }

        Tile nearest = getNearestWaterHole();

        Tile minDistance;
        if (waterHoles.get(0) != nearest) {
            minDistance = waterHoles.get(0);
        } else {
            minDistance = waterHoles.get(1);
        }
        for (int i = 1; i < waterHoles.size(); i++) {
            if (Math.sqrt(Math.pow(waterHoles.get(i).x - 10, 2) + Math.pow(waterHoles.get(i).y - 10, 2))
                    < Math.sqrt(Math.pow(minDistance.x - 10, 2) + Math.pow(minDistance.y - 10, 2)) && waterHoles.get(i) != nearest) {
                minDistance = waterHoles.get(i);
            }
        }
        return minDistance;
    }

    public ArrayList<Tile> getPathToWaterHole() {
        ArrayList<Tile> waterHolePath = new ArrayList<>();

//        waterHolePath.add(tiles[8][11]);
//        waterHolePath.add(tiles[8][10]);
//        waterHolePath.add(tiles[8][9]);
//        waterHolePath.add(tiles[8][8]);
//        waterHolePath.add(tiles[8][7]);
//        waterHolePath.add(tiles[8][6]);
//                
//        waterHolePath.add(tiles[12][11]);
//        waterHolePath.add(tiles[12][10]);
//        waterHolePath.add(tiles[12][9]);
//        waterHolePath.add(tiles[12][8]);
//        waterHolePath.add(tiles[12][7]);
//        waterHolePath.add(tiles[12][6]);
        waterHolePath.add(tiles[10][9]);
        waterHolePath.add(tiles[10][8]);
        waterHolePath.add(tiles[10][7]);

        waterHolePath.add(tiles[10][6]);
        waterHolePath.add(tiles[10][5]);
        waterHolePath.add(tiles[10][4]);
        waterHolePath.add(tiles[10][3]);
        waterHolePath.add(tiles[10][2]);
        waterHolePath.add(tiles[10][1]);

        Tile nearest = getNearestWaterHole();
        if (nearest.x > 10) {
            for (int i = 10; i <= nearest.x; i++) {
                try {
                    waterHolePath.add(tiles[i][0]);
                } catch (Exception e) {
                    System.err.println(e.getMessage() + ":GetCanalPath:1");
                }
            }
        } else {
            for (int i = 10; i >= nearest.x; i--) {
                try {
                    waterHolePath.add(tiles[i][0]);
                } catch (Exception e) {
                    System.err.println(e.getMessage() + ":GetCanalPath:1");

                }
            }
        }
        for (int i = 1; i < nearest.y; i++) {
            try {
                waterHolePath.add(tiles[nearest.x][i]);
            } catch (Exception e) {
                System.err.println(e.getMessage() + ":GetCanalPath:1");

            }
        }

        return waterHolePath;
    }

    public int getNumberRedBoats() {
        int num = 0;
        for (Tile[] row : tiles) {
            for (Tile t : row) {
                if (t.hasBoat() && t.getBoatColor().equals(Color.RED)) {
                    num++;
                }
            }
        }
        return num;
    }

    public ArrayList<Tile> getPathToWaterHolePartTwo() {
        ArrayList<Tile> list = new ArrayList<>();
        if (Player.out != null) {
            Player.out.println("getPathToWaterHolePartTwo()");
        }
//        Path p = Player.canalFinder.findPath(10, 10, get2ndNearestWaterHole().x, get2ndNearestWaterHole().y);
        Path p = Player.canalFinder.findPath(10, 10, getFarthestWaterHole().x, getFarthestWaterHole().y);
        for (int i = 1; i < p.getLength() - 1; i++) {
            list.add(tiles[p.getX(i)][p.getY(i)]);
        }

        return list;
    }

    public ArrayList<Tile> getFillList(ArrayList<Tile> canal) {
        ArrayList<Tile> list = new ArrayList<>();
        int[][] neighbors = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (Tile t : canal) {
            for (int[] c : neighbors) {
                try {
                    Tile n = tiles[t.x + c[0]][t.y + c[1]];
                    if (!canal.contains(n) && !n.isWaterHole() && !n.isWaterSource() && !list.contains(n)) {
                        list.add(n);
                    }
                } catch (IndexOutOfBoundsException e) {

                }
            }
        }
        return list;
    }
}
