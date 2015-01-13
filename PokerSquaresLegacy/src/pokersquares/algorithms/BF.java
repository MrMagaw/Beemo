/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pokersquares.algorithms;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import pokersquares.Card;
import pokersquares.Play;
import pokersquares.players.Beemo;

/**
 *
 * @author newuser
 */
public class BF extends Algorithm{
    
    public BF(Beemo parent) {
        super(parent);
    }
    
    @Override
    public int[] search(Card card, long millisRemaining) {
        int[] bestPos = {0,0};
        
        /*
        //OPTIMIZATION always play the first card in the center
        if (BMO.getTurn() == 0) {
            bestPos[0] = 2;
            bestPos[1] = card.getSuit();
            return bestPos;
        }
        */
        
        //ROOT PLAY
        Play root = new Play(BMO);
        root.buildAndEvaluateHands();
        root.evaluatePlay();
        
        //GENERATE PLAYS
        LinkedList <Play> moves =  new LinkedList();
        genPlays(card, root, moves);
        
        Collections.sort(moves);
        
        bestPos = moves.get(0).getPos();
        
        return bestPos;
    }
    
    public List genPlays(Card card, Play play, List generatedPlays) {
        
        //FOR EACH POS
        LinkedList <Play> topPlays = new LinkedList();
        for (Integer pos : BMO.getPlayPos()) {
            
            //SKIP PLAYED POS
            if (!play.getPlayedPositions().contains(pos)) {
                //DECODE pos
                int row = pos/5;
                int col = pos%5;
                
                //MAKE A NEW PLAY
                Play newPlay = new Play(play);
                        
                //RECORD PLAY + EVALUATE
                newPlay.recordPlayPos(card, pos);
                newPlay.evaluateHands(row, col);
                newPlay.updateEvaluation(row, col);
                
                //System.out.println(newPlay.getPlayedPositions().get(0) + " " + newPlay.getEvaluation());
                
                topPlays.add(newPlay); 
                
            }
        }
        
        Collections.sort(topPlays);
        
        for (int i = 0; i < BMO.getRootPlaySampleSize(); i++) {
            if (topPlays.size() == 0) break;
            generatedPlays.add(topPlays.removeFirst());
            //System.out.println(((Play) generatedPlays.get(i)).getPlayedPositions().get(0));
        }
        
        return generatedPlays;
    }
    
    
}
