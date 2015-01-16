package pokersquares.environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import pokersquares.evaluations.PatternPolicy;

public class Board {

    private Card[][] grid;
    private ArrayList<Integer[]> openPos;
    private final LinkedList<Card> deck = new LinkedList();
    private static final ArrayList<Integer[]> ALL_POS = new ArrayList(25);
    public ArrayList <Hand> hands = new ArrayList <Hand> ();
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
        deck.addAll(Arrays.asList(Card.allCards));
        openPos = new ArrayList(ALL_POS);
        buildHands();
    }
    public Board(Board parent){
        grid = new Card[5][5];
        for(int i = 0; i < 5; ++i) 
            grid[i] = parent.grid[i].clone(); //May have to clone each array
        deck.clear();
        deck.addAll(parent.deck);
        openPos = new ArrayList(parent.openPos);
        buildHands();
    }
    public void buildHands(){
        //check each row 
        for (int row = 0; row < 5; ++row) {
            
            Hand hand = new Hand(new Card[5], false);
            
            for (int col = 0; col < 5; ++col) {
                //build hand
                hand.cards[col] = getCard(row, col);
                //check for null pos
                if (getCard(row, col) == null) hand.openPos.add(col);
            }
            
            hands.add(hand);
        }
        
        //check each column
        for (int col = 0; col < 5; ++col) {
            
            Hand hand = new Hand(new Card[5], true);
            
            for (int row = 0; row < 5; ++row) {
                //build hand
                hand.cards[row] = getCard(row, col);
                
                //check for null pos
                if (getCard(row, col) == null) hand.openPos.add(row);
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
        hands.get(pos[0]).cards[pos[1]] = card;
        hands.get(pos[0]).openPos.remove((Integer) pos[1]);
        hands.get(pos[1] + 5).cards[pos[0]] = card;
        hands.get(pos[1] + 5).openPos.remove((Integer) pos[0]);
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
    public LinkedList<Card> getDeck(){
        return deck;
    }
}
