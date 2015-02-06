package pokersquares.players;

import pokersquares.config.*;
import pokersquares.config.AdaptiveSettings;
import static pokersquares.config.Settings.BMO.genSettings;
import static pokersquares.config.Settings.Evaluations.updateSettings;
import static pokersquares.config.Settings.Training.bestValues;
import static pokersquares.config.Settings.Training.updateBest;
import pokersquares.config.SettingsReader;
import pokersquares.environment.*;
import pokersquares.evaluations.PatternPolicy;

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
    private boolean needUpdate = true;
    
    @Override
    public void setPointSystem(PokerSquaresPointSystem system, long millis){
        Settings.Environment.system = system;
        
        int[] scores = system.getScoreTable();
        for (int i = 0; i < 10; ++i) Settings.Evaluations.handScores[i] = scores[i];
        
        //READ PATTERNS
        if (Settings.BMO.readPatterns) pokersquares.evaluations.PatternPolicy.patternEvaluations = pokersquares.config.PatternReader.readPatterns(Settings.BMO.patternsFileIn);
        
        //READ SETTINGS
        SettingsReader.readSettings(Settings.BMO.settingsFileIn);
        
        //GENERATE SETTINGS
        if (genSettings) AdaptiveSettings.generateSettings();
        
        //TRAIN
        if (Settings.Training.train) Settings.Training.trainer.runSession(Settings.Training.millis);
        
        
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
            if (Settings.Training.train && needUpdate){
                Settings.Training.trainer.update();
                needUpdate = false;
            }
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
        
        //System.err.println(PatternPolicy.patternEvaluations.size());
        
        for(int i=0; i<3; ++i){
            if(board.getTurn() <= Settings.BMO.turnSplits[i]){
                bestPos = Settings.Algorithms.algorithm[i].search(card, board, millisRemaining);
                break;
            }
        }
        
        if ((bestPos[1] == 2) && bestPos[0] == 2) {
            System.out.println("Algorithm ERROR");
        }
        
        board.playCard(card, bestPos);
        
        return bestPos;
    }
    
    @Override
    public String getName(){
        return "BMO_V2";
    }

}
