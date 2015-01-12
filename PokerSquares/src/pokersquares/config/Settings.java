package pokersquares.config;

import pokersquares.algorithms.*;

public class Settings {
    public static class Algorithms {
        public static int searchDepth = 2;
        public static int simulationSampleSize = 10;
        public static int playSampleSize = 52;
        public static int deckSampleMax = 52;
        
        public static Algorithm simAlgoritm = new GRB();
        private static Algorithm[] algorithm = new Algorithm[] {new IIMC()};
        
        
        
        
        public static Algorithm get(int pos){
            return algorithm[pos];
        }
    }
    public static class BMO {
        public static int[] turnSplits = new int[]{100000000};
    }
    public static class Evaluations {
        public static double pairExp = 1;
        public static double twoPairExp = 1;
        public static double threeOfAKindExp = 1;
        public static double fullHouseExp = 1.2;
        public static double fourOfAKindExp = 1.18;
        public static double flushExp = 0.95;
        public static double straightExp = 0.647;
        public static double straightFlushExp = 1.1;
        public static double royalFlushExp = 1.1;
        
    }
}
