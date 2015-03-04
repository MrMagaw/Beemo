package pokersquares.mcts;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import pokersquares.*;
import static pokersquares.algorithms.MCTS.nodesGenerated;
import pokersquares.algorithms.Simulator;
import pokersquares.environment.Board;
import pokersquares.environment.Card;

public class TreeNode {
    static Random r = new Random();
    static double epsilon = 1e-12;
    Board b;
    public int[] pos;

    public TreeNode[] children;
    public double nVisits = epsilon, totValue = 0;
    
    public TreeNode (Board b) {
        //INITIALIZE NODE
        //each node has it's own play
        this.b = b;
    }

    public void selectAction() {
        //visited nodes in the current run
        List<TreeNode> visited = new LinkedList<TreeNode>();
        
        TreeNode cur = this;
        visited.add(this);
        
        //Select Leaf
        while (!cur.isLeaf()) {
            cur = cur.select();
            visited.add(cur);
        }
        
        //expand leaf's children
        cur.expand();
        
        TreeNode newNode = cur.select();
        visited.add(newNode);
        double value = rollOut(newNode);
        for (TreeNode node : visited) {
            // would need extra logic for n-player game
            // System.out.println(node);
            node.updateStats(value);
        }
    }

    public void expand() {
        //Propagate children
        //A Node's children is the boards resulting from each card in the deck played in every position
        List <TreeNode> childs = new ArrayList <TreeNode> ();
        
        for (int i = 0; i < b.cardsLeft(); ++i) {
            for (int j = 0; j < b.getOpenPos().size(); ++j) {
                Board nb = new Board(b);
                Integer[] p = nb.getOpenPos().get(j);
                int[] arrpos =  {p[0], p[1]};
                nb.playCard(nb.removeCard(i), arrpos);
                TreeNode tn = new TreeNode(nb);
                tn.pos = arrpos;
                childs.add(tn);
                nodesGenerated++;
            }
        }
        
        children = new TreeNode[childs.size()];
        children = (TreeNode[]) childs.toArray(children);
    }
    
    private TreeNode select() {
        //Select the child with the least amount of visits
        TreeNode selected = null;
        
        double minVisits = Double.POSITIVE_INFINITY;
        for (int i = 0; i < children.length; ++i) {
            
            if (children[i].nVisits < minVisits) {
                minVisits = children[i].nVisits;
                selected = children[i];
            }
        }
        
        return selected;
    }

    public boolean isLeaf() {
        return (children == null);
    }

    public double rollOut(TreeNode tn) {
        // ultimately a roll out will end in some value
        // assume for now that it ends in a win or a loss
        // and just return this at random
        double value = Simulator.simulate(b, 1, 1000, 1);
        
        return value;
    }
    
    public void updateStats(double value) {
        nVisits += 1;
        totValue += value;
    }

    public int arity() {
        return children == null ? 0 : children.length;
    }
}