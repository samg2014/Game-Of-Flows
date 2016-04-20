
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
                tiles[i][j] = new Tile();
            }
        }
        excavators = new Excavator[6];
        for (int i = 0; i < 6; i++){
            excavators[i] = new Excavator(i);
            excavators[i].setColor(i < 3 ? Color.RED : Color.BLUE);
        }
    }
    
    // Executes the turn for all RED excavators
    public void executeTurn(){
        for(Excavator ex : excavators){
            if(ex.getColor() == Color.RED){
                ex.execute();
            }
        }
    }
    
    public Excavator getExcavator(int index){
        return excavators[index];
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
                String second = code.substring(1,2);
                
                // Interpret the second part
                switch (second) {
                    case ".": {
                        // a '.' means the space is holding only dirt.
                        // Nothing needs to be done
                    }
                    case "N": {
                        // this space holds a neutral (on land) boat
                        tile.setHasBoat(true);
                        tile.setBoatColor(Color.NEUTRAL);
                    }
                    case "R": {
                        // this space holds the red water source
                        tile.setIsWaterSource(true);
                        tile.setSourceColor(Color.RED);
                    }
                    case "B": {
                        // this space holds the blue water source
                        tile.setIsWaterSource(true);
                        tile.setSourceColor(Color.BLUE);
                    }
                    case "H": {
                        // this space holds a water hole
                        tile.setIsWaterHole(true);
                    }
                    case "F": {
                        // this space has flowing red water
                        tile.setHasWater(true);
                        tile.setWaterColor(Color.RED);
                    }
                    case "G": {
                        // this space has flowing blue water
                        tile.setHasWater(true);
                        tile.setWaterColor(Color.BLUE);
                    }
                    case "r": {
                        // this space has a red boat (on red water)
                        tile.setHasBoat(true);
                        tile.setBoatColor(Color.RED);
                    }
                    case "b": {
                        // this space has a blue boat (on blue water)
                        tile.setHasBoat(true);
                        tile.setBoatColor(Color.BLUE);
                    }
                }
            }
        }
        
        for(int e = 0; e < 6; e++){ 
            // Grab all data regarding this excavator
            int xLoc = Player.in.nextInt();
            int yLoc = Player.in.nextInt();
            String content = Player.in.next();
            
            // Set the excavator's location
            this.excavators[e].setxLoc(xLoc);
            this.excavators[e].setyLoc(yLoc);
            
            // Update the tile this excavator is on to hold this excavator
            this.tiles[xLoc][yLoc].setExcavator(this.excavators[e]);
            
            // Set the excavator's data regarding what it holds
            this.excavators[e].setIsHoldingBoat(content.equals("b"));
            this.excavators[e].setIsHoldingDirt(content.equals("d"));

            
        }
    }
}
