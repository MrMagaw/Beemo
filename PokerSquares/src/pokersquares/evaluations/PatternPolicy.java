package pokersquares.evaluations;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import pokersquares.config.Settings;
import static pokersquares.config.Settings.Evaluations.colHands;
import static pokersquares.config.Settings.Evaluations.rowHands;
import pokersquares.environment.Board;
import pokersquares.environment.Card;
import pokersquares.environment.Hand;

public class PatternPolicy {
    public static Map<String, Double> patternEvaluations = new java.util.HashMap();
    //Hand Analysis
    
    private static class Info{
        private String pattern = "p";
        private boolean straight;
        private boolean royal;
        private double numCards = 0;
        private double handWeight = 0;
        private double numRanks = 0;
        private double numSuits = 0;
        private final int[] ranks = new int[5];
        private final int[] suitCounts = new int[Card.NUM_SUITS];
        private final int[] rankCounts = new int[Card.NUM_RANKS];
        private final int[] rankCountCounts = new int[6];
        
        Info(boolean col) {
            if (col == true) pattern = "c";
            else pattern = "r";
        }
    }
    
    public static double evaluate(Board board){
        double evaluation = 0;
        for(int i = 0; i < 5; ++i){
            evaluation += evaluate(board.hands.get(i), false);
            evaluation += evaluate(board.hands.get(i+5), true);
        }
        return evaluation;
    }
    
    public static double evaluate(Hand hand, boolean col) {
        Info info = new Info(col);
        
        analyzeHand(info, hand.cards, col);
        buildPattern(info, hand, col);
        
        double evaluation;
        
        if (patternEvaluations.containsKey(info.pattern)) 
            evaluation = patternEvaluations.get(info.pattern);
        else 
            evaluation = scoreHand(info, hand.cards, col);
        
        hand.evaluation = evaluation;
        
        return evaluation;
    }
    
    public static String patternate(Hand hand, boolean col) {
        Info info = new Info(col);
         
        //PROCESS hand
        analyzeHand(info, hand.cards, col);
        buildPattern(info, hand, col);
        
        return info.pattern;
    }
    
    private static String buildPattern(Info info, Hand hand, boolean col) {
        //BUILD Pattern
        
        if (Settings.Algorithms.positionRankEnabled) patternB(info, hand, col);
        else {
            if (Settings.Evaluations.pattern == "A") patternA(info, hand, col);
            else if (Settings.Evaluations.pattern == "B") patternB(info, hand, col);
        }
        
        //RECORD Pattern
        hand.pattern = info.pattern;
        
        return info.pattern;
        
    }
    
    private static String patternA(Info info, Hand hand, boolean col) {
        //PATTERN NOTE 
        //Current Pattern builds based on the assumption that 
        //only suits are dealt with in columns 
        //and only ranks are dealt with in rows 
        
        //if col build suit pattern
        if (col) {
            if (info.numSuits == 1) {
                if (info.numCards == 1) info.pattern = info.pattern + "a";
                else if (info.numCards == 2) info.pattern = info.pattern + "aa";
                else if (info.numCards == 3) info.pattern = info.pattern + "aaa";
                else if (info.numCards == 4) info.pattern = info.pattern + "aaaa";
                else info.pattern = info.pattern + "aaaaa";
            }
            else if (info.numSuits > 1) {
                if (info.numCards == 1) info.pattern = info.pattern + "x";
                else if (info.numCards == 2) info.pattern = info.pattern + "xx";
                else if (info.numCards == 3) info.pattern = info.pattern + "xxx";
                else if (info.numCards == 4) info.pattern = info.pattern + "xxxx";
                else info.pattern = info.pattern + "xxxxx";
            }
        }
        //if row build rank pattern
        else {
            
            String [] nums = {"1", "2", "3", "4", "5" };
            int iNums = 0;
            
            //Iterates through num CountCounts, 
            //recording a rank identifier for each rank
            
            if (info.numCards > 0)
                //for the maximum number of rank multiples
                for (int i = 5; i > 0; --i) {
                    //for each rank multiple occuring at least once
                    //System.out.println(pattern + " " + rankCountCounts[i] + " " + nums[iNums] + "HERE");
                    if (info.rankCountCounts[i] > 0) {
                        //for each occurance of a rank multiple
                        for (int j = 0; j < info.rankCountCounts[i]; ++j) {
                            for (int k = 0; k < i; ++k) {
                                info.pattern += nums[iNums];
                            }
                            ++iNums;
                        }
                    }
                }
                
        }
        return info.pattern;
    }
    
