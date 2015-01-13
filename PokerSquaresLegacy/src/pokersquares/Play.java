/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pokersquares;
import pokersquares.evaluations.HandRank;
import pokersquares.players.Beemo;
import java.util.*;
import static pokersquares.PokerSquares.SIZE;
import pokersquares.evaluations.*;

/**
 *
 * @author newuser
 *  A Class to hold a play with its current deck, grid and card
 */
public class Play implements Comparable<Play>{ 
    //PLAY STATE REPRESENTATION
    private final Play parent;  //this play's parent
    private final Beemo BMO;
    private Beemo simBMO = null;
    private final Map <String,Double> bestCardEvals; 
    
    private Card[][] hands; //
    private double[] handEvaluations; //the evaluations of the hands in 
    private LinkedList <Integer> playedPositions; //a list of the moves that have led to this play, in order from most recent to first
    private LinkedList <Card> playedCards; //a list of the cards already played from deck in order from most recent to first
    private double staticEval = 0;
    private double simEval = 0;
    private double evaluation = 0;
    private double simulationSum = 0;
    private int numSimulations = 0; 
    
    
    public Play(Beemo BMO){
        //root play constructor
        this.BMO = BMO;
        parent = null;
        playedPositions = setPlayedPositions(parent);
        playedCards = setPlayedCards(parent);
        bestCardEvals = new HashMap();
        handEvaluations = new double[10];
    }
    
    public Play (Play parent) {
        //branch play constructor
        this.parent = parent;
        this.playedPositions = setPlayedPositions(parent);
        this.playedCards = setPlayedCards(parent);
        this.bestCardEvals = new HashMap();
        this.handEvaluations = new double[10];
        this.hands = parent.hands;
        this.BMO = parent.BMO;
        System.arraycopy(this.parent.handEvaluations, 0, this.handEvaluations, 0, this.parent.handEvaluations.length );
    }
    
    public void updateEvaluation(int row, int col) {
        //update the evaluation as a sum of all the hand evaluations
        
        this.evaluation = 
                (this.parent.evaluation - this.parent.handEvaluations[row] - this.parent.handEvaluations[col + 5]) +
                (this.handEvaluations[row] + this.handEvaluations[col + 5]);
        
        this.staticEval = this.evaluation;
        
    }
    
    public void evaluateHands(int row, int col) {
        //BUILD HANDS up from recorded positions
        for (int i = this.playedPositions.size()-1; i >= 0; i--) {
            int r = this.playedPositions.get(i)/5;
            int c = this.playedPositions.get(i)%5;
            this.hands[r][c] = this.playedCards.get(i);
            this.hands[c+5][r] = this.playedCards.get(i);
        }
        
        //EVALUATE MOST RECENTLY UPDATED HANDS
        this.handEvaluations[row] = getHandEvaluation(BMO, this.hands[row], row, false);
        this.handEvaluations[col + 5] = getHandEvaluation(BMO, this.hands[col + 5], col, true);
        
        //REVERT HANDS to root state
        for (int i = this.playedPositions.size()-1; i >= 0; i--) {
            int r = this.playedPositions.get(i)/5;
            int c = this.playedPositions.get(i)%5;
            this.hands[r][c] = null;
            this.hands[c+5][r] = null;
        }
        
    }
    
    public void evaluatePlay() {
        
        //BUILD GRID up from recorded positions
        for (int i = this.playedPositions.size()-1; i >= 0; i--) {
            int row = this.playedPositions.get(i)/5;
            int col = this.playedPositions.get(i)%5;
            BMO.grid[row][col] = this.playedCards.get(i);
        }
        
        calculateEvaluation(); //when a play is made, recalculate the play's average
        
        //REVERT GRID to root state
        for (int i = this.playedPositions.size()-1; i >= 0; --i) {
            int row = this.playedPositions.get(i)/5;
            int col = this.playedPositions.get(i)%5;
            BMO.grid[row][col] = null;
        }
    }
    
    public void buildAndEvaluateHands() {
        this.hands = new Card[10][SIZE];
        this.handEvaluations = new double[10];
        
        //check each row 
        for (int row = 0; row < 5; ++row) {
            
            Card[] hand = new Card[SIZE];
            //check row forwards
            for (int col = 0; col < 5; ++col) {
                //build hand
                hand[col] = BMO.grid[row][col];
                
                //add hand to hands
                this.hands[row] = hand;
                
                
            }
            //evaluate hand
            this.handEvaluations[row] = getHandEvaluation(BMO, this.hands[row], row, false);
        }
        
        //check each column
        for (int col = 0; col < 5; ++col) {
            
            Card[] hand = new Card[SIZE];
            //check row downwards
            for (int row = 0; row < 5; ++row) {
                //build hand
                hand[row] = BMO.grid[row][col];
                
                //add hand to hands
                this.hands[col+5] = hand;
            }
            //evaluate hand
            this.handEvaluations[col + 5] = getHandEvaluation(BMO, this.hands[col+5], col, true);
        }
    }
    
