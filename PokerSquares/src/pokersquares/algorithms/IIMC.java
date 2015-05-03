package pokersquares.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import pokersquares.config.Settings;
import pokersquares.environment.*;
import pokersquares.evaluations.PatternPolicy;

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
    public int[] internalSearch(final Card card, final Board board, long millisRemaining) {
        Integer[] bestPos = {2, 2};
        Double bestScore = Double.NEGATIVE_INFINITY;
        Long start = System.currentTimeMillis();
        Long buffer = 100L;
        Long turnMillis = millisRemaining / (24 - board.getTurn());
        
        //INSTANTIATE BOARD
        Board pb = new Board(board); //Peanut Butter
        Integer[][] positions = new Integer[board.getOpenPos().size()][];
        positions = board.getOpenPos().toArray(positions); //COMMENT to use symmetry optimization
        ArrayList <Gamer> gamers = new ArrayList <Gamer> ();
        
        //FOR EACH POSITION available in the board
        for(Integer[] pos : positions){
            
            if(pos == null) break;
            
            int numSimulations = Settings.Algorithms.simSampleSize;
            
            Board tb = new Board(board);
            tb.playCard(card, new int[]{pos[0], pos[1]});
            
            //START THREAD
            //SIMULATE Games
            Gamer gamer = new Gamer(tb, numSimulations, 0);
            gamer.start();
            gamers.add(gamer);
        }
        
        //HALT if simulations exceed allotted Millis
        //DETERMINE Best Pos
        for (int i  = 0; i < gamers.size(); ++i) {
            Gamer gamer = gamers.get(i);
            Integer[] pos = positions[i];
            while (gamer.isAlive())
                if ((System.currentTimeMillis() - start) > (turnMillis - buffer))
                    gamer.halt();
            
            double score = gamer.totalScore / gamer.simsRun;
            if (score > bestScore){
                bestScore = score;
                bestPos = pos;
            }
        }
        
        return new int[] {bestPos[0], bestPos[1]};
    }
}