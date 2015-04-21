package pokersquares.players;

import pokersquares.algorithms.Simulator;
import pokersquares.config.*;
import pokersquares.environment.*;
import pokersquares.evaluations.Optimality;
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
    public int gamesPlayed = 0;
    public double totalOptimality = 0;
    
    @Override
    public void setPointSystem(PokerSquaresPointSystem system, long millis){
        //new PatternPolicy();
        Settings.Environment.system = system;
        
        //SET POINT SYSTEM
        int[] scores = system.getScoreTable();
        for (int i = 0; i < 10; ++i) Settings.Evaluations.handScores[i] = scores[i];
        
        //READ PATTERNS
        if (Settings.BMO.readPatterns) pokersquares.evaluations.PatternPolicy.patternEvaluations = pokersquares.config.PatternReader.readPatterns(Settings.BMO.patternsFileIn);
        
        //TRAIN
        if (Settings.Training.train) Settings.Training.trainer.runSession(Settings.Training.millis);
        
        //DEBUG PATTERN VALUES
        if (Settings.BMO.debugPatterns)PatternPolicy.debug();
        
    }

    @Override
    public void init() {
        ++gamesPlayed;
        board = new Board();
    }

    @Override
    public int[] getPlay(Card card, long millisRemaining) {
        board.removeCard(card);
        int[] bestPos = {2, 2}; //2, 2 because 0, 0 isn't good enough.
        
        for(int i=0; i<Settings.BMO.turnSplits.length; ++i){
            if(board.getTurn() <= Settings.BMO.turnSplits[i]){
                bestPos = Settings.Algorithms.algorithm[i].search(card, board, millisRemaining);
                break;
            }
        }
        
        board.playCard(card, bestPos);
        
        //TEST OPTIMALITY
        if ((Settings.Evaluations.testOptimality) && (board.getTurn() == 25)) {
            //System.out.println(gamesPlayed);
            double optimalScore = Optimality.scoreBestHands(board);
            totalOptimality += Settings.Environment.system.getScore(board.getGrid())/optimalScore;
            System.out.println("Optimal Score: " + optimalScore + "\tMean Optimality: " + totalOptimality/gamesPlayed);
        }
        
        return bestPos;
    }
    
    @Override
    public String getName(){
        return "BMO_V2";
    }

}
