package pokersquares.algorithms;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import pokersquares.config.Settings;
import pokersquares.environment.*;
import pokersquares.evaluations.PatternPolicy;
import pokersquares.evaluations.PositionRank;

public class IIMC extends Algorithm{
    private static class Filter implements Comparator<Integer[]>{
        //Compartor class to filter positions by their static evaluation value 
        private final Card card;
        private final Board board;
        @Override
        public int compare(Integer[] o1, Integer[] o2) {
            //o1-o2
            Board tb = new Board(board); 
            //Optimize by building, evaluating and comparing only at corresponding hands, not the entire board
            
            tb.playCard(card, new int[]{o2[0], o2[1]});
            double s1 = PatternPolicy.evaluate(tb);
            tb = new Board(board);
            tb.playCard(card, new int[]{o1[0], o1[1]});
            return ((int)(s1 * 1000)) - ((int)(PatternPolicy.evaluate(tb) * 1000));
        }
        public Filter(Board b, Card c){
            card = c;
            board = b;
        }
    }
    
    @Override
    public int[] search(final Card card, final Board board, long millisRemaining) {
        Integer[] bestPos = {2, 2};
        Double bestScore = Double.NEGATIVE_INFINITY;
        
        //UNIQUE POSITION PATTERNS
        //SYMMETRY REDUNDANCY
        Board pb = new Board(board);
        for (Hand h : pb.hands) if (h.numCards < 5)h.playOpenPos(card);
        
        pb.patternateHands();
        pb.patternatePositions(card);
        
        HashMap <String,Integer[]> uniquePatterns = new HashMap <String, Integer[]>();
        for (int i = 0; i < pb.posPatterns.size(); ++i) {
            Integer[] pos = pb.getOpenPos().get(i);
            String posPattern = pb.posPatterns.get(i);
            uniquePatterns.put(posPattern, pos);
        }
        
        Integer[][] positions = new Integer[uniquePatterns.size()][];
        int i = 0;
        for (Integer[] pos : uniquePatterns.values()) positions[i++] = pos;
        
        //System.out.println(pb.posPatterns.size() + " " + uniquePatterns.size());
        //positions = board.getOpenPos().toArray(positions); //COMMENT to use symmetry optimization
        
        //FOR EACH POSITION available in the board
        for(Integer[] pos : positions){
            if(pos == null) break;
            int numSimulations = Settings.Algorithms.simSampleSize;
            double score = 0;
            
            Board tb = new Board(board);
            tb.playCard(card, new int[]{pos[0], pos[1]});
            
            //SIMULATE Games
            score = Simulator.simulate(tb, numSimulations, millisRemaining, 1);
            
            if(score > bestScore){
                bestScore = score;
                bestPos = pos;
            }
        }
        
        //UPDATE Position Rank
        if (Settings.Algorithms.positionRankEnabled)
            PositionRank.update(board, bestPos); 
        
        return new int[] {bestPos[0], bestPos[1]};
    }
}