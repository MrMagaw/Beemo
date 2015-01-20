package pokersquares.players;

import pokersquares.config.Settings;
import pokersquares.environment.*;

/**
 *
 * @author Karo & William
 *                                   _________        
 *                                ||.     . ||
 *                                ||   ‿    ||
 *_|_|_|    _|      _|    _|_|    ||        ||
 *_|    _|  _|_|  _|_|  _|    _| /||-----V2-||\
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
    public void setPointSystem(PokerSquaresPointSystem system, long millis){
        Settings.Environment.system = system;
        int[] scores = system.getScoreTable();
        Settings.Evaluations.highCardValue = scores[0];
        Settings.Evaluations.pairValue = scores[1];
        Settings.Evaluations.twoPairValue = scores[2];
        Settings.Evaluations.threeOfAKindValue = scores[3];
        Settings.Evaluations.straightValue = scores[4];
        Settings.Evaluations.flushValue = scores[5];
        Settings.Evaluations.fullHouseValue = scores[6];
        Settings.Evaluations.fourOfAKindValue = scores[7];
        Settings.Evaluations.straightFlushValue = scores[8];
        Settings.Evaluations.royalFlushValue = scores[9];
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
        
        //Debug information to find if bestPos was not updated.
        //if(bestPos[0] == 2 && bestPos[1] == 2) 
        //    System.out.println("Algorithm Error (Or unable to find algorithm).");
        
        
        board.playCard(card, bestPos);
        
        return bestPos;
    }
    
    @Override
    public String getName(){
        return "BMO_V2";
    }
}
