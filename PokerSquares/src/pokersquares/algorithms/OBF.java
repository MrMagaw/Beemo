package pokersquares.algorithms;

import pokersquares.environment.Board;
import pokersquares.environment.Card;
import pokersquares.environment.Hand;
import pokersquares.evaluations.PatternPolicy;

public class OBF extends Algorithm{
    @Override
    public int[] search(Card card, Board board, long millisRemaining) {
        double[] postEvaluations = new double[10];
        double[] preEvaluations = new double[10];
        double evaluation = 0;
        double bestScore = Integer.MIN_VALUE;
        Integer[] bestPos = {2, 2};
        
        evaluate(card, board, preEvaluations, postEvaluations);
        
        //FOR EACH pos represented by intersecting hands
        for (Integer[] pos : board.getOpenPos()) {
            int row = pos[0];
            int col = pos[1];
            
            //SCORE the Position
            double score = 
                    (-preEvaluations[row] - preEvaluations[col + 5]) +
                    (postEvaluations[row] + postEvaluations[col + 5]);
            if(bestScore < score){
                bestScore = score;
                bestPos = pos;
            }
        }
        
        return new int[] {bestPos[0], bestPos[1]};
    }
    
    private void evaluate(Card card, Board board, double[] preEvaluations, double[] postEvaluations)  {
        //Pre Evaluates Hands
        //Places Card
        //Post Evaluates Hands
        //Removes Card 
        
        int i = 0;
        for (Hand hand : board.hands) {
            //hand.debug();
            //PRE EVALUATE
            preEvaluations[i] = hand.evaluate();
            
            //POST EVALUATE
            if (hand.openPos.size() > 0) {
                
                int openPos = hand.openPos.getFirst();
                hand.cards[openPos] = card;
                postEvaluations[i] = hand.evaluate();
                
                //RESTORE hand to original state
                hand.cards[openPos] = null;
            }
            else postEvaluations[i] = preEvaluations[i];
            
            ++i;
        }
        
    }
    
}