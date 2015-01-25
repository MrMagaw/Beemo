package pokersquares.evaluations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import pokersquares.environment.Board;

public class PositionRank {
    //Stores the relative rankings of positions and their patterns
    public static ArrayList <String> positionRanks = new ArrayList <String> ();
    public static HashMap <String, Integer> patternRanks = new HashMap <String, Integer> ();
    
    public static boolean contains(Board board) {
        //Checks if pattern Ranks contains all the positions 
        for (String posPattern : board.posPatterns) 
            if (!patternRanks.containsKey(posPattern)) return false;
        
        return true;
    }
    public static Integer[] getBestPos(Board board) {
        String topPattern = null;
        int topRank = Integer.MIN_VALUE;
        
        for (String pattern : board.posPatterns) {
            int rank = patternRanks.get(pattern);
            if (patternRanks.get(pattern) > topRank) {
                topPattern = pattern;
                topRank = rank;
            }
        }
        
        return board.getOpenPos().get(board.posPatterns.indexOf(topPattern)); 
    }
    
    public static void update(Board board, Integer[] bestPos) {
        //CHANGE *********** 
        //to ensure that all new pattern maintain rank
        //ALSO OPTIMIZE
        
        //ADD all patterns to pattern ranks
        int rank;
        String betaPattern = null;
        int betaRank = Integer.MIN_VALUE;
        for (String pattern : board.posPatterns) {
            if (!patternRanks.containsKey(pattern)) patternRanks.put(pattern, patternRanks.size()+1);
            
            rank = patternRanks.get(pattern);
            if (patternRanks.get(pattern) > betaRank) {
                betaPattern = pattern;
                betaRank = rank;
            }
        }
        
        //System.out.println(patternRanks.size());
        
        //RANK bestPos in the top position of all board pos
        Integer[] topRankedPos = board.getOpenPos().get(board.posPatterns.indexOf(betaPattern));
        //if bestPos is not the highest ranked pos
        if (!( (topRankedPos[0] == bestPos[0]) && (topRankedPos[1] == bestPos[1]) )) {
            String alphaPattern = board.getPosPattern(bestPos);
            int alphaRank = patternRanks.get(alphaPattern);
            
            patternRanks.remove(alphaPattern);
            patternRanks.remove(betaPattern);
            
            patternRanks.put(betaPattern, alphaRank);
            patternRanks.put(alphaPattern, betaRank);
        }
    }
    
    public static void debug() {
        System.out.println(patternRanks.toString());
    }
}
