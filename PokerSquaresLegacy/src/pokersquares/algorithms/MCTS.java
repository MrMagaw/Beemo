package pokersquares.algorithms;

import java.util.ArrayList;
import java.util.List;
import pokersquares.config.Settings;
import pokersquares.environment.Board;
import pokersquares.environment.Card;
import pokersquares.mcts.TreeNode;
import pokersquares.mcts.TreeView;

/**
 *
 * @author karo
 */
public class MCTS extends Algorithm{
    //Monte Carlo Tree Search
    
    public static int nodesGenerated;
    
    @Override
    public int[] internalSearch(final Card card, final Board board, long millisRemaining) {
         nodesGenerated = 0;
        int[] bestPos = {2,2};
        int numSim = Settings.Algorithms.simSampleSize;
        
        TreeNode root = new TreeNode(board);
        
        while(--numSim > 0) {
            root.selectAction();
        }
        
        //choose best
        double bestScore = Double.NEGATIVE_INFINITY;
        for(int i = 0; i < root.children.length; i++) {
            double score = root.children[i].totValue/root.children[i].nVisits;
            if (score > bestScore) {
                bestScore = score;
                bestPos = root.children[i].pos;
            }
        }
        
        System.out.println(nodesGenerated);
        
        TreeView tv = new TreeView(root);
        //tv.showTree("MCTS");
        
        return bestPos;
    }
    
}
