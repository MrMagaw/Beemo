
package pokersquares.trainers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import pokersquares.algorithms.Simulator;
import pokersquares.config.Settings;
import static pokersquares.config.Settings.Environment.system;
import pokersquares.environment.Board;
import pokersquares.environment.Card;
import pokersquares.environment.Hand;
import static pokersquares.evaluations.PatternPolicy.buildPattern;
import static pokersquares.evaluations.PatternPolicy.decodePattern;
import static pokersquares.evaluations.PatternPolicy.patternEvaluations;

public class Billy implements Trainer {
    
    //Billy uses hand classification and monte carlo sampling to assign hands an average value
    public static Map<Integer, Double> bestPatternEvaluations = new java.util.HashMap();
    double bestScore = Double.NEGATIVE_INFINITY;

    @Override
    public void update() {
        patternEvaluations.clear();
        bestPatternEvaluations.keySet().stream().forEach((p) -> {
            patternEvaluations.put(p, bestPatternEvaluations.get(p));
        });
        pokersquares.config.PatternReader.writePatterns(Settings.Training.patternsFileOut, bestPatternEvaluations);  
    }
    
    private class PatternScore {
        public double totalScore = 0;
        public int numTrials = 0;
        public Hand h;
    }
    
    @Override
    public void runSession(long millis) {
		long tBuffer = millis - 1000 + System.currentTimeMillis(); //Some amount of millis to make sure we dont exceed alotted millis        
		System.out.print("\nBilly is in your Mind\n");
        Random r = new Random();
        
        HashMap <Integer, PatternScore> patternScores = new HashMap();
        
        //SET BENCHMARK
        bestScore = Simulator.simulate(new Board(), 10000, 10000, 1) / 10000;
        bestPatternEvaluations = new java.util.HashMap(patternEvaluations);
        
        //SIMULATE Games
        int trials = 0 ;
        int nextCheck = 8192;
        int nextNextCheck = 65536;
        //double trialScore = 0;
        while(System.currentTimeMillis() < tBuffer) {
        //while(true){
            Board b = new Board();
            List <List> boardPatterns = initBoardPatterns();
            
            //Simulate a Game
            while (b.getTurn() < 25) {
                 Card c = b.getDeck().remove(r.nextInt(b.getDeck().size())); 
                //Card c = b.getDeck().remove((trials + 52) % b.getDeck().size()); 
                
                int[] p = Settings.Algorithms.simAlgorithm.search(c, b, millis);
                b.playCard(c, p);
                
                //CLASSIFY Hands
                classifyHands(b, boardPatterns);
            }
            //SCORE and UPDATE Pattern Scores
            mapScores(b, boardPatterns, patternScores, trials <= 100000);
            
            if (trials % nextCheck == 0) {
                double score = refreshScores(patternScores);
                System.out.println(
                        "Trials: " + trials + 
                        "\tScore: " + score + 
                        "\tBest Score: " + bestScore);
                if (trials >= nextNextCheck){
                    nextCheck = nextNextCheck;
                    nextNextCheck <<= 3;
                }
            }
            ++trials;
            double trialScore = Settings.Environment.system.getScore(b.getGrid());
            
            if (trials % 10000 == 0) {
                int tpt = 0;
                for (PatternScore ps : patternScores.values()) {
                    //System.out.println("ps: " + ps.totalScore/ps.numTrials + " pt: " + ps.numTrials);
                    tpt += ps.numTrials;
                }
                System.out.println(
                        "Trials: " + trials + 
                        " Score: " + (Simulator.simulate(new Board(), 10000, millis, 1) / 10000) + 
                        " Best Score: " + bestScore);
                
                System.out.println(
                        "Trials: " + trials + 
                        " Score: " + trialScore/trials);
               
                System.out.println(
                        "Average Pattern Trials: " + (tpt/patternScores.size()) + 
                        " Number of Patterns: " + patternScores.size());
                
                if (Settings.Training.verbose) debugPatternScores(patternScores);
            }
        }
        /*
        //SET BEST SCORE
        patternEvaluations.clear();
        for (Integer p : bestPatternEvaluations.keySet()) {
            patternEvaluations.put(p, bestPatternEvaluations.get(p));
        }
        
        pokersquares.config.PatternReader.writePatterns(Settings.Training.patternsFileOut, bestPatternEvaluations);
        */
    }
    
    private double refreshScores(HashMap <Integer,PatternScore> patternScores) { 
        //map all the scores in pattern scores to pattern valuations and 
        //refresh pattern scores
        patternEvaluations.clear();
        //PUT scores into patternEvaluations
        for (Integer p : patternScores.keySet()) {
            PatternScore ps = patternScores.get(p);
            patternEvaluations.put(p, (ps.totalScore / ps.numTrials));
        }
        
        //TEST CURRENT SCORES
        double score = Simulator.simulate(new Board(), 10000, 10000, 1) / 10000;
        if (score > bestScore) {
            System.out.print("*");
            bestScore = score;
            bestPatternEvaluations.clear();
            for (Integer p : patternScores.keySet()) {
                bestPatternEvaluations.put(p,patternEvaluations.get(p));
            }
            pokersquares.config.PatternReader.writePatterns(Settings.Training.patternsFileOut, bestPatternEvaluations);
        }
        
        //CLEAR patternScores
        patternScores.clear();
        return score;
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
    
    public void debugPatternScores(HashMap <Integer,PatternScore> patternScores) {
        
        System.out.println("\n" + patternScores.size() + " Pattern Scores:");
        
        Map <String, PatternScore> sorted = new TreeMap <String, PatternScore> ();
                
        //SORT Patterns for at least a little bit of catagorical order
        for (Integer p : patternScores.keySet()) {
            PatternScore val = patternScores.get(p);
            
            String code = decodePattern(p);
            
            sorted.put (code, val);
        }
        
        for (String code : sorted.keySet()) {
            PatternScore val = sorted.get(code);
            
            System.out.println(code + " Trials: " + val.numTrials + "   \tAverage Score: " + val.totalScore/val.numTrials);
        }
    }
    
}
