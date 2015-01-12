package pokersquares.algorithms;

public abstract class Algorithm {
    public abstract int[] search(pokersquares.environment.Card card, pokersquares.environment.Grid grid, long millisRemaining);
}
