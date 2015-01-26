package pokersquares.environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import pokersquares.evaluations.PatternPolicy;

public class Hand {
    private final Card[] cards;
    private Integer pattern = -1;
    public boolean 
            isCol,
            hasStraight,
            hasRoyal;
    public int
            numCards,
            numRanks,
            numSuits;
    public final int[] 
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
    }
    
    public Hand(boolean isCol) {
        this.cards = new Card[5];
        this.isCol = isCol;
        this.openPos.addAll(allPos);
    }
    
    public void playOpenPos(Card card) {
        placeCard(openPos.getFirst(), card);
    }
    
    public boolean hasOpenPos(){
        return !openPos.isEmpty();
    }
    
    public double evaluate() {
        return PatternPolicy.evaluate(this);
    }
     
    public void patternate() {
        PatternPolicy.buildPattern(this);
    }
    
    public boolean hasPattern(){
        return (pattern != -1);
    }
    
    public Integer getPattern(){
        return pattern;
    }

    public void setPattern(int pattern) {
        this.pattern = pattern;
    }
    
    public Card getCard(int i) {
        return cards[i];
    }
    
    public void placeCard(int i, Card c){
        //update pattern
        pattern = -1;
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
    }
    
    public void buildRankCounts(){
        for(int i = 0; i < Card.NUM_RANKS; ++i)
            ++rankCountCounts[rankCounts[i]];
    }
    
    public void debug() {
        for(Card card : cards) {
            if (card == null) System.out.print("--");
            else System.out.print(card.toString());
            System.out.print(" / ");
        }
        System.out.println("\n" + pattern + ", " + isCol + ", " + openPos);
        System.out.println(numCards + ", " + Arrays.toString(rankCountCounts) + "\n");
    }
}
