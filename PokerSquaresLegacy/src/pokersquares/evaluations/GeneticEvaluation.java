/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pokersquares.evaluations;
import pokersquares.Card;

/**
 *
 * @author newuser
 */
public class GeneticEvaluation {
    
    static double s = 1;
    static double r = 1;
    static double o = 1;
    
    static public double geneticEvaluation(Card[] hand, int n, boolean col) {
        //num is the respective number of the hand, then nth col or row
        
        //An Evaluation based on the genetic combination of weighted policies
        double evaluation = 0;
        
        //Genetic Rule Base
        //plays positions based on the weighted influences of guiding policies
        //principles are based on the probability a certain hand will be completed and the reward associated with that hand
        
        //Suit Policy
        //Suits are grouped in columns
        //Every Column has a primary policy
        
        //Rank Policy
        //Ranks Are grouped in rows
        //Every Row has a primary rank
        
        //Order Policy
        //Straights are grouped primarily into columns
        
        double suitScore = 0;
        double rankScore = 0;
        double orderScore = 0;
        
        suitScore = s * scoreSuitPolicy(hand, n);
        rankScore = r * scoreRankPolicy(hand);
        orderScore = o * scoreOrderPolicy(hand);
        
        if (rankScore > suitScore) suitScore = rankScore;
        
        if (col) evaluation = suitScore + orderScore;
        if (!col) evaluation = rankScore + orderScore;
        
        //evaluation = rankScore;
        
        //Every Position receives a score that is the sum of the scores from each policy
        //The positions that receives the best score from all policies is chosen
        
        
        return evaluation;
    }
    
    static public double scoreSuitPolicy(Card hand[], int n) {
        //This policy rewards hands with a single suit
        int maxScore = 5;
        
        double suitPolicyScore = 0;
        
        //Count suits
        Card rep = null;
        int[] suitCounts = new int[Card.NUM_SUITS];
        for (Card card : hand) {
            if (card != null) {
		suitCounts[card.getSuit()]++;
                rep = card;
            }
        }
        
        //Count number of suits in hand
        int numSuits = 0;
        for (int i = 0; i < Card.NUM_SUITS; i++) {
            if (suitCounts[i] > 0) {
                numSuits ++;
            }
        }
        
        if (numSuits == 1) suitPolicyScore += (suitCounts[rep.getSuit()] * 1);
        if ((numSuits == 1) && (rep.getSuit() != n)) suitPolicyScore -= 0.1;
        
        suitPolicyScore = suitPolicyScore/maxScore;
        
        
        return suitPolicyScore;
    }
    
    static public double scoreRankPolicy(Card hand[]) {
        //This policy rewards hands able to achieve a full house
        int maxScore = 10;
        
        double rankPolicyScore = 0;
        
        //Count ranks
        Card rep = null;
        int[] rankCounts = new int[Card.NUM_RANKS];
        for (Card card : hand) {
            if (card != null) {
		rankCounts[card.getRank()]++;
                rep = card;
            }
        }
        
        //Count number of ranks in hand
        int numRanks = 0;
        for (int i = 0; i < Card.NUM_RANKS; i++) {
            if (rankCounts[i] > 0) {
                numRanks ++;
            }
        }
        
        //Count no of cards with same rank
        int maxOfAKind = 0;
        int[] rankCountCounts = new int[hand.length + 1];
	for (int count : rankCounts) {
            rankCountCounts[count]++;
            if (count > maxOfAKind) {
                maxOfAKind = count;
            }
        }
        
        //if there are three or more ranks in a row
        if (numRanks > 2) rankPolicyScore -= 3;
        
        //if there is a double or a triple
        if (rankCountCounts[2] == 1) rankPolicyScore += 1;
        if (rankCountCounts[3] == 1) rankPolicyScore += 2.5;
        
        //including this lowers the chances of a four of a kind
        if (rankCountCounts[2] == 2) rankPolicyScore += 2;
        
        //if there are less than two ranks in a row
        if (numRanks < 3) {
            if (rankPolicyScore > 0) rankPolicyScore += 2;
        } 
        
        //if there is a full house
        if ((rankCountCounts[3] == 1) && (rankCountCounts[2] == 1)) rankPolicyScore = 8;
        //if there is a four of a kind
        if (rankCountCounts[4] == 1) rankPolicyScore = 10;
        
        rankPolicyScore = rankPolicyScore/maxScore;
        
        return rankPolicyScore;
    }
    
    static public double scoreOrderPolicy(Card hand[]) {
        double orderPolicyScore = 0;
        
        
        
        return orderPolicyScore;
    }
    
}
