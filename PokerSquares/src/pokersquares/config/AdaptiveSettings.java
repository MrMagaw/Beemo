/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pokersquares.config;

import static java.lang.Math.pow;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static pokersquares.config.Settings.Evaluations.handScores;
import static pokersquares.config.Settings.Training.policyMax;
import static pokersquares.config.Settings.Training.policyMin;

/**
 *
 * @author newuser
 */
public class AdaptiveSettings {
    // Generate Hand Settings following Heuristics
    
    public static void generateSettings() {
        //sets initial values for settings with scaled hand Score values
        
        double max = Double.NEGATIVE_INFINITY;
        double min = Double.POSITIVE_INFINITY;
        double[] scaledScores = new double [handScores.length];
        double[] pScores = new double [handScores.length];
        
        //System.arraycopy( handScores, 0, pScores, 0, handScores.length );
        
        //ADJUST scores to account for probability
        //THESE PROBABILITY VALUES NEED TO BE PONDERED ON
        double n = 0.0;
        pScores[0] = handScores[0] * pow(0.5,n); //High Card
        pScores[1] = handScores[1] * pow(0.423,n); //Pair
        pScores[2] = handScores[2] * pow(0.0475,n); //Two Pair
        pScores[3] = handScores[3] * pow(0.0211,n); //Three of a Kind
        pScores[4] = handScores[4] * pow(0.00392,n); //Straight
        pScores[5] = handScores[5] * pow(0.00198,n); //Flush
        pScores[6] = handScores[6] * pow(0.00144,n); //Full House
        pScores[7] = handScores[7] * pow(0.00024,n); //Four of a Kind
        pScores[8] = handScores[8] * pow(0.00001423,n); //Straight Flsuh
        pScores[9] = handScores[9] * pow(0.000001423,n); //Royal Flush
        
        System.out.println(Arrays.toString(pScores));
        
        //SCALE the scores in hand scores to the range of [policyMin, policyMax]
        for (int i = 0; i < pScores.length; ++i) {
            double val = pScores[i];
            if (val > max) max = val;
            if (val < min) min = val;
        }
        for (int i = 0; i < pScores.length; ++i) {
            scaledScores[i] = 
                    ( ( (policyMax - policyMin) * (pScores[i] - min) )
                    /
                    (max - min) ) 
                    + policyMin;
        }
        
        //Policies to be set
        List <double[]> policies = new ArrayList();
        
        policies.add(Settings.Evaluations.highCardPolicy);
        policies.add(Settings.Evaluations.pairPolicy);
        policies.add(Settings.Evaluations.twoPairPolicy);
        policies.add(Settings.Evaluations.threeOfAKindPolicy);
        policies.add(Settings.Evaluations.straightPolicy);
        policies.add(Settings.Evaluations.flushPolicy);
        policies.add(Settings.Evaluations.fullHousePolicy);
        policies.add(Settings.Evaluations.fourOfAKindPolicy);
        
        //SET Policies
        for (int i = 0; i < policies.size(); ++i) {
            double[] policy = policies.get(i);
            for (int j = 0; j < policy.length; ++j) 
                policy[j] = scaledScores[i];
        }
        
        System.out.println(Arrays.toString(scaledScores));
    }
}
