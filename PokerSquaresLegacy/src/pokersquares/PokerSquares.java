package pokersquares;

import pokersquares.players.PokerSquaresPlayer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.Stack;
import java.io.*;

/* The solitaire game "Poker Squares"
 * Author: Todd W. Neller

Notes: 

A Poker Squares grid is represented as a 5-by-5 array of Card objects.  A null indicates an empty position.
In the solitaire game of Poker Squares, a deck is initially shuffled.
Each turn, the player draws a card from the deck and places it in any empty cell of a 5-by-5 grid.  Once placed, cards may not be moved.
After the last cell is filled, each row and column are scored according to the American point system for Poker Squares hands:

100 - Royal Flush: A T-J-Q-K-A sequence all of the same suit. Example: TC, JC, QC, KC, AC.
75 - Straight Flush: Five cards in sequence all of the same suit. Example: AD, 2D, 3D, 4D, 5D.
50 - Four of a Kind: Four cards of the same rank. Example: 9C, 9D, 9H, 9S, 6H.
25 - Full House: Three cards of one rank with two cards of another rank. Example: 7S, 7C, 7D, 8H, 8S.
20 - Flush: Five cards all of the same suit. Example: AH, 2H, 3H, 5H, 8H.
15 - Straight: Five cards in sequence. Aces may be high or low but not both. (Straights do not wrap around.) Example: 8C, 9S, TH, JD, QC.
10 - Three of a Kind: Three cards of the same rank. Example: 2S, 2H, 2D, 5C, 7S.
5 - Two Pair: Two cards of one rank with two cards of another rank. Example: 3H, 3D, 4C, 4S, AC.
2 - One Pair: Two cards of one rank.  Example: 5D, 5H, TC, QS, AH.
(0 otherwise)

The player's total score is the sum of the scores for each of the 10 row and column hands.

Relevant Resources: http://tinyurl.com/pokersqrs

For our purposes, a player is considered better if it has a higher expected game score, i.e. has a higher score average over many games.

In our implementation, each turn a PokerSquaresPlayer will be passed (1) a Card object and (2) the number of milliseconds remaining in the game, 
and will return a length 2 integer array with the row and column the player placed the card.  In the event that the player makes an illegal 
play or "times out", i.e. runs out of time for play, the player loses with a final score of 0.

This file contains not only the code to run a simple demonstration game with a random player, but also has utility functions for scoring
that will be useful for coding good players.

 */

public class PokerSquares {
	
	public static final int SIZE = 5; // square grid size
	public static final long GAME_MILLIS = 60000L; // 2013 contest maximum milliseconds per game

	private PokerSquaresPlayer player; // current player
	private long gameMillis; // maximum milliseconds for current game
	private boolean verbose = true; // whether or not to print move-by-move transcript of the game
	private Card[][] grid = new Card[SIZE][SIZE]; // current game grid
	private Random random = new Random(); // current game random number generator
	
	/**
	 * @param player Poker Squares player object
	 * @param gameMillis maximum milliseconds permitted for game
	 */
	public PokerSquares(PokerSquaresPlayer player, long gameMillis) {
		this.player = player;
		this.gameMillis = gameMillis;
	}

	/**
	 * Play a game of Poker Squares with the given PokerSquaresPlayer and time limit, returning the game score.
	 * @return final game score
	 */
	public int play() {
		player.init();

		// shuffle deck
		Stack<Card> deck = new Stack<Card>();
		for (Card card : Card.allCards)
			deck.push(card);
		Collections.shuffle(deck, random);
		
		// clear grid
		for (int row = 0; row < SIZE; row++)
			for (int col = 0; col < SIZE; col++)
				grid[row][col] = null;
		
		// play game
		long millisRemaining = gameMillis;
		int cardsPlaced = 0;
		while (cardsPlaced < SIZE * SIZE) {
			Card card = deck.pop();
			long startTime = System.currentTimeMillis();
			int[] play = player.getPlay(card, millisRemaining);
			millisRemaining -= System.currentTimeMillis() - startTime;
			if (millisRemaining < 0) { // times out
				System.err.println("Player Out of Time");
				return 0;
			}
			if (play.length != 2 || play[0] < 0 || play[0] >= SIZE || play[1] < 0 || play[1] >= SIZE || grid[play[0]][play[1]] != null) { // illegal play
				System.err.printf("Illegal play: %s\n", Arrays.toString(play));
				return 0;
			}
			grid[play[0]][play[1]] = card;
			cardsPlaced++;
			if (verbose) {
				printGrid(grid);
				System.out.println();
			}
		}
		return getScore(grid);
	}
	
	/**
	 * Play a sequence of games, collecting and reporting statistics.
	 * @param numGames number of games to play
	 * @param startSeed seed of first game. Successive games use successive seeds
	 * @param verbose whether or not to provide verbose output of game play
	 * @return integer array of game scores
	 */
	public int[] playSequence(int numGames, long startSeed, boolean verbose) {
		this.verbose = verbose;
		int[] scores = new int[numGames];
		double scoreMean = 0;
		int min = 10000, max = scores[0];
		for (int i = 0; i < numGames; i++) {
			random.setSeed(startSeed + i);
			int score = play();
			scores[i] = score;
			scoreMean += score;
			if (scores[i] < min) min = scores[i];
			if (scores[i] > max) max = scores[i];
			System.out.println(score);
		}
		scoreMean /= numGames;
		double scoreStdDev = 0;
		for (int i = 0; i < numGames; i++) {
			double diff = scores[i] - scoreMean;
			scoreStdDev += diff * diff;
		}
		scoreStdDev = Math.sqrt(scoreStdDev / numGames);
                String Stats = "Mean Score: " +scoreMean+", Standard Deviation: "+scoreStdDev+", Minimum: "+min+", Maximum: "+max+", Games: "+numGames;
		//new StatWriter(Stats, "PokerSquaresStats.txt");
                System.out.println(Stats);
		return scores;
	}
	
