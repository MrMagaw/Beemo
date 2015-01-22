/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pokersquares.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static pokersquares.config.Settings.Evaluations.colHands;
import static pokersquares.config.Settings.Evaluations.exps;
import static pokersquares.config.Settings.Evaluations.flushPolicy;
import static pokersquares.config.Settings.Evaluations.fourOfAKindPolicy;
import static pokersquares.config.Settings.Evaluations.fullHousePolicy;
import static pokersquares.config.Settings.Evaluations.handScores;
import static pokersquares.config.Settings.Evaluations.pairPolicy;
import static pokersquares.config.Settings.Evaluations.rowHands;
import static pokersquares.config.Settings.Evaluations.threeOfAKindPolicy;
import static pokersquares.config.Settings.Evaluations.twoPairPolicy;

/**
 *
 * @author newuser
 */
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
            System.err.println("File Read Error");
            CRASH_BECAUSE_OF_MISSING_SETTINGS;
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
        
        double[] dataArray = parseArray(data);
        
        if (tag.equals("handScores")) Settings.Evaluations.handScores = dataArray;
        if (tag.equals("rowHands")) Settings.Evaluations.rowHands = dataArray;
        if (tag.equals("colHands")) Settings.Evaluations.colHands = dataArray;
        if (tag.equals("exps")) Settings.Evaluations.exps = dataArray;
        if (tag.equals("pairPolicy")) Settings.Evaluations.pairPolicy = dataArray;
        if (tag.equals("twoPairPolicy")) Settings.Evaluations.twoPairPolicy = dataArray;
        if (tag.equals("threeOfAKindPolicy")) Settings.Evaluations.threeOfAKindPolicy = dataArray;
        if (tag.equals("flushPolicy")) Settings.Evaluations.flushPolicy = dataArray;
        if (tag.equals("fullHousePolicy")) Settings.Evaluations.fullHousePolicy = dataArray;
        if (tag.equals("fourOfAKindPolicy")) Settings.Evaluations.fourOfAKindPolicy = dataArray;
    }
    
    private static double[] parseArray(String data) {
        List <Double> dataArray = new ArrayList <Double> ();
        
        //FORMAT data string
        data = data.substring(1,data.length() -1);
        String[] doubles = data.split(",");
        
        //PARSE to double
        for (int i = 0; i < doubles.length; ++i) dataArray.add(Double.parseDouble(doubles[i]));
        
        //CONVERT form list to double array
        double[] doubleArray = new double[dataArray.size()];
        for (int i = 0; i < dataArray.size(); ++i) doubleArray[i] = dataArray.get(i);
            
        return doubleArray;
    }
    
    public static void writeSettings(String filename) {
        //WRITE SETTINGS to file        
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filename), "utf-8"))){
            
            writer.write("handScores " + Arrays.toString(handScores) + "\n");
            writer.write("rowHands " + Arrays.toString(rowHands) + "\n");
            writer.write("colHands " + Arrays.toString(colHands) + "\n");
            writer.write("exps " + Arrays.toString(exps) + "\n");
            writer.write("pairPolicy " + Arrays.toString(pairPolicy) + "\n");
            writer.write("twoPairPolicy " + Arrays.toString(twoPairPolicy) + "\n");
            writer.write("threeOfAKindPolicy " + Arrays.toString(threeOfAKindPolicy) + "\n");
            writer.write("flushPolicy " + Arrays.toString(flushPolicy) + "\n");
            writer.write("fullHousePolicy " + Arrays.toString(fullHousePolicy) + "\n");
            writer.write("fourOfAKindPolicy " + Arrays.toString(fourOfAKindPolicy) + "\n");
            writer.close();
            
        } catch (IOException ex) {
            System.err.println("Failed writing settings.");
            WRITING_FAILURE;
        }
    }
}
