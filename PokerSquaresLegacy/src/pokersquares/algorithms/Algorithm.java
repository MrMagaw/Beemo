package pokersquares.algorithms;

import pokersquares.players.Beemo;
import pokersquares.Card;

public abstract class Algorithm {
    public final Beemo BMO;
    public Algorithm(Beemo parent){
        BMO = parent;
    };
    public abstract int[] search(Card card, long millisRemaining);
}
