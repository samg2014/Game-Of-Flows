
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
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

    /**
     * Summary of priorities:
     *
     *
     *
     *
     *
     * EXCAVATOR 0:
     *
     * 0) Un-stall
     * 
     * 1) Deposit boat if holding it
     * 
     * 2) Grab a boat
     *
     * 3) Build canal from water hole to water source
     *
     *
     *
     *
     * EXCAVATOR 1:
     *
     * 0) Deposit boat if holding one
     * 
     * 1) Build canal from source to hole (and keep it clear)
     * 
     * 2) Grab boats
     *
     *
     *
     *
     *
     * EXCAVATOR 2:
     *
     * 0) Build canal around opponent's source
     * 
     * 1) Build canal and keep it clear
     *
     */
    // flag to say whether we are running in tournament mode or not.  this is based on parameter passed into main()
    public static boolean fisTournament = false;

    // a very simple log (i.e. file) interface.  cannot be used in tournament mode.
    public static PrintWriter out;

    // holds information about all the excavators on the field
    //private Excavator[] fExcavators;
    // scores for each player
    private static int redScore;
    private static int blueScore;

    private static int turnNumber;

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

        // the game engine sends a -1 for a turn number when the game is over
        while (turnNumber >= 0) {
            // Read current game score.
            redScore = in.nextInt();
            blueScore = in.nextInt();

            // Decode the field input
            field.decodeField();

            pathFinder = new AStarPathFinder(field.convertToMap(), 50);

            canalFinder = new CanalFinder(field.convertToMap(), 2000);

            canal = field.getPathToWaterHolePartTwo();

            //NOT USED ANY MORE
            fillList = field.getFillList(canal);

            field.dontDig = fillList;

            ArrayList<int[]> dontDump = new ArrayList<>();
            for (Tile t : canal) {
                dontDump.add(new int[]{t.x, t.y});
            }
            field.dontDump = dontDump;

            if (out != null) {
                out.println("-----------------------------------");
                out.println("----------------" + turnNumber + "-----------------");
                out.println("-----------------------------------");
            }

            //The try catches isolate the commands from each other
            //One excavator's failures will not prevent the others from succeeding
            //Each method strategizes for its respective excavator
            try {
                command0();
            } catch (Exception e) {
                System.err.println(e.toString() + ":" + turnNumber + ":e0");
                for (StackTraceElement el : e.getStackTrace()) {
                    System.err.println("\t" + el);
                }
            }
            try {
                command1();
            } catch (Exception e) {
                System.err.println(e.toString() + ":" + turnNumber + ":e1");
                for (StackTraceElement el : e.getStackTrace()) {
                    System.err.println("\t" + el);
                }
            }
            try {
                command2();
            } catch (Exception e) {
                System.err.println(e.toString() + ":" + turnNumber + ":e2");
                for (StackTraceElement el : e.getStackTrace()) {
                    System.err.println("\t" + el);
                }
            }

            field.executeTurn();
            // Go on to the next turn
            turnNumber = in.nextInt();
        }
    }

    public static int numCapturedBoats = 0;

    // Used to identify if this excavator has stalled
    private static int last0LocX;
    private static int last0LocY;
    private static int zeroStallCount = 0;

    // The command phase this excavator is in
    static int zeroPhase = 1;
    static int boats = 0;
    public static int zeroSkipNum = 0;

    public static void command0() {
        //Get excavator 0
        Excavator e = field.getExcavator(0);

        //If this excavator has stalled, kill its commands and restart
        if (e.getxLoc() == last0LocX && e.getyLoc() == last0LocY) {
            zeroStallCount++;
            if (zeroStallCount >= 6) {
                e.clearCommands();
                Random r = new Random();
                int[] op = Player.field.optimize(e.getxLoc(), e.getyLoc(), (r.nextInt(4) - 2 + e.getxLoc()), (r.nextInt(4) - 2 + e.getyLoc()));
                e.addCommand("target " + op[0] + " " + op[1]);
                return;
            }
        } else {
            zeroStallCount = 0;
        }

        //Update position
        last0LocX = e.getxLoc();
        last0LocY = e.getyLoc();

        int[] op;

        if (e.isHoldingBoat()) {
            e.clearCommands();
            op = field.optimize(e.getxLoc(), e.getyLoc(), 10, 10);
            if (op == null) {
                op = field.optimize(e.getxLoc(), e.getyLoc(), 0, 0);
                e.addCommand("target " + (op[0]) + " " + op[1]);
                e.addCommand("drop 0 0");
            } else {
                e.addCommand("target " + (op[0]) + " " + op[1]);
                e.addCommand("drop 10 10");
            }
            if (boats == 0) {
                zeroPhase = 0;
            }
            boats++;
            return;
        }
        
            if (!e.isHoldingBoat()) {

                //Get the location for the nearest boat
                int[] loc = field.findNearestBoat(e.getxLoc(), e.getyLoc());
                //If there is no neares boat, set phase to 1
                if (loc == null) {
                } else { // If there is a boat
                    //Prevents targeting a boat that has been taken
                    e.clearCommands();
                    //Optimize a target to get it
                    op = field.optimize(e.getxLoc(), e.getyLoc(), loc[0], loc[1]);
                    //Target that tile
                    e.addCommand("target " + (op[0]) + " " + op[1]);
                    //If this excavator is holding dirt, find a good dump get rid of it
                    if (e.isHoldingDirt()) {
                        int[] dump = field.findAdjacentDump(op[0], op[1], loc[0], loc[1]);
                        e.addCommand("drop " + dump[0] + " " + dump[1]);
                    }
                    //Pick up the boat
                    e.addCommand("pickup " + (loc[0]) + " " + loc[1]);
                }
            }
        
        zeroPhase = 0;
        if (e.getCommands().isEmpty()) {
            for (int i = canal.size() - zeroSkipNum - 1; i >= 1; i--) {
                Tile t = canal.get(i);
                if (t.getDirtHeight() > 0) {
                    op = field.optimize(e.getxLoc(), e.getyLoc(), t.x, t.y);
                    e.addCommand("target " + (op[0]) + " " + op[1]);
                    if (e.isHoldingDirt()) {
                        int[] dump;
                        dump = field.findAdjacentDump(op[0], op[1], t.x, t.y);
                        e.addCommand("drop " + dump[0] + " " + dump[1]);
                    }
                    if (t.hasBoat()) {
                        e.addCommand("pickup " + t.x + " " + t.y);
                        return;
                    }
                    e.addCommand("dig " + t.x + " " + t.y);
                    int dump[];
                    dump = field.findAdjacentDump(op[0], op[1], t.x, t.y);
                    e.addCommand("drop " + dump[0] + " " + dump[1]);
                    t.dug = true;
                    return;
                }
            }
            zeroPhase = 1;
        }

//        if (zeroPhase == 1) {
//            //If this excavator is not holding a boat and has no commands, try to find a boat
//            if (!e.isHoldingBoat()) {
//
//                //Get the location for the nearest boat
//                int[] loc = field.findNearestBoat(e.getxLoc(), e.getyLoc());
//                //If there is no neares boat, set phase to 1
//                if (loc == null) {
//                } else { // If there is a boat
//                    //Prevents targeting a boat that has been taken
//                    e.clearCommands();
//                    //Optimize a target to get it
//                    op = field.optimize(e.getxLoc(), e.getyLoc(), loc[0], loc[1]);
//                    //Target that tile
//                    e.addCommand("target " + (op[0]) + " " + op[1]);
//                    //If this excavator is holding dirt, find a good dump get rid of it
//                    if (e.isHoldingDirt()) {
//                        int[] dump = field.findAdjacentDump(op[0], op[1], loc[0], loc[1]);
//                        e.addCommand("drop " + dump[0] + " " + dump[1]);
//                    }
//                    //Pick up the boat
//                    e.addCommand("pickup " + (loc[0]) + " " + loc[1]);
//                }
//            }
//        }
        //}

        //if (redScore > blueScore || noBoat) {
//            //If it has no commands
//            if (e.getCommands().isEmpty()) {
//                //ATTACK CODE
//                ArrayList<Tile> targets = field.findOpponentCanal();
//                if (targets.size() > 0) {
//                    Random r = new Random();
//                    Tile t = targets.get(r.nextInt(targets.size()));
//                    int[] target = new int[]{t.x, t.y};
//                    int[] op = field.optimize(e.getxLoc(), e.getyLoc(), target[0], target[1]);
//                    e.addCommand("target " + (op[0]) + " " + op[1]);
//                    int[] dirt = field.findAdjacentDirt(op[0], op[1]);
//                    field.getTile(dirt[0], dirt[1]).setDirtHeight(field.getTile(dirt[0], dirt[1]).getDirtHeight() - 1);
//                    e.addCommand("dig " + dirt[0] + " " + dirt[1]);
//                    e.addCommand("drop " + (target[0]) + " " + target[1]);
//                }
//            }
        //}
    }

