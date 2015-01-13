/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pokersquares.algorithms;

import pokersquares.Card;
import pokersquares.Play;
import pokersquares.players.Beemo;
import java.util.*;
import pokersquares.PokerSquares;
import static pokersquares.PokerSquares.SIZE;
import pokersquares.evaluations.PolicyEvaluation;

/**
 *
 * @author newuser
 */
public class GRB extends Algorithm{
    
    private double[] handEvaluations = new double[10];
    private double[] preEvaluations = new double[10];
    private double evaluation = 0;
    
    public GRB(Beemo parent) {
        super(parent);
    }
    
    @Override
    public int[] search(Card card, long millisRemaining) {
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
        
        //Every Position receives a score that is the some of the scores form each policy
        //The positions that recieves the best score from all policies is chosen
        
        //EVALUATE all hands representing the grid
        
        preEvaluate(card);
        
        postEvaluate(card);
        
        SortedMap <Double, Integer> policyScores = new TreeMap <Double, Integer> ();
        
        //FOR EACH pos represented by intersecting hands
        for (Integer pos : BMO.playPos) {
            int row = pos/5;
            int col = pos%5;
            
            //SCORE the Position
            double score = 
                    (this.evaluation - this.preEvaluations[row] - this.preEvaluations[col + 5]) +
                    (this.handEvaluations[row] + this.handEvaluations[col + 5]);
            
            //MAP the score to the pos
            policyScores.put(score, pos);
        }
        
        //CHOOSE best scored pos
        int pos = policyScores.get(policyScores.lastKey());
        int row = pos/5;
        int col = pos%5;
        
        int[] playPos = {row, col};
        
        return playPos;
    }
    
    public void postEvaluate(Card card) {
        //check each row 
        for (int row = 0; row < 5; ++row) {
            
            Card[] hand = new Card[SIZE];
            
            boolean cardPlaced = false;
            for (int col = 0; col < 5; ++col) {
                //build hand
                hand[col] = BMO.grid[row][col];
                
                //place the new card in each hand 
                if ((hand[col] == null) && (cardPlaced == false)) {
                    hand[col] = card;
                    cardPlaced = true;
                }
                
            }
            //evaluate hand
            if (cardPlaced == true) 
                handEvaluations[row] = Play.getHandEvaluation(BMO, hand, row, false);
        }
        
        //check each column
        for (int col = 0; col < 5; ++col) {
            
            Card[] hand = new Card[SIZE];
            
            boolean cardPlaced = false;
            for (int row = 0; row < 5; ++row) {
                //build hand
                hand[row] = BMO.grid[row][col];
                
                //place the new card in each hand 
                if ((hand[row] == null) && (cardPlaced == false)) {
                    hand[row] = card;
                    cardPlaced = true;
                }
            }
            //evaluate hand
            if (cardPlaced == true) 
                handEvaluations[col + 5] = Play.getHandEvaluation(BMO, hand, col, true);
        }
    }
    
    public void preEvaluate(Card card) {
        //check each row 
        for (int row = 0; row < 5; ++row) {
            
            Card[] hand = new Card[SIZE];
            
            for (int col = 0; col < 5; ++col) {
                //build hand
                hand[col] = BMO.grid[row][col];
                
            }
            //evaluate hand
            preEvaluations[row] = Play.getHandEvaluation(BMO, hand, row, false);
            evaluation += preEvaluations[row];
            //PokerSquares.printHand(hand, handEvaluations[row]);
        }
        
        //check each column
        for (int col = 0; col < 5; ++col) {
            
            Card[] hand = new Card[SIZE];
            
            for (int row = 0; row < 5; ++row) {
                //build hand
                hand[row] = BMO.grid[row][col];
                
            }
            //evaluate hand
            preEvaluations[col + 5] = Play.getHandEvaluation(BMO, hand, col, true);
            evaluation += preEvaluations[col + 5];
        }
    }
    
}
