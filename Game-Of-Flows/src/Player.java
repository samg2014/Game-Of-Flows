import java.io.FileWriter;
import java.io.PrintWriter;
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
     * The main player run loop.
     */
    public void run() {
        // Scanner to parse input from the game engine.
        Scanner in = new Scanner( System.in );

        // Keep reading states until the game ends.
        int turnNumber = in.nextInt();

        // the game engine sends a -1 for a turn number when the game is over
        while ( turnNumber >= 0 )
        {
            // Read current game score.
            in.nextInt();
            in.nextInt();

            // Read the current field configuration and store in the field contents array
            for ( int i = 0; i < 31; i++ )
            {
                for ( int j = 0; j < 31; j++ )
                {
                    // Decode the field space encoding
                    String fieldEncoding = in.next();
//                    fField[ i ][ j ] = decodeFieldSymbol( fieldEncoding );
                }
            }

            // Read the states of all the excavators.
            for ( int i = 0; i<6; i++ )
            {
                //String encoding = in.next();

                // Record the excavator's location.
                int x = in.nextInt(); //Integer.parseInt( encoding );
                int y = in.nextInt();
                
                //excavator.setPosition( new Point( x, y ) );

                // read inventory
                String encoding = in.next();
//                int holding = Constants.NOTHING;
//                if ( "d".equals( encoding ) )
//                {
//                    holding = Constants.DIRT;
//                }
//                else if ( "b".equals( encoding ) )
//                {
//                    holding = Constants.BOAT;
//                }
                //excavator.setHolding( holding );
            }
            

            
                System.err.println("start turn " + turnNumber);
                if(turnNumber == 0) System.out.println("move 10 11");
                else System.out.println("idle");
                System.out.println("move 1 0");
                System.out.println("move 2 0");
                System.err.println("finish");
turnNumber = in.nextInt();
            }
        }
        // flag to say whether we are running in tournament mode or not.  this is based on parameter passed into main()
    private static boolean fisTournament = false;

    // a very simple log (i.e. file) interface.  cannot be used in tournament mode.
    private static PrintWriter sLog;

    // holds information about all the excavators on the field
    //private Excavator[] fExcavators;
    // scores for each player
    private int[] fScores;

    // a basic representation of a field
    private int[][] fField;

    /**
     * Construct a basic player object.
     */
    private Player() {
        //fExcavators = new Excavator[ Constants.NUM_EXCAVATORS_PER_TEAM * 2 ];

//        fScores = new int[ 2 ];
        //fField = new int[ Constants.FIELD_DIMENSION ][ Constants.FIELD_DIMENSION ];
//        for ( int i = 0; i < fExcavators.length; i++ )
//        {
//            fExcavators[ i ] = new Excavator( i < Constants.NUM_EXCAVATORS_PER_TEAM ? Constants.RED : Constants.BLUE );
//        }
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
                sLog = new PrintWriter(new FileWriter(String.format("sp-%d.log", (int) (Math.random() * 1000))));
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
