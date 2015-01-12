package pokersquares.environment;

import java.util.ArrayList;
import java.util.Arrays;

public class Grid {
    private Card[][] grid;
    private ArrayList<Integer[]> playPos;
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
    
    public Grid(){
        grid = new Card[5][5];
        playPos = new ArrayList(ALL_POS);
    }
    public Grid(Grid parent){
        grid = parent.grid.clone(); //May have to clone each array
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
    
    //Temp
    public Card[][] getGrid(){
        return grid;
    }
}
