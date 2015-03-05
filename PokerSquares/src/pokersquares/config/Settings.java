package pokersquares.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import pokersquares.algorithms.*;
import pokersquares.environment.*;
import pokersquares.players.BeemoV2;
import pokersquares.trainers.Billy;
import pokersquares.trainers.Jake;
import pokersquares.trainers.Prismo;
import pokersquares.trainers.Trainer;
import pokersquares.trainers.ValueReinforcement;

public class Settings {
    //Holds all CONSTANTS for all classes for easy reference and tweaking
    public enum PointSystem {RANDOM, AMERICAN, AMERITISH, BRITISH, HYPERCORNER, SINGLEHAND };
    
    public static class Main {
        public static int games = 10000;
        public static int seed = 1;
        public static boolean verbose = false;
        public static boolean tournament = false;
        public static int randomPointSystemSeed = 1123;
        public static PointSystem pointSystem = PointSystem.AMERICAN;
    }
    
    public static class Environment {
        public static PokerSquaresPointSystem system;
    }
    
    public static class Algorithms {
        public static boolean debugUCT = false;
        public static int searchDepth = 2;      //Currently unused.
        public static int simSampleSize = 1000; 
        public static int playSampleSize = 24;  //Max = 24, Min = 1
        public static int deckSampleMax = 52;   //Currently unused?
        
        public static boolean enableSymmetry = false;
        
        //UCT
        public static double UCT = 2.5;
        
        public static boolean positionRankEnabled = false;
        
        public static Algorithm simAlgorithm = new OBF();
        public static Algorithm[] algorithm = 
                new Algorithm[] {new OBF(), new OBF(), new OBF()};
    }
    
    public static class Greedy {
        public static double timeRatio = 0.05; //ratio of the total alloted time the player will play
    }
    
    public static class BMO {
        public static int[] turnSplits = new int[]{5, 25, 25};
        public static BeemoV2 BMO;
        
        //SETTINGS
        public static boolean genSettings = true;
        public static boolean readPatterns = false;
        public static String patternsFileIn = Main.pointSystem.name() + ".pattern";
        public static String settingsFileIn = "test";
    }
    
    public static class Training {
        public static boolean train = true;
        public static long millis = 60000;
        public static boolean verbose = true; 
        
        public static double policyMax = 1;
        public static double policyMin = -1;
        public static boolean randomize = false;
        public static String patternsFileOut = Main.pointSystem.name() + ".pattern";
        public static String settingsFileOut = "test";
        public static boolean updateBest = true;
    
        //VALUES to be adjusted
        public static List <double[]> values = new ArrayList();
        public static double score = Double.NEGATIVE_INFINITY;

        public static List <double[]> bestValues = new ArrayList ();
        public static double bestScore = Double.NEGATIVE_INFINITY;
        
        public static Trainer trainer = new Prismo();
        //public static Trainer trainer = new Billy();
        //public static Trainer trainer = new Jake();
        //public static Trainer trainer = new ValueReinforcement();
        
    }
    
    public static class Evaluations {
        public static int numThreads = 16;
        
        //Pattern Policy
        public static boolean simpleScoring = false;
        public static boolean patternate = true;
        //public static String pattern = "A"; //A,B,C
        
        public static double[] handScores = new double[10];
        
        public static boolean[] rowHands;
        public static boolean[] colHands;
        
        //array hold exponents for values of hands in american order
        
        public static double[] highCardPolicy;
        public static double[] pairPolicy;
        public static double[] twoPairPolicy;
        public static double[] threeOfAKindPolicy;
        public static double[] straightPolicy;
        public static double[] flushPolicy;
        public static double[] fullHousePolicy;
        public static double[] fourOfAKindPolicy;
        
        public static void debug () {
            System.err.println("Settings");
            System.err.println("Hand Scores: " + Arrays.toString(handScores));
            System.err.println("Row Hands: " + Arrays.toString(rowHands));
            System.err.println("Col Hands: " + Arrays.toString(colHands));
            System.err.println("High Card Policy: " + Arrays.toString(highCardPolicy));
            System.err.println("Pair Policy: " + Arrays.toString(pairPolicy));
            System.err.println("Two Pair Policy: " + Arrays.toString(twoPairPolicy));
            System.err.println("Three Of A Kind Policy : " + Arrays.toString(threeOfAKindPolicy));
            System.err.println("Straight Policy: " + Arrays.toString(straightPolicy));
            System.err.println("Flush Policy: " + Arrays.toString(flushPolicy));
            System.err.println("Full House Policy: " + Arrays.toString(fullHousePolicy));
            System.err.println("Four Of A Kind Policy: " + Arrays.toString(fourOfAKindPolicy));
        }
        
        public static void updateSettings(List <double[]> values) {
            highCardPolicy = values.get(0);
            pairPolicy = values.get(1);
            twoPairPolicy = values.get(2);
            threeOfAKindPolicy = values.get(3);
            straightPolicy = values.get(4);
            flushPolicy = values.get(5);
            fullHousePolicy = values.get(6);
            fourOfAKindPolicy = values.get(7);
        }
    }
    
}
