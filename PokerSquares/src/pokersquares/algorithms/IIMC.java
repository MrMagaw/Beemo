package pokersquares.algorithms;

import java.util.Arrays;
import java.util.Comparator;
import pokersquares.config.Settings;
import pokersquares.environment.*;
import pokersquares.evaluations.PatternPolicy;
import pokersquares.evaluations.PositionRank;

public class IIMC extends Algorithm{
    private static class Filter implements Comparator<Integer[]>{
        //Compartor class to filter positions by they're static evaluation value 
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
        
        Double bestScore = Double.MIN_VALUE;
        
        Integer[][] positions = new Integer[board.getOpenPos().size()][];
        positions = board.getOpenPos().toArray(positions);
        
        if(Settings.Algorithms.playSampleSize < positions.length){
            Arrays.sort(positions, new Filter(board, card));
            positions[Settings.Algorithms.playSampleSize] = null;
        }
        
        //POSITION RANK OPTIMIZATION
        if (Settings.Algorithms.positionRankEnabled) {
            board.patternateHands();
            board.patternatePositions(card);
        }
        
        //FOR EACH POSITION available in the board
        for(Integer[] pos : positions){
            if(pos == null) break;
            int numSimulations = Settings.Algorithms.simSampleSize;
            double score = 0;
            
            Board tb = new Board(board);
            tb.playCard(card, new int[]{pos[0], pos[1]});
            
            //SIMULATE Games
            while(--numSimulations > 0){
                Board b = new Board(tb);
                
                while (b.getTurn() < 25) {
                    Card c = b.getDeck().remove(numSimulations % b.getDeck().size()); 
                    int[] p = Settings.Algorithms.simAlgoritm.search(c, b, millisRemaining);
                    b.playCard(c, p);
                }
                
                score += Settings.Environment.psps.getScore(b.getGrid());
            }
            
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