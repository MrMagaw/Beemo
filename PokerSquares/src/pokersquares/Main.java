package pokersquares;

import java.util.ArrayList;
import pokersquares.config.Settings;
import pokersquares.environment.*;
import pokersquares.environment.PokerSquares;
import pokersquares.players.*;

public class Main {
    /*
    public static void main(String[] args){
        int games = Settings.Main.games;
        int seed = Settings.Main.seed;
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
        new PokerSquares(new BeemoV2(), GAME_MILLIS).playSequence(games, seed, Settings.Main.verbose);
    }
    */
    
    public static void main(String[] args) {
                /*
		// Demonstration of single game play (30 seconds)
		System.out.println("Single game demo:");
		PokerSquaresPointSystem.setSeed(0L);
		PokerSquaresPointSystem system = PokerSquaresPointSystem.getAmeritishPointSystem();
		System.out.println(system);
		new PokerSquares(new GreedyMCPlayer(2), PokerSquaresPointSystem.getAmeritishPointSystem()).play();
                */

		// Demonstration of batch game play (30 seconds per game)
                PokerSquaresPointSystem system = PokerSquaresPointSystem.getAmeritishPointSystem();
		System.out.println("\n\nSingle Player Game Sequence:");
		System.out.println(system);
		new PokerSquares(new BeemoV2(), PokerSquaresPointSystem.getAmericanPointSystem())
                        .playSequence(Settings.Main.games, Settings.Main.seed, Settings.Main.verbose);
		
                
                /*
		// Demonstration of tournament evaluation (3 players, 2 point systems, 100 x 30s games for each of the 3*2=6 player-system pairs) 
		System.out.println("\n\nTournament:");
		ArrayList<PokerSquaresPlayer> players = new ArrayList<PokerSquaresPlayer>();
                
                //Players
		players.add(new BeemoV2());
		players.add(new GreedyMCPlayer(2));
                
		ArrayList<PokerSquaresPointSystem> systems = new ArrayList<PokerSquaresPointSystem>();
		PokerSquaresPointSystem.setSeed(0L);
                
                //Point Systems
		//systems.add(PokerSquaresPointSystem.getAmeritishPointSystem());
		systems.add(PokerSquaresPointSystem.getRandomPointSystem());
                
		PokerSquares.playTournament(players, systems, 5, 0L); // use fewer games per system for faster testing
                */
	}
}