    public double calculateEvaluation() {
        //IMPLEMENT precalculate as much as possible, pass on evaluation, recalcualte only the hands effected by a play
        //static evaluation calculating the completeness/totality of all possible hands in each row and column
        double evaluation = 0;
        
        //check each row 
        for (int row = 0; row < 5; ++row) {
            
            Card[] hand = new Card[SIZE];
            //check row forwards
            for (int col = 0; col < 5; ++col) {
                //build hand
                
                hand[col] = BMO.grid[row][col];
                
            }
            //evaluate hand
            evaluation += getHandEvaluation(BMO, hand, row, false);
            
        }
        
        //check each column
        for (int col = 0; col < 5; ++col) {
            
            Card[] hand = new Card[SIZE];
            //check row downwards
            for (int row = 0; row < 5; ++row) {
                //build hand
                hand[row] = BMO.grid[row][col];
            }
            //evaluate hand
            evaluation += getHandEvaluation(BMO, hand, col, true);
        }
        
        this.evaluation = evaluation;
        this.staticEval = evaluation;
        return evaluation;
    }
    
    public static double getHandEvaluation(Beemo BMO, Card[] hand, int n, boolean col) {
        
        //double handEvaluation = reverseEvaluation(hand, col);
        //double handEvaluation = GeneticEvaluation.geneticEvaluation(hand, n, col);
        double handEvaluation = (new PatternPolicy()).evaluate(BMO, hand, n, col);
        
        return handEvaluation;
    }
    
