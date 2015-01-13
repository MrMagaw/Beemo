package pokersquares.players;


import java.util.Collections;
import java.util.Stack;
import pokersquares.Card;

/**
 * RandomPokerSquaresPlayer - a simple example implementation of the player interface for PokerSquares that makes random placements.
 * Author: Todd W. Neller
 */
public class RandomPokerSquaresPlayer implements PokerSquaresPlayer {

	public Stack<Integer> plays = new Stack<Integer>();
	
	//@Override
	public void init() { // Create a stack permutation of 25 integers (0-24) for random plays
		plays.clear();
		for (int i = 0; i < 25; i++)
			plays.push(i);
		Collections.shuffle(plays); 
	}

	//@Override
	public int[] getPlay(Card card, long millisRemaining) {
		int play = plays.pop(); // get the next random position for play
		int[] playPos = {play / 5, play % 5}; // decode it into row and column
		return playPos; // return it
	}

        /*
	public static void main(String[] args) {
		new PokerSquares(new RandomPokerSquaresPlayer(), 60000L).play(); // play a single game
	}*/

}
