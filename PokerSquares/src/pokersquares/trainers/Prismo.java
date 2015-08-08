
package pokersquares.trainers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.TreeMap;
import pokersquares.algorithms.Simulator;
import pokersquares.config.Settings;
import static pokersquares.config.Settings.BMO.readPatterns;
import static pokersquares.config.Settings.Environment.system;
import pokersquares.environment.Board;
import pokersquares.environment.Card;
import pokersquares.environment.Hand;
import pokersquares.evaluations.PatternPolicy;
import static pokersquares.evaluations.PatternPolicy.buildPattern;
import static pokersquares.evaluations.PatternPolicy.decodePattern;
import static pokersquares.evaluations.PatternPolicy.patternEvaluations;
import static pokersquares.trainers.Billy.bestPatternEvaluations;

public class Prismo implements Trainer{
    public static Map<Integer, Double> bestPatternEvaluations = new java.util.HashMap();
    double bestScore = Double.NEGATIVE_INFINITY;
    int trials = 0;
    double maxHandScore = Double.NEGATIVE_INFINITY;
    double minHandScore = Double.POSITIVE_INFINITY;
    static double epsilon = 0.00000001; //Some Small Number
    public static boolean mapUCT = false;
    
    static double sampleRatio = 0.01; //the latest (trials * sR ) scores are kept
    
    private class SummedMap {
        //Keep track of pattern scores within the current range
        public Map <Integer, Double> scores = new HashMap <Integer, Double> ();
        public double totalScore = 0;
        public double numTrials = 0;
        
        public void add(double score) {
            scores.put(trials ,score);
            totalScore += score;
            ++numTrials;
        }
        
        public void drop(int trial) { 
            if (scores.containsKey(trial)) {
                //System.out.println("DROPPED " + trial);
                totalScore -= scores.remove(trial); 
                --numTrials;
            }
        }
        
        public double sum() { return totalScore / size(); }
        public double size() { return numTrials; }
        public void debug() { scores.toString(); }
    }
    
    @Override
    public String getName () { return "Prismo"; }
    
    @Override
    public Map getBestPatterns () { return bestPatternEvaluations; }
    
    @Override
    public void runSession(long millis) {
        long tBuffer = millis - 1000 + System.currentTimeMillis(); //Some amount of millis to make sure we dont exceed alotted millis        
	System.out.print("\nPrismo Forever\n");
        Random r = new Random();
        
        HashMap <Integer, SummedMap> patternScores = new HashMap();
        SummedMap sampleScore = new SummedMap();
        
        //DETERMINE Max and Min Hand Scores
        for (Integer score : system.getScoreTable()) {
            maxHandScore = (score > maxHandScore) ? score : maxHandScore;
            minHandScore = (score < minHandScore) ? score : minHandScore;
        }
        
        //SET BENCHMARK
        if (readPatterns) {
            bestScore = Simulator.simulate(new Board(), 10000, 10000, 1);
            bestPatternEvaluations = new java.util.HashMap(patternEvaluations);
            patternEvaluations.clear();
        }
        else bestScore = Double.NEGATIVE_INFINITY;
        System.out.println("Best Score: " + bestScore);
        patternScores.clear();
        
        //SIMULATE Games
        trials = 0;
        int reportInterval = 1000;
        
        //WHILE time remains to train
        while(System.currentTimeMillis() < tBuffer) {
            //SIMULATE a Game
            Board b = new Board();
            List <List> boardPatterns = initBoardPatterns();
            
            //SHUFFLE deck
            Stack<Card> deck = new Stack<Card>();
            for (Card card : Card.getAllCards())
                    deck.push(card);
            Collections.shuffle(deck, r);
            
            //RUN trial
            while (b.getTurn() < 25) {
                Card c = deck.pop();
                b.removeCard(c);
                
                int[] p = Settings.Algorithms.simAlgorithm.search(c, b, millis);
                b.playCard(c, p);
                
                //CLASSIFY Hands
                classifyHands(b, boardPatterns);
            }
            
            sampleScore.add(system.getScore(b.getGrid()));
            
            //SCORE and UPDATE Pattern Scores
            mapScores(b, boardPatterns, patternScores, trials < 5000000);
            //DROP oldest Scores
            int i = (int) Math.floor(trials * (1 - sampleRatio));
            sampleScore.drop(i);
            dropScores(patternScores, i);
            
            ++trials;
            
            if (trials % reportInterval == 0) {
                System.out.println(
                        "Trials: " + trials + 
                        "\tRelevant Trials: " + trials * sampleRatio + 
                        "\tScore: " /*+ Simulator.simulate(new Board(), 10000, 10000, 1)*/ + " " + sampleScore.sum());
                
                System.out.println(
                        "Number of Patterns: " + patternScores.size());
                
                if (Settings.Training.verbose) {
                    debugPatternScores(patternScores);
                    PatternPolicy.debug();
                }
                
            }
        }
    }
    
