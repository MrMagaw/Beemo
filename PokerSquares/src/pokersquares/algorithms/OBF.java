package pokersquares.algorithms;

import java.util.Arrays;
import pokersquares.config.Settings;
import pokersquares.environment.Board;
import pokersquares.environment.Card;
import pokersquares.environment.Hand;
import pokersquares.evaluations.PositionRank;

public class OBF extends Algorithm{
    @Override
    public int[] internalSearch(Card card, Board board, long millisRemaining) {
        double[] postEvaluations = new double[10];
        double[] preEvaluations = new double[10];
        
        Integer[] bestPos;
        
        evaluate(card, board, preEvaluations, postEvaluations);
        
        if (Settings.Algorithms.positionRankEnabled) {
            board.patternatePositions(card);
            if (PositionRank.contains(board)){
                bestPos = PositionRank.getBestPos(board);
                //DEBUG for duplicate patterns
                /*
                if (!(A[0] == B[0]) && (A[1] == B[1])) {
                    System.out.println("Duplicate Pattern ERROR");
                    board.debug();
                    System.out.println("( "+A[0] + ", " + A[1] + " )" +"( "+B[0] + ", " + B[1] + " )");
                }
                */   
            }else{
                bestPos = getBestPos(board, preEvaluations, postEvaluations);
            }
        }else{
            bestPos = getBestPos(board, preEvaluations, postEvaluations);
        }
        
        return new int[] {bestPos[0], bestPos[1]};
    }
    
    private Integer[] getBestPos(Board board, double[] preEvaluations, double[] postEvaluations) {
        double bestScore = Double.NEGATIVE_INFINITY;
        Integer[] bestPos = {2, 2};
        
        //FOR EACH pos represented by intersecting hands
        for (Integer[] pos : board.getOpenPos()) {
            int row = pos[0];
            int col = pos[1];
            
            //SCORE the Position
            double score = 
                (-preEvaluations[row] - preEvaluations[col + 5]) +
                (postEvaluations[row] + postEvaluations[col + 5]);
            //System.out.println(Arrays.toString(preEvaluations) + "\n" + Arrays.toString(postEvaluations));
            if(bestScore < score){
                bestScore = score;
                bestPos = pos;
            }
        }
        //System.out.println(Arrays.toString(bestPos) + ": " + bestScore);
        return bestPos;  
    }
    
    private void evaluate(Card card, Board board, double[] preEvaluations, double[] postEvaluations)  {
        //Pre Evaluates Hands
        //Places Card
        //Post Evaluates Hands
        //Removes Card
        for (int i = board.hands.size() - 1; i >= 0; --i) {
            Hand hand = new Hand(board.hands.get(i));
            //PRE EVALUATE
            preEvaluations[i] = hand.evaluate();
            //POST EVALUATE
            if (hand.hasOpenPos()) {
                hand.playOpenPos(card);
                postEvaluations[i] = hand.evaluate();
                
            }else postEvaluations[i] = preEvaluations[i];
        }
    }
}