    public double probabilityEvaluation(Card[] hand, boolean col) {
        //Evaluation based on the probability of completing the poker hand
        //GOOD CONCEPT - NEEDS BETTER EXECUTION
        
        double handEvaluation = 0; 
	int[] rankCounts = new int[Card.NUM_RANKS];
	int[] suitCounts = new int[Card.NUM_SUITS]; //contains an array with a count of cards in each suit
        int[] rankCountCounts = new int[hand.length+1];
        int placedCards = 0;
        int maxOfAKind = 0;
        int nullCards = 0;
        
        //Count no of placed cards, no of cards of each rank, no of cards of each suit
	for (Card card : hand) {
            if (card != null) {
                ++placedCards;
		++rankCounts[card.getRank()];
		++suitCounts[card.getSuit()];
            }
        }
        nullCards = SIZE - placedCards;
        //Count no of cards with same rank
	for (int count : rankCounts) {
            ++rankCountCounts[count];
            if (count > maxOfAKind) maxOfAKind = count;
            
        }
        
        //COMPLETE HANDS
        // Flush check
	boolean hasFlush = false;
	for (int i = 0; i < Card.NUM_SUITS; ++i) {
            if (suitCounts[i] != 0) {
		if ((suitCounts[i] == placedCards) && (col)) { //only values flushes in columns
                    hasFlush = true;
                    
                }
                break;
		
            }
	}
        
        // Straight check
	boolean hasStraight = false;
	boolean hasRoyal = false;
	int lowestRankedCard = rankCounts[0];
        int highestRankedCard = rankCounts[0];
	for (int i = 0; i < Card.NUM_RANKS; ++i) {
            if (rankCounts[i] > highestRankedCard) highestRankedCard = rankCounts[i];
            if (rankCounts[i] < lowestRankedCard) lowestRankedCard = rankCounts[i];
        }
        
	hasStraight = (lowestRankedCard <= Card.NUM_RANKS - 5 && rankCounts[lowestRankedCard] == 1 && rankCounts[lowestRankedCard + 1] == 1 && rankCounts[lowestRankedCard + 2] == 1 && rankCounts[lowestRankedCard + 3] == 1 && rankCounts[lowestRankedCard + 4] == 1);
	if (rankCounts[0] == 1 && rankCounts[12] == 1 && rankCounts[11] == 1 && rankCounts[10] == 1 && rankCounts[9] == 1) {
            hasStraight = hasRoyal = true;
        }
        
        //Return score
        if (hasFlush) {
            if (hasRoyal)
		return 100; // Royal Flush
            if (hasStraight)
		return 75; // Straight Flush
            }
	if (rankCountCounts[4] == 1) return 50; // Four of a Kind
	if (rankCountCounts[3] == 1 && rankCountCounts[2] == 1) return 25; // Full House
	if (hasFlush && (placedCards == 5)) return 20; // Flush
	if (hasStraight) return 15; // Straight
        
        if (nullCards == 0) nullCards = 1; 
	
        //PARTIAL HANDS
        //STRAIGHT
        LinkedList <Double> partialScores = new LinkedList();
        boolean hasPartialRoyal = false;
        boolean hasPartialStraightFlush = false;
        boolean hasPartialFourKind = false;
        boolean hasPartialFullHouse = false;
        boolean hasPartialFlush = false;
        boolean hasPartialStraight = false;
        boolean hasPartialThree = false;
        boolean hasPartialTwoPair = false;
        boolean hasPartialPair = false;
        
        //check partial straight
        if ((rankCountCounts[4] == 0) && (rankCountCounts[3] == 0) && (rankCountCounts[2] == 0) //if there are no pairs
                && (lowestRankedCard <= (Card.NUM_RANKS-(4-nullCards)))
                && (highestRankedCard <= (lowestRankedCard + 4))){
            hasPartialStraight = true;
            for (int i = 0; i < Card.NUM_RANKS; i++) {
                if ((rankCounts[i] == 1) && !((i <= highestRankedCard) && (i >= lowestRankedCard))){
                    hasPartialStraight = false;
                }
            }
            
        }
        
        //ROYAL FLUSH
        //if FLUSH and STRAIGHT and (lowest ranked card >= (highest rank - 4)) cards needed = num null values
        if (col && (hasPartialStraight && hasFlush && (lowestRankedCard >= (Card.NUM_RANKS - 4)))) hasPartialRoyal = true;
        
        //STRAIGHT FLUSH
        //if FLUSH and STRAIGHT cards needed = num null values
        if ((hasPartialStraight && hasFlush) && (col)) hasPartialStraightFlush = true;
        
        //FOUR OF A KIND
        //if ((cards of same suit + null values) >= 4) cards needed = num null values
        if (((maxOfAKind + nullCards) >= 4) && (!col)) hasPartialFourKind = true;
        
        //FULL HOUSE
        //if (there are three nulls) cards needed = 3
        //if (there is one pair) and (two null values) cards needed = 2 
        //if (there are two pairs) cards needed = 1
        //if (there is one three of a kind) and (one null) cards needed = 1
        if (!col &&(nullCards == 3)) hasPartialFullHouse = true;
        if (!col &&((rankCountCounts[2] == 1) && nullCards >= 2)) hasPartialFullHouse = true;
        if (!col &&(rankCountCounts[2] == 2)) hasPartialFullHouse = true;
        if (!col &&((rankCountCounts[3] == 1) && (nullCards >= 1))) hasPartialFullHouse = true;
        
        //FLUSH
        //if (all cards are of the same suit) cards needed = num null values
        if (hasFlush) hasPartialFlush = true;
        
        //STRAIGHT
        //if (there are 4 null values) cards needed = 4
        //if (there are 3 null values) 
        //and ((lowest ranked card) is less than or equal to  (the max num of ranks minus five minus the number of null values)
        //and ((highest ranked card) is less than or equal to (lowest ranked card plus five))
        //for each extra card, check if card is between the lowest ranked card and the highest ranked card 
        //there must be no three of a kinds or pairs
        //if (all of the above) cards needed = num null values
        
        //THREE OF A KIND 
        //if (there is a pair) && (one null value) cards needed = 1
        //if (there is two null values) cards needed = 2
        if ((rankCountCounts[2] == 1) && (nullCards == 1)) hasPartialThree = true;
        if (nullCards == 2) hasPartialThree = true;
        
        //TWO PAIR
        //if (there is a pair) && (one null value) cards needed = 1
        //if (there is two null values) cards needed = 2
        if ((rankCountCounts[2] == 1) && (nullCards == 1)) hasPartialTwoPair = true;
        if (nullCards == 2) hasPartialTwoPair = true;
        
        //PAIR 
        //if (there is a null value in the hand) cards needed = 1
        if (nullCards == 1) hasPartialPair = true;
        
        //evaluation = (1 / (odd's fifth root ^ cards needed)) * (hand value) 
        
        //return highest value or sum of all values?
        //SUM OF ALL PROBABILITIES
        if (hasPartialRoyal) handEvaluation += Math.pow((1/Math.pow(14.53944, nullCards)),1) * 100;
        if (hasPartialStraightFlush) handEvaluation += Math.pow((1/Math.pow(9.36909, nullCards)),1) * 75;
        if (hasPartialFourKind) handEvaluation += Math.pow((1/Math.pow(5.29544, nullCards)),1) * 50;
        if (hasPartialFullHouse) handEvaluation += Math.pow((1/Math.pow(3.6995, nullCards)),1) * 25;
        if (hasPartialFlush) handEvaluation += Math.pow((1/Math.pow(3.4767, nullCards)), (1/4)) * 20;
        if (hasPartialStraight) handEvaluation += Math.pow((1/Math.pow(3.0227, nullCards)),5) * 15;
        if (hasPartialThree) handEvaluation += Math.pow((1/Math.pow(2.1533, nullCards)),5) * 10;
        if (hasPartialTwoPair) handEvaluation += Math.pow((1/Math.pow(1.8205, nullCards)),5) * 5;
        if (hasPartialPair) handEvaluation += Math.pow((1/Math.pow(1.0634, nullCards)),5) * 2;
        
        /*
        //only the highest hand counts
        if (hasPartialRoyal) partialScores.addLast((Math.pow((1/Math.pow(14.53944, nullCards)),1)) * 100);
        if (hasPartialStraightFlush) partialScores.addLast(Math.pow((1/Math.pow(9.36909, nullCards)),1) * 75);
        if (hasPartialFourKind) partialScores.add(Math.pow((1/Math.pow(5.29544, nullCards)),1) * 50);
        if (hasPartialFullHouse) partialScores.add(Math.pow((1/Math.pow(3.6995, nullCards)),1) * 25);
        if (hasPartialFlush) partialScores.add(Math.pow((1/Math.pow(3.4767, nullCards)), (1/4)) * 20);
        if (hasPartialStraight) partialScores.add(Math.pow((1/Math.pow(3.0227, nullCards)),4) * 15);
        if (hasPartialThree) partialScores.add(Math.pow((1/Math.pow(2.1533, nullCards)),4) * 10);
        if (hasPartialTwoPair) partialScores.add(Math.pow((1/Math.pow(1.8205, nullCards)),4) * 5);
        if (hasPartialPair) partialScores.addLast(Math.pow((1/Math.pow(1.0634, nullCards)),4) * 2);
        
        if (partialScores.isEmpty()) partialScores.add((double)0);
        Collections.sort(partialScores);
        
        handEvaluation = partialScores.getLast();
        */
        //only the highest hand counts
        
        return handEvaluation;
    }
    
