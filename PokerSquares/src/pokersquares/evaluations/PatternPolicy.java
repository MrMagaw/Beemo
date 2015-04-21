package pokersquares.evaluations;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import pokersquares.config.Settings;
import static pokersquares.config.Settings.Evaluations.colHands;
import static pokersquares.config.Settings.Evaluations.patternate;
import static pokersquares.config.Settings.Evaluations.rowHands;
import static pokersquares.config.Settings.Evaluations.simpleScoring;
import pokersquares.environment.Board;
import pokersquares.environment.Card;
import pokersquares.environment.Hand;

public class PatternPolicy {
    public static Map<Integer, Double> patternEvaluations = new java.util.HashMap();

    public PatternPolicy () {
        
    }
    
    public static double evaluate(Board board){
        double evaluation = 0;
        for(int i = 0; i < 5; ++i){
            evaluation += evaluate(board.hands.get(i));
            evaluation += evaluate(board.hands.get(i+5));
        }
        return evaluation;
    }
    
    public static double evaluate(Hand hand) {
        if(!hand.hasPattern() && patternate){
            buildPattern(hand);
        }
        
        if (patternEvaluations.containsKey(hand.getPattern()))
            return patternEvaluations.get(hand.getPattern());
        else
            return 0;
    }
    
    public static void buildPattern(Hand hand) {
        //[isCol][hasStraight][flushCapable][3xnumOfHighCards][2xnumOfPairs][numOfThreeOfAKind][numOfFourOfAKind]
        //10 bits / 32 bits
        
        //Merge Suits left into flushCapable?
        
        //[2x primary rank remain][2x secondary rank][2xcards in suit left]
        //0->Not possible
        //1->Barely possible
        //2->Very possible
            
        //buildRankCounts()
        hand.rankCountCounts = new int[6];
        for(int i = 0; i < Card.NUM_RANKS; ++i)
            ++hand.rankCountCounts[hand.rankCounts[i]];
        
        int pattern = (hand.isCol ? 4 : 0);
        pattern += (hand.hasStraight) ? 2 : 0;
        pattern += (hand.numSuits <= 1 ? 1 : 0);
        pattern <<= 3;
        pattern += hand.rankCountCounts[1];
        pattern <<= 3;
        pattern += hand.rankCountCounts[2];
        pattern <<= 2;
        pattern += hand.rankCountCounts[3];
        pattern <<= 1;
        pattern += hand.rankCountCounts[4];
        
        pattern <<= 2;
        
        int suitPattern = (hand.numSuits == 1) ? -1 : 0;
        
        int usedRank;
        if(hand.numRanks == 1){
            usedRank = Integer.MAX_VALUE;
            pattern <<= 2;
        }else{
            usedRank = (hand.numRanks == 2) ? -1 : Integer.MAX_VALUE;
        }
        
        for(Card c : hand.getCards()){
            if(c != null){
                if(usedRank == c.getRank()) continue;
                if(suitPattern == -1){
                    int numLeft = hand.getBoard().suitsLeft(c.getSuit());
                    int needed = hand.numOpenPos();
                    pattern += (numLeft >= needed) ? ((numLeft >= (needed << 2)) ? 2 : 1) : 0;
                }
                pattern += hand.getBoard().ranksLeft(c.getRank());
                if(usedRank != -1) break;
                pattern <<= 2;
                usedRank = c.getRank();
            }
        }
        
        pattern <<= 2;
        
        pattern += suitPattern;
        
        hand.setPattern(pattern);
    }
    
    public static String decodePattern(int p) {
        
        String patternCode;
        
        //Flags are stored in reverse order
        int mask = 1 << 14;
        int isCol = (p & mask) >> 14; 
        mask = 1 << 13;
        int hasStraight = (p & mask) >> 13;
        mask = 1 << 12;
        int flushCapable =(p & mask) >> 12;
        
        patternCode = "[" + isCol + "][" + hasStraight + "][" + flushCapable + "]";
        
        for (int i = 0; i < 4; i++) {
            mask = 7 << (3 * (3-i));
            int flag = (p & mask) >> (3 * (3-i));
            patternCode += "[" + flag + "]";
            
        }
        
        return patternCode;
    }
    
    public static void debug() {
        
        System.out.println("\n" + patternEvaluations.size() + " Pattern Evaluations:");
        
        Map <String, Double> sorted = new TreeMap <String, Double> ();
                
        //SORT Patterns for at least a little bit of catagorical order
        for (Integer p : patternEvaluations.keySet()) {
            double val = patternEvaluations.get(p);
            
            String code = decodePattern(p);
            
            sorted.put (code, val);
        }
        
        for (String code : sorted.keySet()) {
            double val = sorted.get(code);
            
            System.out.println(code + " " + val);
        }
    }
}
