package pokersquares.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import pokersquares.config.Settings;
import static pokersquares.config.Settings.Environment.system;
import pokersquares.environment.Board;
import pokersquares.environment.Card;
import pokersquares.environment.PokerSquares;
import pokersquares.players.BeemoV2;

public class Simulator {
    
    private final Board board;
    private final int numSimulations, variator;
    private final long millisRemaining;
    
    public int simsRun = 0;
    public int numThreads = 0;
    private double totalScore = 0;
    public final List<Board> allBoards = new ArrayList();
    public final List<Board> allFinalBoards = new ArrayList();
    
    public Simulator(Board tb, int numSimulations, long millisRemaining, int variator, boolean genBoards){
        this.numThreads = Runtime.getRuntime().availableProcessors(); //Set num threads to num cores
        this.board = tb;
        this.numSimulations = numSimulations;
        this.millisRemaining = millisRemaining;
        this.variator = variator;
    }
    
    public void run(){
        //Number of threads used is 16 right now...
        Gamer[] gamers = new Gamer[this.numThreads];
        int simPerThread = numSimulations/gamers.length;
        int extraThread = numSimulations - (simPerThread / gamers.length);
        
        for(int i = 0; i < gamers.length; ++i){
            gamers[i] = new Gamer(board, (i < extraThread) ? simPerThread : simPerThread + 1, (i * simPerThread) + variator);
            gamers[i].start();
        }
        
        totalScore = 0;
        
        for(int i = 0; i < gamers.length; ++i){
            try{
                gamers[i].join();
                totalScore += gamers[i].totalScore;
                simsRun += gamers[i].simsRun;
            }catch(InterruptedException ex){System.err.println("Simulator Interrupted!");}
        }
    }
    
    public static double simulate(Board tb, int numSimulations, long millisRemaining, int variator){
        
        Simulator sim = new Simulator(tb, numSimulations, millisRemaining, variator, false);
        sim.run();
        //System.out.println(sim.totalScore + " " + sim.simsRun);
        return sim.totalScore / sim.simsRun;
    }
}
