package application;
/**
 * ICS4U RST Final Project
 * Point.java
 * @author V. Aiden
 * 
 * HighScoreHandler is used to save and load high scores from a text file
 * This class is used to separate it from the main class and make it easier to debug
 */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import simpleIO.Console;

public class HighScoreHandler {
	//load the existing high score for display
	public static int loadHighScore() {
		//file reading from U3 A4
		FileReader tetrisFile;
	    BufferedReader tetrisStream;
	    
        try {
        	//load highscores.txt
        	tetrisFile = new FileReader("data/highscores.txt");
        	tetrisStream = new BufferedReader(tetrisFile);
        	
        	//read the first line and set high score to it
            String line = tetrisStream.readLine();
            int highScore = Integer.parseInt(line);
            
            if (line != null) { //if there is a score there then return it
            	tetrisStream.close(); //close it!!
                return highScore;
            }
            //if not then close and return 0
            tetrisStream.close();
            
            //exceptions copied from U3 A4
        } catch (FileNotFoundException e) {
            Console.print("No file was found: " + e.getMessage());
          } catch (IOException e) {
            Console.print("Problems reading the file: " + e.getMessage());
          } catch (NumberFormatException e) {
            Console.print("File not formatted properly: " + e.getMessage());
          }
        
        //default to 0 if there is no high score
        return 0;
    }
	
	/**
     * method to save new highscore to the txt file
     *
     * @param new high score
     */
    public static void saveHighScore(int score) {
    	//file writing from U3 A4
    	FileWriter tetrisFile;
    	PrintWriter tetrisWriter;
    	
        try {
        	//load highscores.txt
        	tetrisFile = new FileWriter("data/highscores.txt");
        	tetrisWriter = new PrintWriter(tetrisFile);
        	
        	//make the first line to the new high score (overwrite the old one)
        	tetrisWriter.println(String.valueOf(score));
        	//CLOSE IT
        	tetrisFile.close();
        	
        	Console.print("File saved successfully");
        	
        	//exception
        } catch (IOException e) {
          Console.print("Error writing to file: " + e.getMessage());
        }
    }

}