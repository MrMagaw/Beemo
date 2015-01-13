/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pokersquares.evaluations;

import pokersquares.Card;
import pokersquares.PokerSquares;
import pokersquares.players.Beemo;
import java.util.*;
import static pokersquares.PokerSquares.SIZE;

/**
 *
 * @author newuser
 */
public class PatternPolicy {
    //Beemo
    Beemo BMO;
    
    //Policy Exponents
    private double p = 1; //pair
    private double tp = 1; //two pair (optimal at 1)
    private double t = 1; //three of a kind (optimal at 1) 2.2
    private double s = 0.647; //straight
    private double f = 0.95; //flush (optimal at 0.95) 0.8
    private double fh = 1.2; //row full house
    private double fk = 1.18; //four of a kind
    private double sf = 1.1; //straight flush
    private double rf = 1.1; //royal flush
    
    //Hand Analysis
    private String pattern;
    private boolean straight = false;
    private boolean royal = false;
    private double numCards = 0;
    private double handWeight = 0;
    private double numSuits = 0;
    private double numRanks = 0;
    private int maxRank = 0;
    private double maxOfAKind = 0;
    private int[] suitCounts = new int[Card.NUM_SUITS];
    private int[] rankCounts = new int[Card.NUM_RANKS];
    private int[] rankCountCounts = new int[PokerSquares.SIZE + 1];
    private Card suitRep;
    
    
    //Evaluation
    double evaluation = 0;
    
    public double evaluate(Beemo BMO, Card[] hand, int n, boolean col) {
        
        //n is the respective number of the hand, then nth col or row
        
        //An Evaluation based on the genetic combination of weighted policies
        
        //Genetic Policy Scores
        //plays positions based on the weighted influences of hand policies
        //policy scores reflect the extent to which a policy is satisfied and the worth of a policy
        
        this.BMO = BMO;
        
        //ANALYZE 
        analyzeHand(hand);
        
        //PATTERNATE
        buildPattern(hand, col);
        
        //EVALUATE
        //if pattern scores contains the current hand's pattern
        if (BMO.containsPattern((String) pattern)) getPatternEvaluation();
        //otherwise score the hand
        else scoreHand(hand, n, col);
        
        if (false) {
            System.out.println("mr " + (maxRank+1));
            PokerSquares.printHand(hand, evaluation);
            System.out.println(pattern);
        }
        
        //Every Position receives a score that is the sum of the scores from each policy
        //The positions that receives the best score from all policies is chosen
        
        return evaluation;
    }
    
    private void buildSimplePattern(Card[] hand, boolean col) {
        //build pattern by sorting string reps of cards in a hand
        
        LinkedList <String> cardStrings = new LinkedList <String> ();
        for (Card card : hand) {
            if (card != null) cardStrings.add(card.toString());
        }
        
        Collections.sort(cardStrings);
        
        if (col) pattern = "col ";
        else pattern = "row ";
        
        for (String strCard : cardStrings) {
            pattern = pattern + strCard;
        }
    }
    
    private void buildPattern(Card[] hand, boolean col) {
        //PATTERN NOTE 
        //Current Pattern builds based on the assumption that 
        //only suits are dealt with in columns 
        //and only ranks are dealt with in rows 
        
        //if col build suit pattern
        if (col) {
            if (numCards > 0)
                if (numSuits == 1)
                    if (numCards == 1) pattern = pattern + "a";
                    else if (numCards == 2) pattern = pattern + "aa";
                    else if (numCards == 3) pattern = pattern + "aaa";
                    else if (numCards == 4) pattern = pattern + "aaaa";
                    else if (numCards == 5) pattern = pattern + "aaaaa";
        }
        //if row build rank pattern
        else {
            
            String [] nums = {"1", "2", "3", "4", "5" };
            int iNums = 0;
            
            //Iterates through num CountCounts, 
            //recording a rank identifier for each rank
            
            if (numCards > 0)
                //for the maximum number of rank multiples
                for (int i = 5; i > 0; i--) {
                    //for each rank multiple occuring at least once
                    //System.out.println(pattern + " " + rankCountCounts[i] + " " + nums[iNums] + "HERE");
                    if (rankCountCounts[i] > 0) {
                        //for each occurance of a rank multiple
                        for (int j = 0; j < rankCountCounts[i]; j++) {
                            for (int k = 0; k < i; k++) {
                                pattern = pattern + nums[iNums];
                            }
                            iNums++;
                        }
                    }
                }
                
        }
        
    }
    
    private void getPatternEvaluation() {
        evaluation = BMO.getPatternEvaluation(pattern);
    }
    
    private void scoreHand(Card[] hand, int n, boolean col) {
        double[] handScores = {0,0,0,0,0,0,0,0,0}; //records a score for each poker hand from pair [0] to royal flush [8]
        
        //Policy Scores should relate to probability, 
        //They are currently assigned by intuition,
        //The probability should be calculated or else learned 
        
        //if (!col) handScores[0] = 2 * Math.pow(scorePairPolicy(hand, n), p);
        if (!col) handScores[1] = 5 * Math.pow(scoreTwoPairPolicy(hand, n), tp);
        if (!col) handScores[2] = 10 * Math.pow(scoreThreeOfAKindPolicy(hand, n), t);
        //if (!col) handScores[3] = 15 * Math.pow(scoreStraightPolicy(), s);
        if (col) handScores[4] = 20 * Math.pow(scoreFlushPolicy(hand, n), f);
        if (!col) handScores[5] = 25 * Math.pow(scoreFullHousePolicy(hand, n), fh);
        if (!col) handScores[6] = 50 * Math.pow(scoreFourOfAKindPolicy(hand, n), fk);
        //handScores[7] = 75 * Math.pow(scoreStraightFlushPolicy(hand, n), sf);
        //handScores[8] = 100 * Math.pow(scoreRoyalFlushPolicy(hand, n), sf);
        
        //the evaluation is the highest policy score
        evaluation = handScores[0];
        for (int i = 0; i < 9; i++) if (handScores[i] > evaluation) evaluation = handScores[i];
        
        //map pattern to evaluation 
        if (pattern != null) BMO.mapPattern(pattern, evaluation);
    }
    
