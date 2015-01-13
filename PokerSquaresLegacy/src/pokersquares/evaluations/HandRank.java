package pokersquares.evaluations;

import pokersquares.Card;
import pokersquares.Card;

public class HandRank {
    public static boolean[] hasHands(Card[] hand, int place, Card ncard) {
        
        boolean pre2P;
        boolean pre3K;
        boolean pre4K;
        boolean prefH;
        
        boolean post2P;
        boolean post3K;
        boolean post4K;
        boolean postFH;
        
        boolean[] handBools = {false, false, false, false};
        
        //PRE
        // Compute counts
	int[] rankCounts = new int[Card.NUM_RANKS];
        int maxOfAKind = 0;
	int[] rankCountCounts = new int[6];
        
        //Count no of placed cards, no of cards of each rank, no of cards of each suit
	for (Card card : hand) {
            if (card != null) {
		++rankCounts[card.getRank()];
            }
        }
        
        //Count no of cards with same rank
	for (int count : rankCounts) {
            rankCountCounts[count]++;
            if (count > maxOfAKind) {
                maxOfAKind = count;
            }
        }
        
        //record if there is a 4K before card is placed
        pre2P = (rankCountCounts[2] == 2);
        pre3K = (rankCountCounts[3] == 1);
        pre4K = (rankCountCounts[4] == 1);
        
        //place card
        hand[place] = ncard;
        
        //POST
        rankCounts = new int[Card.NUM_RANKS];
        maxOfAKind = 0;
	rankCountCounts = new int[6];
        
        //Count no of placed cards, no of cards of each rank, no of cards of each suit
	for (Card card : hand) {
            if (card != null) {
		++rankCounts[card.getRank()];
            }
        }
        
        //Count no of cards with same rank
	for (int count : rankCounts) {
            ++rankCountCounts[count];
            if (count > maxOfAKind) {
                maxOfAKind = count;
            }
        }
        
        //record if there is a 3K after card is placed
        post2P = (rankCountCounts[2] == 2);
        post3K = (rankCountCounts[3] == 1);
        post4K = (rankCountCounts[4] == 1);
        postFH = (rankCountCounts[3] == 1) && (rankCountCounts[2] == 1);
        
        hand[place] = null;
        
        handBools[0] = !pre2P && post2P; //two pair
        handBools[1] = !pre3K && post3K; //three kind
        handBools[2] = postFH; //full house
        handBools[3] = !pre4K && post4K; //four kind
        
        return handBools;
    }
    
    //Moved some of RB's functionality into this class
    public static boolean hasFullHouse(Card[] hand, int place, Card ncard) {
        //true if hand plus card at column has a four of a kind
        //else false
        boolean pre;
        boolean post;
        
        // Compute counts
	int[] rankCounts = new int[Card.NUM_RANKS];
        int maxOfAKind = 0;
	int[] rankCountCounts = new int[6];
        
        //place card
        hand[place] = ncard;
        
        //Count no of placed cards, no of cards of each rank, no of cards of each suit
	for (Card card : hand) {
            if (card != null) {
		++rankCounts[card.getRank()];
            }
        }
        
        //Count no of cards with same rank
	for (int count : rankCounts) {
            ++rankCountCounts[count];
            if (count > maxOfAKind) {
                maxOfAKind = count;
            }
        }
        
        post = (rankCountCounts[3] == 1) && (rankCountCounts[2] == 1);
        
        hand[place] = null;
        
        return post;
    }
    
