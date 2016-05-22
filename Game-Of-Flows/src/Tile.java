/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Sam
 */
public class Tile {
    
    private int dirtHeight;
    private boolean hasBoat;
    private boolean hasWater;
    private Color waterColor;
    private boolean hasExcavator;
    private Excavator excavator;
    private boolean isWaterSource;
    private Color sourceColor;
    private boolean isWaterHole;
    private Color boatColor;
    public int x;
    public int y;
    
    public Tile(int x, int y){
        this.x = x;
        this.y = y;
        this.dirtHeight = 1;
        this.hasBoat = false;
        this.hasWater = false;
        this.hasExcavator = false;
        this.isWaterSource = false;
        this.isWaterHole = false;
        this.excavator = null;
        this.waterColor = Color.NULL;
        this.sourceColor = Color.NULL;
        this.boatColor = Color.NULL;
    }
    
    // Resets all values to their default values
    public void toDefault(){
        this.excavator = null;
        this.dirtHeight = 1;
        this.hasBoat = false;
        this.hasWater = false;
        this.hasExcavator = false;
        this.isWaterSource = false;
        this.isWaterHole = false;
        //this.excavator = null;
        this.waterColor = Color.NULL;
        this.sourceColor = Color.NULL;
        this.boatColor = Color.NULL;
    }

    public Excavator getExcavator() {
        return excavator;
    }

    public void setExcavator(Excavator excavator) {
        this.excavator = excavator;
    }

    public int getDirtHeight() {
        return dirtHeight;
    }

    public void setDirtHeight(int dirtHeight) {
        this.dirtHeight = dirtHeight;
    }

    @Override
    public String toString() {
        return "Tile{" + "dirtHeight=" + dirtHeight + ", hasBoat=" + hasBoat + ", hasWater=" + hasWater + ", waterColor=" + waterColor + ", hasExcavator=" + hasExcavator + ", excavator=" + excavator + ", isWaterSource=" + isWaterSource + ", sourceColor=" + sourceColor + ", isWaterHole=" + isWaterHole + ", boatColor=" + boatColor + '}';
    }

    public boolean isHasBoat() {
        return hasBoat;
    }

    public void setHasBoat(boolean hasBoat) {
        this.hasBoat = hasBoat;
    }

    public boolean isHasWater() {
        return hasWater;
    }

    public void setHasWater(boolean hasWater) {
        this.hasWater = hasWater;
    }

    public Color getWaterColor() {
        return waterColor;
    }

    public void setWaterColor(Color waterColor) {
        this.waterColor = waterColor;
    }

    public boolean isHasExcavator() {
        return hasExcavator;
    }

    public void setHasExcavator(boolean hasExcavator) {
        this.hasExcavator = hasExcavator;
    }

    public boolean isIsWaterSource() {
        return isWaterSource;
    }

    public void setIsWaterSource(boolean isWaterSource) {
        this.isWaterSource = isWaterSource;
    }

    public Color getSourceColor() {
        return sourceColor;
    }

    public void setSourceColor(Color sourceColor) {
        this.sourceColor = sourceColor;
    }

    public boolean isIsWaterHole() {
        return isWaterHole;
    }

    public void setIsWaterHole(boolean isWaterHole) {
        this.isWaterHole = isWaterHole; 
    }

    public Color getBoatColor() {
        return boatColor;
    }

    public void setBoatColor(Color boatColor) {
        this.boatColor = boatColor;
    }
}
