package pokersquares.algorithms;

import java.util.Random;
import pokersquares.config.Settings;
import pokersquares.environment.Card;
import pokersquares.environment.Board;
import pokersquares.environment.PokerSquares;
import pokersquares.evaluations.PatternPolicy;

public class IIMC extends Algorithm{

    @Override
    public int[] search(final Card card, final Board board, long millisRemaining) {
        Double bestScore = Double.MIN_VALUE;
        Integer[] bestPos = {2, 2};
        int playPosCount = Settings.Algorithms.playSampleSize;
        
        for(Integer[] pos : board.getPlayPos()){
            int numSimulations = Settings.Algorithms.simSampleSize;
            double score = 0;
            
            Board tb = new Board(board);
            tb.playCard(card, new int[]{pos[0], pos[1]});
            
            while(--numSimulations > 0){
                Board b = new Board(tb);
                Random r = new Random();
                
                while (b.getTurn() < 25) {
                    Card c = b.getDeck().remove(r.nextInt(b.getDeck().size()));
                    int[] p = Settings.Algorithms.simAlgoritm.search(c, b, millisRemaining);
                    b.playCard(c, p);
                }
                
                score += PokerSquares.getScore(b.getGrid());
            }
            //Maybe remove this? v
            score += (PatternPolicy.evaluate(tb) * Settings.Algorithms.simSampleSize); //Add the score of the move to the evaluation
            //Or decrease the multiplier (ie simSampleSize / 2) ^
            if(score > bestScore){
                bestScore = score;
                bestPos = pos;
            }
            if(--playPosCount <= 0) break;
        }
        
        return new int[] {bestPos[0], bestPos[1]};
    }
}