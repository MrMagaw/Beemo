package pokersquares.evaluations;

import java.util.Map;
import pokersquares.config.Settings;
import pokersquares.environment.Card;
import pokersquares.environment.Board;

public class PatternPolicy {
    private static Map<String, Double> patternEvaluations = new java.util.HashMap();
    //Hand Analysis
    
    
    private static class Info{
        private String pattern;
        private boolean straight;
        private boolean royal;
        private double numCards = 0;
        private double handWeight = 0;
        private double numSuits = 0;
        private int maxRank = 0;
        private double maxOfAKind = 0;
        private int[] suitCounts = new int[Card.NUM_SUITS];
        private int[] rankCounts = new int[Card.NUM_RANKS];
        private int[] rankCountCounts = new int[6];
    }
    
    public static double evaluate(Board grid){
        double evaluation = 0;
        for(int i = 0; i < 5; ++i){
            evaluation += evaluate(grid.getRow(i), false);
            evaluation += evaluate(grid.getColumn(i), true);
        }
        return evaluation;
    }
    
    public static double evaluate(Card[] hand, boolean col) {
        Info info = new Info();
        
        analyzeHand(info, hand);
        buildPattern(info, hand, col);
        
        double evaluation;
        
        if (patternEvaluations.containsKey(info.pattern)) evaluation = patternEvaluations.get(info.pattern);
        else evaluation = scoreHand(info, hand, col);
        
        return evaluation;
    }
    
    private static void buildPattern(Info info, Card[] hand, boolean col) {
        //PATTERN NOTE 
        //Current Pattern builds based on the assumption that 
        //only suits are dealt with in columns 
        //and only ranks are dealt with in rows 
        
        //if col build suit pattern
        if (col) {
            if (info.numCards > 0)
                if (info.numSuits == 1)
                    if (info.numCards == 1) info.pattern = info.pattern + "a";
                    else if (info.numCards == 2) info.pattern = info.pattern + "aa";
                    else if (info.numCards == 3) info.pattern = info.pattern + "aaa";
                    else if (info.numCards == 4) info.pattern = info.pattern + "aaaa";
                    else if (info.numCards == 5) info.pattern = info.pattern + "aaaaa";
        }
        //if row build rank pattern
        else {
            
            String [] nums = {"1", "2", "3", "4", "5" };
            int iNums = 0;
            
            //Iterates through num CountCounts, 
            //recording a rank identifier for each rank
            
            if (info.numCards > 0)
                //for the maximum number of rank multiples
                for (int i = 5; i > 0; i--) {
                    //for each rank multiple occuring at least once
                    //System.out.println(pattern + " " + rankCountCounts[i] + " " + nums[iNums] + "HERE");
                    if (info.rankCountCounts[i] > 0) {
                        //for each occurance of a rank multiple
                        for (int j = 0; j < info.rankCountCounts[i]; j++) {
                            for (int k = 0; k < i; k++) {
                                info.pattern = info.pattern + nums[iNums];
                            }
                            iNums++;
                        }
                    }
                }
                
        }
        
    }
    
