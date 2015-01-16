package pokersquares.players;

import java.util.Map;
import pokersquares.config.Settings;
import pokersquares.environment.Board;
import pokersquares.environment.Card;
import pokersquares.evaluations.BoardPolicy;
import pokersquares.evaluations.PatternPolicy;

/**
 *
 * @author Karo & William
 *                                   _________        
 *                                ||.     . ||
 *                                ||   ‿    ||
 *_|_|_|    _|      _|    _|_|    ||        ||
 *_|    _|  _|_|  _|_|  _|    _| /||--------||\
 *_|_|_|    _|  _|  _|  _|    _|  ||===   . ||
 *_|    _|  _|      _|  _|    _|  || +  o  0||
 *_|_|_|    _|      _|    _|_|    ||________||
 *                                   |    |
 *plays poker
 * 
 * 
 */

public class BeemoV2 implements PokerSquaresPlayer{
    private Board board;
    
    public BeemoV2(){
    }

    @Override
    public void init() {
        board = new Board();
        
    }

    @Override
    public int[] getPlay(Card card, long millisRemaining) {
        board.getDeck().remove(card);
        int[] bestPos = {2, 2}; //2, 2 because 0, 0 isn't good enough.
        
        //First Turn Optimization
        if(board.getTurn() == 0){
            board.playCard(card, bestPos);
            return bestPos;
        }
        
        for(int i=0; i<3; ++i){
            if(board.getTurn() <= Settings.BMO.turnSplits[i]){
                bestPos = Settings.Algorithms.algorithm[i].search(card, board, millisRemaining);
                break;
            }
        }
        
        if(bestPos[0] == 2 && bestPos[1] == 2) 
            System.out.println("Algorithm Error (Or unable to find algorithm).");
        
        
        board.playCard(card, bestPos);
        
        return bestPos;
    }
    
}
