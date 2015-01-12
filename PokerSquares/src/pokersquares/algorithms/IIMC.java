package pokersquares.algorithms;

import java.util.Random;
import pokersquares.config.Settings;
import pokersquares.environment.Card;
import pokersquares.environment.Board;
import pokersquares.environment.PokerSquares;

public class IIMC extends Algorithm{

    @Override
    public int[] search(Card card, Board board, long millisRemaining) {
        Double bestScore = Double.MIN_VALUE;
        Integer[] bestPos = {2, 2};
        int playPosCount = Settings.Algorithms.playSampleSize;
                
        for(int i = board.getPlayPos().size()-1; i >= 0; --i){
            Integer[] pos = board.getPlayPos().get(i);
            int numSimulations = Settings.Algorithms.simulationSampleSize;
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
                
                //PokerSquares.printGrid(grid.getGrid());
                //(new Scanner(System.in)).next();
                
                //double score = PatternPolicy.evaluate(grid);
                score += PokerSquares.getScore(b.getGrid());
            }
            //System.out.println("{" + pos[0] + ", " + pos[1] + "} - " + score);
            //score += Pattern
            if(score > bestScore){
                bestScore = score;
                bestPos = pos;
            }
            if(--playPosCount <= 0) break;
        }
        
        return new int[] {bestPos[0], bestPos[1]};
    }
}