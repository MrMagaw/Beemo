package pokersquares.config;

import pokersquares.algorithms.*;
import pokersquares.environment.*;

public class Settings {
    //Holds all CONSTANTS for all classes for easy reference and tweaking
    
    public static class Main {
        public static int games = 10000;
        public static int seed = 0;
        public static boolean verbose = false;
    }
    
    public static class Environment {
        public static PokerSquaresPointSystem system;
    }
    
    public static class Algorithms {
        public static int searchDepth = 2;      //Currently unused.
        public static int simSampleSize = 1000; 
        public static int playSampleSize = 24;  //Max = 24, Min = 1
        public static int deckSampleMax = 52;   //Currently unused?
        
        public static boolean positionRankEnabled = false;
        
        public static Algorithm simAlgoritm = new OBF();
        public static Algorithm[] algorithm = 
                new Algorithm[] {new OBF(), new OBF(), new OBF()};
    }
    
    public static class BMO {
        public static int[] turnSplits = new int[]{5, 25, 25};
    }
    
    public static class Evaluations {
        
        //Pattern Policy
        public static String pattern = "C"; //A,B,C
        
        public static double pairExp = 1;
        public static double twoPairExp = 1;
        public static double threeOfAKindExp = 1;
        public static double fullHouseExp = 1.2;
        public static double fourOfAKindExp = 1.18;
        public static double flushExp = 0.95;
        public static double straightExp = 0.647;
        public static double straightFlushExp = 1.1;
        public static double royalFlushExp = 1.1;
        
        public static double[] pairPolicy = { 1, 0.1};
        public static double[] twoPairPolicy = { 1, 0.5, 0.1, -0.1 };
        public static double[] threeOfAKindPolicy = { 1, 0.7, 2, 0.5, 50 };
        public static double[] flushPolicy = { 2 };
        public static double[] fullHousePolicy = { 1, 0.7, 0.7, 0.8, 0.55, 0.6, 0.4, 0.4, 0 };
        public static double[] fourOfAKindPolicy = { 1, 0.466, 0.51, 0.01, 0.01, 0.01, 0 };
    }
}
