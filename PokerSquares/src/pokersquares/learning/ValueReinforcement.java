/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pokersquares.learning;

import pokersquares.config.Settings;
import java.util.*;
import pokersquares.algorithms.Simulator;
import pokersquares.config.SettingsReader;
import pokersquares.environment.Board;
import pokersquares.environment.Card;

/**
 *
 * @author newuser
 */
public class ValueReinforcement implements Trainer {
    
    @Override
    public void runSession(long millis){
        
        long tStart = System.currentTimeMillis();
        
        //VALUES to be adjusted
        List <double[]> values = new ArrayList();
        
        //values.add((double[])Settings.Evaluations.exps);
        values.add(Settings.Evaluations.colHands); 
        values.add(Settings.Evaluations.rowHands);
        values.add(Settings.Evaluations.highCardPolicy);
        values.add(Settings.Evaluations.pairPolicy);
        values.add(Settings.Evaluations.twoPairPolicy);
        values.add(Settings.Evaluations.threeOfAKindPolicy);
        values.add(Settings.Evaluations.flushPolicy);
        values.add(Settings.Evaluations.fullHousePolicy);
        values.add(Settings.Evaluations.fourOfAKindPolicy);
        
        int i = 3, j = 0;
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
            
            int og;
            
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
    
    public static boolean trainValuesIncrementally(List<double[]> values, int i, int j) {
        //adjust the specified value in a positive or negative direction until a max score is reached
        boolean systemChanged = false;
        double 
                baseScore, 
                newScore,
                og,
                scale = 1;
        
        double[] 
                va = values.get(i); //value array
        
        int isign = -1;
        
        boolean train = true;
        boolean verbose = false;
        
        while (train) {
            baseScore = scoreGames();
            if(verbose) System.out.println("\nTraining Values Incrementally:"  + " " + i + " " + j);
            if(verbose) System.out.println("Base Score: " + baseScore);
            //STORE original value
            og = va[j];
            
            //ADJUST value 
            if(verbose) System.out.print(va[j]);
            va[j] = va[j] + (isign*scale);
            if (va[j] < 0) va[j] = 0.0;
            else if (va[j] > 1) va[j] = 1.0;
            if(verbose) System.out.print("-->" + va[j] + ": ");
            //SCORE PERFORMANCE
            //Update scoring
            //for(int k = va.length-1; k >= 0; --k)
              //  Settings.Evaluations.handScores[k] = va[k];
            
            
            //
            newScore = scoreGames();
            if(verbose) System.out.println("New Score: " + newScore);
            
            //if PERFORMANCE DECREASES
            if (newScore < baseScore) {
                //RESET value
                va[j] = og;
                
                //INCREMENT value adjustors
                if (scale > 0.000001) scale = scale / 2.0;
                else if (isign == -1) {
                    isign = 1;
                    scale = 1.0;
                }
                else train = false;
            } else if (newScore == baseScore) {
                //if performance does not change 
                if (isign == -1) {
                    isign = 1;
                } else {
                    train = false;
                }
                //RESET value
                va[j] = og;
                
            } else {
                System.out.println(og + "-->" + va[j] + ": Δ" + (newScore - baseScore));
                //PERFORMANCE INCREASES
                //RECORD
                systemChanged = true;
                SettingsReader.writeSettings(Settings.Training.outputFile);
            } 
        }
        
        return systemChanged;
    }
    
    public static double scoreGames() {
        int numGames, numSimulations;
        numGames = numSimulations = 1000;
        //RESET patterns, so as not to retain old, bad evaluations
        pokersquares.evaluations.PatternPolicy.patternEvaluations = new java.util.HashMap();
        //SIMULATE Games
        return Simulator.simulate(new Board(), numSimulations, 10000) / (double)(numGames+1);
    }
    
}
