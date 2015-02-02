
package pokersquares.trainers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import pokersquares.algorithms.Simulator;
import pokersquares.config.Settings;
import static pokersquares.config.Settings.Environment.system;
import pokersquares.environment.Board;
import pokersquares.environment.Card;
import pokersquares.environment.Hand;
import static pokersquares.evaluations.PatternPolicy.buildPattern;
import static pokersquares.evaluations.PatternPolicy.patternEvaluations;
import static pokersquares.trainers.ValueReinforcement.scoreGames;

public class Billy implements Trainer {
    
    //Billy uses hand classification and monte carlo sampling to assign hands an average value
    
    private class PatternScore {
        public double totalScore = 0;
        public int numTrials = 0;
        public Hand h;
    }
    
    public Billy () { }
    
    @Override
    public void runSession(long millis) {
        System.err.print("\nBilly is in your Mind\n");
        
        Random r = new Random();
        long tStart = System.currentTimeMillis();
        long tBuffer = 1000; //Some amount of millis to make sure we dont exceed alotted millis
        HashMap <Integer, PatternScore> patternScores = new HashMap <Integer, PatternScore> ();
        
        //SIMULATE Games
        
        int trials = 0;
        double trialScore = 0;
        while((System.currentTimeMillis() - tStart) < (millis - tBuffer)) {
            
            Board b = new Board();
            List <List> boardPatterns = initBoardPatterns();
            
            //Simulate a Game
            while (b.getTurn() < 25) {
                Card c = b.getDeck().remove(r.nextInt(b.getDeck().size())); 
                //Card c = b.getDeck().remove((int)( System.currentTimeMillis() - tStart) % b.getDeck().size()); 
                int[] p = Settings.Algorithms.simAlgorithm.search(c, b, millis);
                b.playCard(c, p);
                
                //CLASSIFY Hands
                classifyHands(b, boardPatterns);
            }
            
            boolean update = true;
            if (trials > 100000) update = false;
            
            //SCORE and UPDATE Pattern Scores
            mapScores(b, boardPatterns, patternScores, update);
            
            //REFRESH and Update Pattern Scores in stages
            //if (trials % 1000 == 0) refreshScores(patternScores);
            
            
            if (trials < 100000) {
                //if (trials % 10000 == 0) refreshScores(patternScores);
            }
            else
                if (trials % 200000 == 0) refreshScores(patternScores);
            
            ++trials;
            trialScore += Settings.Environment.system.getScore(b.getGrid());
            if (trials % 10000 == 0) {
                int tpt = 0;
                for (PatternScore ps : patternScores.values()) {
                    //System.out.println("ps: " + ps.totalScore/ps.numTrials + " pt: " + ps.numTrials);
                    tpt += ps.numTrials;
                }
                System.err.println(
                        "Trials: " + trials + 
                        " Score: " + (Simulator.simulate(new Board(), 10000, millis) / 10000));
                /*
                System.err.println(
                        "Trials: " + trials + 
                        " Score: " + trialScore/trials);
                */
                System.err.println(
                        "Average Pattern Trials: " + (tpt/patternScores.size()) + 
                        " Number of Patterns: " + patternScores.size());
                        
            }
        }
    }
    
    private void refreshScores(HashMap <Integer,PatternScore> patternScores) { 
        //map all the scores in pattern scores to pattern valuations and 
        //refresh pattern scores
        patternEvaluations.clear();
        //PUT scores into patternEvaluations
        for (Integer p : patternScores.keySet()) {
            PatternScore ps = patternScores.get(p);
            patternEvaluations.put(p, (ps.totalScore / ps.numTrials));
        }
        
        //CLEAR patternScores
        patternScores.clear();
    }
    
    private void mapScores(Board b, List <List> bp, HashMap <Integer,PatternScore> patternScores, boolean update) {
        
        //SCORE and UPDATE hands 
        for (int h = 0; h < 10; ++h) {
            Hand hand  = b.hands.get(h);
            
            //SCORE hand
            double score = system.getHandScore(hand.getCards());
            
            //UPDATE Pattern Scores
            for (Integer p : (List <Integer>) bp.get(h)) {
                PatternScore ps;
                
                //if pattern is not already mapped
                if (!patternScores.containsKey(p)) {
                    ps = new PatternScore();
                    patternScores.put(p, ps);
                } else ps = patternScores.get(p);
                
                
                ps.totalScore += score;
                ++ps.numTrials;
                
                //Update Pattern Evaluations
                if (!(hand.isCol && (hand.numSuits > 1))) if (update) patternEvaluations.put(p, (ps.totalScore / ps.numTrials));
                //if (update) patternEvaluations.put(p, (ps.totalScore / ps.numTrials));
            }
        }
    } 
    
    private List classifyHands(Board b, List <List> bp) {
        
        //classify hands 
        for (int h = 0; h < 10; ++h) {
            Hand hand  = b.hands.get(h);
            
            hand.buildRankCounts();
            hand.checkStraight();
            buildPattern(hand);
            if (!bp.get(h).contains(hand.getPattern()))
                bp.get(h).add(hand.getPattern());
            
        }
        
        return bp;
    }
    
    private List initBoardPatterns() {
        List <List> boardPatterns = new ArrayList <List> ();
        
        for (int i = 0; i < 10; ++i)
            boardPatterns.add(new ArrayList <Integer> ());
        
        return boardPatterns;
    }
    
}
