
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
    //Prismo Uses (roughly) the same system as Billy but also uses UCT to better distribute the plays
    public static Map<Integer, Double> bestPatternEvaluations = new java.util.HashMap();
    double bestScore = Double.NEGATIVE_INFINITY;
    int trials;
    double maxHandScore = Double.NEGATIVE_INFINITY;
    static double epsilon = 0.00000001; //Some Small Number
    int tpt = 0;
    
    static double scoreDropRate = 3;
    
    private class PatternScore {
        public ArrayList <Double> scores = new ArrayList <Double> ();
        public double totalScore = 0;
        public int numTrials = 0;
        public int iNextDroppedScore = 0;
        public Hand h;
        
        public void add(double score) {
            scores.add(score);
            numTrials = scores.size();
            totalScore += score;
            ++ tpt;
            
            if ((numTrials > 0) && !(numTrials % scoreDropRate == 0)){
                totalScore -= scores.get(iNextDroppedScore);
                ++iNextDroppedScore;
                --tpt;
            }
        }
        
        public double getPatternScore() {
            return totalScore / getNumRelevantScores();
        }
        
        public double getNumRelevantScores() { return numTrials - iNextDroppedScore + epsilon; }
        
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
        
        HashMap <Integer, PatternScore> patternScores = new HashMap();
        
        //DETERMINE Highest Hand Score
        for (Integer score : system.getScoreTable())
            maxHandScore = (score > maxHandScore) ? score : maxHandScore;
        
        //SIMULATE Games
        trials = 0 ;
        double sampleScore = 0;
        int reportInterval = 1000;
        
        //WHILE time remains to train
        while(System.currentTimeMillis() < tBuffer) {
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
            
            //SCORE and UPDATE Pattern Scores
            mapScores(b, boardPatterns, patternScores, trials <= 500000);
            
            ++trials;
            sampleScore += Settings.Environment.system.getScore(b.getGrid());
            
            if (trials % reportInterval == 0) {
                System.out.println(
                        "Trials: " + trials + 
                        " Score: " + (sampleScore / reportInterval));
                
                sampleScore = 0;
                
                System.out.println(
                        "Average Pattern Trials: " + (tpt/patternScores.size()) + 
                        " Number of Patterns: " + patternScores.size());
                
                if (Settings.Training.verbose) {
                    debugPatternScores(patternScores);
                    PatternPolicy.debug();
                }
                
            }
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
        
                ps.add(score);
                
                double uctScore =     //average simulation value of a node scaled to the continuous range {0,1}
                    //ps.getPatternScore() * 1 / (maxHandScore + epsilon)
                    ps.getPatternScore()    
                    //uct term
                    + maxHandScore * 0.5 * Math.sqrt( Math.abs(Math.log(trials + epsilon)) / (ps.getNumRelevantScores() + epsilon));
                
                //Update Pattern Evaluations WHY IS THAT FIRST IF NEEDED??????? Wats going yo
                patternEvaluations.put(p, ps.getPatternScore());
                //if (!(hand.isCol && (hand.numSuits > 1))) patternEvaluations.put(p, ps.getPatternScore());
                //if (update) patternEvaluations.put(p, uctScore);
                //else patternEvaluations.put(p, ps.getPatternScore());
                //if (!(hand.isCol && (hand.numSuits > 1))) if (update) patternEvaluations.put(p, ps.getPatternScore());
            }
        }
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
            
            System.out.println(code + "\tTrials: " + val.numTrials + "\tRelevant Trials: " + val.getNumRelevantScores() + "\tAverage Score: " + val.getPatternScore());
        }
    }
    
}
