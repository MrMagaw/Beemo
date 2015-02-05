
package pokersquares.trainers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import pokersquares.algorithms.Simulator;
import pokersquares.config.Settings;
import static pokersquares.config.Settings.Training.bestValues;
import static pokersquares.config.Settings.Training.policyMax;
import static pokersquares.config.Settings.Training.policyMin;
import static pokersquares.config.Settings.Training.score;
import static pokersquares.config.Settings.Training.updateBest;
import static pokersquares.config.Settings.Training.values;
import pokersquares.config.SettingsReader;
import pokersquares.environment.Board;
import static pokersquares.trainers.ValueReinforcement.redistribute;
import static pokersquares.trainers.ValueReinforcement.trainValuesIncrementally;



public class Jake implements Trainer{
    
    public Jake() {
        values.add(Settings.Evaluations.highCardPolicy);
        values.add(Settings.Evaluations.pairPolicy);
        values.add(Settings.Evaluations.twoPairPolicy);
        values.add(Settings.Evaluations.threeOfAKindPolicy);
        values.add(Settings.Evaluations.straightPolicy);
        values.add(Settings.Evaluations.flushPolicy);
        values.add(Settings.Evaluations.fullHousePolicy);
        values.add(Settings.Evaluations.fourOfAKindPolicy);
    }

    @Override
    public void runSession(long millis) {
        
        //Training With Prioritized Value Adjustment
        //And with local maximum settings saving
        System.err.print("\nTraining\n");
        
        long tStart = System.currentTimeMillis();
        long tBuffer = 1000; //Some amount of millis to make sure we dont exceed alotted millis
        
        Settings.Evaluations.debug();
        
        LinkedList <Integer> trainingRegimen = new <Integer> LinkedList();
        
        //BUILD Training Regimen
        for (int i = 0; i < values.size(); ++i) {
            double[] value = values.get(i);
            for (int j = 0; j < value.length; ++j) {
                int ij = i * 1000 + j;
                trainingRegimen.add(ij);
            }
        }
        
        double bestScore = Double.NEGATIVE_INFINITY;
        
        //ITERATE
        //Linked List filled with all value indices
        int n = 0;
        int nLimit = 2;
        int itr = 0;
        while((System.currentTimeMillis() - tStart) < (millis - tBuffer)) {
            
            int ij = trainingRegimen.get(itr);
            int i = ij / 1000;
            int j = ij % 1000;
            
            //boolean smallTrial = (nLimit <= 2) && (itr >= nLimit);
            boolean smallTrial = false;
            
            //Train Value
            boolean systemChanged = trainValueIncrementally(values, i, j, smallTrial);
            
            System.err.println("Score: " + score + " Values To Train: " + (trainingRegimen.size() - itr) + " ij: " + i + " " + j);
            System.err.println("n: " + n + " nLimit: " + nLimit + " Small Trial: " + smallTrial);
            
            //If the system has changed
            if (systemChanged) {
                //move the value to the top of the regimen
                trainingRegimen.add(n++, trainingRegimen.remove(itr));
            }
            
            //if at least two values have been moved to the head of the regimen
            //restart regimen iteration
            /*
            if (n >= nLimit) {
                itr = 0;
                n = 0;
            }
                   
            else ++itr;
             */
            
            ++itr;
            
            //END
            if (itr >= trainingRegimen.size()) {
                //if (nLimit > 1 || n > 0) {
                if (n > 0) {
                    //nLimit = (nLimit == 1) ? 2 : n;
                    itr = 0;
                    n = 0;
                } else {
                    
                    redistribute(values);
                    System.err.println("RESTART");
                    itr = 0;
                    n = 0;
                }
            }
            
            if (score > bestScore) {
                bestScore = score;
                bestValues = (cloneValues(values));
                SettingsReader.writeSettings(Settings.Training.settingsFileOut);
            }
        }
        
        Settings.Evaluations.updateSettings(bestValues);
        SettingsReader.writeSettings(Settings.Training.settingsFileOut);
        updateBest = false;
        
    }
    
    public static boolean trainValueIncrementally(List<double[]> values, int i, int j, boolean smallTrial) {
        //adjust the specified value in a positive or negative direction until a max score is reached
        double[]  va = values.get(i); //value array
        boolean systemChanged = false;
        int sign = -1;
        double 
                baseScore, 
                newScore,
                og,
                scale = va[j];
        
        int numGames = smallTrial ? 500 : 1000;
        
        boolean train = true;
        
        boolean verbose = false;

        while (train) {
            baseScore = scoreGames(numGames);
            if(verbose) System.out.println("\nTraining Values Incrementally:"  + " " + i + " " + j);
            if(verbose) System.out.println("Base Score: " + baseScore);
            //STORE original value
            og = va[j];
            
            //ADJUST value 
            if(verbose) System.out.print(va[j]);
            va[j] = va[j] + (sign*scale);
            if (va[j] < policyMin) va[j] = 0.0;
            else if (va[j] > policyMax) va[j] = 1.0;
            if(verbose) System.out.print("-->" + va[j] + ": ");
            //SCORE PERFORMANCE
            
            newScore = scoreGames(numGames);
            if(verbose) System.out.println("New Score: " + newScore);
            
            //if PERFORMANCE DECREASES
            if (newScore < baseScore) {
                //RESET value
                va[j] = og;
                
                //INCREMENT value adjustors
                if (scale > 0.0001) scale = scale / 2.0;
                else if (sign == -1) {
                    sign = 1;
                    scale = 1.0 - va[j];
                }
                else train = false;
            } else if (newScore == baseScore) {
                score = baseScore;
                //if performance does not change 
                if (scale > 0.0001) scale = scale / 2.0;
                if (sign == -1) {
                    sign = 1;
                    scale = 1.0 - va[j];
                } else {
                    train = false;
                }
                //RESET value
                va[j] = og;
                
            } else {
                if(verbose) System.out.println(og + "-->" + va[j] + ": Δ" + (newScore - baseScore));
                //PERFORMANCE INCREASES
                //RECORD
                score = baseScore;
                systemChanged = true;
                
                if (smallTrial) {
                    va[j] = og;
                    break;
                }
                
                SettingsReader.writeSettings(Settings.Training.settingsFileOut);
            } 
        }
        
        return systemChanged;
    }
    
    public static List cloneValues(List values) {
        List <double[]> clonedValues = new ArrayList <double[]> ();
        
        for (int i = 0; i < values.size(); ++i) {
            double[] v = ((double [])values.get(i));
            double[] cv = new double[v.length];
            System.arraycopy(v, 0, cv, 0, v.length);
            clonedValues.add(cv);
        }
        
        return clonedValues;
    }
    
    public static double scoreGames(int numGames) {
        int numSimulations = numGames;
        //RESET patterns, so as not to retain old, bad evaluations
        pokersquares.evaluations.PatternPolicy.patternEvaluations = new java.util.HashMap();
        //SIMULATE Games
        return Simulator.simulate(new Board(), numSimulations, 10000, 1) / (double)(numGames+1);
    }
    
}
