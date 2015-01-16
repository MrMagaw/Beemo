/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pokersquares.environment;

import java.util.ArrayList;
import java.util.LinkedList;
import pokersquares.evaluations.PatternPolicy;

/**
 *
 * @author newuser
 */
public class Hand {
    public Card[] cards;
    public String pattern;
    public double evaluation;
    public boolean isCol;
    public LinkedList <Integer> openPos = new LinkedList <Integer> ();
    
    public Hand(Card[] hand, boolean isCol) {
        this.cards = hand;
        this.isCol = isCol;
    }
    public void playOpenPos(Card card) {
        cards[openPos.removeFirst()] = card;
    }
    public double evaluate() {
        return PatternPolicy.evaluate(this,isCol);
    }
     
    public String patternate() {
        return PatternPolicy.patternate(this,isCol);
    }
    
    public Card getCard(int i) {
        return cards[i];
    }
    public void debug() {
        for(Card card : cards) {
            if (card == null) System.out.print("--");
            else System.out.print(card.toString());
        }
        System.out.print(" " + pattern + " " + isCol + " " + openPos + "\n");
    }
}
