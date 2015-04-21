package pokersquares.environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;
import pokersquares.config.Settings;

public final class Board {
    public static class Deck{
        private static final long suitMask[] = {0xFFF8000000000L, 0x0007FFC000000L, 0x0000003FFE000, 0x0000000001FFF};
        private static final long rankMask = 0x0008004002001L;
        
        private long pattern;
        private int cardsLeft;
        
        public Deck(){
            pattern = 0xFFFFFFFFFFFFFL;
            cardsLeft = 52;
        }
        public Deck(Deck d){
            pattern = d.pattern;
            cardsLeft = d.cardsLeft;
        }
        
        private Card bitToCard(long mask){
            int suit;
            for(suit = 0; suit < suitMask.length; ++suit)
                if((mask&suitMask[suit]) > 0) break;
            int rank;
            for(rank = 0; rank < Card.NUM_RANKS; ++rank)
                if((mask&(rankMask << rank)) > 0) break;
            return Card.getCard((suit * Card.NUM_RANKS) + rank);
        }
        
        public Card getCard(int index){
            long i = 1;
            for(int j = 0; j < index; j += (i&pattern) > 0 ? 1 : 0, i<<=1);
            return bitToCard(i);
        }
        
        public void removeCard(Card c){
            --cardsLeft;
            pattern &= ~(suitMask[c.getSuit()] & (rankMask << c.getRank()));
        }
        
        public int cardsLeft() {
            return cardsLeft;
        }
        
        public int suitsLeft(int suit){
            return cardsLeft(suitMask[suit]);
        }
        
        public int ranksLeft(int rank){
            return cardsLeft(rankMask << rank);
        }
        
        private int cardsLeft(long mask) {
            long i = (pattern & mask);
            i = i - ((i >> 1) & 0x5555555555555555L);
            i = (i & 0x3333333333333333L) + ((i >> 2) & 0x3333333333333333L);
            return (int)((((i + (i >> 4)) & 0xF0F0F0F0F0F0F0FL) * 0x101010101010101L) >> 56);
        }
    }
    
    private final Card[][] grid;
    private final ArrayList<Integer[]> openPos;
    private final Deck deck;
    private final LinkedList<Card> deck_old = new LinkedList();
    private static final ArrayList<Integer[]> ALL_POS = new ArrayList(25);
    public ArrayList <Hand> hands = new ArrayList();
    public ArrayList <String> posPatterns = new ArrayList();
    public String pattern;
    static{
        ALL_POS.addAll(Arrays.asList(new Integer[][]{
                {0, 0}, {0, 1}, {0, 2}, {0, 3}, {0, 4},
                {1, 0}, {1, 1}, {1, 2}, {1, 3}, {1, 4},
                {2, 0}, {2, 1}, {2, 2}, {2, 3}, {2, 4},
                {3, 0}, {3, 1}, {3, 2}, {3, 3}, {3, 4},
                {4, 0}, {4, 1}, {4, 2}, {4, 3}, {4, 4},
            }));
    }
    
    public Board(){
        grid = new Card[5][5];
        openPos = new ArrayList(ALL_POS);
        deck = new Deck();
        deck_old.addAll(Arrays.asList(Card.getAllCards()));
        buildHands();
    }
    
    public Board(Card[][] grid) {
        
        this.grid = grid;
        deck_old.addAll(Arrays.asList(Card.getAllCards()));
        openPos = new ArrayList(ALL_POS);
        deck = new Deck();
        
        buildHands();
        
        //OFFICIALLY PLAY ALL CARDS IN GRID
        for (int col = 0; col < 5; ++col) {
            for (int row = 0; row < 5; ++row) {
                Card card = grid[row][col];
                playCard(card , new int[] {row, col});
                
                deck.removeCard(card);
            }
        }
    }
    
    public Board(Board parent){
        grid = new Card[5][5];
        for(int i = 0; i < 5; ++i) 
            grid[i] = parent.grid[i].clone(); //May have to clone each array
        deck = new Deck(parent.deck);
        deck_old.addAll(parent.deck_old);
        openPos = new ArrayList(parent.openPos);
        buildHands();
    }
    
    public void buildHands(){
        //check each row 
        for (int row = 0; row < 5; ++row) {
            
            Hand hand = new Hand(false, this);
            
            for (int col = 0; col < 5; ++col) {
                //build hand
                hand.placeCard(col, getCard(row, col));
            }
            
            hands.add(hand);
        }
        
        //check each column
        for (int col = 0; col < 5; ++col) {
            
            Hand hand = new Hand(true, this);
            
            for (int row = 0; row < 5; ++row) {
                //build hand
                hand.placeCard(row, getCard(row, col));
            }
            
            hands.add(hand);
        }
    }
    
    public void playCard(Card card, int[] pos){
        //UPDATE GRID
        grid[pos[0]][pos[1]] = card;
        
        for(Integer[] i : openPos){
            if(i[0] == pos[0] && i[1] == pos[1]){
                openPos.remove(i);
                break;
            }
        }
        
        //UPDATE HANDS
        hands.get(pos[0]).placeCard(pos[1], card);
        hands.get(pos[1] + 5).placeCard(pos[0], card);
    }
    public void patternateHands() {
        hands.stream().forEach((hand) -> {
            hand.patternate();
        });
    }
    public void patternatePositions(Card card) {
        posPatterns.clear();
        for (Integer[] pos : openPos) {
            //SORT Patterns
            ArrayList <Integer> orderedPatterns = new ArrayList <Integer> ();
            orderedPatterns.add(hands.get(pos[0]).getPattern());
            orderedPatterns.add(hands.get(pos[1] + 5).getPattern());
            Collections.sort(orderedPatterns);
            
            //PATTERNATE Position
            posPatterns.add(orderedPatterns.get(0) + "/" + orderedPatterns.get(1) + " " + card.toString());
        }
    }
    public String getPosPattern(Integer[] pos) {
        int i = 0;
        
        for (Integer[] rpos : openPos) {
            if ((Objects.equals(rpos[0], pos[0])) && (Objects.equals(rpos[1], pos[1]))) return posPatterns.get(i);
            ++i;
        }
        
        return null;
    }
    public ArrayList<Integer[]> getOpenPos(){
        return openPos;
    }
    public Card getCard(int[] pos){
        return getCard(pos[0], pos[1]);
    }
    public Card getCard(int x, int y){
        return grid[x][y];
    }
    public Card[] getRow(int row){
        return grid[row];
    }
    public Card[] getColumn(int col){
        Card[] hand = new Card[5];
        for(int i = 0; i < 5; ++i){
            hand[i] = grid[i][col];
        }
        return hand;
    }
    //Temp
    public Card[][] getGrid(){
        return grid;
    }
    
    public int getTurn(){
        return 25 - openPos.size();
    }
    
    public Card getCard(int index){
        return deck.getCard(index);
    }
    
    public int cardsLeft(){
        return deck.cardsLeft();
    }
    
    public int suitsLeft(int suit){
        return deck.suitsLeft(suit);
    }

    public int ranksLeft(int rank){
        return deck.ranksLeft(rank);
    }
    
    public void removeCard(Card card){
        deck.removeCard(card);
    }
    
    public Card removeCard(int index){
        Card c = getCard(index);
        deck.removeCard(c);
        return c;
    }
    
    public void debug() {
        Settings.Environment.system.printGrid(grid);
        for (Integer[] pos: openPos) System.out.print("(" + pos[0] + ", " + pos[1] + ") ");
        System.out.println("");
        for (String posPattern : posPatterns) System.out.println(posPattern);
    }
}
