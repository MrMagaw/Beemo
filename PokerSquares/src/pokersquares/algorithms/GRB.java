package pokersquares.algorithms;

import pokersquares.environment.Card;
import pokersquares.environment.Grid;
import pokersquares.evaluations.PatternPolicy;

public class GRB extends Algorithm{
    private double[] preEvaluations;
    private double[] postEvaluations;
    private double evaluation;
    @Override
    public int[] search(Card card, Grid grid, long millisRemaining) {
        postEvaluations = new double[10];
        preEvaluations = new double[10];
        evaluation = Integer.MIN_VALUE;
        Integer[] bestPos = {2, 2};
        
        evaluate(card, grid);
        
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
    
    public void evaluate(Card card, Grid grid)  {
        //Pre Evaluates Hands
        //Places Card
        //Post Evaluates Hands
        
        //check each row 
        for (int row = 0; row < 5; ++row) {
            
            Card[] hand = new Card[5];
            int iNull = -1; //index of Null pos in hand
            
            for (int col = 0; col < 5; ++col) {
                //build hand
                hand[col] = grid.getCard(row, col);
                
                //check for null pos
                if (hand[col] == null) iNull = col;
            }
            
            //preEvaluate
            preEvaluations[row] = PatternPolicy.evaluate(hand, false);

            //place card
            if (iNull != -1) hand[iNull] = card;
            
            //postEvaluate
            postEvaluations[row] = PatternPolicy.evaluate(hand, false);
            
        }
        
        //check each column
        for (int col = 0; col < 5; ++col) {
            
            Card[] hand = new Card[5];
            int iNull = -1; //index of Null pos in hand
            
            for (int row = 0; row < 5; ++row) {
                //build hand
                hand[row] = grid.getCard(row, col);
                
                //check for null pos
                if (hand[row] == null) iNull = row;
                
            }
            //evaluate hand
            preEvaluations[col + 5] = PatternPolicy.evaluate(hand, true);
            
            //place card
            if (iNull != -1) hand[iNull] = card;
            
            //postEvaluate
            postEvaluations[col + 5] = PatternPolicy.evaluate(hand, true);
        }
        
    }
    
}
