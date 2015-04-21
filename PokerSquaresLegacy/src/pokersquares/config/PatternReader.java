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
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author newuser
 */
public class PatternReader {
    
    public static void writePatterns(Map <Integer, Double> patternEvaluations) {
        //WRITE SETTINGS to file        
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream(Settings.Training.patternsFileOut), "utf-8"))){
            
            for (Integer p : patternEvaluations.keySet()) {
                writer.write(p + " " + patternEvaluations.get(p) + "\n");
            }
            writer.close();
            
        } catch (IOException ex) {
            System.err.println("Failed writing settings.");
            
        }
    }
    
    public static Map <Integer, Double> readPatterns(String filename) {
        Map<Integer, Double> patternEvaluations = new HashMap();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))){
            String line = reader.readLine();
            while (line != null) {
                //STORE line
                
                int pEnd = line.indexOf(' ');

                String sPattern = line.substring(0, pEnd);
                String sScore = line.substring(pEnd+1, line.length());

                int pattern = Integer.parseInt(sPattern);
                double score = Double.parseDouble(sScore);
                
                patternEvaluations.put(pattern, score);
        
                line = reader.readLine();
                
            }
        } catch (IOException ex) {
            //FILE NOT INITIALIZED
            System.err.println("Pattern File Read Error");
            
        }
        
        return patternEvaluations;
    }
    
    
}
