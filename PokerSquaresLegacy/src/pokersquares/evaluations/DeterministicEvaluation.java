/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pokersquares.evaluations;
import pokersquares.Card;


public class DeterministicEvaluation {
    
    private Card[][] grid;
    private Card[] cardSet;
    
    //Input -> Grid, Card set
    public DeterministicEvaluation(Card[][] grid, Card[] cardSet) {
        //for the grid and card set, return the best score possible
        this.grid = grid;
        this.cardSet = cardSet;
    }
    
    public double getScore() {
        //returns best score possible with cards known
        return 0.0;
    }
}
