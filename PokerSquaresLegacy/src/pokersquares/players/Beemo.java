/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


package pokersquares.players;

import java.util.*;
import pokersquares.Card;
import pokersquares.Play;
import pokersquares.PokerSquares;
import static pokersquares.PokerSquares.SIZE;
import pokersquares.algorithms.*;
import pokersquares.montecarlo.*;
import pokersquares.players.PokerSquaresPlayer;

/**
 *
 * @author newuser
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
public class Beemo  implements PokerSquaresPlayer {
    //1 minute per game
    //25 moves, ~2 seconds per move
    
    //Stats
    int dSum = 0;
    int games = 0;
    
    //Recursive Depth
    int rdepth;
    
    //MONTECARLO
    int monteCarloSampleSize;
    
    //Speed
    //smaller is faster
    double timeCoefficient = 1.0;
    
    //sample of plays to run simulations on
    int playSampleSize;
    
    //OX 
    //Search Depth
    final int searchDepth = 2;
    
    //Monte Carlo Sample Size
    //Largest Monte Carlo SampleSize at searchDepth 3 = 11 
    final int deckSampleSize = 52;
    
    //Sample size of the best positions generated
    int rootPlaySampleSize = 25;
    
    //Sample size of the best positions generated
    int branchPlaySampleSize = 25;
    
    String algorithmID = null;
    
    public int turn = 0;
    public Card[][] grid; // current game grid
    public LinkedList <Card> deck; //current game deck
    public LinkedList <Integer> playPos; //available positions
    Map <String, Double> patternEvaluations; //mapped pattern evaluations
    LinkedList <Card> playedCards = new LinkedList <Card> ();
    
    //TIMERS
    ElapsedTimer t;
    static final long totalMillis = 60000;
    
    public Beemo () { 
        grid = new Card[SIZE][SIZE]; 
        deck = new LinkedList <Card> ();
        playPos = new LinkedList <Integer> (); 
        patternEvaluations = new HashMap <String, Double> ();
    }
    
    public Beemo (Beemo BeemoSr, String algorithmID) {
        //inherit all fields
        this.rdepth = BeemoSr.rdepth + 1;
        this.monteCarloSampleSize = BeemoSr.monteCarloSampleSize;
        this.rootPlaySampleSize = BeemoSr.rootPlaySampleSize;
        this.branchPlaySampleSize = BeemoSr.branchPlaySampleSize;
        this.turn = BeemoSr.getTurn();
        this.grid = BeemoSr.cloneGrid();
        this.deck = new LinkedList <Card> (BeemoSr.getDeck());
        this.playPos = new LinkedList <Integer> (BeemoSr.getPlayPos());
        this.algorithmID = algorithmID;
        this.patternEvaluations = BeemoSr.getPatternEvaluations();
    }
    
    public Beemo (int monteCarloSampleSize, int playSampleSize, String algorithmID) {
        this.rdepth = 0;
        this.monteCarloSampleSize = monteCarloSampleSize;
        this.playSampleSize = playSampleSize;
        this.algorithmID = algorithmID;
        this.grid = new Card[SIZE][SIZE]; 
        this.deck = new LinkedList <Card> ();
        this.playPos = new LinkedList <Integer> (); 
        patternEvaluations = new HashMap <String, Double> ();
    }
    
    @Override
    public void init() { 
        games++;
        resetGame();
        Collections.shuffle(deck); //shuffle deck for a better Sample Distribution... maybe true
    }

    @Override
    public int[] getPlay(Card card, long millisRemaining) {
        //START TIMER
        t = new ElapsedTimer();
        
        //Initialize Turn
        int icard = deck.indexOf(card);
        playedCards.add(deck.remove(icard)); //record all cards remaining in deck, remove cards as they are removed
        int[] bestPos = {0,0}; //best pos to be returned
        
        //OPTIMIZATION
        Algorithm rb = new RB(this);
        Algorithm grb = new GRB(this);
        Algorithm ox = new OX(this);
        //theory : the first 8 moves can be played optimally by a rule base
        int opTurns = 0;
        if (turn < opTurns) bestPos = grb.search(card, millisRemaining); //first moves
        //else if (turn > 22) bestPos = ox.search(card, millisRemaining); //last moves
        else if (algorithmID == "rb") bestPos = rb.search(card, millisRemaining);
        else if (algorithmID == "grb") bestPos = (new GRB(this)).search(card, millisRemaining);
        else if (algorithmID == "bf") bestPos = (new BF(this)).search(card, millisRemaining);
        else if (algorithmID == "ox") bestPos = (new OX(this)).search(card, millisRemaining);
        else if (algorithmID == "iimc") bestPos = (new IIMC(this)).search(card, millisRemaining);
        else if (algorithmID == "iiuct") bestPos = (new IIUCT(this)).search(card, millisRemaining);
        else if (algorithmID == "oxmc") bestPos = (new OXMC(this)).search(card, millisRemaining);
        
        //Evaluate Deterministc Play DOESNT DO ANYTHING USEFUL
        if ((playedCards.size() == 25) && (rdepth == 0) && false) {
            Play deterministicPlay =  new Play(this);
            deterministicPlay.simulateDeterministicGame(playedCards, "ox", totalMillis);
            System.out.println(deterministicPlay.getSimulationSum() + " de");
            dSum += deterministicPlay.getSimulationSum();
            if (games == 100) System.out.println("dScore: " + (double) dSum/games);
        }
        
        //Game Trackers
        ++turn;
        recordPlay(card, bestPos); 
        
	return bestPos; 
    }
    
    public LinkedList genPlayPos() {
        playPos.clear();
        
        for (int pos = 0; pos < (SIZE*SIZE); pos++) {
            playPos.add(pos);
        }
        
        return playPos;
    }
    
    public void recordPlay(Card card, int[] pos) {
        grid[pos[0]][pos[1]] = card;
        int loc = pos[0] * SIZE +pos[1];
        playPos.remove((Integer) loc);
    }
    
    public void resetGame() {
        //sets all Game trackers to intial state
        deck.clear();
        deck.addAll(Arrays.asList(Card.allCards));
        grid = new Card[SIZE][SIZE];
        genPlayPos(); //record all playable positions on the current grid (all positions marked null)
        turn = 0;
    }
    
    public Card[][] cloneGrid(){
        Card[][] gridClone = new Card[SIZE][SIZE];
        
        for (int row  = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                gridClone[row][col] = grid[row][col];
            }
        }
        
        return gridClone;
    }
    
    public void mapPattern(String pattern, Double evaluation) {
        patternEvaluations.put( pattern, evaluation);
        
    }
    
    //Value obtaining.
    public boolean containsPattern(String pattern) { return patternEvaluations.containsKey(pattern); }
    public double getPatternEvaluation(String pattern) { return patternEvaluations.get(pattern); }
    public Map getPatternEvaluations() { return patternEvaluations; }
    public double getTimeCoefficient() { return timeCoefficient; }
    public long getTotalMillis() { return totalMillis; }
    public int getPlaySampleSize() { return playSampleSize; }
    public int getTurn(){ return turn; }
    public int getSearchDepth(){ return searchDepth; }
    public int getDeckSampleSize(){ return deckSampleSize; }
    public int getRootPlaySampleSize(){ return rootPlaySampleSize; }
    public int getBranchPlaySampleSize(){ return branchPlaySampleSize; }
    public LinkedList<Integer> getPlayPos(){ return playPos; }
    public LinkedList<Card> getDeck(){ return deck; }
    public int getMonteCarloSampleSize(){ return monteCarloSampleSize; }
    public ElapsedTimer getTimer(){ return t; }
    public Card[][] getGrid(){ return grid; }
}