    private static double scoreHand(Info info, Card[] hand, boolean col) {
        //double[] handScores = {0,0,0,0,0,0,0,0,0}; //records a score for each poker hand from pair [0] to royal flush [8]
        double handScore = 0, evaluation;
        //Policy Scores should relate to probability, 
        //They are currently assigned by intuition,
        //The probability should be calculated or else learned 
        
        if(!col){
            //if (!col) handScores[0] = 2 * Math.pow(scorePairPolicy(hand, n), Settings.Evaluations.pairExp);
            evaluation = 5 * Math.pow(scoreTwoPairPolicy(info, hand), Settings.Evaluations.twoPairExp);
            handScore = 10 * Math.pow(scoreThreeOfAKindPolicy(info, hand), Settings.Evaluations.threeOfAKindExp);
            evaluation = evaluation < handScore ? handScore : evaluation;
            //handScores[3] = 15 * Math.pow(scoreStraightPolicy(), Settings.Evaluations.straightExp);
            handScore = 25 * Math.pow(scoreFullHousePolicy(info, hand), Settings.Evaluations.fullHouseExp);
            evaluation = evaluation < handScore ? handScore : evaluation;
            handScore = 50 * Math.pow(scoreFourOfAKindPolicy(info, hand), Settings.Evaluations.fourOfAKindExp);
            evaluation = evaluation < handScore ? handScore : evaluation;
        }else{
            evaluation = 20 * Math.pow(scoreFlushPolicy(info, hand), Settings.Evaluations.flushExp);
        }
        
        if (info.pattern != null) patternEvaluations.put(info.pattern, evaluation);
        
        return evaluation;
    }
    
    private static double scorePairPolicy(Info info, Card hand[]) {
        double pairScore = 0;
        
        //if there is a pair
        if (info.rankCountCounts[2] == 1) pairScore = 1;
        //if there is a possibility of a pair
        else if (info.numCards < 5) pairScore = 0.1;
        
        return pairScore;
    }
    
    private static double scoreTwoPairPolicy(Info info, Card hand[]) {
        double twoPairScore = 0;
        
        //if there is a two pair
        if (info.rankCountCounts[2] == 2) twoPairScore = 1;
        //if there is the possibility of a two pair
        else if ((info.rankCountCounts[2] == 1) && (info.numCards < 5))twoPairScore = 0.5;
        else if (info.numCards < 4) twoPairScore = 0.1;
        //if the hand has no chance of a two pair
        else if (info.numCards > 3) twoPairScore = -0.1;
        
        return twoPairScore;
    }
    
    private static double scoreThreeOfAKindPolicy(Info info, Card hand[]) {
        double threeOfAKindScore = 0;
        
        //if there is a three of a kind
        if (info.rankCountCounts[3] == 1) threeOfAKindScore = 1;
        //if there is the possibility of a three of a kind
        else if ((info.rankCountCounts[2] == 1) && (info.numCards < 5)) threeOfAKindScore = 0.7 - Math.pow((info.numCards/10),2);
        else if (info.numCards < 4) threeOfAKindScore = 0.5 - Math.pow((info.numCards/10),31);
        
        //System.out.println(numCards + " " + n + " " + threeOfAKindScore);
        
        return threeOfAKindScore;
    }
    
    private static double scoreFlushPolicy(Info info, Card hand[]) {
        double flushScore = 0;
        
        //this still works amazingly but I dont know 
        if ((info.numSuits == 1) && (info.numCards <= 5)) 
            flushScore = ((double)Math.pow(info.handWeight, 1/2))*info.handWeight*info.handWeight;
        
        return flushScore;
    }
    
    private static double scoreFullHousePolicy(Info info, Card hand[]) {
        double fullHouseScore = 0;
        
        if ((info.rankCountCounts[3] == 1) && (info.rankCountCounts[2] == 1)) fullHouseScore = 1;
        //if there is the possibility of a Full House
        else if ((info.rankCountCounts[2] == 2) && (info.numCards == 4)) fullHouseScore = 0.7;
        else if ((info.rankCountCounts[3] == 1) && (info.numCards == 4)) fullHouseScore = 0.7;
        else if ((info.rankCountCounts[3] == 1) && (info.numCards == 3)) fullHouseScore = 0.8;
        else if ((info.rankCountCounts[2] == 1) && (info.numCards == 3)) fullHouseScore = 0.55;
        else if ((info.rankCountCounts[2] == 1) && (info.numCards == 2)) fullHouseScore = 0.6;
        else if (info.numCards == 2) fullHouseScore = 0.4;
        else if (info.numCards == 1) fullHouseScore = 0.4;
        else fullHouseScore = 0.0;
        
        return fullHouseScore;
    }
    
