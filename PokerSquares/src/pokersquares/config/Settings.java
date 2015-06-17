package pokersquares.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import pokersquares.algorithms.*;
import pokersquares.environment.*;
import pokersquares.players.BeemoV2;
import pokersquares.trainers.Billy;
import pokersquares.trainers.Prismo;
import pokersquares.trainers.Trainer;

public class Settings {
    //Holds all CONSTANTS for all classes for easy reference and tweaking
    public enum PointSystem {
        RANDOM(true), AMERICAN(false), AMERITISH(true), 
        BRITISH(false), HYPERCORNER(true), SINGLEHAND(true), CUSTOM(false);
        private final boolean isRandom;
        private PointSystem(boolean isRandom){
            this.isRandom = isRandom;
        }
        public boolean isRandom(){return isRandom;} 
   };
    
    public static class Main {
        public static int games = 10;
        public static int seed = 3;
        public static boolean verbose = false;
        public static boolean tournament = false;
        public static boolean test = false;
        public static long randomPointSystemSeed = 3L;  //1123 : High Card
        public static PointSystem pointSystem = PointSystem.BRITISH;
        public static int[] customPointSystem = new int[] {-128, 11, -126, 118, 16, -102, -64, 32, -55, -23};
    }
    
    public static class Environment {
        public static PokerSquaresPointSystem system;
    }
    
    public static class Algorithms {
        public static Algorithm simAlgorithm = new OBF();
        public static Algorithm[] algorithm = 
                new Algorithm[] {new OBF(), new IIMC(), new OBF()};
        
        //MC
        public static int simSampleSize = 1000;
        
        //UCT
        public static double UCT = 2.5;
        public static boolean debugUCT = true;
    }
    
    public static class Greedy {
        public static double timeRatio = 1; //ratio of the total alloted time the player will play
    }
    
    public static class BMO {
        public static int[] turnSplits = new int[]{4, 25, 25};
        public static BeemoV2 BMO;
        
        //SETTINGS
        public static boolean readPatterns = false;
        public static boolean debugPatterns = false;
        public static String patternsFileIn = 
                Main.pointSystem.name() + 
                (Main.pointSystem.isRandom() ? Main.randomPointSystemSeed : "") + 
                ".pattern";
    }
    
    public static class Training {
        public static boolean train = false;
        public static boolean inheritTrainMillis = false;
        public static long millis = 20000;
        public static boolean verbose = false; 
        
        public static double policyMax = 1;
        public static double policyMin = -1;
        public static boolean randomize = false;
        public static String patternsFileOut = 
                Main.pointSystem.name() + 
                (Main.pointSystem.isRandom() ? Main.randomPointSystemSeed : "") + 
                ".pattern";
        
        public static Trainer trainer = new Billy();
        //public static Trainer trainer = new Prismo();
    }
    
    public static class Evaluations {
        public static int numThreads = 16;
        
        //Optimality Testing
        public static boolean testOptimality = false;
        
        //Pattern Policy
        public static boolean patternate = true;
        
        public static double[] handScores = new double[10];
        
        
        public static void debug () {
            System.err.println("Settings");
            System.err.println("Hand Scores: " + Arrays.toString(handScores));
        }
    }
    
}
