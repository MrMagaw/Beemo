package pokersquares.learning;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import pokersquares.algorithms.Simulator;
import pokersquares.config.Settings;
import pokersquares.config.SettingsReader;
import pokersquares.environment.Board;

/*

                                    ▄███▄────────────────────────▄███▄
                                    █───█▄──────────────────────▄█───█
                                    █────▀██████████████████████▀────█
                                    █────────────────────────────────█
                                    █────────────────────────────────█
                                    █─────────▄████████████▄─────────█
                                    █───────▄█░░░░░░░░░░░░░░█▄───────█
 _____ _____ _   _  _   _           █─────▄█░░░░░░░░░░░░░░░░░░█▄─────█
|  ___|_   _| \ | || \ | |          █────▄█░░░░░░░░░░░░░░░░░░░░█▄────█
| |_    | | |  \| ||  \| |          █────█░░░██░░░░░░░░░░░░██░░░█────█
|  _|   | | | . ` || . ` |          █────█░░░░░░░░░░░░░░░░░░░░░░█────█
| |    _| |_| |\  || |\  |          █────█░░░░░░▄░░░░░░░░▄░░░░░░█────█
\_|    \___/\_| \_/\_| \_/          █────▀█░░░░░░▀██████▀░░░░░░█▀────█
                                    █─────▀█░░░░░░░░░░░░░░░░░░█▀─────█
                                    █───────▀█░░░░░░░░░░░░░░█▀───────█
                                    █─────────▀████████████▀─────────█
                                    █────────────────────────────────█
                                    ███▄▄────────────────────────▄▄███
                                    ██████████████████████████████████
                                    ██████████████████████████████████
                                    ██████████████████████████████████

*/

public class Finn implements Trainer{
    @Override
    public void runSession(long millis) {
        long endTime = millis + System.currentTimeMillis() - 500;
        
        List <double[]> values = new ArrayList();
        
        values.add(Settings.Evaluations.colHands); 
        values.add(Settings.Evaluations.rowHands);
        values.add(Settings.Evaluations.highCardPolicy);
        values.add(Settings.Evaluations.pairPolicy);
        values.add(Settings.Evaluations.twoPairPolicy);
        values.add(Settings.Evaluations.threeOfAKindPolicy);
        values.add(Settings.Evaluations.flushPolicy);
        values.add(Settings.Evaluations.fullHousePolicy);
        values.add(Settings.Evaluations.fourOfAKindPolicy);
        
        Random r = new Random();
        do{
            int index = r.nextInt(9);
            boolean changed;
                    
            if(index <= 1)
                changed = booleanTrain(values.get(index), millis);
            else
                changed = settingsTrain(r, values.get(index), millis);
            
            if(changed) 
                SettingsReader.writeSettings(Settings.Training.outputFile);
            
        }while(((millis = System.currentTimeMillis()) < endTime));
    }
    
    private boolean booleanTrain(double[] values, long millis){
        for(int i = values.length-1; i >= 0 ; --i){
            double preScore = Simulator.simulate(new Board(), 1000, millis);
            double preNum = values[i];
            if(preNum > 0.5)
                values[i] = 0.0;
            else
                values[i] = 1.0;
            double postScore = Simulator.simulate(new Board(), 1000, millis);
            if(postScore > preScore){
                System.out.println(preNum + "-->" + values[i] + ": Δ" + (postScore - preScore));
                return true;
            }
            values[i] = preNum;
        }
        return false;
    }
    //29.385000
    
    private boolean settingsTrain(Random r, double[] values, long millis){
        int sign = -1;
        double scale = 1;
        
        for(int i = values.length-1; i >= 0 ; --i){
            double preScore = Simulator.simulate(new Board(), 1000, millis);
            double preNum = values[i];
            
            values[i] = values[i] + (sign*scale);
            if (values[i] < 0) values[i] = 0.0;
            else if (values[i] > 1) values[i] = 1.0;
            
            double postScore = Simulator.simulate(new Board(), 1000, millis);
            
            if(postScore > preScore){
                System.out.println(preNum + "-->" + values[i] + ": Δ" + (postScore - preScore));
                return true;
            }else if(postScore == preScore){
                if (sign == -1)
                    sign = 1;
                else
                    continue;
            }else{
                if (scale > Double.MIN_NORMAL) scale = scale / 2.0;
                else if (sign == -1){
                    sign = 1;
                    scale = 1.0;
                }else
                    continue;
            }
            values[i] = preNum;
            
        }
        return false;
    }
}
