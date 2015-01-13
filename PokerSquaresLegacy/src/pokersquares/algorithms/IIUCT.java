package pokersquares.algorithms;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import pokersquares.players.Beemo;
import pokersquares.Card;
import pokersquares.Play;

/**
 *
 * @author newuser
 */
public class IIUCT extends IIMC{
    static double epsilon = 1e-12;
    int totalNumSimulations;
    
    public IIUCT(Beemo BMO) {
        super(BMO);
    }
    
    public int[] search(Card card, Long millisRemaining) {
        
        //MONTE CARLO TREE SEARCH 
        //Based on Buro and Furtak's IIMC algorithm
        //Imperfect Information Monte Carlo
        //implements the UCT algorithm for node selection
        
        int[] bestPos = new int[2];
        
        //OPTIMIZATION always play the first card in the center
        if (BMO.getTurn() == 0) {
            bestPos[0] = 2;
            bestPos[1] = card.getSuit();
            return bestPos;
        }
        
        //ROOT PLAY
        Play root = new Play(BMO); 
        root.buildAndEvaluateHands();
        root.evaluatePlay();
        
        //GENERATE PLAYS
        LinkedList <Play> moves =  new LinkedList();
        genPlays(card, root, moves);
        
        //RUN SIMULATIONS
        //IN <- moves, timeRemaining
        bestPos = runSimulations(moves, millisRemaining);
        //OUT -> BESTPOS
        
        return bestPos;
    }
    
    @Override
    public int[] runSimulations(List <Play>  moves, long millisRemaining) {
        int[] bestPos;
        boolean timeLeft = true;
        int totalSampleSize = BMO.getMonteCarloSampleSize() * moves.size() + 1;
        Collections.sort(moves);
        
        //WHILE TIME IS LEFT
        totalNumSimulations = 1;
        while(timeLeft) {
            //SELECT MOVE to rollout from
            Play selected = null;
            double bestValue = Double.MIN_VALUE;
            
            for (Play move : moves) {
                double uctValue = UCT(move,0.001, 1, 100, 0); //all inital tests at 0.002
                
                // small random number to break ties randomly in unexpanded nodes
                //System.out.println(Math.log(totalNumSimulations+ epsilon )/ (move.getNumSimulations() + epsilon));
                if (uctValue >= bestValue) {
                    selected = move;
                    bestValue = uctValue;
                }
                
            }
            
            //ROLLOUT from selected
            //selected.simulateGBFGame(t.elapsed());
            //selected.simulateRandomGame(BMO.getTimer().elapsed());
            //selected.simulateFlushGame(BMO.getTimer().elapsed());
            selected.simulateRB2Game(BMO.getTimer().elapsed());
            //selected.simulateGame("grb",BMO.getTimer().elapsed());
            
            //LIMITED BACKPROPAGATION
            selected.updateSimAve();
            totalNumSimulations++;
            
            //TERMINATION CONDITIONS
            
            //TIME RUNS OUT
            //if (t.elapsed() > ((timeCoefficient * millisRemaining*0.4)-150)) timeLeft = false; //Negative Concave Time
            //if (t.elapsed() > (timeCoefficient * (totalMillis/(15+(turn%25)))-100)) timeLeft = false; //Flat Time
            
            //TARGET SAMPLE SIZE REACHED
            if (totalNumSimulations >= totalSampleSize) timeLeft = false;
            
        }
        
        //UPDATE EVALUATION
        //as the average of all simulations from that move
        for (Play move : moves) {
            move.updateSimAve();
        }
        
        //CHOOSE BEST MOVE
        Collections.sort(moves);
        Play best = moves.get(0);
        
        /*
        //print num sim and their corresponding evalutions
        for (Play move : moves) {
            System.out.println("numSim: " + move.getNumSimulations() + " sum: " + move.getSimulationSum());
            System.out.println(move.getEvaluation() +" "+ move.getPlayedPositions().get(0));
        }
        */
        
        bestPos = best.getPos();
        
        return bestPos;
    }
    
    public double UCT(Play move, double B, double C, double D, double E) {
        //return the UCT value
        
        double uctValue = 
            //average simulation value of a node scaled to the continuous range {0,1}
            B * move.getEvaluation()
                
            //uct term
            + C * Math.sqrt( (Math.log(totalNumSimulations+epsilon)) / (move.getNumSimulations()+epsilon)) 
                
            //uncertainty term 
            //sqrt( (sum of the squared results so far - expected results + large constant ensures unexplored nodes are considered uncertain) / number of simulations)
            //sum of (result1^2 + result2^2...)    
            + E * Math.sqrt((Math.pow(move.getEvaluation()*move.getNumSimulations(),2.0) - move.getNumSimulations()*Math.pow(move.getEvaluation(), 2.0) + D) 
                    / (move.getNumSimulations()+epsilon));
        
        return uctValue;
    }
}
