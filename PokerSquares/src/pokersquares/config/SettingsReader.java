package pokersquares.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static pokersquares.config.Settings.Evaluations.colHands;
import static pokersquares.config.Settings.Evaluations.flushPolicy;
import static pokersquares.config.Settings.Evaluations.fourOfAKindPolicy;
import static pokersquares.config.Settings.Evaluations.fullHousePolicy;
import static pokersquares.config.Settings.Evaluations.handScores;
import static pokersquares.config.Settings.Evaluations.highCardPolicy;
import static pokersquares.config.Settings.Evaluations.pairPolicy;
import static pokersquares.config.Settings.Evaluations.rowHands;
import static pokersquares.config.Settings.Evaluations.threeOfAKindPolicy;
import static pokersquares.config.Settings.Evaluations.twoPairPolicy;

import static pokersquares.config.Settings.Evaluations.*;


public class SettingsReader {
    
    public static void readSettings(String fileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))){
            String line = reader.readLine();
            while (line != null) {
                readLine(line);
                line = reader.readLine();
                //STORE line
            }
        } catch (IOException ex) {
            //FILE NOT INITIALIZED
            System.err.println("Settings File Read Error");
            
            Settings.Evaluations.rowHands = new boolean[]{false, true};
            Settings.Evaluations.colHands = new boolean[]{true, false};
            
            Settings.Evaluations.highCardPolicy = new double[5];
            Settings.Evaluations.pairPolicy = new double[4];
            Settings.Evaluations.twoPairPolicy = new double[3];
            Settings.Evaluations.threeOfAKindPolicy = new double[4];
            Settings.Evaluations.straightPolicy = new double[5];
            Settings.Evaluations.flushPolicy = new double[6];
            Settings.Evaluations.fullHousePolicy = new double[1];
            Settings.Evaluations.fourOfAKindPolicy = new double[1];
            
            writeSettings(Settings.Training.settingsFileOut);
            
        }
    }
    
    private static void readLine(String line) {
        //READ SETTINGS DATA
        //FOR EACH LINE
                
        if (line == null) return;
                
        int tagEnd = line.indexOf(' ');
        if (tagEnd == -1) return;
                
        //SET TAG AND DATA
        String tag = line.substring(0, tagEnd);
        String data = line.substring(tagEnd+1, line.length());
        
        data = data.replaceAll(" ","");        

        try {
            switch(tag){
                case "handScores": break;
                case "rowHands": case "colHands":
                    Settings.Evaluations.class.getField(tag).set(null, parseBooleanArray(data));
                    break;
                default:
                    Settings.Evaluations.class.getField(tag).set(null, parseDoubleArray(data));
            }
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            System.err.println("Failed setting field: " + tag);
        }

    }
    
    private static double[] parseDoubleArray(String data) {
        //FORMAT data string
        data = data.substring(1,data.length() -1);
        String[] strings = data.split(",");
        double[] doubles = new double[strings.length];
        for(int i = 0; i < strings.length; ++i)
            doubles[i] = Double.parseDouble(strings[i]);
        return doubles;
    }
    
    private static boolean[] parseBooleanArray(String data) {
        //FORMAT data string
        data = data.substring(1,data.length() -1);
        String[] strings = data.split(",");
        boolean[] booleans = new boolean[strings.length];
        for(int i = 0; i < strings.length; ++i)
            booleans[i] = Boolean.parseBoolean(strings[i]);
        return booleans;
    }
    
    public static void writeSettings(String filename) {
        //WRITE SETTINGS to file        
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filename), "utf-8"))){
            
            writer.write("handScores " + Arrays.toString(handScores) + "\n");
            writer.write("rowHands " + Arrays.toString(rowHands) + "\n");
            writer.write("colHands " + Arrays.toString(colHands) + "\n");
            writer.write("highCardPolicy " + Arrays.toString(highCardPolicy) + "\n");
            writer.write("pairPolicy " + Arrays.toString(pairPolicy) + "\n");
            writer.write("twoPairPolicy " + Arrays.toString(twoPairPolicy) + "\n");
            writer.write("threeOfAKindPolicy " + Arrays.toString(threeOfAKindPolicy) + "\n");
            writer.write("straightPolicy " + Arrays.toString(straightPolicy) + "\n");
            writer.write("flushPolicy " + Arrays.toString(flushPolicy) + "\n");
            writer.write("fullHousePolicy " + Arrays.toString(fullHousePolicy) + "\n");
            writer.write("fourOfAKindPolicy " + Arrays.toString(fourOfAKindPolicy) + "\n");
            writer.close();
            
        } catch (IOException ex) {
            System.err.println("Failed writing settings.");
            
        }
    }
}