    public static double reverseEvaluation(Card[] hand, boolean col) {
        
        double handEvaluation = 0;
        // Compute counts
	int[] rankCounts = new int[Card.NUM_RANKS];
	int[] suitCounts = new int[Card.NUM_SUITS]; //contains an array with a count of cards in each suit
        int placedCards = 0;
        int maxOfAKind = 0;
	int[] rankCountCounts = new int[hand.length + 1];
        
        //Count no of placed cards, no of cards of each rank, no of cards of each suit
	for (Card card : hand) {
            if (card != null) {
                placedCards++;
		rankCounts[card.getRank()]++;
		suitCounts[card.getSuit()]++;
            }
        }
        
        int emptyPositions = SIZE - placedCards;
        double handWeight = placedCards/(double)SIZE; //the weight of hand with respect to how many cards are played
        //System.out.println(handWeight);
        
        //Count no of cards with same rank
	for (int count : rankCounts) {
            ++rankCountCounts[count];
            if (count > maxOfAKind) {
                maxOfAKind = count;
            }
        }
        
        // Flush check
	boolean hasFlush = false;
	for (int i = 0; i < Card.NUM_SUITS; i++) {
            if (suitCounts[i] != 0) {
		if ((suitCounts[i] == placedCards) && (col)) { //only values flushes in columns
                    hasFlush = true;
                }
		break;
            }
	}
        
        // Straight check
	boolean hasStraight = false;
	boolean hasRoyal = false;
	int rank = 0;
	while (rank <= Card.NUM_RANKS - 5 && rankCounts[rank] == 0){
            //finds the rank of lowest ranked card in hand
            ++rank;
        }
        
	hasStraight = (rank <= Card.NUM_RANKS - 5 && rankCounts[rank] == 1 && rankCounts[rank + 1] == 1 && rankCounts[rank + 2] == 1 && rankCounts[rank + 3] == 1 && rankCounts[rank + 4] == 1);
	if (rankCounts[0] == 1 && rankCounts[12] == 1 && rankCounts[11] == 1 && rankCounts[10] == 1 && rankCounts[9] == 1) {
            hasStraight = hasRoyal = true;
        }
        
        //Test
        //if (hasFlush) handEvaluation = ((double)20*((double)Math.pow(handWeight, 1/2))*handWeight*handWeight); //Flush WORKS
        
        //Consistent Hand Evaluation
        if (hasFlush) {
            if (hasRoyal)
                handEvaluation = ((double)100);  // Royal Flush
            else if (hasStraight) 
                handEvaluation = ((double)75); // Straight Flush
            else
                handEvaluation = ((double)20*((double)Math.pow(handWeight, 1/2))*handWeight*handWeight); //Flush WORKS
        }else
            if (hasStraight) 
                handEvaluation = ((double)15); //Straight
            else
                if (rankCountCounts[4] == 1) 
                    handEvaluation = ((double)50); //Four of a Kind
                else if (rankCountCounts[3] == 1)
                    if(rankCountCounts[2] == 1)
                        handEvaluation = ((double)25); //Full House
                    else
                        handEvaluation = ((double)10); // Three of a Kind
                else if (rankCountCounts[2] == 2) 
                    handEvaluation = ((double)5); // Two Pair
                else if (rankCountCounts[2] == 1) 
                    handEvaluation = ((double)2); //One Pair
        
        return handEvaluation;
    }
    
    public void recordPlayPos(Card card, int playPos) {  
        //RECORD POSITION
        this.playedPositions.addFirst(playPos);
        this.playedCards.addFirst(card);
    }
    
    public void propagateEvaluation() {
        //calculate the average eval for all evals in bestCardEvals
        
        double cardEvalTotal = 0;
        for (Double cardEval : this.bestCardEvals.values()) {
            cardEvalTotal += cardEval;
        }
        
        this.evaluation = cardEvalTotal / (double) this.bestCardEvals.size();
    }
    
    public void updatebestCardEvals(Card card, double eval) {
        //updates the evaluation for card in bestCardEvals if eval is greater than current stored val
        String cardString = card.toString();
        if (bestCardEvals.containsKey(cardString)){
            if ((double)bestCardEvals.get(cardString) < eval ){
                bestCardEvals.put(cardString, eval);
            }
        } else {
            bestCardEvals.put(cardString, eval);
        }
    }
    
    public void simulateGame(String algorithmID, long seed) {
        //simulate a game playing each turn using the specified search algorithm
        
        simBMO = new Beemo(BMO, algorithmID);
        
        Random r = new Random(seed);
        
        //play Play's moves in simBMO
        for (int i = this.playedPositions.size()-1; i >= 0; i--) {
            int row = this.playedPositions.get(i)/5;
            int col = this.playedPositions.get(i)%5;
            //play card in simBMO's grid
            simBMO.grid[row][col] = this.playedCards.get(i);
            //remove card from deck
            simBMO.deck.remove(this.playedCards.get(i));
            //increment turn
            simBMO.turn++;
        }
        
        //playout game for remainder of turns
        while (simBMO.turn < 25) {
            Card card = simBMO.deck.get(r.nextInt(simBMO.deck.size()));
            
            simBMO.getPlay(card, seed);
        }
        
        //System.out.println("simGrid");
        //PokerSquares.printGrid(simBMO.grid);
        
        //RECORD EVALUATION
        simulationSum += PokerSquares.getScore(simBMO.grid);
        numSimulations++;
    }
    
