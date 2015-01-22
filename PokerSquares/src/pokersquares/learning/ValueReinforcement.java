/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pokersquares.learning;

import pokersquares.config.Settings;
import pokersquares.environment.PokerSquares;
import pokersquares.players.BeemoV2;
import java.util.*;
import pokersquares.config.SettingsReader;
import pokersquares.environment.Board;
import pokersquares.environment.Card;

/**
 *
 * @author newuser
 */
public class ValueReinforcement {
    
    public static void runSession(long millis){
        
        long tStart = System.currentTimeMillis();
        
        //VALUES to be adjusted
        List <double[]> values = new ArrayList <double[]> ();
        
        values.add((double[])Settings.Evaluations.exps);
        values.add(Settings.Evaluations.colHands); 
        values.add(Settings.Evaluations.rowHands);
        values.add(Settings.Evaluations.highCardPolicy);
        values.add(Settings.Evaluations.pairPolicy);
        values.add(Settings.Evaluations.twoPairPolicy);
        values.add(Settings.Evaluations.threeOfAKindPolicy);
        values.add(Settings.Evaluations.flushPolicy);
        values.add(Settings.Evaluations.fullHousePolicy);
        values.add(Settings.Evaluations.fourOfAKindPolicy);
        
        int i = 0,j = 0;
        int valuesToTrain = values.size() - 1;
        boolean systemChanged = false;
        //WHILE there is time left, continue training
        while ((System.currentTimeMillis() - tStart) < millis) {
            
            //TRAIN Value
            //if value is successfully trained, the system has changed
            if (i == 0) {
                //pass
            } else if (i < 3) {
                systemChanged = trainHandCombinations(values, i);
            } else {
                systemChanged = trainValuesIncrementally(values, i, j);
            }
            
            if (systemChanged) valuesToTrain = values.size() - 1;
            
            
            if (j == values.get(i).length-1) {
                if (i == values.size()-1) {
                    i = 0;
                    j = 0;
                    
                } else {
                    ++i;
                    j = 0;
                }
                --valuesToTrain;
                
            } else if (i < 3) {
                ++i;
            } else {
                ++j;
            }
            
            if (!systemChanged && (valuesToTrain == 0)) break;
        }
        
        SettingsReader.writeSettings(Settings.Training.outputFile);
        
        Settings.Evaluations.debug();
    }
    
    public static boolean trainHandCombinations(List values, int i) {
        //for each hand ID possible in the array, toggle them and compare performance
        boolean systemChanged = false; 
        
        double baseScore,deltaScore;
        
        //INSTANTIATE list to 
        double[] ha = ((double[])values.get(i));
        
        //COMPARE PERFORMANCE for each hand
        for (int id = 0; id < 10; ++id) {
            
            //establish baseline
            baseScore = scoreGames();
            
            System.out.println("\nTraining Hand Combinations:" + " " + i + " " + id);
            System.out.println("Base Score: " + baseScore);
            System.out.println(Arrays.toString(ha));
            
            int og = 0;
            //TOGGLE hand id
            if (ha[id] == 1) {
                ha[id] = 0;
                og = 1;
            } else {
                ha[id] = 1;
                og = 0;
            }
            
            System.out.println(Arrays.toString(ha));
            
            deltaScore = scoreGames();
            
            System.out.println("Delta Score: " + deltaScore);
            
            //if PERFORMANCE DECREASES or remains the same
            //RESET
            if (deltaScore <= baseScore) ha[id] = og;
            else systemChanged = true;
            
        }
            
        
        return systemChanged;
    }
    
    public static boolean trainValuesIncrementally(List values, int i, int j) {
        //adjust the specified value in a positive or negative direction until a max score is reached
        boolean systemChanged = false; 
        
        double baseScore;
        
        double deltaScore = Double.NaN;
        
        double[] va = (double[]) values.get(i); //value array
        
        double[] sign = { -1, 1 };
        int isign = 0;
        double scale = 1;
        
        double og = 0;
        
        boolean train = true;
        while (train) {
            baseScore = scoreGames();
            System.out.println("\nTraining Values Incrementally:"  + " " + i + " " + j);
            System.out.println("Base Score: " + baseScore);
            //STORE original value
            og = va[j];
            
            //ADJUST value 
            System.out.println(va[j]);
            va[j] = va[j] + (sign[isign]*scale);
            if (va[j] < 0) va[j] = 0;
            if (va[j] > 1) va[j] = 1;
            System.out.println(va[j]);
            
            //SCORE PERFORMANCE
            deltaScore = scoreGames();
            System.out.println("Delta Score: " + deltaScore);
            
            //if PERFORMANCE DECREASES
            if (deltaScore < baseScore) {
                //RESET value
                va[j] = og;
                
                //INCREMENT value adjustors
                if (scale > 0.01) scale = scale / 2;
                else if (isign == 0) {
                    isign = 1;
                    scale = 1;
                }
                else train = false;
            } else if (deltaScore == baseScore) {
                //if performance does not change 
                if (isign == 0) {
                    isign = 1;
                    
                } else {
                    train = false;
                }
                //RESET value
                va[j] = og;
                
            } else {
                //PERFORMANCE INCREASES
                //RECORD
                baseScore = deltaScore;
                systemChanged = true;
                SettingsReader.writeSettings(Settings.Training.outputFile);
            } 
        }
        
        return systemChanged;
    }
    
    public static double scoreGames() {
        int numGames = 1000;
        int numSimulations = numGames;
        double score = 0;
        Random r = new Random();
        
        //RESET patterns, so as not to retain old, bad evaluations
        pokersquares.evaluations.PatternPolicy.patternEvaluations = new java.util.HashMap();
        
        //SIMULATE Games
        while(--numSimulations > 0){
            Board b = new Board();
                
            while (b.getTurn() < 25) {
                if (b.getDeck().size() == 0) {
                    System.out.println("ERROR" + " turn: " + b.getDeck().size());
                    b.debug();
                    
                }
                //Card c = b.getDeck().remove(r.nextInt(b.getDeck().size())); 
                Card c = b.getDeck().remove(numSimulations % b.getDeck().size()); 
                int[] p = Settings.Algorithms.simAlgoritm.search(c, b, 10000);
                b.playCard(c, p);
            }
                
            score += Settings.Environment.system.getScore(b.getGrid());
        }
        
        return score / (numGames +1);
            
    }
    
}
