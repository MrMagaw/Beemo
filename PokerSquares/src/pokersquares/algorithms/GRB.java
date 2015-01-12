package pokersquares.algorithms;

import pokersquares.environment.Card;
import pokersquares.environment.Grid;
import pokersquares.evaluations.PatternPolicy;

public class GRB extends Algorithm{
    private double[] postEvaluations;
    private double[] preEvaluations;
    private double evaluation;
    @Override
    public int[] search(Card card, Grid grid, long millisRemaining) {
        postEvaluations = new double[10];
        preEvaluations = new double[10];
        evaluation = Integer.MIN_VALUE;
        Integer[] bestPos = {2, 2};
        preEvaluate(grid);
        postEvaluate(card, grid); //Combine these two!
        
        //FOR EACH pos represented by intersecting hands
        for (Integer[] pos : grid.getPlayPos()) {
            int row = pos[0];
            int col = pos[1];
            
            //SCORE the Position
            double score = 
                    (-this.preEvaluations[row] - this.preEvaluations[col + 5]) +
                    (this.postEvaluations[row] + this.postEvaluations[col + 5]);
            if(evaluation < score){
                evaluation = score;
                bestPos = pos;
            }
        }
        
        return new int[] {bestPos[0], bestPos[1]};
    }
    
    public void postEvaluate(Card card, Grid grid) {
        //check each row 
        for (int row = 0; row < 5; ++row) {
            Card[] hand = new Card[5];
            boolean cardPlaced = false;
            
            for (int col = 0; col < 5; ++col) {
                //build hand
                hand[col] = grid.getCard(row, col);
                //Evaluate hand
                
                //place the new card in each hand 
                if ((hand[col] == null) && (cardPlaced == false)) {
                    hand[col] = card;
                    cardPlaced = true;
                }
            }
            //evaluate hand
            if (cardPlaced) 
                postEvaluations[row] = PatternPolicy.evaluate(hand, false);
        }
        
        //check each column
        for (int col = 0; col < 5; ++col) {
            
            Card[] hand = new Card[5];
            
            boolean cardPlaced = false;
            for (int row = 0; row < 5; ++row) {
                //build hand
                hand[row] = grid.getCard(row, col);
                
                //place the new card in each hand 
                if ((hand[row] == null) && (cardPlaced == false)) {
                    hand[row] = card;
                    cardPlaced = true;
                }
            }
            //evaluate hand
            
            
            if (cardPlaced == true) 
                postEvaluations[col + 5] = PatternPolicy.evaluate(hand, true);
        }
    }
    
    public void preEvaluate(Grid grid) {
        //check each row 
        for (int row = 0; row < 5; ++row) {
            
            Card[] hand = new Card[5];
            
            for (int col = 0; col < 5; ++col) {
                //build hand
                hand[col] = grid.getCard(row, col);
                
            }
            //evaluate hand
            
            
            preEvaluations[row] = PatternPolicy.evaluate(hand, false);

            //PokerSquares.printHand(hand, handEvaluations[row]);
        }
        
        //check each column
        for (int col = 0; col < 5; ++col) {
            
            Card[] hand = new Card[5];
            
            for (int row = 0; row < 5; ++row) {
                //build hand
                hand[row] = grid.getCard(row, col);
                
            }
            //evaluate hand
            preEvaluations[col + 5] = PatternPolicy.evaluate(hand, true);
            
        }
    }
    
}
