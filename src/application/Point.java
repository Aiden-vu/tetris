package application;
/**
 * ICS4U RST Final Project
 * Point.java
 * @author V. Aiden
 * 
 * Point object used to simplify coordinate logic in the tetromino class
 * this avoids the annoyance of using 2 arrays since I can just use an object
 */

public class Point {
	//variables x and y
    public int x, y;

    /**
     * constructor for x and y coordinates
     *
     * @param x value
     * @param y value
     */
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
}