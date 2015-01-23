package pokersquares.players;

import pokersquares.config.*;
import pokersquares.config.SettingsReader;
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
        Settings.BMO.BMO = this;
    }
    
    @Override
    public void setPointSystem(PokerSquaresPointSystem system, long millis){
        Settings.Environment.system = system;
                
        int[] scores = system.getScoreTable();
        for (int i = 0; i < 10; ++i) Settings.Evaluations.handScores[i] = scores[i];
        
        SettingsReader.readSettings("american");
        
        //if (Settings.BMO.train) pokersquares.learning.ValueReinforcement.runSession(millis);
        if (Settings.BMO.train) pokersquares.learning.ValueReinforcement.runSession(200000);
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
        
        board.playCard(card, bestPos);
        
        return bestPos;
    }
    
    @Override
    public String getName(){
        return "BMO_V2";
    }
}