    private double scorePairPolicy(Card hand[], int n) {
        double pairScore = 0;
        
        //if there is a pair
        if (rankCountCounts[2] == 1) pairScore = 1;
        //if there is a possibility of a pair
        else if (numCards < 5) pairScore = 0.1;
        
        return pairScore;
    }
    
    private double scoreTwoPairPolicy(Card hand[], int n) {
        double twoPairScore = 0;
        
        //if there is a two pair
        if (rankCountCounts[2] == 2) twoPairScore = 1;
        //if there is the possibility of a two pair
        else if ((rankCountCounts[2] == 1) && (numCards < 5))twoPairScore = 0.5;
        else if (numCards < 4) twoPairScore = 0.1;
        //if the hand has no chance of a two pair
        else if (numCards > 3) twoPairScore = -0.1;
        
        return twoPairScore;
    }
    
    private double scoreThreeOfAKindPolicy(Card hand[], int n) {
        double threeOfAKindScore = 0;
        
        //if there is a three of a kind
        if (rankCountCounts[3] == 1) threeOfAKindScore = 1;
        //if there is the possibility of a three of a kind
        else if ((rankCountCounts[2] == 1) && (numCards < 5)) threeOfAKindScore = 0.7 - Math.pow((numCards/10),2);
        else if (numCards < 4) threeOfAKindScore = 0.5 - Math.pow((numCards/10),31);
        
        //System.out.println(numCards + " " + n + " " + threeOfAKindScore);
        
        return threeOfAKindScore;
    }
    
    private double scoreStraightPolicy() {
        double straightScore = 0;
        
        if (straight) straightScore = numCards / 5;
        
        return straightScore;
    }
    
    private double scoreFlushPolicy(Card hand[], int n) {
        double flushScore = 0;
        
        //this still works amazingly but I dont know 
        if ((numSuits == 1) && (numCards <= 5)) flushScore = ((double)Math.pow(handWeight, 1/2))*handWeight*handWeight;
        
        return flushScore;
    }
    
    private double scoreFullHousePolicy(Card hand[], int n) {
        double fullHouseScore = 0;
        
        if ((rankCountCounts[3] == 1) && (rankCountCounts[2] == 1)) fullHouseScore = 1;
        //if there is the possibility of a Full House
        else if ((rankCountCounts[2] == 2) && (numCards == 4)) fullHouseScore = 0.7;
        else if ((rankCountCounts[3] == 1) && (numCards == 4)) fullHouseScore = 0.7;
        else if ((rankCountCounts[3] == 1) && (numCards == 3)) fullHouseScore = 0.8;
        else if ((rankCountCounts[2] == 1) && (numCards == 3)) fullHouseScore = 0.55;
        else if ((rankCountCounts[2] == 1) && (numCards == 2)) fullHouseScore = 0.6;
        else if (numCards == 2) fullHouseScore = 0.4;
        else if (numCards == 1) fullHouseScore = 0.4;
        else fullHouseScore = 0.0;
        
        return fullHouseScore;
    }
    
    private double scoreFourOfAKindPolicy(Card hand[], int n) {
        double fourOfAKindScore = 0;
        
        if (rankCountCounts[4] == 1) fourOfAKindScore = 1;
        //if there is a possibility of a four of a kind
        else if ((rankCountCounts[3] == 1) && (numCards == 4)) fourOfAKindScore = 0.466;
        else if ((rankCountCounts[3] == 1) && (numCards == 3)) fourOfAKindScore = 0.51;
        else if ((rankCountCounts[2] == 1) && (numCards == 3)) fourOfAKindScore = 0.01;
        else if ((rankCountCounts[2] == 1) && (numCards == 2)) fourOfAKindScore = 0.01;
        else if ((numCards == 1)) fourOfAKindScore = 0.01;
        else fourOfAKindScore = 0.0;
        
        return fourOfAKindScore;
    }
    
    private double scoreStraightFlushPolicy(Card hand[], int n) {
        double straightFlushScore = 0;
        
        //if there is a StraightFlush
        if (straight && (numSuits == 1) && (numCards == 5)) straightFlushScore = 1;
        
        return straightFlushScore;
    }
    
    private double scoreRoyalFlushPolicy(Card hand[], int n) {
        double royalFlushScore = 0;
        
        //if there is a StraightFlush
        if (royal && (numSuits == 1) && (numCards == 5)) royalFlushScore = 1;
        
        return royalFlushScore;
    }
    
    private void analyzeHand(Card[] hand) {
        
        //TRY to combine all for loops
        
        //Count suits, ranks, cards
        for (Card card : hand) {
            if (card != null) {
		suitCounts[card.getSuit()]++;
                rankCounts[card.getRank()]++;
                numCards++;
                suitRep = card;
            }
        }
        
        //Count number of ranks occuring multiple times,
        //number of ranks, suits
	for (int i = 0; i < Card.NUM_RANKS; i++) {
            rankCountCounts[rankCounts[i]]++;
            if (rankCounts[i] > maxOfAKind) {
                maxOfAKind = rankCounts[i];
                maxRank = i;
            }
            
            if (rankCounts[i] > 0) {
                numRanks ++;
            }
            
            if (i < Card.NUM_SUITS) {
                if (suitCounts[i] > 0) {
                    numSuits ++;
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
        
        handWeight = numCards / 5;
    }
}
