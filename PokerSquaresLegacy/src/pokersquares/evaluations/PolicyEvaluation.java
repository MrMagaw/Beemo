/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pokersquares.evaluations;

import pokersquares.Card;
import pokersquares.players.Beemo;
import pokersquares.PokerSquares;
import static pokersquares.evaluations.GeneticEvaluation.s;

/**
 *
 * @author newuser
 */
public class PolicyEvaluation {
    
    //Beemo
    Beemo BMO;
    
    //Policy Exponents
    private double p = 1; //pair
    private double tp = 1; //two pair
    private double t = 1; //three of a kind
    private double s = 1; //straight
    private double f = 0.95; //flush
    private double fh = 1.2; //full house
    private double fk = 1.16; //four of a kind
    private double sf = 1; //straight flush
    private double rf = 1; //royal flush
    
    //Hand Analysis
    private String handPattern = null;
    private double numCards = 0;
    private double handWeight = 0;
    private double numSuits = 0;
    private double numRanks = 0;
    private double maxOfAKind = 0;
    private int[] suitCounts = new int[Card.NUM_SUITS];
    private int[] rankCounts = new int[Card.NUM_RANKS];
    private int[] rankCountCounts = new int[PokerSquares.SIZE + 1];
    private Card suitRep;
    
    //Evaluation
    double evaluation = 0;
    
    public double policyEvaluation(Beemo BMO, Card[] hand, int n, boolean col) {
        //n is the respective number of the hand, then nth col or row
        
        //An Evaluation based on the genetic combination of weighted policies
        
        //Genetic Policy Scores
        //plays positions based on the weighted influences of hand policies
        //policy scores reflect the extent to which a policy is satisfied and the worth of a policy
        
        
        analyzeHand(hand);
        
        scoreHand(hand, n, col);
        
        //Every Position receives a score that is the sum of the scores from each policy
        //The positions that receives the best score from all policies is chosen
        
        return evaluation;
    }
    
    private void scoreHand(Card[] hand, int n, boolean col) {
        double[] handScores = new double[9]; //records a score for each poker hand from pair [0] to royal flush [8]
        
        for (int i = 0; i < 9; i ++) handScores[i] = 0;
        
        //Policy Scores should relate to probability, 
        //They are currently assigned by intuition,
        //The probability should be calculated or else learned 
        
        //if (!col) handScores[0] = 2 * Math.pow(scorePairPolicy(hand, n), p);
        handScores[1] = 5 * Math.pow(scoreTwoPairPolicy(hand, n), tp);
        if (!col) handScores[2] = 10 * Math.pow(scoreThreeOfAKindPolicy(hand, n), t);
        if (col) handScores[4] = 20 * Math.pow(scoreFlushPolicy(hand, n), f);
        if (!col) handScores[5] = 25 * Math.pow(scoreFullHousePolicy(hand, n), fh);
        handScores[6] = 50 * Math.pow(scoreFourOfAKindPolicy(hand, n), fk);
        
        //the evaluation is the highest policy score
        evaluation = handScores[0];
        for (int i = 0; i < 9; i++) if (handScores[i] > evaluation) evaluation = handScores[i];
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
        else if ((rankCountCounts[2] == 1) && (numCards < 5)) threeOfAKindScore = 0.9 - Math.pow((numCards/10),2);
        else if (numCards < 4) threeOfAKindScore = 0.5 - Math.pow((numCards/10),2);
        
        //System.out.println(numCards + " " + n + " " + threeOfAKindScore);
        
        return threeOfAKindScore;
    }
    
    private double scoreFlushPolicy(Card hand[], int n) {
        double flushScore = 0;
        
        if ((numSuits == 1) && (numCards <= 5)) flushScore = ((double)Math.pow(handWeight, 1/2))*handWeight*handWeight;
        
        /*
        //if there is a flush
        if ((numSuits == 1) && (numCards == 5)) flushScore = 1;
        //if there is the possibility of a flush 
        else if ((numSuits == 1) && (numCards < 5)) {
            
            //flushScore = 0.5 - (0.5 - Math.pow((numCards/10),2));
            //flushScore = 0.8;
            if (suitRep.getSuit() == n) flushScore += 0.1;
        }
        //if there is no possibility of a flush
        //else if (numSuits > 1) flushScore = -0.1;
        */
        
        return flushScore;
    }
    
    private double scoreFullHousePolicy(Card hand[], int n) {
        double fullHouseScore = 0;
        
        //if there is a Full House
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
        
        //if there is a Four Of A Kind
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
        
        return straightFlushScore;
    }
    
    private void analyzeHand(Card[] hand) {
        
        //TRY to combine all for loops
        
        //Count suits
        for (Card card : hand) {
            if (card != null) {
		suitCounts[card.getSuit()]++;
                numCards++;
                suitRep = card;
            } 
        }
        
        //Count number of suits in hand
        for (int i = 0; i < Card.NUM_SUITS; i++) {
            if (suitCounts[i] > 0) {
                numSuits ++;
            }
        }
        
        //Count ranks
        for (Card card : hand) {
            if (card != null) {
		rankCounts[card.getRank()]++;
            }
        }
        
        //Count number of ranks in hand
        for (int i = 0; i < Card.NUM_RANKS; i++) {
            if (rankCounts[i] > 0) {
                numRanks ++;
            }
        }
        
        //Count no of cards with same rank
	for (int count : rankCounts) {
            rankCountCounts[count]++;
            if (count > maxOfAKind) {
                maxOfAKind = count;
            }
        }
        
        handWeight = numCards / 5;
    }
    
}
