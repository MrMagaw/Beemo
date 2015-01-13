package pokersquares.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import pokersquares.players.Beemo;
import pokersquares.Card;
import pokersquares.Play;

public class OXMC extends Algorithm{
    public OXMC(Beemo parent) {
        super(parent);
    }
    @Override
    public int[] search(Card card, long millisRemaining) {
        
        //OPTIMIZED XPECTIMAX SEARCH version II
        int[] bestPos = new int[2];
        
        //Start turn.
        if (BMO.getTurn() == 0) {
            bestPos[0] = 2;
            bestPos[1] = 2;
            return bestPos;
        }
        
        List <Play> generatedPlays = new ArrayList((25^BMO.getSearchDepth())*(BMO.getDeckSampleSize()^BMO.getSearchDepth())); //generated play array of search depth size
        
        Play root = new Play(BMO);
        root.buildAndEvaluateHands();
        root.evaluatePlay();
        Play play = root;
        
        //OPTIMIZATION always play the first card in the center
        
        
        //GENERATE PLAYS up to search depth
        int maxLVL = 0;
        int iPlay = -1;
        while (thereIsTimeLeft() && (play.getLVL() < BMO.getSearchDepth())) {
            //GENERATE PLAYS
            if (play.getLVL() == 0) genPlaysForLVL0(card, play, generatedPlays);
            else genPlaysFor(play, generatedPlays, maxLVL);
            
            //INCREMENT to next play
            iPlay += 1;
            if (generatedPlays.size() <= iPlay) break;
            play = generatedPlays.get(iPlay);
            if (play.getLVL() > maxLVL) maxLVL = play.getLVL();
        } 
        //System.out.println("plays generated: " + generatedPlays.size());
        
        //BACKPROPAGATE EVALUATIONS to the base lvl plays starting from the second lowest level *ASSUMING SEARCH DEPTH WAS REACHED*
        iPlay = backpropagateEvaluations(iPlay, generatedPlays);
        
        //CHOOSE BEST PLAYS
        LinkedList <Play> bestPlays = getBestPlays(generatedPlays, iPlay);
        
        //RUN SIMULATIONS
        bestPos = runSimulations(bestPlays, millisRemaining);
        
        return bestPos;
    }
    
    public List genPlaysForLVL0(Card card, Play play, List generatedPlays) {
        
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
        
        for (int i = 0; i < BMO.getRootPlaySampleSize(); i++) {
            if (topPlays.size() == 0) break;
            generatedPlays.add(topPlays.removeFirst());
        }
        
        return generatedPlays;
    }
    
    public List genPlaysFor(Play play, List generatedPlays, int maxLVL) {
        int playSampleSize = 0;
        //FOR EACH CARD 
        for (Card nextCard : BMO.getDeck()) {
            //SKIP PLAYED CARDS
            if (!play.getPlayedCards().contains(nextCard)) {
                
                LinkedList <Play> topPlays = new LinkedList();
                //FOR EACH POS
                for (Integer pos : BMO.getPlayPos()) {
                    //SKIP PLAYED POS
                    if (!play.getPlayedPositions().contains(pos)) {
                        //DECODE pos
                        int row = pos/5;
                        int col = pos%5;
                        
                        //MAKE A NEW PLAY
                        Play newPlay = new Play(play);
                        
                        //RECORD PLAY + EVALUATE
                        newPlay.recordPlayPos(nextCard, pos);
                        newPlay.evaluateHands(row, col);
                        newPlay.updateEvaluation(row, col);
                        
                        
                        if (newPlay.getLVL() > maxLVL) newPlay.getParent().updatebestCardEvals((Card) newPlay.getPlayedCards().get(0) , newPlay.getEvaluation());
                        
                        topPlays.add(newPlay); 
                    }
                }
                Collections.sort(topPlays);
                for (int i = 0; i < BMO.getBranchPlaySampleSize(); i++) {
                    if (topPlays.size() == 0) break;
                    generatedPlays.add(topPlays.removeFirst());
                }
                
                playSampleSize += 1;
                if (playSampleSize == BMO.getDeckSampleSize()) break;
            }   
        }
        
        return generatedPlays;
    }
    
    public int backpropagateEvaluations(int iPlay, List generatedPlays) {
        //iPlay starts at the first play in lvl searchDepth - 1
        iPlay -= 1;
        Play play = (Play) generatedPlays.get(iPlay);
        
        while (play.getParent().getLVL() != 0) {
            int lvl = play.getLVL();
            
            //CALCULATE POSITION AVERAGES of all plays at each level
            while (play.getLVL() == lvl) {
                //RECORD BEST EVALUATION per card in parent play's bestCardEvals Map
                
                play.propagateEvaluation(); 
                
                //UPDATE PARENT PLAY with evaluation
                play.getParent().updatebestCardEvals((Card) play.getPlayedCards().get(0) , play.getEvaluation());
                
                //ITERATE to next play
                iPlay -= 1;
                play = (Play) generatedPlays.get(iPlay); //iterate through plays from last play generated to root play
                
            }
        }
        //iPlay finishes at the first play of lvl 1
        return iPlay;
    }
    
    public LinkedList getBestPlays(List <Play> generatedPlays, int iPlay) {
        Play play = generatedPlays.get(iPlay);
        Play bestPlay = play;
        
        LinkedList <Play> levelOnePlays = new LinkedList <Play> ();
        
        while (play.getLVL() != 0) {
            play.propagateEvaluation();
            
            //System.out.println(play.getEvaluation());
            if (play.getEvaluation() > bestPlay.getEvaluation()) bestPlay = play;
            
            //STORE level one plays
            levelOnePlays.add(play);
            
            iPlay -= 1;
            if (iPlay == -1) break;
            play = generatedPlays.get(iPlay);
        } 
        
        Collections.sort(levelOnePlays);
        
        LinkedList <Play> topPlays = new LinkedList();
        for (int i = 0; i < BMO.getPlaySampleSize(); i++) {
            if (levelOnePlays.size() == 0) break;
            topPlays.add(levelOnePlays.removeFirst());
            //System.out.println(((Play) topPlays.get(i)).getPlayedPositions().get(0));
        }
        return topPlays;
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
                //move.simulateRandomGame(t.elapsed());
                //move.simulateFlushGame(BMO.getTimer().elapsed());
                //move.simulateRB2Game(BMO.getTimer().elapsed());
                //move.simulateOXGame(BMO.getTimer().elapsed());
                move.simulateGame("grb", BMO.getTimer().elapsed());
                
                //TERMINATION CONDITIONS
                
                //TIME RUNS OUT
                //if (t.elapsed() > ((timeCoefficient * millisRemaining*0.4)-150)) timeLeft = false; //Negative Concave Time
                //if (t.elapsed() > (timeCoefficient * (totalMillis/(15+(turn%25)))-100)) timeLeft = false; //Flat Time
                
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
            System.out.println(move.getEvaluation() +" "+ move.getStaticEval()+" " + move.getSimEval() + " "+ move.getPlayedPositions().get(0));
        }
        */
        
        bestPos = best.getPos();
        
        return bestPos;
    }   
    
    public boolean thereIsTimeLeft() {
        //MODIFY to return true if there is enought time left 
        //to evaluate the next play
        return true;
    }
    
}
