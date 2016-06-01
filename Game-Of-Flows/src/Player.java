
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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
     * 1) Find neutral boats, pick them up and bring the to the water source
     *
     * 2) Attack the opponent's canal
     *
     *
     *
     *
     *
     *
     * EXCAVATOR 1:
     *
     * 1) If a boat is in the water source and it is not #4, expand the boat
     * holding canal
     *
     * 2) If the time is right open up the canal
     *
     * 3) If the canal has been open, engage in maintenence
     *
     * 4) If the canal has not been opened, build up a canal along the lower
     * portion of the board to the right and up the right side
     *
     *
     *
     *
     *
     *
     * EXCAVATOR 2:
     *
     * 0) First, engage in canal protection (minimal, places to things of dirt)
     *
     * 1) If #0 is complete, grab a neutral boat if it is within 20 spaces
     *
     * 2) Else, engage in attack activities
     *
     *
     *
     *
     *
     *
     * New strategy:?
     *
     * #0: Grab boats, protect canal #1: Protect canal, build long canal #2:
     * Grab boats, protect canal
     */
    // flag to say whether we are running in tournament mode or not.  this is based on parameter passed into main()
    private static boolean fisTournament = false;

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

            secondCanal = field.getPathToWaterHolePartTwo();

            fillList = field.getFillList(secondCanal);

            field.dontDig = fillList;

            ArrayList<int[]> dontDump = new ArrayList<>();
            dontDump.addAll(canal);
            dontDump.add(skipTile);
            for (Tile t : secondCanal) {
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
    static int zeroPhase = 0;

    public static void command0() {
        //Get excavator 0
        Excavator e = field.getExcavator(0);

        //If this excavator has stalled, kill its commands and restart
        if (e.getxLoc() == last0LocX && e.getyLoc() == last0LocY) {
            zeroStallCount++;
            if (zeroStallCount >= 3) {
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

        //If phase is 1 and there is a neutral boat to acquire, reset phase and kill all commands0
        if (zeroPhase == 1 && field.findNearestBoat(e.getxLoc(), e.getyLoc()) != null) {
            zeroPhase = 0;
            e.clearCommands();
        }

        //If this excavator is not holding a boat and has no commands, try to find a boat
        if (!e.isHoldingBoat()) {

            //Get the location for the nearest boat
            int[] loc = field.findNearestBoat(e.getxLoc(), e.getyLoc());
            //If there is no neares boat, set phase to 1
            if (loc == null) {
                zeroPhase = 1;
            } else { // If there is a boat
                //Prevents targeting a boat that has been taken
                e.clearCommands();
                //Optimize a target to get it
                int[] op = field.optimize(e.getxLoc(), e.getyLoc(), loc[0], loc[1]);
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
        //If the excavator is holding a boat, bring it to the water source
        if (e.isHoldingBoat()) {
            e.clearCommands();
            int[] op = field.optimize(e.getxLoc(), e.getyLoc(), 10, 10);
            e.addCommand("target " + (op[0]) + " " + op[1]);
            e.addCommand("drop 10 10");
            return;
        }

        //If there are no boats to be had, go into attack phase
        if (zeroPhase == 1 && e.getCommands().isEmpty()) {
            if (Player.fisTournament) {//No attacking in debug mode
                //Find the opponents canal
                ArrayList<Tile> targets = field.findOpponentCanal();
                if (targets.size() > 0) {
                    Random r = new Random();
                    //Randomly choose a target
                    Tile t = targets.get(r.nextInt(targets.size()));
                    int[] target = new int[]{t.x, t.y};
                    //Optimize for that target and target it
                    int[] op = field.optimize(e.getxLoc(), e.getyLoc(), target[0], target[1]);
                    e.addCommand("target " + (op[0]) + " " + op[1]);
                    //Find dirt near the target
                    int[] dirt = field.findAdjacentDirt(op[0], op[1]);
                    if (e.isHoldingDirt()) {
                        e.addCommand("drop " + target[0] + " " + target[1]);
                    }
                    //Get the dirt and dump it on the target
                    e.addCommand("dig " + dirt[0] + " " + dirt[1]);
                    e.addCommand("drop " + (target[0]) + " " + target[1]);
                }
            }
        }
    }

    //Excavator 1's command phase
    public static int onePhase = 0;
    //The canal being build
    public static ArrayList<int[]> canal = new ArrayList<>();
    public static ArrayList<Tile> secondCanal;
    public static ArrayList<Tile> fillList;
    public static final int[] skipTile = new int[]{10, 6};
//    public static int[] lastTile = new int[]{10, 6};
//    public static int[] beforeLastTile = new int[]{-1, -1};
    // private static int canalLength = 0;
    private static boolean openedCanal = false;
    // Used to identify if this excavator has stalled
    private static int last1LocX;
    private static int last1LocY;

    public static void command1() {
        //Get the excavator
        Excavator e = field.getExcavator(1);

        //If this excavator has stalled, kill its commands and restart
//        if (e.getxLoc() == last1LocX && e.getyLoc() == last1LocY) {
//            e.clearCommands();
//        }
        //If the water source has a boat and phase = 1
        if (field.getTile(10, 10).hasBoat() && onePhase == 1) {
            //If this excavator has commands
            if (!e.getCommands().isEmpty()) {
                //Clear the commands
                e.clearCommands();
            }
            //Set phase to 0
            onePhase = 0;
        }
        //If it has no commands
        if (e.getCommands().isEmpty()) {
            //If canal is opened
            if (openedCanal) {
                //For every tile in the canal, if the tile has dirt on it, clear the tile
                for (int[] target : canal) {
                    if (field.getTile(target[0], target[1]).getDirtHeight() > 0) {
                        int[] op = field.optimize(e.getxLoc(), e.getyLoc(), target[0], target[1]);
                        int dump[];
                        e.addCommand("target " + (op[0]) + " " + op[1]);
                        if (e.isHoldingDirt()) {
                            dump = field.findAdjacentDump(op[0], op[1], target[0], target[1]);
                            e.addCommand("drop " + dump[0] + " " + dump[1]);
                        }
                        e.addCommand("dig " + target[0] + " " + target[1]);
                        dump = field.findAdjacentDump(op[0], op[1], target[0], target[1]);
                        e.addCommand("drop " + dump[0] + " " + dump[1]);
                        break;
                    }
                }
            } else { // If the canal isn't open yet
                boolean go = false;

                //If the water source has a boat and phase is not 2
                if (field.getTile(10, 10).hasBoat() && onePhase != 2) {
                    onePhase = 0;
                    int[] target = null;
                    //Find the first target in canal that isn't clear
                    //If one exists, set flag go to true, executing commands to clear the tile
                    //If one doesn't exist, don't try to clear the tile
                    for (int[] tar : canal) {
                        if (field.getTile(tar[0], tar[1]).getDirtHeight() >= 1) {
                            target = tar;
                            //go = true;
                            break;
                        }
                    }

                    //Clear the canal tile to move up the boats
                    if (go) {
                        if (field.getTile(target[0], target[1]).getDirtHeight() < 1) {
                        }
                        int[] op = field.optimize(e.getxLoc(), e.getyLoc(), target[0], target[1]);
                        int dump[];
                        e.addCommand("target " + (op[0]) + " " + op[1]);
                        if (e.isHoldingDirt()) {
                            dump = field.findAdjacentDump(op[0], op[1], target[0], target[1]);
                            e.addCommand("drop " + dump[0] + " " + dump[1]);
                        }
                        e.addCommand("dig " + target[0] + " " + target[1]);
                        dump = field.findAdjacentDump(op[0], op[1], target[0], target[1]);
                        e.addCommand("drop " + dump[0] + " " + dump[1]);
                        secondCanal.add(0, field.getTile(target[0], target[1]));

                    }
                }

                //Continue building the canal
                if (!go) {
                    onePhase = 1;

                    //Find optimal path back home, so we know we can make it tile
                    int[] op = field.optimize(e.getxLoc(), e.getyLoc(), skipTile[0], skipTile[1]);
                    Path p = pathFinder.findPath(e.getxLoc(), e.getyLoc(), op[0], op[1]);

                    //Find the travel time for the boats along the canal
                    int travelTime = 0;
                    try {
                        travelTime = ((5 + secondCanal.size()) / field.getTile(10, 9).getWaterFlow());
                    } catch (ArithmeticException ae) {
                        //For if water flow is equal to 0
                    }

                    //Check to see if it is time to open the canal
//                    if (p != null && 199 - Player.turnNumber - p.getLength() - travelTime < 20) {
//                        //If it is, let everyone know
//                        onePhase = 2;
//
//                        //Target the closest tile neighboring the skipped tile
//                        e.addCommand("target " + (op[0]) + " " + op[1]);
//
//                        //If holding dirt, plan to dump it
//                        if (e.isHoldingDirt()) {
//                            int[] dump;
//                            dump = field.findAdjacentDump(op[0], op[1], skipTile[0], skipTile[1]);
//                            e.addCommand("drop " + dump[0] + " " + dump[1]);
//                        }
//                        //Open up the skipped tile
//                        e.addCommand("dig " + skipTile[0] + " " + skipTile[1]);
//                        int dump[];
//                        dump = field.findAdjacentDump(op[0], op[1], skipTile[0], skipTile[1]);
//                        e.addCommand("drop " + dump[0] + " " + dump[1]);
//
//                        openedCanal = true;
//                        canal.add(skipTile);
//                        secondCanal.stream().forEach((t) -> {
//                            canal.add(new int[]{t.x, t.y});
//                        });
//                    } else {
                    ArrayList<Tile> workList = new ArrayList<>();
                    for (Tile t : secondCanal) {
                        if (t.getDirtHeight() > 0) {
                            workList.add(t);
                        }
                    }

                    if (workList.size() == 1) {
                        Tile t = workList.get(0);
                        op = field.optimize(e.getxLoc(), e.getyLoc(), t.x, t.y);
                        e.addCommand("target " + (op[0]) + " " + op[1]);
                        if (e.isHoldingDirt()) {
                            int[] dump;
                            dump = field.findAdjacentDump(op[0], op[1], t.x, t.y);
                            field.getTile(dump[0], dump[1]).setDirtHeight(field.getTile(dump[0], dump[1]).getDirtHeight() + 1);
                            e.addCommand("drop " + dump[0] + " " + dump[1]);
                        }
                        e.addCommand("dig " + t.x + " " + t.y);
                        int dump[];
                        dump = field.findAdjacentDump(op[0], op[1], t.x, t.y);
                        field.getTile(dump[0], dump[1]).setDirtHeight(field.getTile(dump[0], dump[1]).getDirtHeight() + 1);
                        e.addCommand("drop " + dump[0] + " " + dump[1]);
                    } else if (workList.size() > 1) {
                        Tile first = workList.get(0);
                        Tile second = workList.get(1);
                        ArrayList<Tile> firstNeighbors = new ArrayList<>();
                        ArrayList<Tile> secondNeighbors = new ArrayList<>();
                        ArrayList<Tile> common = new ArrayList<>();
                        int[][] neighbors = {{0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}};
                        for (int[] n : neighbors) {
                            try {
                                if (!field.getTile(first.x + n[0], first.y + n[1]).blocked && !secondCanal.contains(field.getTile(first.x + n[0], first.y + n[1]))) {
                                    firstNeighbors.add(field.getTile(first.x + n[0], first.y + n[1]));
                                }
                            } catch (Exception ex) {

                            }
                            try {
                                if (!field.getTile(second.x + n[0], second.y + n[1]).blocked && !secondCanal.contains(field.getTile(second.x + n[0], second.y + n[1]))) {
                                    secondNeighbors.add(field.getTile(second.x + n[0], second.y + n[1]));
                                }
                            } catch (Exception ex) {

                            }
                        }

                        for (Tile t : firstNeighbors) {
                            if (secondNeighbors.contains(t)) {
                                common.add(t);
                            }
                        }

                        if (common.size() < 1) {
                            Tile t = workList.get(0);
                            op = field.optimize(e.getxLoc(), e.getyLoc(), t.x, t.y);
                            e.addCommand("target " + (op[0]) + " " + op[1]);
                            if (e.isHoldingDirt()) {
                                int[] dump;
                                dump = field.findAdjacentDump(op[0], op[1], t.x, t.y);
                                field.getTile(dump[0], dump[1]).setDirtHeight(field.getTile(dump[0], dump[1]).getDirtHeight() + 1);
                                e.addCommand("drop " + dump[0] + " " + dump[1]);
                            }
                            e.addCommand("dig " + t.x + " " + t.y);
                            int dump[];
                            dump = field.findAdjacentDump(op[0], op[1], t.x, t.y);
                            field.getTile(dump[0], dump[1]).setDirtHeight(field.getTile(dump[0], dump[1]).getDirtHeight() + 1);
                            e.addCommand("drop " + dump[0] + " " + dump[1]);
                        } else {
                            double minD = Double.MAX_VALUE;
                            Tile min = null;
                            for (Tile t : common) {
                                if (Math.sqrt(Math.pow(Math.abs(t.x - e.getxLoc()), 2) + Math.pow(Math.abs(t.y - e.getyLoc()), 2)) < minD) {
                                    minD = Math.sqrt(Math.pow(Math.abs(t.x - e.getxLoc()), 2) + Math.pow(Math.abs(t.y - e.getyLoc()), 2));
                                    min = t;
                                }
                            }

                            op = new int[]{min.x, min.y};
                            e.addCommand("target " + (op[0]) + " " + op[1]);
                            if (e.isHoldingDirt()) {
                                int[] dump;
                                dump = field.findAdjacentDump(op[0], op[1], first.x, first.y);
                                field.getTile(dump[0], dump[1]).setDirtHeight(field.getTile(dump[0], dump[1]).getDirtHeight() + 1);
                                e.addCommand("drop " + dump[0] + " " + dump[1]);
                            }
                            e.addCommand("dig " + first.x + " " + first.y);
                            int dump[];
                            dump = field.findAdjacentDump(op[0], op[1], first.x, first.y);
                            field.getTile(dump[0], dump[1]).setDirtHeight(field.getTile(dump[0], dump[1]).getDirtHeight() + 1);
                            e.addCommand("drop " + dump[0] + " " + dump[1]);

                            e.addCommand("dig " + second.x + " " + second.y);
                            dump = field.findAdjacentDump(op[0], op[1], second.x, second.y);
                            field.getTile(dump[0], dump[1]).setDirtHeight(field.getTile(dump[0], dump[1]).getDirtHeight() + 1);
                            e.addCommand("drop " + dump[0] + " " + dump[1]);
                        }

//                    for (Tile t : secondCanal) {
//                        if (t.getDirtHeight() > 0) {
//                            op = field.optimize(e.getxLoc(), e.getyLoc(), t.x, t.y);
//                            e.addCommand("target " + (op[0]) + " " + op[1]);
//                            if (e.isHoldingDirt()) {
//                                int[] dump;
//                                dump = field.findAdjacentDump(op[0], op[1], t.x, t.y);
//                                e.addCommand("drop " + dump[0] + " " + dump[1]);
//                            }
//                            e.addCommand("dig " + t.x + " " + t.y);
//                            int dump[];
//                            dump = field.findAdjacentDump(op[0], op[1], t.x, t.y);
//                            e.addCommand("drop " + dump[0] + " " + dump[1]);
//                            t.dug = true;
//                            break;
//                        }
//                    }
                        if (e.getCommands().isEmpty()) {
                            for (Tile t : fillList) {
                                if (t.getDirtHeight() < 2) {
                                    op = field.optimize(e.getxLoc(), e.getyLoc(), t.x, t.y);
                                    e.addCommand("target " + (op[0]) + " " + op[1]);
                                    if (e.isHoldingDirt()) {
                                        e.addCommand("drop " + t.x + " " + t.y);
                                    }
                                    int dirt[];
                                    dirt = field.findAdjacentDirt(op[0], op[1]);
                                    e.addCommand("dig " + dirt[0] + " " + dirt[1]);
                                    e.addCommand("drop " + t.x + " " + t.y);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            //Update position
            last1LocX = e.getxLoc();
            last1LocY = e.getyLoc();
        }
    }

    private static int twoPhase = 0;

    public static int last2LocX;
    public static int last2LocY;
    public static int stallCount;

    public static void command2() {
        //Get excavator #2
        Excavator e = field.getExcavator(2);

        if (last2LocX == e.getxLoc() && last2LocY == e.getyLoc()) {
            stallCount++;
            if (stallCount >= 3) {
                e.clearCommands();
                Random r = new Random();
                int[] op = Player.field.optimize(e.getxLoc(), e.getyLoc(), (r.nextInt(4) - 2 + e.getxLoc()), (r.nextInt(4) - 2 + e.getyLoc()));
                e.addCommand("target " + op[0] + " " + op[1]);
                return;
            }
        } else {
            stallCount = 0;
        }

        last2LocX = e.getxLoc();
        last2LocY = e.getyLoc();

        //If the excavator is holding a boat and has no commands, bring it to the water source
        if (e.isHoldingBoat()) {
            e.clearCommands();
            int[] op = field.optimize(e.getxLoc(), e.getyLoc(), 10, 10);
            e.addCommand("target " + (op[0]) + " " + op[1]);
            e.addCommand("drop 10 10");
            return;
        }

        int[] op;
        if (e.getCommands().isEmpty()) {
            for (int i = secondCanal.size() - 1; i >= 0; i--) {
                Tile t = secondCanal.get(i);
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
                    break;
                }
            }
        }

        //If it has no commands
        if (e.getCommands().isEmpty()) {
            //ATTACK CODE
            if (Player.fisTournament) {//No attack mode in debug mode
                ArrayList<Tile> targets = field.findOpponentCanal();
                if (targets.size() > 0) {
                    twoPhase = 2;
                    Random r = new Random();
                    Tile t = targets.get(r.nextInt(targets.size()));
                    int[] target = new int[]{t.x, t.y};
                    op = field.optimize(e.getxLoc(), e.getyLoc(), target[0], target[1]);
                    e.addCommand("target " + (op[0]) + " " + op[1]);
                    int[] dirt = field.findAdjacentDirt(op[0], op[1]);
                    e.addCommand("dig " + dirt[0] + " " + dirt[1]);
                    e.addCommand("drop " + (target[0]) + " " + target[1]);
                    return;
                }
            }

            if (!e.isHoldingBoat()) {
                //Get the location for the nearest boat
                int[] loc = field.findFarthestBoat(e.getxLoc(), e.getyLoc());
                //If there is no neares boat, set phase to 1
                if (loc == null) {
                    zeroPhase = 1;
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
        Random r = new Random();
        a = r.nextBoolean();
        field = new Field();

        Player.canal.add(new int[]{10, 9});
        Player.canal.add(new int[]{10, 8});
        Player.canal.add(new int[]{10, 7});
        //Player.canal.add(new int[]{10, 6});
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