    private static String patternB(Info info, Hand hand, boolean col) {
        //Build row pattern from hand rank and suit information
        
        if (col) {
            if (info.numSuits == 1) {
                for (Card card : hand.cards) {
                    if (card != null) info.pattern += card.getSuit();
                }
            }
            else if (info.numSuits > 1) {
                if (info.numCards == 1) info.pattern = info.pattern + "x";
                else if (info.numCards == 2) info.pattern = info.pattern + "xx";
                else if (info.numCards == 3) info.pattern = info.pattern + "xxx";
                else if (info.numCards == 4) info.pattern = info.pattern + "xxxx";
                else info.pattern = info.pattern + "xxxxx";
            }
        }
        else {
            
            int iRanks = 0;
            
            //Iterates through num CountCounts, 
            //recording a rank identifier for each rank
            
            if (info.numCards > 0)
                //for the maximum number of rank multiples
                for (int i = 5; i > 0; --i) {
                    //for each rank multiple occuring at least once
                    //System.out.println(pattern + " " + rankCountCounts[i] + " " + nums[iNums] + "HERE");
                    if (info.rankCountCounts[i] > 0) {
                        //for each occurance of a rank multiple
                        for (int j = 0; j < info.rankCountCounts[i]; ++j) {
                            for (int k = 0; k < i; ++k) {
                                info.pattern += info.ranks[iRanks];
                                info.pattern += "-";
                            }
                            ++iRanks;
                        }
                    }
                }
                
        }
        
        return info.pattern;
    }
    
    private static double scoreHand(Info info, Card[] hand, boolean col) {
        
        //Policy Scores should relate to probability, 
        //They are currently assigned by gut,
        //The probability should be calculated or else learned 
        
        double temp = Double.NEGATIVE_INFINITY;
        double handScore = 0;
        
        if (col) {
            for (int i = 0; i < colHands.length; ++i) {
                if (colHands[i] == 1) {
                    temp = selectScorePolicy(info, hand, (int) i);
                    handScore += temp;
                    //if (temp > handScore) handScore = temp;
                }
                
            }
        }
        else {
            for (int i = 0; i < rowHands.length; ++i) {
                if (rowHands[i] == 1) {
                    temp = selectScorePolicy(info, hand, (int) i);
                    handScore += temp;
                    //if (temp > handScore) handScore = temp;
                }
            }
        }
        
        if (!info.pattern.equals("p")) patternEvaluations.put(info.pattern, handScore);
        
        return handScore;
    }
    
    private static double selectScorePolicy(Info info, Card hand[], int i) {
        double handScore = Double.NEGATIVE_INFINITY;
            
        if (i == 0) handScore = Settings.Evaluations.handScores[0] * Math.pow(scoreHighCardPolicy(info, hand), Settings.Evaluations.exps[0]);
        else if (i == 1) handScore = Settings.Evaluations.handScores[1] * Math.pow(scorePairPolicy(info, hand), Settings.Evaluations.exps[1]);
        else if (i == 2) handScore = Settings.Evaluations.handScores[2] * Math.pow(scoreTwoPairPolicy(info, hand), Settings.Evaluations.exps[2]);
        else if (i == 3) handScore = Settings.Evaluations.handScores[3] * Math.pow(scoreThreeOfAKindPolicy(info, hand), Settings.Evaluations.exps[3]);
        else if (i == 4) handScore = Settings.Evaluations.handScores[4] * Math.pow(scoreStraightPolicy(info, hand), Settings.Evaluations.exps[4]);
        else if (i == 5) handScore = Settings.Evaluations.handScores[5] * Math.pow(scoreFlushPolicy(info, hand), Settings.Evaluations.exps[5]);
        else if (i == 6) handScore = Settings.Evaluations.handScores[6] * Math.pow(scoreFullHousePolicy(info, hand), Settings.Evaluations.exps[6]);
        else if (i == 7) handScore = Settings.Evaluations.handScores[7] * Math.pow(scoreFourOfAKindPolicy(info, hand), Settings.Evaluations.exps[7]);
        else if (i == 8) handScore = Settings.Evaluations.handScores[8] * Math.pow(scoreStraightFlushPolicy(info, hand), Settings.Evaluations.exps[8]);
        else if (i == 9) handScore = Settings.Evaluations.handScores[9] * Math.pow(scoreRoyalFlushPolicy(info, hand), Settings.Evaluations.exps[9]);
        
        return handScore;
    }
    
