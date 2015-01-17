package pokersquares.algorithms;

import pokersquares.config.Settings;
import pokersquares.environment.Board;
import pokersquares.environment.Card;
import pokersquares.environment.Hand;
import pokersquares.evaluations.PatternPolicy;
import pokersquares.evaluations.PositionRank;

public class OBF extends Algorithm{
    @Override
    public int[] search(Card card, Board board, long millisRemaining) {
        double[] postEvaluations = new double[10];
        double[] preEvaluations = new double[10];
        double evaluation = 0;
        
        Integer[] bestPos = {2, 2};
        
        evaluate(card, board, preEvaluations, postEvaluations);
        if (Settings.Algorithms.positionRankEnabled) board.patternatePositions(card);
        
        Integer[] A,B;
        
        A = getBestPos(board, preEvaluations, postEvaluations);
        
        //COMMENT OUT to not use position rank
        //if (Settings.Algorithms.positionRankEnabled) PositionRank.update(board, A); 
        
        if (Settings.Algorithms.positionRankEnabled && PositionRank.contains(board)) {
            B = PositionRank.getBestPos(board);
            A = B;
            
            //DEBUG for duplicate patterns
            /*
            if (!(A[0] == B[0]) && (A[1] == B[1])) {
                System.out.println("Duplicate Pattern ERROR");
                board.debug();
                System.out.println("( "+A[0] + ", " + A[1] + " )" +"( "+B[0] + ", " + B[1] + " )");
            }
            */    
        }
        
        
        bestPos = A;
        
        return new int[] {bestPos[0], bestPos[1]};
    }
    
    private Integer[] getBestPos(Board board,double[] preEvaluations, double[] postEvaluations) {
        double bestScore = Integer.MIN_VALUE;
        Integer[] bestPos = {2, 2};
        
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
        
        return bestPos;  
    }
    
    private void evaluate(Card card, Board board, double[] preEvaluations, double[] postEvaluations)  {
        //Pre Evaluates Hands
        //Places Card
        //Post Evaluates Hands
        //Removes Card 
        
        int i = 0;
        for (Hand hand : board.hands) {
            //PRE EVALUATE
            preEvaluations[i] = hand.evaluate();
            
            String OGPattern = hand.pattern;
            double OGEvaluation = hand.evaluation;
            
            //POST EVALUATE
            if (hand.openPos.size() > 0) {
                
                int openPos = hand.openPos.getFirst();
                hand.cards[openPos] = card;
                postEvaluations[i] = hand.evaluate();
                
                //RESTORE hand to original state
                hand.cards[openPos] = null;
                hand.pattern = OGPattern;
                hand.evaluation = OGEvaluation;
            }
            else postEvaluations[i] = preEvaluations[i];
            
            ++i;
        }
    }
}