    private static double scoreFourOfAKindPolicy(Info info, Card hand[]) {
        double fourOfAKindScore = 0;
        
        if (info.rankCountCounts[4] == 1) fourOfAKindScore = 1;
        //if there is a possibility of a four of a kind
        else if ((info.rankCountCounts[3] == 1) && (info.numCards == 4)) fourOfAKindScore = 0.466;
        else if ((info.rankCountCounts[3] == 1) && (info.numCards == 3)) fourOfAKindScore = 0.51;
        else if ((info.rankCountCounts[2] == 1) && (info.numCards == 3)) fourOfAKindScore = 0.01;
        else if ((info.rankCountCounts[2] == 1) && (info.numCards == 2)) fourOfAKindScore = 0.01;
        else if ((info.numCards == 1)) fourOfAKindScore = 0.01;
        else fourOfAKindScore = 0.0;
        
        return fourOfAKindScore;
    }
    
    private static double scoreStraightFlushPolicy(Info info, Card hand[]) {
        double straightFlushScore = 0;
        
        //if there is a StraightFlush
        if (info.straight && (info.numSuits == 1) && (info.numCards == 5)) straightFlushScore = 1;
        
        return straightFlushScore;
    }
    
    private static double scoreRoyalFlushPolicy(Info info, Card hand[]) {
        double royalFlushScore = 0;
        
        //if there is a StraightFlush
        if (info.royal && (info.numSuits == 1) && (info.numCards == 5)) royalFlushScore = 1;
        
        return royalFlushScore;
    }
    
    private static void analyzeHand(Info info, Card[] hand) {
        //TRY to combine all for loops
        
        //Count suits, ranks, cards
        for (Card card : hand) {
            if (card != null) {
		++info.suitCounts[card.getSuit()];
                ++info.rankCounts[card.getRank()];
                ++info.numCards;
                //suitRep = card;
            }
        }
        
        //Count number of ranks occuring multiple times,
        //number of ranks, suits
	for (int i = 0; i < Card.NUM_RANKS; ++i) {
            ++info.rankCountCounts[info.rankCounts[i]];
            if (info.rankCounts[i] > info.maxOfAKind) {
                info.maxOfAKind = info.rankCounts[i];
                info.maxRank = i;
            }
            
            //if (rankCounts[i] > 0) {
                //numRanks ++;
            //}
            
            if (i < Card.NUM_SUITS) {
                if (info.suitCounts[i] > 0) {
                    ++info.numSuits;
                }
            }
        }
        
        //STRAIGHT AND ROYAL CHECKING 
        /*
        double nullCards = SIZE - numCards;
        int lowestRankedCard = rankCounts[0];
        int highestRankedCard = rankCounts[0];
	for (int i = 0; i < Card.NUM_RANKS; ++i) {
            if (rankCounts[i] > highestRankedCard) highestRankedCard = rankCounts[i];
            if (rankCounts[i] < lowestRankedCard) lowestRankedCard = rankCounts[i];
        }
        
        //check partial straight
        if ((rankCountCounts[4] == 0) && (rankCountCounts[3] == 0) && (rankCountCounts[2] == 0) //if there are no pairs
                && (lowestRankedCard <= (Card.NUM_RANKS-(4-nullCards)))
                && (highestRankedCard <= (lowestRankedCard + 4))){
            straight = true;
            for (int i = 0; i < Card.NUM_RANKS; i++) {
                if ((rankCounts[i] == 1) && !((i <= highestRankedCard) && (i >= lowestRankedCard))){
                    straight = false;
                }
            }
        }
        
        if (rankCounts[0] == 1 && rankCounts[12] == 1 && rankCounts[11] == 1 && rankCounts[10] == 1 && rankCounts[9] == 1) {
            if (numCards == 5) straight = royal = true;
        }
        */
        
        info.handWeight = info.numCards / 5;
    }
}
