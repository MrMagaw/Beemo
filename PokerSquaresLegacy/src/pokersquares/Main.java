package pokersquares;

import pokersquares.players.*;
import static pokersquares.PokerSquares.GAME_MILLIS;

public class Main {
    public static void main(String[] args) {
        //new PokerSquares(new RandomPokerSquaresPlayer(), GAME_MILLIS).playSequence(10000, 0, false);
        //new PokerSquares(new FlushPokerSquaresPlayer(), GAME_MILLIS).playSequence(1, 0, true);
        //new PokerSquares(new RuleBasedPokerSquaresPlayer(), GAME_MILLIS).playSequence(10000, 0, false);

        //for (int i = 0; i < 100; i++) new PokerSquares(new Beemo(100,4,"iimc"), GAME_MILLIS).playSequence(100, i*100, false);
        
        new PokerSquares(new Beemo(1000,6,"grb"), GAME_MILLIS).playSequence(100000, 0, false);
    }
}
