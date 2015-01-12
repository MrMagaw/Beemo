package pokersquares;

import pokersquares.environment.PokerSquares;
import static pokersquares.environment.PokerSquares.GAME_MILLIS;
import pokersquares.players.BeemoV2;

public class Main {
    public static void main(String[] args){
        new PokerSquares(new BeemoV2(), GAME_MILLIS).playSequence(10000, 0, false);
    }
}
