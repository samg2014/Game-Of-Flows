
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
                if(second.equals(".")){
                        // a '.' means the space is holding only dirt.
                        // Nothing needs to be done
                }
                    if(second.equals("N")){
                        // this space holds a neutral (on land) boat
                        tile.setHasBoat(true);
                        tile.setBoatColor(Color.NEUTRAL);
                    }
                    if(second.equals("R")){
                        // this space holds the red water source
                        tile.setIsWaterSource(true);
                        tile.setSourceColor(Color.RED);
                    }
                    if(second.equals("B")){
                        // this space holds the blue water source
                        tile.setIsWaterSource(true);
                        tile.setSourceColor(Color.BLUE);
                    }
                    if(second.equals("H")){
                        // this space holds a water hole
                        tile.setIsWaterHole(true);
                    }
                    if(second.equals("F")){
                        // this space has flowing red water
                        tile.setHasWater(true);
                        tile.setWaterColor(Color.RED);
                    }
                    if(second.equals("G")){
                        // this space has flowing blue water
                        tile.setHasWater(true);
                        tile.setWaterColor(Color.BLUE);
                    }
                    if(second.equals("r")){
                        // this space has a red boat (on red water)
                        tile.setHasBoat(true);
                        tile.setBoatColor(Color.RED);
                    }
                    if(second.equals("b")){
                        // this space has a blue boat (on blue water)
                        tile.setHasBoat(true);
                        tile.setBoatColor(Color.BLUE);
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
                    //Player.out.println(x + ", " + y + " is Blocked - dirt");
                    map.terrain[x][y] = GameMap.BLOCKED;
                } else if (tile.isHasBoat()) {
                    //Player.out.println(x + ", " + y + " is Blocked - boat");
                    map.terrain[x][y] = GameMap.BLOCKED;
                } else if (tile.isHasExcavator()) {
                    //Player.out.println(x + ", " + y + " is Blocked - excavator");
                    map.terrain[x][y] = GameMap.BLOCKED;
                } else if (tile.isHasWater()) {
                    //Player.out.println(x + ", " + y + " is Blocked - water");
                    map.terrain[x][y] = GameMap.BLOCKED;
                } else if (tile.isIsWaterHole()) {
                    //Player.out.println(x + ", " + y + " is Blocked - water hole");
                    map.terrain[x][y] = GameMap.BLOCKED;
                } else if (tile.isIsWaterSource()) {
                    //Player.out.println(x + ", " + y + " is Blocked - water source");
                    map.terrain[x][y] = GameMap.BLOCKED;
                } else {
                    map.terrain[x][y] = GameMap.OPEN;
                }
            }
        }
        return map;
    }
    
    public int[] findBoat(){
        int[] loc = new int[2];
        for(Tile[] row : tiles){
            for(Tile tile : row){
                if(tile.isHasBoat() && tile.getBoatColor() == Color.NEUTRAL){
                    loc[0] = tile.x;
                    loc[1] = tile.y;
                }
            }
        }
        return loc;
    }
    
    public int[] optimize(int x, int y, int targetX, int targetY) {
        double distance = Double.MAX_VALUE;
        int[] coordinates = new int[2];
        GameMap map = new GameMap();
        for( int i = -1; i <= 1; i++)
        {
            for(int j = -1; j <= 1; j++)
            {
                int newX = x + i;
                int newY = y + j;
                if(newX > -1 && newY > -1 && newX < 31 && newY < 31 && map.terrain[newX][newY] != GameMap.BLOCKED)
                    if(Math.sqrt(Math.pow(Math.abs(newX-targetX), 2)+Math.pow(Math.abs(newY-targetY),2)) < distance)
                    {
                        distance = Math.sqrt(Math.pow(Math.abs(newX-targetX), 2)+Math.pow(Math.abs(newY-targetY),2));
                        coordinates[0] = newX;
                        coordinates[1] = newY;
                    }
            }
            
        }
        return coordinates;
    }

}
