/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pokersquares.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import pokersquares.config.Settings;
import pokersquares.environment.Board;
import pokersquares.environment.Card;

/**
 *
 * @author karo
 */
public class Gamer extends Thread {
        public final Board board;
        private final int offset;
        private int numSimulations;
        public double totalScore = 0;
        public int simsRun = 0;
        private boolean running = true;
        
        public Gamer(Board board, int numSimulations, int offset){
            this.board = board;
            this.numSimulations = numSimulations + offset;
            this.offset = offset;
        }
        
        @Override
        public void run() {
            //Random r = new Random(Settings.Main.seed + offset);
            Random r = new Random();
            
            Stack<Card> masterDeck = new Stack<Card>();
            List<Card> playedCards = new ArrayList<Card>();
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    Card c = board.getCard(i,j);
                    if (c != null){
                        playedCards.add(c);
                    }
                }
            }
            
            for (Card c : Card.getAllCards()) 
                if (!playedCards.contains(c)) masterDeck.push(c);
            
            Collections.shuffle(masterDeck, r);
            
            //PLAY Game
            while(numSimulations-- > offset){
                //SHUFFLE deck
                Stack<Card> deck = new Stack<Card>();
                for (Card card : masterDeck)
                        deck.push(card);
                Collections.shuffle(deck, r);
                
                
                Board b = new Board(board);
                 //System.out.println(b.)
                while (b.getTurn() < 25) {
                    Card c = deck.pop();
                    b.removeCard(c);
                    int[] p = Settings.Algorithms.simAlgorithm.search(c, b, 100);
                    b.playCard(c, p);
                }
                simsRun++;
                totalScore += Settings.Environment.system.getScore(b.getGrid());
                
                if (!running) return;
            } 
       }
        
       public void halt() { this.running = false; }
    }