    private static double scoreHighCardPolicy(Info info, Card hand[]) {
        double highCardScore = 0;
        
        if ((info.numCards == info.numRanks) && (info.numCards == 5)) highCardScore = Settings.Evaluations.highCardPolicy[0];
        else if ((info.numCards == info.numRanks) && (info.numCards == 4)) highCardScore = Settings.Evaluations.highCardPolicy[1];
        else if ((info.numCards == info.numRanks) && (info.numCards == 3)) highCardScore = Settings.Evaluations.highCardPolicy[2];
        else if ((info.numCards == info.numRanks) && (info.numCards == 2)) highCardScore = Settings.Evaluations.highCardPolicy[3];
        else if ((info.numCards == info.numRanks) && (info.numCards == 1)) highCardScore = Settings.Evaluations.highCardPolicy[4];
        
        return highCardScore;
    }
    
    private static double scorePairPolicy(Info info, Card hand[]) {
        double pairScore = 0;
        
        //if there is a pair
        if (info.rankCountCounts[2] == 1) pairScore = Settings.Evaluations.pairPolicy[0];
        //if there is a possibility of a pair
        else if ((info.numCards == info.numRanks) && (info.numCards == 4)) pairScore = Settings.Evaluations.pairPolicy[1];
        else if ((info.numCards == info.numRanks) && (info.numCards == 3)) pairScore = Settings.Evaluations.pairPolicy[2];
        else if ((info.numCards == info.numRanks) && (info.numCards == 2)) pairScore = Settings.Evaluations.pairPolicy[3];
        else if ((info.numCards == info.numRanks) && (info.numCards == 1)) pairScore = Settings.Evaluations.pairPolicy[4];
        //if there is no possibility of a pair
        else pairScore = Settings.Evaluations.pairPolicy[5];
        
        return pairScore;
    }
    
    private static double scoreTwoPairPolicy(Info info, Card hand[]) {
        double twoPairScore = 0;
        
        //if there is a two pair
        if (info.rankCountCounts[2] == 2) twoPairScore = Settings.Evaluations.twoPairPolicy[0];
        //if there is the possibility of a two pair
        else if ((info.rankCountCounts[2] == 1) && (info.numCards < 5)) twoPairScore = Settings.Evaluations.twoPairPolicy[1];
        else if (info.numCards < 4) twoPairScore = Settings.Evaluations.twoPairPolicy[2];
        //if the hand has no chance of a two pair
        else if (info.numCards > 3) twoPairScore = Settings.Evaluations.twoPairPolicy[3];
        
        return twoPairScore;
    }
    
    private static double scoreThreeOfAKindPolicy(Info info, Card hand[]) {
        double threeOfAKindScore = 0;
        
        //if there is a three of a kind
        if (info.rankCountCounts[3] == 1) threeOfAKindScore = Settings.Evaluations.threeOfAKindPolicy[0];
        //if there is the possibility of a three of a kind
        else if ((info.rankCountCounts[2] == 1) && (info.numCards == 2)) threeOfAKindScore = Settings.Evaluations.threeOfAKindPolicy[1];
        else if ((info.rankCountCounts[2] == 1) && (info.numCards == 3)) threeOfAKindScore = Settings.Evaluations.threeOfAKindPolicy[2];
        else if ((info.rankCountCounts[2] == 1) && (info.numCards == 4)) threeOfAKindScore = Settings.Evaluations.threeOfAKindPolicy[3];
        else if ((info.numCards == info.numRanks) && (info.numCards == 1)) threeOfAKindScore = Settings.Evaluations.threeOfAKindPolicy[4];
        else if ((info.numCards == info.numRanks) && (info.numCards == 2)) threeOfAKindScore = Settings.Evaluations.threeOfAKindPolicy[5];
        else if ((info.numCards == info.numRanks) && (info.numCards == 3)) threeOfAKindScore = Settings.Evaluations.threeOfAKindPolicy[6];
        else threeOfAKindScore = Settings.Evaluations.threeOfAKindPolicy[7];
        
        return threeOfAKindScore;
    }
    
    private static double scoreStraightPolicy(Info info, Card hand[]) {
        return 0;
    }
    
    private static double scoreFlushPolicy(Info info, Card hand[]) {
        double flushScore = 0;
        
        if ((info.numSuits == 1) && (info.numCards == 5)) flushScore = Settings.Evaluations.flushPolicy[0];
        else if ((info.numSuits == 1) && (info.numCards == 4)) flushScore = Settings.Evaluations.flushPolicy[1];
        else if ((info.numSuits == 1) && (info.numCards == 3)) flushScore = Settings.Evaluations.flushPolicy[2];
        else if ((info.numSuits == 1) && (info.numCards == 2)) flushScore = Settings.Evaluations.flushPolicy[3];
        else if ((info.numSuits == 1) && (info.numCards == 1)) flushScore = Settings.Evaluations.flushPolicy[4];
        else if (info.numSuits > 1) flushScore = Settings.Evaluations.flushPolicy[5];
        
        return flushScore;
    }
    
