package pokersquares.algorithms;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import pokersquares.config.Settings;
import pokersquares.environment.Card;
import pokersquares.environment.Board;
import pokersquares.evaluations.PatternPolicy;

public class IIMC extends Algorithm{

    @Override
    public int[] search(Card card, Board grid, long millisRemaining) {
        int numSimulations = Settings.Algorithms.simulationSampleSize;
        HashMap<Integer[], Double> scores = new HashMap();
        
        while(--numSimulations > 0){
            for(int i = grid.getPlayPos().size()-1; i >= 0; --i){
                Integer[] pos = grid.getPlayPos().get(i);
                Random r = new Random();
                Board b = new Board(grid);
                
                while (b.getTurn() < 25) {
                    Card c = b.getDeck().remove(r.nextInt(b.getDeck().size()));
                    int[] p = Settings.Algorithms.simAlgoritm.search(c, b, millisRemaining);
                    b.playCard(c, p);
                    
                }
                
                double score = PatternPolicy.evaluate(grid);
                if(scores.containsKey(pos)){
                    score += scores.get(pos);
                }
                scores.put(pos, score);
            }
        }
        Double bestScore = Double.MIN_VALUE;
        Integer[] bestPos = {2, 2};
        System.out.println("---------------------");
        for(Entry<Integer[], Double> e : scores.entrySet()){
            System.out.println("{" + e.getKey()[0] + ", " + e.getKey()[1] + "} : " + e.getValue());
            if(e.getValue() > bestScore){
                bestScore = e.getValue();
                bestPos = e.getKey();
            }
        }
        return new int[] {bestPos[0], bestPos[1]};
    }
}