/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pokersquares.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import pokersquares.config.Settings;
import static pokersquares.config.Settings.Algorithms.UCT;
import static pokersquares.config.Settings.Algorithms.debugUCT;
import static pokersquares.config.Settings.Algorithms.simSampleSize;
import pokersquares.environment.*;
import pokersquares.evaluations.PatternPolicy;

/**
 *
 * @author karo
 */
public class UCTII extends Algorithm {
    //Search Algorithm using a UCT function to determine which positions to simulate from
    //Round II
    static double epsilon = 0.000000001; //Some Small Number
    int totalSimulations = 0;
    double totalScore = 0;
    
    
    private class PosVal {
        Integer[] xy;
        double totalScore = 0;
        double numSim = 0;
    }
    
    @Override
    public int[] internalSearch(final Card card, final Board board, long millisRemaining) {
        totalSimulations = 0;
        Integer[] bestPos = {2, 2};
        Long start = System.currentTimeMillis();
        Long buffer = 100L;
        Long turnMillis = millisRemaining / (24 - board.getTurn());
        
        //ESTABLISH BASELINE SCORE
        
        if (debugUCT) System.out.println("\nUCT");
        //INSTANTIATE BOARD
        Board pb = new Board(board); //Peanut Butter
        Integer[][] positions = new Integer[board.getOpenPos().size()][];
        positions = board.getOpenPos().toArray(positions); //COMMENT to use symmetry optimization
        
        //INITIALIZE posValues
        HashMap <Integer, PosVal> posValues = new HashMap <Integer, PosVal> ();
        int maxSim = simSampleSize * positions.length;
        for (Integer[] p : positions) {
            int ph = p[0]*5 + p[1];
            PosVal pv = new PosVal();
            pv.xy = p;
            posValues.put(ph,pv);
        }
        
        while ((System.currentTimeMillis() - start) < (turnMillis - buffer)) {
            //GET next pos
            Integer[] pos = nextPos(positions, posValues);
            
            //PLAY Card
            Board b = new Board(board);
            b.playCard(card, new int[]{pos[0], pos[1]});
            
            //SIMULATE Game
            Gamer g = new Gamer (b, 1, 0);
            g.run();
            double score = g.totalScore;
            
            //RECORD Score
            int ph = pos[0] * 5 + pos[1];
            PosVal pv = posValues.get(ph);
            pv.totalScore += score;
            pv.numSim++;
            totalSimulations++;
            totalScore += score;
            
            if (totalSimulations >= maxSim) break;
        }
        
        if (debugUCT) System.out.println("Total Simulations " + totalSimulations);
        
        //CHOOSE Best Pos
        double bestScore = Double.NEGATIVE_INFINITY;
        for (Integer ph : posValues.keySet()) {
            PosVal pv = posValues.get(ph);
            double score = pv.totalScore/pv.numSim;
            if (debugUCT) System.out.println("{ " + pv.xy[0] + " ," + pv.xy[1] + " } " + pv.numSim + " " + score);
            
            if (score > bestScore) {
                bestScore = score;
                bestPos = pv.xy;
                
            }
        }
        
        return new int[] {bestPos[0], bestPos[1]};
    }
    
    public Integer[] nextPos(Integer[][] positions, HashMap posValues) {
        Integer[] bestPos = {0,0};
        double bestUCT = Double.NEGATIVE_INFINITY;
        
        for(Integer[] pos : positions){
            int ph = pos[0] * 5 + pos[1];
            PosVal pv = (PosVal) posValues.get(ph);
            
            double posUCT = getUCT( pv, 1, UCT);
            
            if (posUCT > bestUCT) {
                bestUCT = posUCT;
                bestPos = pos;
            }
            
        }
        
        return bestPos;    
    }
    
    private double getUCT(PosVal pv, double A, double B) {
        //return the UCT value
        double score = (pv.totalScore / (pv.numSim+epsilon));
        double uctValue = 
            score * A/(totalScore/(totalSimulations + epsilon)+epsilon) 
            //uct term
            + B * Math.sqrt( Math.abs(Math.log(totalSimulations+epsilon)) / (pv.numSim+epsilon));
        //System.out.println(score * A / (maxScore + epsilon) +"\t" + pv.numSim+ " \t"  + B * Math.sqrt( Math.abs(Math.log(totalSimulations+epsilon)) / (pv.numSim+epsilon)));
        return uctValue;
    }
    
}
