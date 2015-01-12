/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pokersquares.players;

import static pokersquares.PokerSquares.SIZE;
import java.util.*;
import pokersquares.Card;
import pokersquares.PokerSquares;

/**
 *
 * @author newuser
 */
public class RuleBasedPokerSquaresPlayer implements PokerSquaresPlayer{
        private final int SIZE = PokerSquares.SIZE;
	private Card[][] grid = new Card[SIZE][SIZE];
	private int[] numColCards = new int[SIZE];
        private int[] numRowCards = new int[SIZE];
        private HashMap <Integer, Integer> rowRanks;
	
	@Override
	public void init() {
            rowRanks = new HashMap <Integer,Integer>(); // <Rank,Row>
		for (int row = 0; row < SIZE; row++)
			for (int col = 0; col < SIZE; col++)
				grid[row][col] = null;
		for (int col = 0; col < SIZE; col++)
			numColCards[col] = 0;
                for (int row = 0; row < SIZE; row++)
			numColCards[row] = 0;
	}

	@Override
	public int[] getPlay(Card card, long millisRemaining) {
                //Rule Based card placement
                
                //Choose Column
		int[] playPos = new int[2];
		int col = card.getSuit();  // try to play the card in the column of the suit number
		if (numColCards[col] == SIZE) { // if that's not possible
			if (numColCards[SIZE - 1] < SIZE)  // try to put it in the last column
				col = SIZE - 1;
			else { // or the first column with a free spot
				col = 0;
				while (numColCards[col] == SIZE)
					col++;
			}
		}
                
                //Find Available Rows
                
                LinkedList <Integer> availableRows = new LinkedList <Integer> ();
                for (int row = 0; row < SIZE; row++) if (grid[row][col] == null) availableRows.add(row);
                
                //Choose Rows
                boolean hasFirst, hasSecond;
                int firstBest, secondBest;
                hasFirst = hasSecond = false;
                firstBest = secondBest = availableRows.get(0);
                for (Integer row : availableRows) {
                    //First Choice: row with most cards containing cards of same rank
                    if ((rowRanks.containsKey(card.getRank())) 
                            && (rowRanks.get(card.getRank()) == row) 
                            && (numRowCards[row] >= numRowCards[firstBest])) {
                        firstBest = row;
                        hasFirst = true;
                    } 
                    //Second Choice: row with least cards containing no cards of same rank
                    else if (numRowCards[row] <= numRowCards[secondBest]) {
                        secondBest = row;
                        hasSecond = true;
                    }
                }
                
                int row = availableRows.get(0);
                if (hasFirst) row = firstBest;
                else if (hasSecond) row = secondBest;
                
                /*
                int bestrow = 0;
                for (row = 0; row < SIZE; row++) {
                    //If there is an available position
                    if (grid[row][col] == null) {
                        bestrow = row;
                        if ((rowRanks.containsKey(card.getRank())) 
                                && (rowRanks.get(card.getRank()) == row)) 
                                    break;
                        
                    }
                }
                
                row = bestrow;
                */
                
                numRowCards[row]++;
                numColCards[col]++;
                rowRanks.put(card.getRank(),row);
		playPos[1] = col;
                playPos[0] = row;
		grid[playPos[0]][playPos[1]] = card;
		return playPos;
	}
}
