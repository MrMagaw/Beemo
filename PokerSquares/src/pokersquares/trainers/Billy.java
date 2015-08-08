
package pokersquares.trainers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Stack;
import java.util.TreeMap;
import pokersquares.algorithms.Simulator;
import pokersquares.config.Settings;
import static pokersquares.config.Settings.Environment.system;
import pokersquares.environment.Board;
import pokersquares.environment.Card;
import pokersquares.environment.Hand;
import pokersquares.environment.PokerSquares;
import pokersquares.evaluations.PatternPolicy;
import static pokersquares.evaluations.PatternPolicy.buildPattern;
import static pokersquares.evaluations.PatternPolicy.decodePattern;
import static pokersquares.evaluations.PatternPolicy.patternEvaluations;
import pokersquares.players.BeemoV2;
import static pokersquares.trainers.Prismo.bestPatternEvaluations;
import static pokersquares.trainers.Prismo.epsilon;

public class Billy implements Trainer {
    //Billy uses hand classification and monte carlo sampling to assign hands an average value
    public static Map<Integer, Double> bestPatternEvaluations = new java.util.HashMap();
    double bestScore = Double.NEGATIVE_INFINITY;
    HashMap <Integer, PatternScore> patternScores;

    double maxHandScore = Double.NEGATIVE_INFINITY;
    double minHandScore = Double.POSITIVE_INFINITY;
    int trials = 0;
    int trainingInterval = 5000;
    boolean mapUCT = true;
    
    private class PatternScore {
        public double totalScore = 0;
        public int numTrials = 0;
        public Hand h;
    }
    
    @Override
    public String getName () { return "Billy"; }
    
    @Override
    public Map getBestPatterns () { return bestPatternEvaluations; }
    
    @Override
    public void runSession(long millis) {
        long tBuffer = millis - 1000 + System.currentTimeMillis(); //Some amount of millis to make sure we dont exceed alotted millis        
	System.out.print("\nBilly is in your Mind\n");
        Random r = new Random();
        
        patternScores = new HashMap();
        
        //DETERMINE Max and Min Hand Scores
        for (Integer score : system.getScoreTable()) {
            maxHandScore = (score > maxHandScore) ? score : maxHandScore;
            minHandScore = (score < minHandScore) ? score : minHandScore;
        }
        
        //SET BENCHMARK
        bestScore = Simulator.simulate(new Board(), 10000, 10000, 1);
 
        System.out.println("Best Score: " + bestScore);
        bestPatternEvaluations = new java.util.HashMap(patternEvaluations);
        patternEvaluations.clear();
        patternScores.clear();
        
        //SIMULATE Games
        int nextCheck = trainingInterval;
        int nextNextCheck = Integer.MAX_VALUE;
        //int nextNextCheck = nextCheck * 2;
        
        while(System.currentTimeMillis() < tBuffer) {
            
            //SIMULATE a Game
            Board b = new Board();
            List <List> boardPatterns = initBoardPatterns();
            //SHUFFLE deck
            Stack<Card> deck = new Stack<Card>();
            for (Card card : Card.getAllCards())
                    deck.push(card);
            Collections.shuffle(deck, r);
            
            while (b.getTurn() < 25) {
                Card c = deck.pop();
                b.removeCard(c);
                
                int[] p = Settings.Algorithms.simAlgorithm.search(c, b, millis);
                b.playCard(c, p);
                
                //CLASSIFY Hands
                classifyHands(b, boardPatterns);
            }
            
            //SCORE and UPDATE Pattern Scores
            mapScores(b, boardPatterns, patternScores, trials <= 10000000);
            if ((++trials % nextCheck) == 0) {
                double score = refreshScores(patternScores);
                System.out.println(
                        "Trials: " + trials + 
                        "\tPatterns: " + patternScores.size() +
                        "\tScore: " + score + 
                        "\tBest Score: " + bestScore);
                
                patternEvaluations.clear();
                patternScores.clear();
                
                if (trials >= nextNextCheck){
                    nextCheck = nextNextCheck;
                    nextNextCheck *= 2;
                }
            }
        }
        
        patternEvaluations.putAll(bestPatternEvaluations);
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
                
                //average simulation value of a node scaled to the continuous range {0,1}
                double uctScore = scale(score);
                //uct term (AMERICAN:small coeff, FLUSH: 0 coeff, STRAIGHT: 0, FH: .01, )
                double uctTerm; 
                
                //SOMETIMES 0.001 is ideal, sometimes 0 is ideal
                int trialSector = (int) Math.floor(trials/trainingInterval)%3;
                if (trialSector < 2) uctTerm =  0.0001 * Math.pow( Math.abs(Math.log(trials%trainingInterval + epsilon)) / (ps.numTrials + epsilon), 0.5);
                else uctTerm = 0;
                uctScore += uctTerm;
                
                if (trialSector == 2) patternEvaluations.put(p, (ps.totalScore / ps.numTrials));
                else if (!(hand.isCol && (hand.numSuits > 1))) patternEvaluations.put(p, uctScore);
                
            }
        }
    } 
    
    private double refreshScores(HashMap <Integer,PatternScore> patternScores) { 
        //map all the scores in pattern scores to pattern evaluations and 
        //refresh pattern scores
        patternEvaluations.clear();
        //PUT scores into patternEvaluations
        for(Entry<Integer, PatternScore> e : patternScores.entrySet()) {
            Integer p = e.getKey();
            PatternScore ps = e.getValue();
            Double score = ps.totalScore / ps.numTrials;
            patternEvaluations.put(p, score);
        }
        
        //TEST CURRENT SCORES
        double score = Simulator.simulate(new Board(), 10000, 10000, 10000);
        if (score > bestScore) {
            System.out.print("*");
            bestScore = score;
            bestPatternEvaluations.clear();
            bestPatternEvaluations.putAll(patternEvaluations);
            pokersquares.config.PatternReader.writePatterns(bestPatternEvaluations);
        }
        
        //CLEAR Scores
        patternEvaluations = new HashMap();
        patternScores = new HashMap();
        patternEvaluations.clear();
        patternScores.clear();
        return score;
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
        List <List> boardPatterns = new ArrayList();
        
        for (int i = 0; i < 10; ++i)
            boardPatterns.add(new ArrayList());
        
        return boardPatterns;
    }
    
    private double scale(double x ){
        //scales x in the range of [min, max] hand scores to [0,1]
        return ((x - minHandScore) / (maxHandScore - minHandScore + epsilon));
    }
    
    private void debugPatternScores(HashMap <Integer,PatternScore> patternScores) {
        
        System.out.println("\n" + patternScores.size() + " Pattern Scores:");
        
        Map <String, PatternScore> sorted = new TreeMap();
                
        //SORT Patterns for at least a little bit of catagorical order
        patternScores.keySet().stream().forEach((p) -> {
            PatternScore val = patternScores.get(p);
            
            String code = decodePattern(p);
            
            sorted.put (code, val);
        });
        
        sorted.keySet().stream().forEach((code) -> {
            PatternScore val = sorted.get(code);
            
            System.out.println(code + " Trials: " + val.numTrials + "   \tAverage Score: " + val.totalScore/val.numTrials);
        });
    }
}
