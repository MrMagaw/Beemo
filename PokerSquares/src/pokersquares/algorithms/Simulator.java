package pokersquares.algorithms;

import pokersquares.config.Settings;
import pokersquares.environment.Board;
import pokersquares.environment.Card;

public class Simulator {
    public static double simulate(Board tb, int numSimulations, long millisRemaining){
        double score = 0;
        while(--numSimulations > 0){
            Board b = new Board(tb);

            while (b.getTurn() < 25) {
                Card c = b.getDeck().remove(numSimulations % b.getDeck().size()); 
                int[] p = Settings.Algorithms.simAlgoritm.search(c, b, millisRemaining);
                b.playCard(c, p);
            }

            score += Settings.Environment.system.getScore(b.getGrid());
        }
        return score;
    }
}
