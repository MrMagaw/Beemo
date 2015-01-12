package pokersquares.players;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;
import pokersquares.config.Settings;
import pokersquares.environment.Card;
import pokersquares.environment.Grid;
import pokersquares.environment.PokerSquares;

/**
 *
 * @author Karo & William
 *                                   _________        
 *                                ||.     . ||
 *                                ||   ‿    ||
 *_|_|_|    _|      _|    _|_|    ||        ||
 *_|    _|  _|_|  _|_|  _|    _| /||--------||\
 *_|_|_|    _|  _|  _|  _|    _|  ||===   . ||
 *_|    _|  _|      _|  _|    _|  || +  o  0||
 *_|_|_|    _|      _|    _|_|    ||________||
 *                                   |    |
 *plays poker
 * 
 * 
 */

public class BeemoV2 implements PokerSquaresPlayer{
    private int turn;
    private final LinkedList<Card> deck = new LinkedList();
    private Grid grid;
    
    public BeemoV2(){
    }
    
    public BeemoV2(BeemoV2 BeemoSr){
        grid = new Grid(BeemoSr.grid); //May need to clone each array within
        turn = BeemoSr.turn;
        deck.addAll(BeemoSr.deck);
    }

    @Override
    public void init() {
        deck.clear();
        deck.addAll(Arrays.asList(Card.allCards));
        grid = new Grid();
        turn = 0;
        //Shuffle deck?
    }

    @Override
    public int[] getPlay(Card card, long millisRemaining) {
        deck.remove(card);
        int[] bestPos = {2, 2}; //2, 2 because 0, 0 isn't good enough.
        if(++turn == 0){ //Increment turn by one, and check if it's the first turn
            grid.playCard(card, bestPos);
            return bestPos;
        }
        for(int i=0; i<3; ++i){
            if(turn <= Settings.BMO.turnSplits[i]){
                bestPos = Settings.Algorithms.get(i).search(card, grid, millisRemaining);
                break;
            }
        }
        //PokerSquares.printGrid(grid.getGrid());
        //(new Scanner(System.in)).next();
        //Temp
        if(bestPos == new int[] {2, 2}) 
            System.out.println("Couldn't find algorithm for turn.");
        //
        
        grid.playCard(card, bestPos);
        return bestPos;
    }
    
}