    public static boolean hasThreeofaKind(Card[] hand, int place, Card ncard) {
        //true if hand plus card at column has a four of a kind
        //else false
        boolean pre;
        boolean post;
        
        
        //PRE
        // Compute counts
	int[] rankCounts = new int[Card.NUM_RANKS];
        int maxOfAKind = 0;
	int[] rankCountCounts = new int[6];
        
        //Count no of placed cards, no of cards of each rank, no of cards of each suit
	for (Card card : hand) {
            if (card != null) {
		++rankCounts[card.getRank()];
            }
        }
        
        //Count no of cards with same rank
	for (int count : rankCounts) {
            rankCountCounts[count]++;
            if (count > maxOfAKind) {
                maxOfAKind = count;
            }
        }
        
        //record if there is a 4K before card is placed
        pre = (rankCountCounts[3] == 1);
        
        //place card
        hand[place] = ncard;
        
        //POST
        rankCounts = new int[Card.NUM_RANKS];
        maxOfAKind = 0;
	rankCountCounts = new int[6];
        
        //Count no of placed cards, no of cards of each rank, no of cards of each suit
	for (Card card : hand) {
            if (card != null) {
		++rankCounts[card.getRank()];
            }
        }
        
        //Count no of cards with same rank
	for (int count : rankCounts) {
            ++rankCountCounts[count];
            if (count > maxOfAKind) {
                maxOfAKind = count;
            }
        }
        
        //record if there is a 3K after card is placed
        post = (rankCountCounts[3] == 1);
        
        hand[place] = null;
        
        return !pre && post;
    }
    
    public static boolean hasFourofaKind(Card[] hand, int place, Card ncard) {
        //true if hand plus card at column has a four of a kind
        //else false
        boolean pre;
        boolean post;
        
        
        //PRE
        // Compute counts
	int[] rankCounts = new int[Card.NUM_RANKS];
        int maxOfAKind = 0;
	int[] rankCountCounts = new int[6];
        
        //Count no of placed cards, no of cards of each rank, no of cards of each suit
	for (Card card : hand) {
            if (card != null) {
		++rankCounts[card.getRank()];
            }
        }
        
        //Count no of cards with same rank
	for (int count : rankCounts) {
            rankCountCounts[count]++;
            if (count > maxOfAKind) {
                maxOfAKind = count;
            }
        }
        
        //record if there is a 4K before card is placed
        pre = (rankCountCounts[4] == 1);
        
        //place card
        hand[place] = ncard;
        
        //POST
        rankCounts = new int[Card.NUM_RANKS];
        maxOfAKind = 0;
	rankCountCounts = new int[6];
        
        //Count no of placed cards, no of cards of each rank, no of cards of each suit
	for (Card card : hand) {
            if (card != null) {
		++rankCounts[card.getRank()];
            }
        }
        
        //Count no of cards with same rank
	for (int count : rankCounts) {
            ++rankCountCounts[count];
            if (count > maxOfAKind) {
                maxOfAKind = count;
            }
        }
        
        //record if there is a 4K after card is placed
        post = (rankCountCounts[4] == 1);
        
        hand[place] = null;
        
        return !pre && post;
    }
    
    public static boolean hasStraightFlush(Card[] hand, int place, Card ncard) {
        //true if hand plus card at column has a four of a kind
        //else false
        boolean pre;
        boolean post;
        
        // Compute counts
	int[] rankCounts = new int[Card.NUM_RANKS];
        int[] suitCounts = new int[Card.NUM_SUITS];
        int maxOfAKind = 0;
	int[] rankCountCounts = new int[6];
        int numCards= 0;
        
        //place card
        hand[place] = ncard;
        
        //Count no of placed cards, no of cards of each rank, no of cards of each suit
	for (Card card : hand) {
            if (card != null) {
		++rankCounts[card.getRank()];
                ++suitCounts[card.getSuit()];
                ++numCards;
            }
        }
        
        // Flush check
	boolean hasFlush = false;
        int numSuits = 0;
	for (int i = 0; i < Card.NUM_SUITS; i++) {
            if (suitCounts[i] != 0) {
		++numSuits;
            }
	}
        
        if ((numSuits == 1) && (numCards == 5)) hasFlush = true;
        
        //Count no of cards with same rank
	for (int count : rankCounts) {
            ++rankCountCounts[count];
            if (count > maxOfAKind) {
                maxOfAKind = count;
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
        
        
        hand[place] = null;
        
        return (hasStraight || hasRoyal);
    }
}
