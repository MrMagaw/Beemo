package pokersquares.algorithms;

import java.util.ArrayList;
import java.util.List;
import pokersquares.config.Settings;
import pokersquares.environment.Board;
import pokersquares.environment.Card;

public class Simulator {
    private class Gamer extends Thread {
        private final Board board;
        private final int offset;
        private int numSimulations;
        private double totalScore = 0;
        
        public Gamer(Board board, int numSimulations, int offset){
            this.board = board;
            this.numSimulations = numSimulations + offset;
            this.offset = offset;
        }
        
        @Override
        public void run() {
            while(numSimulations-- > offset){
                Board b = new Board(board);
                while (b.getTurn() < 25) {
                    Card c = b.removeCard(numSimulations % b.cardsLeft());
                    //Card c = b.getDeck().remove(numSimulations % b.getDeck().size());
                    int[] p = Settings.Algorithms.simAlgorithm.search(c, b, millisRemaining);
                    b.playCard(c, p);
                }
                totalScore += Settings.Environment.system.getScore(b.getGrid());
            }
        }
    }
    
    private final Board board;
    private final int numSimulations, variator;
    private final long millisRemaining;
    
    private double totalScore = 0;
    public final List<Board> allBoards = new ArrayList();
    public final List<Board> allFinalBoards = new ArrayList();
    
    public Simulator(Board tb, int numSimulations, long millisRemaining, int variator, boolean genBoards){
        this.board = tb;
        this.numSimulations = numSimulations;
        this.millisRemaining = millisRemaining;
        this.variator = variator;
    }
    
    public void run(){
        //Number of threads used is 16 right now...
        Gamer[] gamers = new Gamer[16];
        int simPerThread = numSimulations >> 4;
        int extraThread = numSimulations - (simPerThread << 4);
        
        for(int i = 0; i < gamers.length; ++i){
            gamers[i] = new Gamer(board, (i < extraThread) ? simPerThread : simPerThread + 1, (i * simPerThread) + variator);
            gamers[i].start();
        }
        for(int i = 0; i < gamers.length; ++i){
            try{
                gamers[i].join();
                totalScore += gamers[i].totalScore;
            }catch(InterruptedException ex){System.err.println("Simulator interrupted!");}
        }
    }
    
    public static double simulate(Board tb, int numSimulations, long millisRemaining, int variator){
        /*double stime = System.currentTimeMillis();
        int nSim = numSimulations;
        System.err.println(nSim + " games finished in " + (System.currentTimeMillis() - stime) / 1000 + "s.");*/
        
        Simulator sim = new Simulator(tb, numSimulations, millisRemaining, variator, false);
        sim.run();
        return sim.totalScore;
        /*
        double score = 0;
        numSimulations += variator;
        while(numSimulations-- > variator){ //DONT CHANGE THE POST FIX, it's necessary here to properly calulate the number of simulations
            Board b = new Board(tb);

            while (b.getTurn() < 25) {
                Card c = b.getDeck().remove(numSimulations % b.getDeck().size()); 
                int[] p = Settings.Algorithms.simAlgorithm.search(c, b, millisRemaining);
                b.playCard(c, p);
            }

            score += Settings.Environment.system.getScore(b.getGrid());
        }
        
        return score;*/
    }
}
