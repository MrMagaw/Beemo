package pokersquares.montecarlo;

import pokersquares.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class TreeNode {
    static Random r = new Random();
    static int nActions = 5;
    static double epsilon = 1e-12;
    Play play;

    TreeNode[] children;
    double nVisits, totValue;
    
    public TreeNode (Play play) {
        //INITIALIZE NODE
        //each node has it's own play
        this.play = play;
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
        
        children = new TreeNode[nActions];
        for (int i=0; i<nActions; i++) {
            children[i] = new TreeNode(play);
        }
    }
    
    private TreeNode select() {
        //Select best child
        TreeNode selected = null;
        double bestValue = Double.MIN_VALUE;
        for (TreeNode c : children) {
            double uctValue =
                    c.totValue / (c.nVisits + epsilon) +
                            Math.sqrt(Math.log(nVisits+1) / (c.nVisits + epsilon)) +
                            r.nextDouble() * epsilon;
            
            // small random number to break ties randomly in unexpanded nodes
            // System.out.println("UCT value = " + uctValue);
            if (uctValue > bestValue) {
                selected = c;
                bestValue = uctValue;
            }
        }
        // System.out.println("Returning: " + selected);
        return selected;
    }

    public boolean isLeaf() {
        return (children == null);
    }

    public double rollOut(TreeNode tn) {
        // ultimately a roll out will end in some value
        // assume for now that it ends in a win or a loss
        // and just return this at random
        return r.nextInt(2);
    }
    
    public void updateStats(double value) {
        nVisits++;
        totValue += value;
    }

    public int arity() {
        return children == null ? 0 : children.length;
    }
}