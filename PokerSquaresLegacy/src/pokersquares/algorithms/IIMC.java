package pokersquares.algorithms;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import pokersquares.players.Beemo;
import pokersquares.Card;
import pokersquares.Play;
import pokersquares.montecarlo.ElapsedTimer;

/**
 *
 * @author newuser
 */
public class IIMC extends Algorithm{

    public IIMC(Beemo parent) {
        super(parent);
    }
    
    @Override
    public int[] search(Card card, long millisRemaining) {
        //MONTE CARLO TREE SEARCH 
        //Based on Buro and Furtak's IIMC algorithm
        //Imperfect Information Monte Carlo
        
        int[] bestPos = new int[2];
        
        /*
        //OPTIMIZATION always play the first card in the center
        if (BMO.getTurn() == 0) {
            bestPos[0] = 2;
            bestPos[1] = card.getSuit();
            return bestPos;
        }
        */
        
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
    
    public List genPlays(Card card, Play play, List generatedPlays) {
        
        //FOR EACH POS
        LinkedList <Play> topPlays = new LinkedList();
        for (Integer pos : BMO.getPlayPos()) {
            
            //SKIP PLAYED POS
            if (!play.getPlayedPositions().contains(pos)) {
                //DECODE pos
                int row = pos/5;
                int col = pos%5;
                
                //MAKE A NEW PLAY
                Play newPlay = new Play(play);
                        
                //RECORD PLAY + EVALUATE
                newPlay.recordPlayPos(card, pos);
                newPlay.evaluateHands(row, col);
                newPlay.updateEvaluation(row, col);
                
                topPlays.add(newPlay); 
                
            }
        }
        
        Collections.sort(topPlays);
        
        for (int i = 0; i < BMO.getPlaySampleSize(); i++) {
            if (topPlays.size() == 0) break;
            generatedPlays.add(topPlays.removeFirst());
            //System.out.println(((Play) generatedPlays.get(i)).getPlayedPositions().get(0));
        }
        
        return generatedPlays;
    }
    
    public int[] runSimulations(List <Play>  moves, long millisRemaining) {
        int[] bestPos;
        boolean timeLeft = true;
        Collections.sort(moves);
        
        //WHILE TIME IS LEFT
        while(timeLeft) {
            //FOR EACH MOVE
            for (Play move : moves) {
                //RECORD RESULT OF SIMULATION
                //move.simulateGBFGame(t.elapsed());
                //move.simulateRandomGame(BMO.getTimer().elapsed());
                //move.simulateFlushGame(BMO.getTimer().elapsed());
                //move.simulateRBGame(BMO.getTimer().elapsed());
                move.simulateRB2Game(BMO.getTimer().elapsed());
                //move.simulateOXGame(BMO.getTimer().elapsed());
                //move.simulateGame("grb",BMO.getTimer().elapsed());
                
                //TERMINATION CONDITIONS
                
                //TIME RUNS OUT
                //if (BMO.getTimer().elapsed() > ((BMO.getTimeCoefficient() * millisRemaining*0.4)-150)) timeLeft = false; //Negative Concave Time
                //if (BMO.getTimer().elapsed() > (BMO.getTimeCoefficient() * (BMO.getTotalMillis()/(15+(BMO.getTurn()%25)))-100)) timeLeft = false; //Flat Time
                
                //TARGET SAMPLE SIZE REACHED
                if (move.getNumSimulations() >= BMO.getMonteCarloSampleSize()) timeLeft = false;
            }
        }
        
        //UPDATE EVALUATION
        //as the average of all simulations from that move
        for (Play move : moves) {
            move.updateSimAve();
        }
        
        //CHOOSE BEST MOVE
        Collections.sort(moves);
        Play best = moves.get(0);
        
        //EVALUATE SAMPLING
        /*
        //print num sim and their corresponding evalutions
        for (Play move : moves) {
            System.out.println("numSim: " + move.getNumSimulations() + " sum: " + move.getSimulationSum());
            System.out.println(move.getEvaluation() +" "+ move.getStaticEval()+" " + move.getSimEval()+" "+ move.getPlayedPositions().get(0));
        }
        */
        
        bestPos = best.getPos();
        
        return bestPos;
    }    
}