    public void simulateDeterministicGame(LinkedList playedCards, String algorithmID, long seed) {
        //simulate a game playing each turn using the specified search algorithm 
        //play cards from cardSet
        //card set is ordered from first card played to last
        
        simBMO = new Beemo(BMO, algorithmID);
        simBMO.init();
        
        Random r = new Random(seed);
        
        //play Play's moves in simBMO
        for (int i = this.playedPositions.size()-1; i >= 0; i--) {
            int row = this.playedPositions.get(i)/5;
            int col = this.playedPositions.get(i)%5;
            //play card in simBMO's grid
            simBMO.grid[row][col] = this.playedCards.get(i);
            //remove card from deck
            simBMO.deck.remove(this.playedCards.get(i));
            //increment turn
            simBMO.turn++;
        }
        
        //playout game for remainder of turns
        while (simBMO.turn < 25) {
            Card card = (Card) playedCards.removeFirst();
            
            simBMO.getPlay(card, seed);
        }
        
        //System.out.println("simGrid");
        //PokerSquares.printGrid(simBMO.grid);
        
        //RECORD EVALUATION
        simulationSum += PokerSquares.getScore(simBMO.grid);
        numSimulations++;
    }
    
    public void simulateOXGame(long seed) {
        //simulate a game playing each turn using the OX search algorithm
        
        simBMO = new Beemo(BMO, "ox");
        
        Random r = new Random(seed);
        
        //play Play's moves in simBMO
        for (int i = this.playedPositions.size()-1; i >= 0; i--) {
            int row = this.playedPositions.get(i)/5;
            int col = this.playedPositions.get(i)%5;
            //play card in simBMO's grid
            simBMO.grid[row][col] = this.playedCards.get(i);
            //remove card from deck
            simBMO.deck.remove(this.playedCards.get(i));
            //increment turn
            simBMO.turn++;
        }
        
        //playout game for remainder of turns
        while (simBMO.turn < 25) {
            Card card = simBMO.deck.get(r.nextInt(simBMO.deck.size()));
            
            simBMO.getPlay(card, seed);
        }
        
        //RECORD EVALUATION
        simulationSum += PokerSquares.getScore(simBMO.grid);
        numSimulations++;
    }
    
    public void simulateRB2Game(long seed) {
        //simulate rule based game
        
        Random r = new Random(seed);
        
        //INITIATE available positions
        LinkedList <Integer> removePos = new LinkedList <Integer> ();
        LinkedList <Integer> availablePos = new LinkedList <Integer> (BMO.playPos);
        availablePos.removeAll(playedPositions);
        
        //INITIATE available cards
        LinkedList <Card> availableCards = new LinkedList <Card> (BMO.deck);
        availableCards.removeAll(playedCards);
        
        //BUILD GRID up from recorded positions
        for (int i = this.playedPositions.size()-1; i >= 0; i--) {
            int row = this.playedPositions.get(i)/5;
            int col = this.playedPositions.get(i)%5;
            BMO.grid[row][col] = this.playedCards.get(i);
            removePos.add((Integer) this.playedPositions.get(i));
        }
        
        //Count Cards per Column
        int[] numColCards = {0,0,0,0,0};
        for (int col = 0; col < SIZE; col++)
            for (int row = 0; row < SIZE; row++) 
		if (BMO.grid[row][col] != null) 
                    numColCards[col]++;
        
        //Count Cards per Row and Card Ranks
        HashMap <Integer, Integer> rowRanks = new HashMap <Integer,Integer> (); // <Rank,Row>
        int[] numRowCards = {0,0,0,0,0};
        for (int row = 0; row < SIZE; row++)
            for (int col = 0; col < SIZE; col++) 
		if (BMO.grid[row][col] != null) {
                    rowRanks.put(BMO.grid[row][col].getRank(), row);
                    numRowCards[row]++;
                }
        
        while (availablePos.size() > 0) {
            
                //CHOOSE CARD
                Card card = availableCards.remove(r.nextInt(availableCards.size()));
            
                //If BMO can complete a hand
                //check each row 
                boolean hasTwoP = false;
                boolean hasThreeK = false;
                boolean hasFourK = false;
                boolean hasFullH = false;
                boolean hasStraightF = false;
                int[] twoPpos = new int[2];
                int[] threeKpos = new int[2];
                int[] fourKpos = new int[2];
                int[] fullHpos = new int[2];
                int[] straightFpos = new int[2];
                
                for (int row = 0; row < 5; row++) {
                    Card[] hand = new Card[SIZE];
                    //build row hand
                    for (int col = 0; col < 5; col++) {
                        //build hand
                        hand[col] = BMO.grid[row][col];
                        
                    }
                    
                    //check if col completes four kind or full house
                    for (int col = 0; col < SIZE; col++) {
                        if (hand[col] == null) {
                            if (hand[col] == null) {
                                boolean[] hasHand = HandRank.hasHands(hand, col, card);
                    
                                //check hand for full house
                                if (hasHand[2]){
                                    hasFullH = true;
                                    fullHpos[0] = row;
                                    fullHpos[1] = col;
                                }
                                //check hand for four of a kind
                                if (hasHand[3]){
                                    hasFourK = true;
                                    fourKpos[0] = row;
                                    fourKpos[1] = col;
                                }
                            }
                        }
                    }
                }
                for (int col = 0; col < 5; ++col) {

                    Card[] hand = new Card[SIZE];
                    //build row hand
                    for (int row = 0; row < 5; ++row) {
                        //build hand
                        hand[row] = BMO.grid[row][col];
                    }

                    //check if col completes four kind or full house
                    for (int row = 0; row < SIZE; ++row) {
                        if (hand[row] == null) {
                            boolean[] hasHand = HandRank.hasHands(hand, row, card);
                            //check hand for three K
                            if (hasHand[1]){
                                hasThreeK = true;
                                threeKpos[0] = row;
                                threeKpos[1] = col;
                            }
                            //check hand for full house
                            if (hasHand[2]){
                                hasFullH = true;
                                fullHpos[0] = row;
                                fullHpos[1] = col;
                            }
                            //check hand for four of a kind
                            if (hasHand[3]){
                                hasFourK = true;
                                fourKpos[0] = row;
                                fourKpos[1] = col;
                            }
                    
                        }
                    }
                }
                
                
                
            
            //CHOOSE Column
            int[] playPos = new int[2];
            int col = card.getSuit();  // try to play the card in the column of the suit number
            if (numColCards[col] == SIZE) { // if that's not possible
                
                if (numColCards[SIZE - 1] < SIZE)  // try to put it in the last column
                    col = SIZE - 1;
		else { // or the first column with a free spot
                    col = 0;
                    while (numColCards[col] == SIZE) col++;
                }
            }
            
            //CHOOSE Row
            for (int row = 0; row < SIZE; row++) if (BMO.grid[row][col] == null) playPos[0] = row;
            
            LinkedList <Integer> availableRows = new LinkedList <Integer> ();
            for (int row = 0; row < SIZE; row++) if (BMO.grid[row][col] == null) availableRows.add(row);
                
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
                
            //if a hand was completed choose that pos
            if (hasFourK) {row = fourKpos[0]; col = fourKpos[1]; }
            if (hasFullH) {row = fullHpos[0]; col = fullHpos[1]; }
            if (hasThreeK) {row = threeKpos[0]; col = threeKpos[1]; }
                
            //RECORD Move
            numRowCards[row]++;
            numColCards[col]++;
            playPos[1] = col;
            playPos[0] = row;
            
            int pos = playPos[0]*5+playPos[1];
            availablePos.remove((Integer) pos);
            
            //RECORD pos added to grid
            removePos.add((Integer) pos);
            BMO.grid[playPos[0]][playPos[1]] = card;
        }
        
        //RECORD EVALUATION
        simulationSum += PokerSquares.getScore(BMO.grid);
        numSimulations++;
        
        //PokerSquares.printGrid(BMO.grid);
        //System.out.println(simulationSum);
        
        //Unplay cards
        for (Integer pos : removePos) {
            //DECODE pos
            int row = pos/5;
            int col = pos%5;
            BMO.grid[row][col] = null;
        }
        
    }
    
