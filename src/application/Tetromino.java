package application;
/**
 * ICS4U RST Final Project
 * Tetromino.java
 * @author V. Aiden
 * 
 * WHAT IS A TETROMINO?:
 * Tetrominos are 4 blocks shapes connected together
 * there are 7 possibilities: O, I, J, L, S, Z, T
 * 
 * Tetromino object that handles all things tetrominos, the class does not use vectors but uses
 * various concepts from vectors
 * 
 * CLASS SUMMARY:
 * All 7 shapes
 * Offsets for all shapes (for wall kicks)
 * Movement check verify
 * Movement of tetrominos
 * Merging of grid
 * Rotations
 * 
 */

import java.util.ArrayList;
import java.util.List;

public class Tetromino { //tetris blocks are called tetrominos
	//the 4 block positions, since every tetromino is made of 4 blocks
    public List<Point> blocks = new ArrayList<>();
    //tetromino type
    public int type;
    //initial offsets when spawned in
    private int xOffset = 3;
    private int yOffset = 0;

    private static final int[][][] SHAPES = { //3D array of each shape
            // O
            {{1, 0}, {2, 0}, {1, 1}, {2, 1}},
            // I
            {{0, 1}, {1, 1}, {2, 1}, {3, 1}},
            // S
            {{1, 1}, {2, 1}, {0, 2}, {1, 2}},
            // Z
            {{0, 1}, {1, 1}, {1, 2}, {2, 2}},
            // T
            {{1, 0}, {0, 1}, {1, 1}, {2, 1}},
            // J
            {{0, 0}, {0, 1}, {1, 1}, {2, 1}},
            // L
            {{2, 0}, {0, 1}, {1, 1}, {2, 1}}
    };

    private static final int[][] WALL_KICKS = { //offsets used for wall kicks, useful for rotating next to a wall
        {0, 0},   // no offset
        {-1, 0},  // left 1
        {1, 0},   // right 1
        {0, -1},  // up 1
        {-2, 0},  // left 2
        {2, 0}    // right 2
    };

    /**
     * constructor for tetromino
     *
     * @param tetromino type
     */
    public Tetromino(int type) {
        this.type = type;
        for (int[] coord : SHAPES[type]) {
        	//offset used to center the blocks 
            blocks.add(new Point(coord[0] + xOffset, coord[1] + yOffset));
        }
    }

    /**
     * method for checking if the tetromino is able to move given an x and y
     *
     * @param grid
     * @param left and right movement (delta x: change in x)
     * @param downward movement (delta y: change in y)
     * @return true or false
     */
    public boolean canMove(int[][] grid, int dx, int dy) {
        for (Point p : blocks) { //check each point in the tetromino
        	//temporary coords
            int newX = p.x + dx;
            int newY = p.y + dy;
            
            //check if its on the edge/wall
            if (newX < 0 || newX >= grid[0].length || newY < 0 || newY >= grid.length) {
                return false;
            }
            
            //check if it collides with other blocks
            if (grid[newY][newX] != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * method for actually moving the tetromino
     *
     * @param left and right movement (delta x: change in x)
     * @param downward movement (delta y: change in y)
     */
    public void move(int dx, int dy) {
        for (Point p : blocks) {
            p.x += dx;
            p.y += dy;
        }
    }

    /**
     * method for locking the tetromino to the grid
     *
     * @param grid (game board)
     */
    public void merge(int[][] grid) {
        for (Point p : blocks) {
            grid[p.y][p.x] = type + 1; //change the grid to have +1 to indicated its occupied and match image index
        }
    }

    /**
     * method for rotating the tetromino block
     *
     * @param grid (game board)
     */
    public void rotate(int[][] grid) {
        if (type == 0) {
        	return; //O block doesn't rotate
        }

        Point pivot = blocks.get(1); //pivot block (usually second block)

        //loops and tries out each wall kick for a valid one
        for (int[] offset : WALL_KICKS) {
            List<Point> rotated = new ArrayList<>();
            for (Point p : blocks) {
            	//moves the block tot he origin that is relative to the pivot
                int relX = p.x - pivot.x;
                int relY = p.y - pivot.y;
                
                //90 degree rotations clockwise: (x, y) -> (-y, x)
                int newX = pivot.x - relY + offset[0];
                int newY = pivot.y + relX + offset[1];
                rotated.add(new Point(newX, newY));
            }

            //Check if rotated tetromino is within bounds and doesn't collide
            boolean canRotateHere = true;
            for (Point p : rotated) {
                if (p.x < 0 || p.x >= grid[0].length || p.y < 0 || p.y >= grid.length || grid[p.y][p.x] != 0) {
                    canRotateHere = false;
                    break;
                }
            }
            
            //If a valid rotated position is found, update the blocks and exit
            if (canRotateHere) {
                blocks = rotated;
                return; //rotation + kick successful
            }
        }
        //rotation failed with all kicks, do nothing
    }
}