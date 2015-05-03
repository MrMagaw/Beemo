/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pokersquares.algorithms;

import java.util.Collections;
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
            
            //PLAY Game
            while(numSimulations-- > offset){
                //SHUFFLE deck
                Stack<Card> deck = new Stack<Card>();
                for (Card card : Card.getAllCards())
                        deck.push(card);
                Collections.shuffle(deck, r);
                
                Board b = new Board(board);
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
