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
        
        values.add(Settings.Evaluations.colHands); 
        values.add(Settings.Evaluations.rowHands);
        
        values.add((double[])Settings.Evaluations.exps);
        values.add(Settings.Evaluations.pairPolicy);
        values.add(Settings.Evaluations.twoPairPolicy);
        values.add(Settings.Evaluations.threeOfAKindPolicy);
        values.add(Settings.Evaluations.flushPolicy);
        values.add(Settings.Evaluations.fullHousePolicy);
        values.add(Settings.Evaluations.fourOfAKindPolicy);
        
        int i = 3,j = 0;
        int valuesToTrain = values.size() - 1;
        boolean systemChanged = false;
        //WHILE there is time left, continue training
        while ((System.currentTimeMillis() - tStart) < millis) {
            
            //TRAIN Value
            if (i > 1) {
                //if value is successfully trained, the system has changed
                systemChanged = trainValue(values, i, j);
                
            } else {
                ++i;
                
            }
            
            if (systemChanged) valuesToTrain = values.size() - 1;
            
            if (j == values.get(i).length-1) {
                if (i == values.size()-1) {
                    i = 0;
                    
                } else {
                    ++i;
                    j = 0;
                }
                --valuesToTrain;
                
            } else {
                ++j;
            }
            
            if (!systemChanged && (valuesToTrain == 0)) break;
        }
        
        SettingsReader.writeSettings("trainingtest");
    }
    
    public static boolean trainValue(List values, int i, int j) {
        //adjust the specified value in a positive or negative direction until a max score is reached
        boolean systemChanged = false; 
        
        double baseScore = scoreGames();
        
        double deltaScore = Double.NaN;
        
        double[] va = (double[]) values.get(i);
        
        double[] sign = { -1, 1 };
        int isign = 0;
        double scale = 1;
        
        double og = 0;
        
        boolean train = true;
        while (train) {
            System.out.println("bs" + baseScore + " " + i + " " + j);
            //STORE original value
            og = va[j];
            
            //ADJUST value 
            System.out.println(va[j]);
            va[j] = va[j] + (sign[isign]*scale);
            System.out.println(va[j]);
            
            //SCORE PERFORMANCE
            deltaScore = scoreGames();
            System.out.println("ds" + deltaScore);
            
            //if PERFORMANCE DECREASES
            if (deltaScore < baseScore) {
                //RESET value
                va[j] = og;
                
                //INCREMENT value adjustors
                if (scale > 0.1) scale = scale / 2;
                else if (isign == 0) {
                    isign = 1;
                    scale = 1;
                }
                else train = false;
            } else if (deltaScore == baseScore) {
                //if performance does not change 
                //RESET value
                va[j] = og;
                train = false;
            } else {
                //PERFORMANCE INCREASES
                //RECORD
                baseScore = deltaScore;
                systemChanged = true;
                SettingsReader.writeSettings("trainingtest");
            } 
        }
        
        return systemChanged;
    }
    
    public static double scoreGames() {
        int numGames = 10000;
        int numSimulations = numGames;
        double score = 0;
        
        //RESET patterns, so as not to retain old, bad evaluations
        pokersquares.evaluations.PatternPolicy.patternEvaluations = new java.util.HashMap();
        
        //SIMULATE Games
        while(--numSimulations > 0){
            Board b = new Board();
                
            while (b.getTurn() < 25) {
                Card c = b.getDeck().remove(numSimulations % b.getDeck().size()); 
                int[] p = Settings.Algorithms.simAlgoritm.search(c, b, 10000);
                b.playCard(c, p);
            }
                
            score += Settings.Environment.system.getScore(b.getGrid());
        }
        
        return score / (numGames +1);
            
    }
    
}
