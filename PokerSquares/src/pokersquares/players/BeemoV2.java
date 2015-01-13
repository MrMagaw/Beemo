package pokersquares.players;

import pokersquares.config.Settings;
import pokersquares.environment.Card;
import pokersquares.environment.Board;

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
        //Shuffle deck?
    }

    @Override
    public int[] getPlay(Card card, long millisRemaining) {
        board.getDeck().remove(card);
        int[] bestPos = {2, 2}; //2, 2 because 0, 0 isn't good enough.
        
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
        
        //Temp
        if(bestPos[0] == 2 && bestPos[1] == 2) 
            System.out.println("Algorithm Error (Or unable to find algorithm).");
        //
        
        board.playCard(card, bestPos);
        
        return bestPos;
    }
    
}
