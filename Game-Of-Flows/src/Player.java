
import java.io.FileWriter;
import java.io.PrintWriter;
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
public class Player {

    // flag to say whether we are running in tournament mode or not.  this is based on parameter passed into main()
    private static boolean fisTournament = false;

    // a very simple log (i.e. file) interface.  cannot be used in tournament mode.
    public static PrintWriter sLog;

    // holds information about all the excavators on the field
    //private Excavator[] fExcavators;
    // scores for each player
    private int redScore;
    private int blueScore;

    private int turnNumber;

    public Field field;

    public static Scanner in;

    /**
     * The main player run loop.
     */
    public void run() {
        // Scanner to parse input from the game engine.
        in = new Scanner(System.in);

        // Keep reading states until the game ends.
        turnNumber = in.nextInt();

        // Test moves
        field.getExcavator(1).addCommand("move 3 3");
        field.getExcavator(1).addCommand("move 5 5");
        field.getExcavator(1).addCommand("move 7 7");
        field.getExcavator(1).addCommand("move 9 9");
        field.getExcavator(1).addCommand("dig 10 9");
        field.getExcavator(1).addCommand("drop 9 10");
        field.getExcavator(1).addCommand("dig 10 8");
        field.getExcavator(1).addCommand("drop 9 10");
        field.getExcavator(1).addCommand("move 9 6");
        field.getExcavator(1).addCommand("dig 10 7");
        field.getExcavator(1).addCommand("drop 8 6");
        field.getExcavator(1).addCommand("dig 10 6");
        field.getExcavator(1).addCommand("drop 8 6");
        field.getExcavator(1).addCommand("dig 10 5");
        field.getExcavator(1).addCommand("drop 8 6");
        field.getExcavator(1).addCommand("move 7 6");

        // the game engine sends a -1 for a turn number when the game is over
        while (turnNumber >= 0) {
            // Read current game score.
            redScore = in.nextInt();
            blueScore = in.nextInt();

            // Decode the field input
            field.decodeField();

            // Should have something here to call something from the yet to be 
            // made strategy class.
            // Execute the current turn
            field.executeTurn();

            // Go on to the next turn
            turnNumber = in.nextInt();
        }
    }

    /**
     * Construct a basic player object.
     */
    private Player() {

        this.field = new Field();
    }

    /**
     * This is the main entry point to the player.
     *
     * @param args command-line arguments tournament: this argument is passed
     * when the player is run as part of the official tournament website no file
     * or network I/O is allowed
     */
    public static void main(String[] args) {
        try {
            if (args.length > 0) {
                fisTournament = args[0].equals("tournament");
            }

            if (!fisTournament) {
                sLog = new PrintWriter(new FileWriter(String.format("sLog.log")));
            }

            Player player = new Player();

            player.run();
        } catch (Throwable t) {
            System.err.format("Unhandled exception %s\n", t.getMessage());
            if (!fisTournament) {
                t.printStackTrace(sLog);
            } else {
                t.printStackTrace();
            }
        }

        if (!fisTournament) {
            sLog.close();
        }

        // explicitly exit with a success status
        System.exit(0);
    }

}
