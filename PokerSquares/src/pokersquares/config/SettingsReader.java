/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

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

/**
 *
 * @author newuser
 */
public class SettingsReader {
    
    public static void readSettings(String fileName) {
        
        List <String> settings = new ArrayList();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(fileName));
            
            String line;
            while (true) {
                line = null;
                try { 
                    line = reader.readLine();
                } catch (Exception e) {};
            
                if (line == null) break;
                
                //STORE line
                settings.add(line);
            }
        } catch (Exception e) {
            //FILE NOT INITIALIZED
            System.out.println("File Read Error");
        }
        
        for (String line : settings) readLine(line);
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
        if (tag.equals("exps")) Settings.Evaluations.exps = dataArray;
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
}
