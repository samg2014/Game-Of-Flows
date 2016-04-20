
import java.util.ArrayList;

/*
 * Represents one of six excavators on the field
 */

/**
 *
 * @author Sam
 */
public class Excavator {
    
    // Coordinates on the field of this excavator
    private int xLoc;
    private int yLoc;
    
    // Status booleans regarding what this excavator is holding
    private boolean isHoldingDirt;
    private boolean isHoldingBoat;
    
    // ID 0-5 of this excavator
    private final int id;
    
    // A list of commands to be executed by this excavator
    private ArrayList<String> commands;
    
    // This excavators color
    // Should be RED or BLUE
    private Color color;
    
    public Excavator(int id){
        this.id = id;
        xLoc = -1;
        yLoc = -1;
        isHoldingDirt = false;
        isHoldingBoat = false;
        commands = new ArrayList<>();
    }
    
    public int getID(){
        return this.id;
    }

    public int getxLoc() {
        return this.xLoc;
    }

    public void setxLoc(int xLoc) {
        this.xLoc = xLoc;
    }

    public int getyLoc() {
        return this.yLoc;
    }

    public void setyLoc(int yLoc) {
        this.yLoc = yLoc;
    }

    public boolean isIsHoldingDirt() {
        return this.isHoldingDirt;
    }

    public void setIsHoldingDirt(boolean isHoldingDirt) {
        this.isHoldingDirt = isHoldingDirt;
    }

    public boolean isIsHoldingBoat() {
        return this.isHoldingBoat;
    }

    public void setIsHoldingBoat(boolean isHoldingBoat) {
        this.isHoldingBoat = isHoldingBoat;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public ArrayList<String> getCommands() {
        return this.commands;
    }

    public void addCommand(String command){
        // Protection against bad commands
        if(command == null) return;
        
        commands.add(command);
    }
    
    public void removeCommand(int index){
        // Boundary limits
        if(index == -1 || index >= commands.size()) return;
        
        commands.remove(index);
    }
    
    // Executes and removes the first command on this excavator's list
    public void execute(){
        //Only red excavators can be commanded by us
        if(this.color != Color.RED) return;
        
        // Print out the next command to the engine or send the idle command
        System.out.println(!commands.isEmpty() ? commands.get(0) : "idle");
        
        // If there was a command printed from the list, remove it
        if(!commands.isEmpty()){
            commands.remove(0);
        }
    }
}