    public void simulateRBGame(long seed) {
        //simulate rule based game
        
        Random r = new Random(seed);
        
        //INITIATE available positions
        LinkedList <Integer> removePos = new LinkedList <Integer> ();
        LinkedList <Integer> availablePos = new LinkedList <Integer> (BMO.playPos);
        availablePos.removeAll(playedPositions);
        
        //INITIATE available cards
        LinkedList <Card> availableCards = new LinkedList <Card> (BMO.deck);
        availableCards.removeAll(playedCards);
        
        //BUILD GRID up from recorded positions
        for (int i = this.playedPositions.size()-1; i >= 0; i--) {
            int row = this.playedPositions.get(i)/5;
            int col = this.playedPositions.get(i)%5;
            BMO.grid[row][col] = this.playedCards.get(i);
            removePos.add((Integer) this.playedPositions.get(i));
        }
        
        //Count Cards per Column
        int[] numColCards = {0,0,0,0,0};
        for (int col = 0; col < SIZE; col++)
            for (int row = 0; row < SIZE; row++) 
		if (BMO.grid[row][col] != null) 
                    numColCards[col]++;
        
        //Count Cards per Row and Card Ranks
        HashMap <Integer, Integer> rowRanks = new HashMap <Integer,Integer> (); // <Rank,Row>
        int[] numRowCards = {0,0,0,0,0};
        for (int row = 0; row < SIZE; row++)
            for (int col = 0; col < SIZE; col++) 
		if (BMO.grid[row][col] != null) {
                    rowRanks.put(BMO.grid[row][col].getRank(), row);
                    numRowCards[row]++;
                }
        
        while (availablePos.size() > 0) {
            
            //CHOOSE CARD
            Card card = availableCards.remove(r.nextInt(availableCards.size()));
            
            //If BMO can complete a four of a kind or a full house
                //check each row 
                boolean hasFourK = false;
                boolean hasFullH = false;
                int[] fourKpos = {0,0};
                int[] fullHpos = {0,0};
                for (int row = 0; row < 5; row++) {
            
                    Card[] hand = new Card[SIZE];
                    //build row hand
                    for (int col = 0; col < 5; col++) {
                        //build hand
                        hand[col] = BMO.grid[row][col];
                        
                    }
                    
                    //check if col completes four kind or full house
                    for (int col = 0; col < SIZE; col++) {
                        if (hand[col] == null) {
                            //check hand for four of a kind
                            if (HandRank.hasFourofaKind(hand,col,card)){
                                hasFourK = true;
                                fourKpos[0] = row;
                                fourKpos[1] = col;
                            }
                            //check hand for full house
                            if (HandRank.hasFullHouse(hand,col,card)){
                                hasFullH = true;
                                fullHpos[0] = row;
                                fullHpos[1] = col;
                            }
                        }
                    }
                }
                
                
            
            //CHOOSE Column
            int[] playPos = new int[2];
            int col = card.getSuit();  // try to play the card in the column of the suit number
            if (numColCards[col] == SIZE) { // if that's not possible
                
                if (numColCards[SIZE - 1] < SIZE)  // try to put it in the last column
                    col = SIZE - 1;
		else { // or the first column with a free spot
                    col = 0;
                    while (numColCards[col] == SIZE) col++;
                }
            }
            
            //CHOOSE Row
            for (int row = 0; row < SIZE; row++) if (BMO.grid[row][col] == null) playPos[0] = row;
            
            LinkedList <Integer> availableRows = new LinkedList <Integer> ();
            for (int row = 0; row < SIZE; row++) if (BMO.grid[row][col] == null) availableRows.add(row);
                
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
                
            //if full house of four kind pos are found choose that position
            if (hasFourK) {row = fourKpos[0]; col = fourKpos[1]; }
            if (hasFullH) {row = fullHpos[0]; col = fullHpos[1]; }
                
            //RECORD Move
            numRowCards[row]++;
            numColCards[col]++;
            playPos[1] = col;
            playPos[0] = row;
            
            int pos = playPos[0]*5+playPos[1];
            availablePos.remove((Integer) pos);
            
            //RECORD pos added to grid
            removePos.add((Integer) pos);
            BMO.grid[playPos[0]][playPos[1]] = card;
        }
        
        //RECORD EVALUATION
        simulationSum += PokerSquares.getScore(BMO.grid);
        numSimulations++;
        
        //PokerSquares.printGrid(BMO.grid);
        //System.out.println(simulationSum);
        
        //Unplay cards
        for (Integer pos : removePos) {
            //DECODE pos
            int row = pos/5;
            int col = pos%5;
            BMO.grid[row][col] = null;
        }
        
    }
    