//The canal being build
    public static ArrayList<Tile> canal;
    public static ArrayList<Tile> fillList;
    private static int last1LocX;
    private static int last1LocY;
    private static int stall1Count = 0;

    public static void command1() {
        //Get the excavator
        Excavator e = field.getExcavator(1);
        int[] op;
//
        
        //STALL CHECK CODE: this excavator ended up sitting and clearing for a while, this made it in-efficient!
//        if (last1LocX == e.getxLoc() && last1LocY == e.getyLoc()) {
//            stall1Count++;
//            if (stall1Count >= 8) {
//                e.clearCommands();
//                Random r = new Random();
//                op = Player.field.optimize(e.getxLoc(), e.getyLoc(), (r.nextInt(4) - 2 + e.getxLoc()), (r.nextInt(4) - 2 + e.getyLoc()));
//                e.addCommand("target " + op[0] + " " + op[1]);
//                return;
//            }
//        } else {
//            stall1Count = 0;
//        }

        //Deposit boats before anything - they bring in the money
        if (e.isHoldingBoat()) {
            e.clearCommands();
            op = field.optimize(e.getxLoc(), e.getyLoc(), 10, 10);
            e.addCommand("target " + (op[0]) + " " + op[1]);
            e.addCommand("drop 10 10");
            if (boats == 0) {
                zeroPhase = 0;
            }
            boats++;
            return;
        }
        last1LocX = e.getxLoc();
        last1LocY = e.getyLoc();
        if (e.getCommands().isEmpty()) {
            //GRACE!:
            //COMMENTED OUT OPTIMIIZED CANAL DIGGING CODE MOVED TO END OF FILE FOR READABILITY!!!!!!!!!!!!!!!!!!!!!
            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            for (int i = 0; i < canal.size() - 13; i++) {
                Tile t = canal.get(i);
                if (t.getDirtHeight() > 0) {
                    op = field.optimize(e.getxLoc(), e.getyLoc(), t.x, t.y);
                    e.addCommand("target " + op[0] + " " + op[1]);
                    if (e.isHoldingDirt()) {
                        int[] dump;
                        dump = field.findAdjacentDump(op[0], op[1], t.x, t.y);
                        e.addCommand("drop " + dump[0] + " " + dump[1]);
                    }
                    if (t.hasBoat()) {
                        e.addCommand("pickup " + t.x + " " + t.y);
                        return;
                    }
                    e.addCommand("dig " + t.x + " " + t.y);
                    int dump[];
                    dump = field.findAdjacentDump(op[0], op[1], t.x, t.y);
                    e.addCommand("drop " + dump[0] + " " + dump[1]);
                    return;
                }
            }
        }

        for (Tile t : canal) {
            if (t.getDirtHeight() > 0) {
                return;
            }
        }

        if (!e.isHoldingBoat()) {

            //Get the location for the nearest boat
            int[] loc = field.findNearestBoat(e.getxLoc(), e.getyLoc());
            //If there is no neares boat, set phase to 1
            if (loc == null) {
            } else { // If there is a boat
                //Prevents targeting a boat that has been taken
                e.clearCommands();
                //Optimize a target to get it
                op = field.optimize(e.getxLoc(), e.getyLoc(), loc[0], loc[1]);
                //Target that tile
                e.addCommand("target " + (op[0]) + " " + op[1]);
                //If this excavator is holding dirt, find a good dump get rid of it
                if (e.isHoldingDirt()) {
                    int[] dump = field.findAdjacentDump(op[0], op[1], loc[0], loc[1]);
                    e.addCommand("drop " + dump[0] + " " + dump[1]);
                }
                //Pick up the boat
                e.addCommand("pickup " + (loc[0]) + " " + loc[1]);
            }
        }
//
        
        //OLD CODE TO FILL IN PROTECTION AROUND CANAL INEFFICIENT and fairly worthless apparently
//        if (e.getCommands().isEmpty()) {
//            for (Tile t : fillList) {
//                if (t.getDirtHeight() < 2) {
//                    op = field.optimize(e.getxLoc(), e.getyLoc(), t.x, t.y);
//                    e.addCommand("target " + (op[0]) + " " + op[1]);
//                    if (e.isHoldingDirt()) {
//                        e.addCommand("drop " + t.x + " " + t.y);
//                    }
//                    int dirt[];
//                    dirt = field.findAdjacentDirt(op[0], op[1]);
//                    e.addCommand("dig " + dirt[0] + " " + dirt[1]);
//                    e.addCommand("drop " + t.x + " " + t.y);
//                    break;
//                }
//            }
//        }
    }

    public static int last2LocX;
    public static int last2LocY;
    public static int stall2Count;
    public static boolean finished = false;

    public static int defensiveMoves = 0;

    public static void command2() {
        //Get excavator #2
        Excavator e = field.getExcavator(2);

        if (last2LocX == e.getxLoc() && last2LocY == e.getyLoc()) {
            stall2Count++;
            if (stall2Count >= 5) {
                e.clearCommands();
                Random r = new Random();
                int[] op = Player.field.optimize(e.getxLoc(), e.getyLoc(), (r.nextInt(4) - 2 + e.getxLoc()), (r.nextInt(4) - 2 + e.getyLoc()));
                e.addCommand("target " + op[0] + " " + op[1]);
                return;
            }
        } else {
            stall2Count = 0;
        }
        last2LocX = e.getxLoc();
        last2LocY = e.getyLoc();

        int[] op;

        //Old fill list filling code
//        if (e.getCommands().isEmpty()) {
//            for (int i = 0; i < 12 && i < fillList.size(); i++) {
//                Tile t = fillList.get(i);
//                if (t.getDirtHeight() < 2) {
//                    if(pathFinder.findPath(e.getxLoc(), e.getyLoc(), t.x, t.y) == null) continue;
//                    op = field.optimize(e.getxLoc(), e.getyLoc(), t.x, t.y);
//                    e.addCommand("target " + (op[0]) + " " + op[1]);
//                    if (e.isHoldingDirt()) {
//                        e.addCommand("drop " + t.x + " " + t.y);
//                    }
//                    int dirt[];
//                    dirt = field.findAdjacentDirt(op[0], op[1]);
//                    e.addCommand("dig " + dirt[0] + " " + dirt[1]);
//                    e.addCommand("drop " + t.x + " " + t.y);
//                    break;
//                }
//            }
//        }
        
        
        //BUILD THE CANAL BACKWARDS (STARTS WITH THE pesky 'attack' canal
        if (e.getCommands().isEmpty()) {
            for (int i = canal.size() - 1; i >= 0; i--) {
                Tile t = canal.get(i);
                if (t.getDirtHeight() > 0) {
                    op = field.optimize(e.getxLoc(), e.getyLoc(), t.x, t.y);
                    e.addCommand("target " + (op[0]) + " " + op[1]);
                    if (e.isHoldingDirt()) {
                        int[] dump;
                        dump = field.findAdjacentDump(op[0], op[1], t.x, t.y);
                        e.addCommand("drop " + dump[0] + " " + dump[1]);
                    }
                    e.addCommand("dig " + t.x + " " + t.y);
                    int dump[];
                    dump = field.findAdjacentDump(op[0], op[1], t.x, t.y);
                    e.addCommand("drop " + dump[0] + " " + dump[1]);
                    t.dug = true;
                    return;
                }
            }
        }
    }

    public static void command(int id) {
        //Again, isolated to protect against errors
        if (id == 0) {
            try {
                command0();
            } catch (Exception e) {
                System.err.println(e.toString() + ":" + turnNumber + ":e0-c");
                for (StackTraceElement el : e.getStackTrace()) {
                    System.err.println("\t" + el);
                }
            }
        }
        if (id == 1) {
            try {
                command1();
            } catch (Exception e) {
                System.err.println(e.toString() + ":" + turnNumber + ":e1-c");
                for (StackTraceElement el : e.getStackTrace()) {
                    System.err.println("\t" + el);
                }
            }
        }
        if (id == 2) {
            try {
                command2();
            } catch (Exception e) {
                System.err.println(e.toString() + ":" + turnNumber + ":e2-c");
                for (StackTraceElement el : e.getStackTrace()) {
                    System.err.println("\t" + el);
                }
            }
        }
    }

    /**
     * Construct a basic player object.
     */
    private Player() {
        Random r = new Random();
        a = r.nextBoolean();
        field = new Field();
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

//            ArrayList<Tile> workList = new ArrayList<>();
//            for (Tile t : canal) {
//                if (t.getDirtHeight() > 0) {
//                    workList.add(t);
//                }
//            }
//
//            if (workList.size() == 1) {
//                Tile t = workList.get(0);
//                op = field.optimize(e.getxLoc(), e.getyLoc(), t.x, t.y);
//                e.addCommand("target " + (op[0]) + " " + op[1]);
//                if (e.isHoldingDirt()) {
//                    int[] dump = field.findAdjacentDump(op[0], op[1], t.x, t.y);
//                    field.getTile(dump[0], dump[1]).setDirtHeight(field.getTile(dump[0], dump[1]).getDirtHeight() + 1);
//                    e.addCommand("drop " + dump[0] + " " + dump[1]);
//                }
//                e.addCommand("dig " + t.x + " " + t.y);
//                int dump[];
//                dump = field.findAdjacentDump(op[0], op[1], t.x, t.y);
//                field.getTile(dump[0], dump[1]).setDirtHeight(field.getTile(dump[0], dump[1]).getDirtHeight() + 1);
//                e.addCommand("drop " + dump[0] + " " + dump[1]);
//            } else if (workList.size() > 1) {
//                Tile first = workList.get(0);
//                Tile second = workList.get(1);
//                ArrayList<Tile> firstNeighbors = new ArrayList<>();
//                ArrayList<Tile> secondNeighbors = new ArrayList<>();
//                ArrayList<Tile> common = new ArrayList<>();
//                int[][] neighbors = {{0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}};
//                for (int[] n : neighbors) {
//                    try {
//                        if (!field.getTile(first.x + n[0], first.y + n[1]).blocked && !canal.contains(field.getTile(first.x + n[0], first.y + n[1]))) {
//                            firstNeighbors.add(field.getTile(first.x + n[0], first.y + n[1]));
//                        }
//                    } catch (Exception ex) {
//
//                    }
//                    try {
//                        if (!field.getTile(second.x + n[0], second.y + n[1]).blocked && !canal.contains(field.getTile(second.x + n[0], second.y + n[1]))) {
//                            secondNeighbors.add(field.getTile(second.x + n[0], second.y + n[1]));
//                        }
//                    } catch (Exception ex) {
//
//                    }
//                }
//
//                for (Tile t : firstNeighbors) {
//                    if (secondNeighbors.contains(t)) {
//                        common.add(t);
//                    }
//                }
//
//                if (common.size() < 1) {
//                    Tile t = workList.get(0);
//                    op = field.optimize(e.getxLoc(), e.getyLoc(), t.x, t.y);
//                    e.addCommand("target " + (op[0]) + " " + op[1]);
//                    if (e.isHoldingDirt()) {
//                        int[] dump;
//                        dump = field.findAdjacentDump(op[0], op[1], t.x, t.y);
//                        field.getTile(dump[0], dump[1]).setDirtHeight(field.getTile(dump[0], dump[1]).getDirtHeight() + 1);
//                        e.addCommand("drop " + dump[0] + " " + dump[1]);
//                    }
//                    e.addCommand("dig " + t.x + " " + t.y);
//                    int dump[];
//                    dump = field.findAdjacentDump(op[0], op[1], t.x, t.y);
//                    field.getTile(dump[0], dump[1]).setDirtHeight(field.getTile(dump[0], dump[1]).getDirtHeight() + 1);
//                    e.addCommand("drop " + dump[0] + " " + dump[1]);
//                } else {
//                    double minD = Double.MAX_VALUE;
//                    Tile min = null;
//                    for (Tile t : common) {
//                        if (Math.sqrt(Math.pow(Math.abs(t.x - e.getxLoc()), 2) + Math.pow(Math.abs(t.y - e.getyLoc()), 2)) < minD) {
//                            minD = Math.sqrt(Math.pow(Math.abs(t.x - e.getxLoc()), 2) + Math.pow(Math.abs(t.y - e.getyLoc()), 2));
//                            min = t;
//                        }
//                    }
//
//                    op = new int[]{min.x, min.y};
//                    e.addCommand("target " + (op[0]) + " " + op[1]);
//                    if (e.isHoldingDirt()) {
//                        int[] dump;
//                        dump = field.findAdjacentDump(op[0], op[1], first.x, first.y);
//                        field.getTile(dump[0], dump[1]).setDirtHeight(field.getTile(dump[0], dump[1]).getDirtHeight() + 1);
//                        e.addCommand("drop " + dump[0] + " " + dump[1]);
//                    }
//                    e.addCommand("dig " + first.x + " " + first.y);
//                    int dump[] = field.findAdjacentDump(op[0], op[1], first.x, first.y);
//                    field.getTile(dump[0], dump[1]).setDirtHeight(field.getTile(dump[0], dump[1]).getDirtHeight() + 1);
//                    e.addCommand("drop " + dump[0] + " " + dump[1]);
//
//                    e.addCommand("dig " + second.x + " " + second.y);
//                    dump = field.findAdjacentDump(op[0], op[1], second.x, second.y);
//                    field.getTile(dump[0], dump[1]).setDirtHeight(field.getTile(dump[0], dump[1]).getDirtHeight() + 1);
//                    e.addCommand("drop " + dump[0] + " " + dump[1]);
//                }
//            }
