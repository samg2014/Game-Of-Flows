
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
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
    public static PrintWriter out;

    // holds information about all the excavators on the field
    //private Excavator[] fExcavators;
    // scores for each player
    private int redScore;
    private int blueScore;

    private int turnNumber;

    public static Field field;

    public static Scanner in;
    boolean a;
    public static AStarPathFinder pathFinder;
    public static CanalFinder canalFinder;

    /**
     * The main player run loop.
     */
    public void run() {
        // Scanner to parse input from the game engine.
        in = new Scanner(System.in);

        // Keep reading states until the game ends.
        turnNumber = in.nextInt();

        // Test moves
//        field.getExcavator(1).addCommand("move 3 3");
//        field.getExcavator(1).addCommand("move 5 5");
//        field.getExcavator(1).addCommand("move 7 7");
//        field.getExcavator(1).addCommand("move 9 9");
//        field.getExcavator(1).addCommand("dig 10 9");
//        field.getExcavator(1).addCommand("drop 9 10");
//        field.getExcavator(1).addCommand("dig 10 8");
//        field.getExcavator(1).addCommand("drop 9 10");
//        field.getExcavator(1).addCommand("move 9 6");
//        field.getExcavator(1).addCommand("dig 10 7");
//        field.getExcavator(1).addCommand("drop 8 6");
//        field.getExcavator(1).addCommand("dig 10 6");
//        field.getExcavator(1).addCommand("drop 8 6");
//        field.getExcavator(1).addCommand("dig 10 5");
//        field.getExcavator(1).addCommand("drop 8 6");
//        field.getExcavator(1).addCommand("move 7 6");
        //field.getExcavator(0).addCommand("target 30 30");
        a = false;
        // the game engine sends a -1 for a turn number when the game is over
        while (turnNumber >= 0) {
            // Read current game score.
            redScore = in.nextInt();
            blueScore = in.nextInt();

            // Decode the field input
            field.decodeField();

            pathFinder = new AStarPathFinder(field.convertToMap(), 1000);
            
            if(out != null){
                out.println("-----------------------------------");
                out.println("----------------"+turnNumber+"-----------------");
                out.println("-----------------------------------");
            }

            command0();
            command1();
            command2();

            // Execute the current turn
            field.executeTurn();

            // Go on to the next turn
            turnNumber = in.nextInt();
        }
    }

    public static void command0() {
        Excavator e = field.getExcavator(0);
        if (!e.isIsHoldingBoat() && e.getCommands().isEmpty()) {
            int[] loc = field.findBoat();
            int[] op = field.optimize(e.getxLoc(), e.getyLoc(), loc[0], loc[1]);
            e.addCommand("target " + (op[0]) + " " + op[1]);
            e.addCommand("pickup " + (loc[0]) + " " + loc[1]);
            e.addCommand("target 11 11");
            e.addCommand("drop 10 10");

        }
    }
    public static int onePhase = 0;

    public static void command1() {
        Excavator e = field.getExcavator(1);
        if (e.getCommands().isEmpty()) {
            int[] dump;
            if (onePhase == 0) {
                e.addCommand("target 9 9");
                e.addCommand("dig 10 9");
                onePhase++;
            }
            else if (onePhase == 1) {
                dump = field.findAdjacentDump(9, 9);
                e.addCommand("drop " + dump[0] + " " + dump[1]);
                onePhase++;
            }
            else if (onePhase == 2) {
                e.addCommand("dig 10 8");
                dump = field.findAdjacentDump(9, 9);
                e.addCommand("drop " + dump[0] + " " + dump[1]);
                onePhase++;
            }
            else if (onePhase == 3) {
                e.addCommand("move 9 6");
                e.addCommand("dig 10 7");
                dump = field.findAdjacentDump(9, 6);
                e.addCommand("drop " + dump[0] + " " + dump[1]);
                onePhase++;
            }
            else if (onePhase == 4) {
                e.addCommand("dig 10 6");
                dump = field.findAdjacentDump(9, 6);
                e.addCommand("drop " + dump[0] + " " + dump[1]);
                onePhase++;
            }
            else if (onePhase == 5) {
                e.addCommand("dig 10 5");
                dump = field.findAdjacentDump(9, 6);
                e.addCommand("drop " + dump[0] + " " + dump[1]);
                onePhase = 0;
            }
        }
    }

    public static void command2() {
        Excavator e = field.getExcavator(2);
        if (field.findOpponentStart() != null && e.getCommands().isEmpty()) {
            int[] target = field.findOpponentStart();
            int[] op = field.optimize(e.getxLoc(), e.getyLoc(), target[0], target[1]);
            e.addCommand("target " + (op[0]) + " " + op[1]);
            int[] dirt = field.findAdjacentDirt(op[0], op[1]);
            e.addCommand("dig " + dirt[0] + " " + dirt[1]);
            e.addCommand("drop " + (target[0]) + " " + target[1]);
        }
    }

    public static void command(int id) {
        if (id == 0) {
            command0();
        }
        if (id == 1) {
            command1();
        }
        if (id == 2) {
            command2();
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
                out = new PrintWriter(new FileWriter(String.format("sLog.log")));
            }

            Player player = new Player();

            player.run();
        } catch (Throwable t) {
            System.err.format("Unhandled exception %s\n", t.getMessage());
            if (!fisTournament) {
                t.printStackTrace(out);
            } else {
                t.printStackTrace();
            }
        }

        if (!fisTournament) {
            out.close();
        }

        // explicitly exit with a success status
        System.exit(0);
    }

}