    public void simulateFlushGame(long seed) {
        //simulate flush biased game
        Random r = new Random(seed);
        
        //INITIATE available positions
        LinkedList <Integer> removePos = new LinkedList <Integer> ();
        LinkedList <Integer> availablePos = new LinkedList <Integer> (BMO.playPos);
        availablePos.removeAll(playedPositions);
        
        //INITIATE available cards
        LinkedList <Card> availableCards = new LinkedList <Card> (BMO.deck);
        availableCards.removeAll(playedCards);
        
        //BUILD GRID up from recorded positions
        for (int i = this.playedPositions.size()-1; i >= 0; i--) {
            int row = this.playedPositions.get(i)/5;
            int col = this.playedPositions.get(i)%5;
            BMO.grid[row][col] = this.playedCards.get(i);
            removePos.add((Integer) this.playedPositions.get(i));
        }
        
        //Count Cards per Column
        int[] numColCards = {0,0,0,0,0};
        for (int col = 0; col < SIZE; col++)
            for (int row = 0; row < SIZE; row++) 
		if (BMO.grid[row][col] != null) 
                    numColCards[col]++;
        
        while (availablePos.size() > 0) {
            
            //CHOOSE CARD
            Card card = availableCards.remove(r.nextInt(availableCards.size()));
            
            //CHOOSE POSITION
            int[] playPos = new int[2];
            int col = card.getSuit();  // try to play the card in the column of the suit number
            if (numColCards[col] == SIZE) { // if that's not possible
                
                if (numColCards[SIZE - 1] < SIZE)  // try to put it in the last column
                    col = SIZE - 1;
		else { // or the first column with a free spot
                    col = 0;
                    while (numColCards[col] == SIZE) col++;
                }
            }
            
            //Record Move
            numColCards[col]++;
            for (int row = 0; row < SIZE; row++) if (BMO.grid[row][col] == null) playPos[0] = row;
            playPos[1] = col;
            
            int pos = playPos[0]*5+playPos[1];
            availablePos.remove((Integer) pos);
            
            //Record pos added to grid
            removePos.add((Integer) pos);
            BMO.grid[playPos[0]][playPos[1]] = card;
        }
        
        //RECORD EVALUATION
        simulationSum += PokerSquares.getScore(BMO.grid);
        numSimulations++;
        
        //PokerSquares.printGrid(BMO.grid);
        //System.out.println(simulationSum);
        
        //Unplay cards
        for (Integer pos : removePos) {
            //DECODE pos
            int row = pos/5;
            int col = pos%5;
            BMO.grid[row][col] = null;
        }
        
    }
    
