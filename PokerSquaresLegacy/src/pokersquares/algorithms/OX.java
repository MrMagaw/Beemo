package pokersquares.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import pokersquares.players.Beemo;
import pokersquares.Card;
import pokersquares.Play;

public class OX extends Algorithm{
    public OX(Beemo parent) {
        super(parent);
    }
    @Override
    public int[] search(Card card, long millisRemaining) {
        
        //OPTIMIZED XPECTIMAX SEARCH version II
        int[] bestPos = new int[2];
        
        //Start turn.
        if (BMO.getTurn() == 0) {
            bestPos[0] = 2;
            bestPos[1] = 2;
            return bestPos;
        }
        
        List <Play> generatedPlays = new ArrayList((25^BMO.getSearchDepth())*(BMO.getDeckSampleSize()^BMO.getSearchDepth())); //generated play array of search depth size
        
        Play root = new Play(BMO);
        root.buildAndEvaluateHands();
        root.evaluatePlay();
        Play play = root;
        
        //OPTIMIZATION always play the first card in the center
        
        
        //GENERATE PLAYS up to search depth
        int maxLVL = 0;
        int iPlay = -1;
        while (thereIsTimeLeft() && (play.getLVL() < BMO.getSearchDepth())) {
            //GENERATE PLAYS
            if (play.getLVL() == 0) genPlaysForLVL0(card, play, generatedPlays);
            else genPlaysFor(play, generatedPlays, maxLVL);
            
            //INCREMENT to next play
            iPlay += 1;
            if (generatedPlays.size() <= iPlay) break;
            play = generatedPlays.get(iPlay);
            if (play.getLVL() > maxLVL) maxLVL = play.getLVL();
        } 
        //System.out.println("plays generated: " + generatedPlays.size());
        
        //BACKPROPAGATE EVALUATIONS to the base lvl plays starting from the second lowest level *ASSUMING SEARCH DEPTH WAS REACHED*
        iPlay = backpropagateEvaluations(iPlay, generatedPlays);
        
        //CALCULATE bestPos from base lvl plays
        bestPos = getBestPos(generatedPlays, iPlay);
        return bestPos;
    }
    
    public List genPlaysForLVL0(Card card, Play play, List generatedPlays) {
        
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
                
                
                topPlays.add(newPlay); 
                
            }
        }
        
        Collections.sort(topPlays);
        
        for (int i = 0; i < BMO.getRootPlaySampleSize(); i++) {
            if (topPlays.size() == 0) break;
            generatedPlays.add(topPlays.removeFirst());
        }
        
        return generatedPlays;
    }
    
    public List genPlaysFor(Play play, List generatedPlays, int maxLVL) {
        int playSampleSize = 0;
        //FOR EACH CARD 
        for (Card nextCard : BMO.getDeck()) {
            //SKIP PLAYED CARDS
            if (!play.getPlayedCards().contains(nextCard)) {
                
                LinkedList <Play> topPlays = new LinkedList();
                //FOR EACH POS
                for (Integer pos : BMO.getPlayPos()) {
                    //SKIP PLAYED POS
                    if (!play.getPlayedPositions().contains(pos)) {
                        //DECODE pos
                        int row = pos/5;
                        int col = pos%5;
                        
                        //MAKE A NEW PLAY
                        Play newPlay = new Play(play);
                        
                        //RECORD PLAY + EVALUATE
                        newPlay.recordPlayPos(nextCard, pos);
                        newPlay.evaluateHands(row, col);
                        newPlay.updateEvaluation(row, col);
                        
                        
                        if (newPlay.getLVL() > maxLVL) newPlay.getParent().updatebestCardEvals((Card) newPlay.getPlayedCards().get(0) , newPlay.getEvaluation());
                        
                        topPlays.add(newPlay); 
                    }
                }
                Collections.sort(topPlays);
                for (int i = 0; i < BMO.getBranchPlaySampleSize(); i++) {
                    if (topPlays.size() == 0) break;
                    generatedPlays.add(topPlays.removeFirst());
                }
                
                playSampleSize += 1;
                if (playSampleSize == BMO.getDeckSampleSize()) break;
            }   
        }
        
        return generatedPlays;
    }
    
    public int backpropagateEvaluations(int iPlay, List generatedPlays) {
        //iPlay starts at the first play in lvl searchDepth - 1
        iPlay -= 1;
        Play play = (Play) generatedPlays.get(iPlay);
        
        while (play.getParent().getLVL() != 0) {
            int lvl = play.getLVL();
            
            //CALCULATE POSITION AVERAGES of all plays at each level
            while (play.getLVL() == lvl) {
                //RECORD BEST EVALUATION per card in parent play's bestCardEvals Map
                
                play.propagateEvaluation(); 
                
                //UPDATE PARENT PLAY with evaluation
                play.getParent().updatebestCardEvals((Card) play.getPlayedCards().get(0) , play.getEvaluation());
                
                //ITERATE to next play
                iPlay -= 1;
                play = (Play) generatedPlays.get(iPlay); //iterate through plays from last play generated to root play
                
            }
        }
        //iPlay finishes at the first play of lvl 1
        return iPlay;
    }
    
    public int[] getBestPos(List <Play> generatedPlays, int iPlay) {
        Play play = generatedPlays.get(iPlay);
        Play bestPlay = play;
        while (play.getLVL() != 0) {
            play.propagateEvaluation();
            
            //System.out.println(play.getEvaluation());
            if (play.getEvaluation() > bestPlay.getEvaluation()) bestPlay = play;
            
            iPlay -= 1;
            if (iPlay == -1) break;
            play = generatedPlays.get(iPlay);
        } 
        
        return bestPlay.getPos();
    }
    
    public boolean thereIsTimeLeft() {
        //MODIFY to return true if there is enought time left 
        //to evaluate the next play
        return true;
    }
    
}
