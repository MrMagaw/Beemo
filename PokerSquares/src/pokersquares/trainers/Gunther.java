/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pokersquares.trainers;

import java.util.Map;

/**
 *
 * @author karo
 */
public class Gunther implements Trainer {
    public static Map<Integer, Double> bestPatternEvaluations = new java.util.HashMap();
    
    @Override
    public String getName () { return "Prismo"; }
    
    @Override
    public Map getBestPatterns () { return bestPatternEvaluations; }
    
    @Override
    public void runSession(long millis) {
        //Gunther Trains using Monte Carlo techniques, 
        //iterate through board positions randomly/sequentially 
        //and use monte carlo to evaluate all positions
        
        
    }
    
    
}
