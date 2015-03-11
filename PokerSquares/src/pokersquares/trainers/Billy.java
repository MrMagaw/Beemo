
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
import pokersquares.evaluations.PatternPolicy;
import static pokersquares.evaluations.PatternPolicy.buildPattern;
import static pokersquares.evaluations.PatternPolicy.decodePattern;
import static pokersquares.evaluations.PatternPolicy.patternEvaluations;
import static pokersquares.trainers.Prismo.epsilon;

public class Billy implements Trainer {
    
    //Billy uses hand classification and monte carlo sampling to assign hands an average value
    public static Map<Integer, Double> bestPatternEvaluations = new java.util.HashMap();
    double bestScore = Double.NEGATIVE_INFINITY;
    double maxHandScore = Double.NEGATIVE_INFINITY;
    int trials = 0;
    
    boolean mapUCT = true;
    
    public Billy() {
        new PatternPolicy();
    }
    
    @Override
    public void update() {
        System.out.println("UPDATING");
        
        patternEvaluations.clear();
        patternEvaluations = new HashMap <Integer, Double> (bestPatternEvaluations);
        
        
        /*
        bestPatternEvaluations.keySet().stream().forEach((p) -> {
            patternEvaluations.put(p, bestPatternEvaluations.get(p));
        });
        */
        
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
        
        //DETERMINE Highest Hand Score
        for (Integer score : system.getScoreTable())
            maxHandScore = (score > maxHandScore) ? score : maxHandScore;
        
        //SET BENCHMARK
        bestScore = Simulator.simulate(new Board(), 10000, 10000, 1);
        bestPatternEvaluations = new java.util.HashMap(patternEvaluations);
        
        //SIMULATE Games
        //int nextCheck = 8192;
        //int nextNextCheck = 65536;
        int nextCheck = 10000;
        int nextNextCheck = Integer.MAX_VALUE;
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
                
                //System.out.println(Simulator.simulate(new Board(), 10000, 10000, trials));
                
                Map place = new HashMap <Integer, Double> (patternEvaluations);
                patternEvaluations = new HashMap <Integer, Double> (bestPatternEvaluations);
                
                //System.out.println(Simulator.simulate(new Board(), 10000, 10000, trials));
                
                patternEvaluations = new HashMap <Integer, Double> (place);
                
                //System.out.println(Simulator.simulate(new Board(), 10000, 10000, trials));
                
                if (trials >= nextNextCheck){
                    nextCheck = nextNextCheck;
                    nextNextCheck <<= 3;
                }
            }
            
            ++trials;
            
            if (trials % 10000 == 0) {
                //System.out.println(Simulator.simulate(new Board(), 10000, 10000, trials + 10000) / 10000);
                /*
                System.out.println(
                        "Trials: " + trials + 
                        "\tScore: " + (Simulator.simulate(new Board(), 10000, millis, trials) / 10000) + 
                        "\tBest Score: " + bestScore + 
                        "\tNumber of Patterns: " + patternScores.size());
                */
                if (Settings.Training.verbose) debugPatternScores(patternScores);
                if (Settings.Training.verbose) PatternPolicy.debug();
            }
        }
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
        
        /*
        Map place = new HashMap <Integer, Double> (patternEvaluations);
        patternEvaluations = new HashMap <Integer, Double> (bestPatternEvaluations);
        bestScore = Simulator.simulate(new Board(), 10000, 10000, trials);
        patternEvaluations = new HashMap <Integer, Double> (place);
        */
        
        //TEST CURRENT SCORES
        double score = Simulator.simulate(new Board(), 10000, 10000, trials);
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
                
                
                double uctScore =     //average simulation value of a node scaled to the continuous range {0,1}
                    score * 1 / (maxHandScore + epsilon) 
                    //uct term
                    +1 * Math.sqrt( Math.abs(Math.log(trials + epsilon)) / (ps.numTrials + epsilon));
                
                //Update Pattern Evaluations
                if (!(hand.isCol && (hand.numSuits > 1))) if (update) 
                    if (mapUCT) patternEvaluations.put(p, uctScore);
                    else patternEvaluations.put(p, (ps.totalScore / ps.numTrials));
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
