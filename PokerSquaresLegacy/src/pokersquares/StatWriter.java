/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pokersquares;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author newuser
 */
public final class StatWriter {
    
    StatWriter(String newStat, String filename) {
        writeStat(newStat, filename);
    }
    
    public void writeStat(String newStat, String filename) {
        //TRY READING STATS FROM EXISTING FILE
        List <String> konameStats = new ArrayList();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filename));
            
            String line;
            while (true) {
                line = null;
                try { 
                    line = reader.readLine();
                } catch (Exception e) {};
            
                if (line == null) break;
                
                //STORE line
                konameStats.add(line);
            }
        } catch (Exception e) {
            //FILE NOT INITIALIZED
            System.out.println("File Read Error");
        }
        
        konameStats.add(0, newStat);
        
        //WRITE STATS TO FILE
        Writer writer = null;
        
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filename), "utf-8"));
            
            for (int line = 0; line < konameStats.size(); line++) writer.write(konameStats.get(line) + "\n");
            
        } catch (IOException ex) {
            // report
        } finally {
            try {writer.close();} catch (Exception ex) {}
        }
    }
}