    private void dropScores(HashMap <Integer, SummedMap> patternScores, int i) {
        for (SummedMap ps : patternScores.values()) {
            ps.drop(i);
        }
    }
    
    private List classifyHands(Board b, List <List> bp) {
        //classify hands 
        for (int h = 0; h < 10; ++h) {
            Hand hand  = b.hands.get(h);
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
    
    private void mapScores(Board b, List <List> bp, HashMap <Integer,SummedMap> patternScores, boolean update) {
        
        //SCORE and UPDATE hands 
        for (int h = 0; h < 10; ++h) {
            Hand hand  = b.hands.get(h);
            
            //SCORE hand
            double score = system.getHandScore(hand.getCards());
            
            //UPDATE Pattern Scores
            for (Integer p : (List <Integer>) bp.get(h)) {
                SummedMap ps;
                
                //if pattern is not already mapped
                if (!patternScores.containsKey(p)) {
                    ps = new SummedMap();
                    patternScores.put(p, ps);
                } else ps = patternScores.get(p);
        
                ps.add(score);
                
                double uctScore = //average simulation value of a node scaled to the continuous range {0,1}
                    scale(ps.sum())  
                    //uct term
                    + 1.000 * Math.sqrt( Math.abs(Math.log(trials + epsilon)) / (ps.size() + epsilon));
                
                //Update Pattern Evaluations WHY IS THAT FIRST IF NEEDED??????? Wats going yo
                /*
                if (update && (!(hand.isCol && (hand.numSuits > 1)))) {
                    if (mapUCT) patternEvaluations.put(p, uctScore);
                    else patternEvaluations.put(p, ps.sum());
                }*/
                patternEvaluations.put(p, ps.sum());
                //if (!(hand.isCol && (hand.numSuits > 1))) patternEvaluations.put(p, ps.getPatternScore());
                //if (!(hand.isCol && (hand.numSuits > 1))) patternEvaluations.put(p, ps.sum());
                //if (update) patternEvaluations.put(p, uctScore);
                //else patternEvaluations.put(p, ps.getPatternScore());
                //if (!(hand.isCol && (hand.numSuits > 1))) if (update) patternEvaluations.put(p, ps.getPatternScore());
            }
        }
    } 
    
    private double scale(double x ){
        //scales x in the range of [min, max] hand scores to [0,1]
        return ((x - minHandScore) / (maxHandScore - minHandScore + epsilon));
    }
    
    public void debugPatternScores(HashMap <Integer,SummedMap> patternScores) {
        
        System.out.println("\n" + patternScores.size() + " Pattern Scores:");
        
        Map <String, SummedMap> sorted = new TreeMap <String, SummedMap> ();
                
        //SORT Patterns for at least a little bit of catagorical order
        for (Integer p : patternScores.keySet()) {
            SummedMap val = patternScores.get(p);
            
            String code = decodePattern(p);
            
            sorted.put (code, val);
        }
        
        for (String code : sorted.keySet()) {
            SummedMap val = sorted.get(code);
            
            System.out.println(code + "\tTrials: " + val.size() + "\tAverage Score: " + val.sum());
        }
    }
    
}
