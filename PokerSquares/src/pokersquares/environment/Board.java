package pokersquares.environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class Board {
    private Card[][] grid;
    private ArrayList<Integer[]> playPos;
    private final LinkedList<Card> deck = new LinkedList();
    private static final ArrayList<Integer[]> ALL_POS = new ArrayList(25);
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
        playPos = new ArrayList(ALL_POS);
    }
    public Board(Board parent){
        grid = new Card[5][5];
        for(int i = 0; i < 5; ++i) 
            grid[i] = parent.grid[i].clone(); //May have to clone each array
        deck.clear();
        deck.addAll(parent.deck);
        playPos = new ArrayList(parent.playPos);
    }
    public void playCard(Card card, int[] pos){
        grid[pos[0]][pos[1]] = card;
        for(Integer[] i : playPos){
            if(i[0] == pos[0] && i[1] == pos[1]){
                playPos.remove(i);
                break;
            }
        }
    }
    public ArrayList<Integer[]> getPlayPos(){
        return playPos;
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
        return 25 - playPos.size();
    }
    public LinkedList<Card> getDeck(){
        return deck;
    }
}
