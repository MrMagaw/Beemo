package pokersquares.players;

import pokersquares.Card;
import pokersquares.PokerSquares;

/**
 * FlushPokerSquaresPlayer - a simple example implementation of the player interface for PokerSquares that only seeks to form vertical flushes.
 * Author: Todd W. Neller
 */
public class FlushPokerSquaresPlayer implements PokerSquaresPlayer {

	private final int SIZE = PokerSquares.SIZE;
	private Card[][] grid = new Card[SIZE][SIZE];
	private int[] numColCards = new int[SIZE];
	
	@Override
	public void init() {
		for (int row = 0; row < SIZE; row++)
			for (int col = 0; col < SIZE; col++)
				grid[row][col] = null;
		for (int col = 0; col < SIZE; col++)
			numColCards[col] = 0;
	}

	@Override
	public int[] getPlay(Card card, long millisRemaining) {
            
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
                
		playPos[0] = numColCards[col]++;
		playPos[1] = col;
		grid[playPos[0]][playPos[1]] = card;
		return playPos;
	}
}
