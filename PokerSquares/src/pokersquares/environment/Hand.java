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
            numSuits,
            primeSuit,
            hiCard = -1,
            loCard = -1,
            loCard2 = -1;
            
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
        this.hiCard = hand.hiCard;
        this.loCard = hand.loCard;
        this.loCard2 = hand.loCard2;
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
        hasStraight = false;
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
            //track Hi & Lo Cards
            int cRank = c.getRank();
        
            if (hiCard == -1) hiCard = cRank;
            else if (cRank > hiCard) hiCard = cRank;
        
            if (loCard == -1) loCard = cRank;
            else if (cRank < loCard) {
                loCard2 = loCard;
                loCard = cRank;
            }
            
            if (loCard2 == -1) loCard2 = cRank;
            else if ((cRank < loCard2) && (cRank > loCard)) loCard2 = cRank;
            else if ((loCard2 == loCard) && (cRank > loCard)) loCard2 = cRank;
            
            if(++rankCounts[c.getRank()] == 1)
                ++numRanks;
            if(++suitCounts[c.getSuit()] == 1)
                ++numSuits;
        }
        cards[i] = c;
    }
    
    public void checkStraight() {
        //if there are no card multiples
        int dHiLo = 0, dHiLo2 = 0;
        if (numRanks == numCards) {
            dHiLo = hiCard - loCard;
            dHiLo2 = 13 - loCard2;
            if (dHiLo < 5) hasStraight = true;
            if ((loCard == 0) && (dHiLo2 < 5)) hasStraight = true;
        } else hasStraight = false;
    }
    
    public void buildRankCounts(){
        rankCountCounts = new int[6];
        for(int i = 0; i < Card.NUM_RANKS; ++i)
            ++rankCountCounts[rankCounts[i]];
    }
    
    public void debug() {
        for(Card card : cards) {
            if (card == null) System.out.print("--");
            else System.out.print(card.toString());
            System.out.print(" / ");
        }
        System.out.println("\n" + pattern + ", " + isCol + ", " + openPos + " hasStraight: " + hasStraight);
        System.out.println("numCards: " + numCards + " numRanks: " + numRanks + " numSuits: " + numSuits );
        System.out.println(Arrays.toString(rankCountCounts) + " Hi: " + hiCard + " Lo: " + loCard + " Lo2: " + loCard2 + "\n");
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