    public void simulateGBFGame(long seed) {
        //simulate greedy best first game
        Random r = new Random(seed);
        
        //INITIATE available positions
        LinkedList <Integer> removePos = new LinkedList <Integer> ();
        LinkedList <Integer> availablePos = new LinkedList <Integer> (BMO.playPos);
        for (Integer pos : playedPositions) 
            if (availablePos.contains(pos))
                    availablePos.remove(pos);
        
        //INITIATE available cards
        LinkedList <Card> availableCards = new LinkedList <Card> (BMO.deck);
        for (Card card : playedCards) 
            if (availableCards.contains(card))
                    availableCards.remove(card);
        
        //choose moves starting with this play
        Play move = this;
        
        //FOR ALL POS
        while (availablePos.size() > 0) {
            //GENERATE ALL MOVES
            LinkedList <Play> nextMoves = new LinkedList <Play> ();
            
            for (Integer pos : move.genPlays()) {
                //DECODE pos
                int row = pos/5;
                int col = pos%5;
                
                //CHOOSE CARD
                Card card = availableCards.get(r.nextInt(availableCards.size()));
                
                //MAKE NEW PLAY
                Play newMove = new Play(move);
                newMove.recordPlayPos(card, pos);
                newMove.evaluatePlay();
                
                //RECORD PLAY
                nextMoves.add(newMove);
            }
            
            Collections.sort(nextMoves);
            
            //CHOOSE best move
            move = nextMoves.removeFirst();
            
            //RECORD MOVE
            availableCards.remove(move.playedCards.getFirst());
            availablePos.remove(move.playedPositions.getFirst());
            removePos.add(move.playedPositions.getFirst());
        }
        
        //System.out.println(move.playedPositions.size());
        
        //RECORD EVALUATION
        simulationSum += PokerSquares.getScore(BMO.grid);
        this.numSimulations++;
    }
    
    public void simulateRandomGame(long seed) {
        //simulate random game
        Random r = new Random(seed);
        
        //INITIATE available positions
        LinkedList <Integer> removePos = new LinkedList <Integer> ();
        LinkedList <Integer> availablePos = new LinkedList <Integer> (BMO.playPos);
        for (Integer pos : playedPositions) 
            if (availablePos.contains(pos))
                    availablePos.remove(pos);
        
        //INITIATE available cards
        LinkedList <Card> availableCards = new LinkedList <Card> (BMO.deck);
        for (Card card : playedCards) 
            if (availableCards.contains(card))
                    availableCards.remove(card);
        
        //BUILD GRID up from recorded positions
        for (int i = this.playedPositions.size()-1; i >= 0; i--) {
            int row = this.playedPositions.get(i)/5;
            int col = this.playedPositions.get(i)%5;
            BMO.grid[row][col] = this.playedCards.get(i);
            removePos.add((Integer) this.playedPositions.get(i));
        }
        
        while (availablePos.size() > 0) {
            //Play a random card at a Random position
            int pos = availablePos.remove(r.nextInt(availablePos.size()));
            
            //Record pos chosen
            removePos.add(pos);
            
            //DECODE pos
            int row = pos/5;
            int col = pos%5;
            
            BMO.grid[row][col] = availableCards.remove(r.nextInt(availableCards.size()));
        }
        
        //RECORD EVALUATION
        simulationSum += PokerSquares.getScore(BMO.grid);
        numSimulations++;
        
        //Unplay cards
        for (Integer pos : removePos) {
            //DECODE pos
            int row = pos/5;
            int col = pos%5;
            BMO.grid[row][col] = null;
        }
    }
    
    protected LinkedList setPlayedPositions(Play parent) {
        if (parent != null) this.playedPositions = new LinkedList <Integer> (parent.getPlayedPositions());
        else this.playedPositions = new LinkedList <Integer> ();
        return this.playedPositions;
    }
    
    protected LinkedList setPlayedCards(Play parent) {
        if (parent != null) this.playedCards = new LinkedList <Card> (parent.getPlayedCards());
        else this.playedCards = new LinkedList <Card> ();
        return this.playedCards;
    }
    
    
    public int[] getPos() {
        int[] pos = {0,0};
        pos[0] = this.playedPositions.get(0)/5;
        pos[1] = playedPositions.get(0)%5;
        return pos;
    }
    
    public double updateGeneticEvaluation() {
        //updates the evaluation to be the average of the static evaluation and the simAve
        evaluation = (evaluation + staticEval) /2;
        return evaluation;
    }
    
    public double updateSimAve() {
        this.simEval = this.evaluation = this.simulationSum/this.numSimulations; 
        
        return this.evaluation;
    }
    
    public double updateStaticEval() {
        //use static eval as primary evaluation
        evaluation = staticEval;
        return evaluation;
    }
    
    public List <Integer> genPlays() {
        //generates a list of new plays from this play
        List <Integer> plays = new ArrayList <Integer> ();
        
        for (int pos = 0; pos < (SIZE*SIZE); pos++) {
            int row = pos/5;
            int col = pos%5;
            
            if ((BMO.grid[row][col] == null) && (!this.playedPositions.contains(pos))) plays.add(pos);
        }
        
        return plays;
    }
    
    public double getSimEval() { return this.simEval; } 
    public double getStaticEval() { return this.staticEval; } 
    public LinkedList getPlayedPositions() { return this.playedPositions; }
    public LinkedList getPlayedCards() {  return this.playedCards; }
    public Play getParent() { return this.parent; }
    public Map getBestCardEvals() { return this.bestCardEvals; }
    public int getLVL() { return this.playedPositions.size(); }
    public double getEvaluation() { return this.evaluation; }
    public double[] getHandEvaluations() { return this.handEvaluations; }
    public int getNumSimulations() { return this.numSimulations; }
    public double getSimulationSum() { return this.simulationSum; }
    
    @Override
    public int compareTo(Play aThat) {
        //compare To over ride for state sorting in java Collections
        
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;
        
        if (this.evaluation == aThat.evaluation) {
            if (this.simEval < aThat.simEval) return AFTER;
        
            if (this.simEval > aThat.simEval) return BEFORE;
             
            return EQUAL;
        }
        
        if (this.evaluation < aThat.evaluation) return AFTER;
        
        if (this.evaluation > aThat.evaluation) return BEFORE;
        
        return EQUAL;
    }
}
