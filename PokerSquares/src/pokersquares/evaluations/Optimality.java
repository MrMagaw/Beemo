
package pokersquares.evaluations;

import java.util.ArrayList;
import java.util.List;
import pokersquares.config.Settings;
import pokersquares.environment.Board;
import pokersquares.environment.Card;
import pokersquares.environment.Hand;

/**
 *
 * @author karo
 */
public class Optimality {
    //Functions to assess the optimal score of a given board (set of 25 cards)
    
    public static double scoreBestHands(Board b) {
        //An Approximation of the optimal score based on the best hands available
        
        double score = 0;
        
        ArrayList <Card> cards = new ArrayList <Card> ();
        
        
        //GET Played Cards
        for (int i = 0; i < 25; ++i) 
            cards.add(b.getGrid()[i%5][i/5]);
        
        //GET 5 best Hands
        for (int i = 0; i < 5; ++i) {
            double bestHandScore = Settings.Environment.system.getHandScore(removeBestHand(cards));
            //System.out.println(bestHandScore);
            score += bestHandScore * 2;
        }
        
        return score;
    }
    
    static Card[] removeBestHand(ArrayList <Card> cards) {
        
        double bestScore = Double.NEGATIVE_INFINITY;
        Card[] bestHand = new Card[5];
        Card[] testHand = new Card[5];
        int numCards = cards.size();
        
        //Compare all hands... 
        for (int i = 0; i < numCards; ++i) {
            testHand[0] =  cards.remove(i);
            for (int j = 0; j < numCards - 1; ++j) {
                testHand[1] = cards.remove(j);
                for (int k = 0; k < numCards - 2; ++k) {
                    testHand[2] = cards.remove(k);
                    for (int l = 0; l < numCards - 3; ++l) {
                        testHand[3] = cards.remove(l);
                        for (int m = 0; m < numCards - 4; ++m) {
                            if (cards.size() == 17)System.out.println(i + " " + j + " " + k + " " + l + " " + m);
                            testHand[4] = cards.remove(m);
                            cards.size();
                            double testScore = Settings.Environment.system.getHandScore(testHand);
                            
                            if (testScore > bestScore) {
                                bestScore = testScore;
                                System.arraycopy( testHand, 0, bestHand, 0, testHand.length );
                            }
                            cards.add(testHand[4]);
                        }
                        cards.add(testHand[3]);
                    }
                    cards.add(testHand[2]);
                }
                cards.add(testHand[1]);
            }
            cards.add(testHand[0]);
        }
        
        //System.out.println(Settings.Environment.system.getHandScore(bestHand));
        
        //Remove best Hand from cards
        for (int i = 0; i < 5; ++i) {
            cards.remove(bestHand[i]);
        }
                
        return bestHand;
    }
    
}
