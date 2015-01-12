package pokersquares.players;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;
import pokersquares.config.Settings;
import pokersquares.environment.Card;
import pokersquares.environment.Board;
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
    private Board board;
    
    public BeemoV2(){
    }
    
    //public BeemoV2(BeemoV2 BeemoSr){
    //    grid = new Grid(BeemoSr.grid); //May need to clone each array within
    //    turn = BeemoSr.turn;
    //    deck.addAll(BeemoSr.deck);
    //}

    @Override
    public void init() {
        board = new Board();
        //Shuffle deck?
        //(new Scanner(System.in)).next();
    }

    @Override
    public int[] getPlay(Card card, long millisRemaining) {
        board.getDeck().remove(card);
        int[] bestPos = {2, 2}; //2, 2 because 0, 0 isn't good enough.
        if(board.getTurn() == 0){ //Increment turn by one, and check if it's the first turn
            board.playCard(card, bestPos);
            return bestPos;
        }
        for(int i=0; i<3; ++i){
            if(board.getTurn() <= Settings.BMO.turnSplits[i]){
                bestPos = Settings.Algorithms.get(i).search(card, board, millisRemaining);
                break;
            }
        }
        //PokerSquares.printGrid(grid.getGrid());
        //(new Scanner(System.in)).next();
        
        //Temp
        if(bestPos[0] == 2 && bestPos[1] == 2) 
            System.out.println("Algorithm Error (Or unable to find algorithm).");
        //
        
        board.playCard(card, bestPos);
        //PokerSquares.printGrid(board.getGrid());
        return bestPos;
    }
    
}
