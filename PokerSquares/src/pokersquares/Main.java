package pokersquares;

import java.util.ArrayList;
import pokersquares.config.Settings;
import static pokersquares.config.Settings.Main.customPointSystem;
import static pokersquares.config.Settings.Main.test;
import pokersquares.environment.*;
import pokersquares.environment.Board.Deck;
import pokersquares.environment.EarlyPPSTesting;
import pokersquares.environment.PokerSquares;
import pokersquares.players.*;

public class Main {
    
    public static void main(String[] args) {
        if (test) {
            EarlyPPSTesting.testPPS();
            return;
        }
        
        if (!Settings.Main.tournament) {
        // Demonstration of batch game play (30 seconds per game)
        PokerSquaresPointSystem.setSeed(Settings.Main.randomPointSystemSeed);
        
        //Point System
        PokerSquaresPointSystem system = PokerSquaresPointSystem.getRandomPointSystem();
        switch (Settings.Main.pointSystem) {
            case RANDOM:
                system = PokerSquaresPointSystem.getRandomPointSystem();
                break;
            
            case AMERICAN:
                system = PokerSquaresPointSystem.getAmericanPointSystem();
                break;
                
            case AMERITISH:
                system = (PokerSquaresPointSystem.getAmeritishPointSystem());
                break;
            
            case BRITISH:
                system = PokerSquaresPointSystem.getBritishPointSystem();
                break;
                
            case HYPERCORNER:
                system = PokerSquaresPointSystem.getHypercornerPointSystem();
                break;
                
            case SINGLEHAND:
                system = PokerSquaresPointSystem.getSingleHandPointSystem();
                break;
            case CUSTOM:
                system = new PokerSquaresPointSystem(customPointSystem);
                
        }
        
        System.out.println("\n\nSingle Player Game Sequence:");
        System.out.println(system);
        new PokerSquares(new BeemoV2(), system)
                .playSequence(Settings.Main.games, Settings.Main.seed, Settings.Main.verbose);
        }
        else {
        
        // Demonstration of tournament evaluation (3 players, 2 point systems, 100 x 30s games for each of the 3*2=6 player-system pairs) 
        System.out.println("\n\nTournament:");
        ArrayList<PokerSquaresPlayer> players = new ArrayList();

        //Players
        //players.add(new BeemoV2());
        players.add(new BeemoV2());
        players.add(new GreedyMCPatternPlayer(1));
        //players.add(new ParamMCTSPlayer());
        

        ArrayList<PokerSquaresPointSystem> systems = new ArrayList();
        PokerSquaresPointSystem.setSeed(Settings.Main.randomPointSystemSeed);

        switch (Settings.Main.pointSystem) {
            case RANDOM:
                systems.add(PokerSquaresPointSystem.getRandomPointSystem());
                break;
            
            case AMERICAN:
                systems.add(PokerSquaresPointSystem.getAmericanPointSystem());
                break;
                
            case AMERITISH:
                systems.add(PokerSquaresPointSystem.getAmeritishPointSystem());
                break;
            
            case BRITISH:
                systems.add(PokerSquaresPointSystem.getBritishPointSystem());
                break;
                
            case HYPERCORNER:
                systems.add(PokerSquaresPointSystem.getHypercornerPointSystem());
                break;
                
            case SINGLEHAND:
                systems.add(PokerSquaresPointSystem.getSingleHandPointSystem());
                break;
        }

        PokerSquares.playTournament(players, systems, Settings.Main.games, Settings.Main.seed ); // use fewer games per system for faster testing
        }
    
    }
    
}
