package pokersquares.algorithms;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import pokersquares.players.Beemo;
import pokersquares.Card;
import pokersquares.evaluations.HandRank;
import static pokersquares.PokerSquares.*;

public class RB extends Algorithm{
    private final int[] numColCards;
    private final int[] numRowCards;
    private final HashMap<Integer, Integer> rowRanks;
    
    public RB(Beemo BMO) {
        super(BMO);
        this.numColCards = new int[SIZE];
        this.numRowCards = new int[SIZE];
        this.rowRanks = new HashMap();
        
        //Count Cards per Column
        //int[] numColCards = {0,0,0,0,0};
        for (int col = 0; col < SIZE; ++col)
            for (int row = 0; row < SIZE; ++row) 
		if (BMO.getGrid()[row][col] != null) 
                    ++numColCards[col];
        
        //Count Cards per Row and Card Ranks
        for (int row = 0; row < SIZE; ++row)
            for (int col = 0; col < SIZE; ++col) 
		if (BMO.getGrid()[row][col] != null) {
                    rowRanks.put(BMO.getGrid()[row][col].getRank(), row);
                    ++numRowCards[row];
                }
    }
    
    @Override
    public int[] search(Card card, long millisRemaining) {
        //Rule Based card placement
        
        int[] playPos = new int[2];
        
        //OPTIMIZATION always play the first card in the center of its column
        if (BMO.getTurn() == 0) {
            playPos[0] = 2;
            playPos[1] = card.getSuit();
            return playPos;
        }
        
        //If BMO can complete a four of a kind or a full house
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

        for (int row = 0; row < 5; ++row) {
            Card[] hand = new Card[SIZE];
            //build row hand
            System.arraycopy(BMO.grid[row], 0, hand, 0, 5);

                //check if col completes four kind or full house
            for (int col = 0; col < SIZE; ++col) {
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
                
        //if straight fluch, full house of four kind pos are found, return values
        if (hasFourK) return fourKpos;
        if (hasFullH) return fullHpos;
        if (hasThreeK) return threeKpos;
        

        //Choose Column
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
        LinkedList <Integer> availableRows = new LinkedList();
        for (int row = 0; row < SIZE; ++row) 
            if (BMO.getGrid()[row][col] == null) 
                availableRows.add(row);

        boolean hasFirst, hasSecond;
        int firstBest, secondBest;
        hasFirst = hasSecond = false;
        firstBest = secondBest = availableRows.get(0);
        for (Integer row : availableRows) {
            //First Choice: row with most cards containing cards of same rank
            if ((rowRanks.containsKey(card.getRank())) 
                    && (Objects.equals(rowRanks.get(card.getRank()), row)) 
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
        

        ++numRowCards[row];
        ++numColCards[col];
        rowRanks.put(card.getRank(),row);
        playPos[1] = col;
        playPos[0] = row;
        BMO.getGrid()[playPos[0]][playPos[1]] = card;
        return playPos;
    }
}