	/**
	 * Print the current game grid and score.
	 * @param grid current game grid 
	 */
        public static void printHand(Card[] hand,double score) {
                for (int i = 0; i < SIZE; i++) {
                    System.out.printf(" %s ", hand[i] == null ? "--" : hand[i].toString());
                }
                System.out.printf("%3f\n", score);
        }
        
	public static void printGrid(Card[][] grid) {
		// get scores
		int[] handScores = getHandScores(grid);
		int totalScore = 0;
		for (int handScore : handScores)
			totalScore += handScore;
		
		// print grid
		for (int row = 0; row < SIZE; row++) {
			for (int col = 0; col < SIZE; col++) 
				System.out.printf(" %s ", grid[row][col] == null ? "--" : grid[row][col].toString());
			System.out.printf("%3d\n", handScores[row]);
		}
		for (int col = 0; col < SIZE; col++) 
			System.out.printf("%3d ", handScores[SIZE + col]);
		System.out.printf("%3d Total\n", totalScore);
	}
	
	/**
	 * Get the score of the given Card grid.
	 * @param grid Card grid
	 * @return score of given Card grid
	 */
	public static int getScore(Card[][] grid) {
		int[] handScores = getHandScores(grid);
		int totalScore = 0;
		for (int handScore : handScores)
			totalScore += handScore;
		return totalScore;
	}
	
	/**
	 * Get an int array with the individual hand scores of rows 0 through 4 followed by columns 0 through 4. 
	 * @param grid 2D Card array representing play grid
	 * @return an int array with the individual hand scores of rows 0 through 4 followed by columns 0 through 4. 
	 */
	public static int[] getHandScores(Card[][] grid) {
		int[] handScores = new int[2 * SIZE];
		for (int row = 0; row < SIZE; row++) {
			Card[] hand = new Card[SIZE];
			for (int col = 0; col < SIZE; col++)
				hand[col] = grid[row][col];
			handScores[row] = getHandScore(hand);
		}
		for (int col = 0; col < SIZE; col++) {
			Card[] hand = new Card[SIZE];
			for (int row = 0; row < SIZE; row++)
				hand[row] = grid[row][col];
			handScores[SIZE + col] = getHandScore(hand);
		}
		return handScores;
	}
	
	/**
	 * Get the score of the given Card hand.
	 * @param hand Card hand
	 * @return score of given Card hand.
	 */
	public static int getHandScore(Card[] hand) {
		// Compute counts
		int[] rankCounts = new int[Card.NUM_RANKS];
		int[] suitCounts = new int[Card.NUM_SUITS];
		for (Card card : hand)
			if (card != null) {
				rankCounts[card.getRank()]++;
				suitCounts[card.getSuit()]++;
			}
		
		// Compute count of rank counts
		int maxOfAKind = 0;
		int[] rankCountCounts = new int[hand.length + 1];
		for (int count : rankCounts) {
				rankCountCounts[count]++;
				if (count > maxOfAKind)
					maxOfAKind = count;
			}
			
		// Flush check
		boolean hasFlush = false;
		for (int i = 0; i < Card.NUM_SUITS; i++)
			if (suitCounts[i] != 0) {
				if (suitCounts[i] == hand.length)
					hasFlush = true;
				break;
			}
		
		// Straight check
		boolean hasStraight = false;
		boolean hasRoyal = false;
		int rank = 0;
		while (rank <= Card.NUM_RANKS - 5 && rankCounts[rank] == 0)
			rank++;
		hasStraight = (rank <= Card.NUM_RANKS - 5 && rankCounts[rank] == 1 && rankCounts[rank + 1] == 1 && rankCounts[rank + 2] == 1 && rankCounts[rank + 3] == 1 && rankCounts[rank + 4] == 1);
		if (rankCounts[0] == 1 && rankCounts[12] == 1 && rankCounts[11] == 1 && rankCounts[10] == 1 && rankCounts[9] == 1) 
			hasStraight = hasRoyal = true;
		
		// Return score
		if (hasFlush) {
			if (hasRoyal)
				return 100; // Royal Flush
			if (hasStraight)
				return 75; // Straight Flush
		}
		if (maxOfAKind == 4)
			return 50; // Four of a Kind
		if (rankCountCounts[3] == 1 && rankCountCounts[2] == 1)
			return 25; // Full House
		if (hasFlush)
			return 20; // Flush
		if (hasStraight)
			return 15; // Straight
		if (maxOfAKind == 3)
			return 10; // Three of a Kind
		if (rankCountCounts[2] == 2)
			return 5; // Two Pair
		if (rankCountCounts[2] == 1)
			return 2; // One Pair
		return 0; // Otherwise, score nothing.
	}
	
	/**
	 * Set the seed of the game pseudorandom number generator.
	 * @param seed pseudorandom number generator seed
	 */
	public void setSeed(long seed) {
		random.setSeed(seed);
	}
}
