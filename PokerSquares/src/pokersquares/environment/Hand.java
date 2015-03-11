package pokersquares.environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import pokersquares.evaluations.PatternPolicy;

public class Hand {
    private final Card[] cards;
    private Integer pattern = -1;
    private final Board board;
    public boolean 
            isCol,
            hasStraight,
            hasRoyal;
    public int
            numCards,
            numRanks,
            numSuits;
            
    public int[] 
            rankCountCounts = new int[6],
            rankCounts = new int[Card.NUM_RANKS],
            suitCounts = new int[Card.NUM_SUITS];
    
    private final LinkedList<Integer> openPos = new LinkedList();
    private static final List<Integer> allPos = new ArrayList();
    static{
        for(int i = 0; i < 5; ++i) allPos.add(i);
    }
    
    public Hand(Hand hand){
        this.openPos.addAll(hand.openPos);
        this.pattern = hand.pattern;
        //this.handWeight = hand.handWeight;
        this.isCol = hand.isCol;
        this.hasStraight = hand.hasStraight;
        this.hasRoyal = hand.hasRoyal;
        this.numCards = hand.numCards;
        this.numRanks = hand.numRanks;
        this.numSuits = hand.numSuits;
        this.cards = new Card[5];
        System.arraycopy(hand.rankCountCounts, 0, this.rankCountCounts, 0, hand.rankCountCounts.length);
        System.arraycopy(hand.cards, 0, this.cards, 0, hand.cards.length);
        System.arraycopy(hand.rankCounts, 0, this.rankCounts, 0, hand.rankCounts.length);
        System.arraycopy(hand.suitCounts, 0, this.suitCounts, 0, hand.suitCounts.length);
        this.board = hand.board;
    }
    
    public Hand(boolean isCol, Board b) {
        this.cards = new Card[5];
        this.isCol = isCol;
        this.openPos.addAll(allPos);
        this.board = b;
    }
    
    public void placeCard(int i, Card c){
        //reset pattern
        pattern = -1;
        //update hand data
        if(cards[i] == null){
            openPos.remove(openPos.indexOf(i));
            ++numCards;
        }else{
            if(--rankCounts[cards[i].getRank()] == 0)
                --numRanks;
            if(--suitCounts[cards[i].getSuit()] == 0)
                --numSuits;
        }
        if(c == null){
            openPos.add(i);
            --numCards;
        }else{
            if(++rankCounts[c.getRank()] == 1)
                ++numRanks;
            if(++suitCounts[c.getSuit()] == 1)
                ++numSuits;
        }
        cards[i] = c;
        //HiLo
        if(numCards == 0)
            hasStraight = true;
        else if (numRanks != numCards)
            hasStraight = false;
        else{
            int loCard = -1, loCard2 = -1, highCard = -1;
            for(int j = 0, k = 0; j < rankCounts.length && k < 2 && k < numCards; ++j){
                if(rankCounts[j] != 0)
                    switch(k++){
                        case 0: loCard = j;
                        case 1: loCard2 = j;
                    }
            }
            for(int j = rankCounts.length-1; j >= 0; --j){
                if(rankCounts[j] != 0){
                    highCard = j;
                    break;
                }
            }
            
            if (((highCard - loCard) < 5) || 
                    ((loCard == 0) && ((13 - loCard2) < 5))) 
                hasStraight = true;
        }
    }
    
    public void debug() {
        for(Card card : cards) {
            if (card == null) System.out.print("--");
            else System.out.print(card.toString());
            System.out.print(" / ");
        }
        System.out.println("\n" + pattern + ", " + isCol + ", " + openPos + " hasStraight: " + hasStraight);
        System.out.println("numCards: " + numCards + " numRanks: " + numRanks + " numSuits: " + numSuits );
        System.out.println(Arrays.toString(rankCountCounts) + "\n");
    }
    
    public void playOpenPos(Card card) { placeCard(openPos.getFirst(), card); } 
    public boolean hasOpenPos(){ return !openPos.isEmpty(); } 
    public int numOpenPos(){ return openPos.size(); } 
    public double evaluate() { return PatternPolicy.evaluate(this); } 
    public void patternate() { PatternPolicy.buildPattern(this); } 
    public boolean hasPattern(){ return (pattern != -1); } 
    public Integer getPattern(){ return pattern; } 
    public void setPattern(int pattern) { this.pattern = pattern; } 
    public Card getCard(int i) { return cards[i]; }
    public Card[] getCards() { return cards; }
    public Board getBoard() { return board; }
}
