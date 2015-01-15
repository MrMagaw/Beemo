package pokersquares;

import pokersquares.environment.PokerSquares;
import static pokersquares.environment.PokerSquares.GAME_MILLIS;
import pokersquares.players.BeemoV2;

public class Main {
    public static void main(String[] args){
        int games = 10;
        int seed = 0;
        if(args.length > 0){
            for(String arg : args){
                if(arg.startsWith("-s=")){
                    seed = Integer.parseInt(arg.substring(3));
                }else if(arg.startsWith("-g=")){
                    games = Integer.parseInt(arg.substring(3));
                }
            }
        }
        System.out.println("Games: " + games + ", Start Seed: " + seed);
        new PokerSquares(new BeemoV2(), GAME_MILLIS).playSequence(games, seed, false);
    }
}