    private static double scoreFullHousePolicy(Info info, Card hand[]) {
        double fullHouseScore = 0;
        
        if ((info.rankCountCounts[3] == 1) && (info.rankCountCounts[2] == 1)) fullHouseScore = Settings.Evaluations.fullHousePolicy[0];
        //if there is the possibility of a Full House
        else if ((info.rankCountCounts[2] == 2) && (info.numCards == 4)) fullHouseScore = Settings.Evaluations.fullHousePolicy[1];
        else if ((info.rankCountCounts[3] == 1) && (info.numCards == 4)) fullHouseScore = Settings.Evaluations.fullHousePolicy[2];
        else if ((info.rankCountCounts[3] == 1) && (info.numCards == 3)) fullHouseScore = Settings.Evaluations.fullHousePolicy[3];
        else if ((info.rankCountCounts[2] == 1) && (info.numCards == 3)) fullHouseScore = Settings.Evaluations.fullHousePolicy[4];
        else if ((info.rankCountCounts[2] == 1) && (info.numCards == 2)) fullHouseScore = Settings.Evaluations.fullHousePolicy[5];
        else if (info.numCards == 2) fullHouseScore = Settings.Evaluations.fullHousePolicy[6];
        else if (info.numCards == 1) fullHouseScore = Settings.Evaluations.fullHousePolicy[7];
        else fullHouseScore = Settings.Evaluations.fullHousePolicy[8];
        
        return fullHouseScore;
    }
    
    private static double scoreFourOfAKindPolicy(Info info, Card hand[]) {
        double fourOfAKindScore = 0;
        
        if (info.rankCountCounts[4] == 1) fourOfAKindScore = Settings.Evaluations.fourOfAKindPolicy[0];
        //if there is a possibility of a four of a kind
        else if ((info.rankCountCounts[3] == 1) && (info.numCards == 4)) fourOfAKindScore = Settings.Evaluations.fourOfAKindPolicy[1];
        else if ((info.rankCountCounts[3] == 1) && (info.numCards == 3)) fourOfAKindScore = Settings.Evaluations.fourOfAKindPolicy[2];
        else if ((info.rankCountCounts[2] == 1) && (info.numCards == 3)) fourOfAKindScore = Settings.Evaluations.fourOfAKindPolicy[3];
        else if ((info.rankCountCounts[2] == 1) && (info.numCards == 2)) fourOfAKindScore = Settings.Evaluations.fourOfAKindPolicy[4];
        else if ((info.numCards == 1)) fourOfAKindScore = Settings.Evaluations.fourOfAKindPolicy[5];
        else fourOfAKindScore = Settings.Evaluations.fourOfAKindPolicy[6];
        
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
    
    private static void analyzeHand(Info info, Card[] hand, boolean col) {
        
        SortedMap <Integer, LinkedList> sortedRankCounts = new TreeMap ();
        
        int ii = 0;
        for (Card card : hand) {
            if (card != null) {
		++info.suitCounts[card.getSuit()];
                ++info.rankCounts[card.getRank()];
                ++info.numCards;
            }
            
            if (!col && Settings.Algorithms.positionRankEnabled) sortedRankCounts.put(ii, new LinkedList <Integer>());
            ++ii;
        }
        
	for (int i = 0; i < Card.NUM_RANKS; ++i) {
            ++info.rankCountCounts[info.rankCounts[i]];
            
            if (info.rankCounts[i] >= 1) ++info.numRanks;
            if (i < Card.NUM_SUITS) {
                if (info.suitCounts[i] > 0) {
                    ++info.numSuits;
                }
            }
            
            if (!col && Settings.Algorithms.positionRankEnabled) if (info.rankCounts[i] > 0) sortedRankCounts.get(4 - info.rankCounts[i]).add(i);
        }
        
        if (!col && Settings.Algorithms.positionRankEnabled) {
            int i = 0;
            for (LinkedList <Integer> rankList : sortedRankCounts.values()) {
                Collections.sort(rankList);
                for (Integer rank : rankList) {
                    info.ranks[i] = rank;
                    ++i;
                }
            }
        }
        
        //DEBUG ranks
        /*
        //System.out.print("\n");
        for(Card card : hand) {
            if (card == null) System.out.print("--");
            else System.out.print(card.toString());
        }
        System.out.print(" / ");
        for(int j = 0; j < 5; ++j) {
            System.out.print(info.ranks[j]);
        }
        System.out.print("\n");
        */
        
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
