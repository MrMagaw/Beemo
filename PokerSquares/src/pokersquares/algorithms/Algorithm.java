package pokersquares.algorithms;

public abstract class Algorithm {
    public int[] search(pokersquares.environment.Card card, pokersquares.environment.Board board, long millisRemaining) {
        int[] bestPos = {2, 2}; //2, 2 because 0, 0 isn't good enough.
        //First Turn Optimization
        if(board.getTurn() == 0){
            
            board.playCard(card, bestPos);
            return bestPos;
        }
        //Last Turn Optimization
        if (board.getTurn() == 24){
            Integer[] bp = board.getOpenPos().get(0);
            bestPos = new  int[] { bp[0], bp[1] } ;
            board.playCard(card, bestPos);
            return bestPos;
        }
        
        bestPos = internalSearch (card, board, millisRemaining);
        
        return bestPos;
    }
    
    public abstract int[] internalSearch(pokersquares.environment.Card card, pokersquares.environment.Board board, long millisRemaining);
}
