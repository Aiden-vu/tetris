import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Tetromino {
    public List<Point> blocks = new ArrayList<>();
    public int type;
    private int xOffset = 3;
    private int yOffset = 0;
    private int rotationState = 0;

    private static final int[][][] SHAPES = {
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

    public Tetromino(int type) {
        this.type = type;
        for (int[] coord : SHAPES[type]) {
            blocks.add(new Point(coord[0] + xOffset, coord[1] + yOffset));
        }
    }

    public static Tetromino randomTetromino() {
        Random rand = new Random();
        return new Tetromino(rand.nextInt(SHAPES.length));
    }

    public boolean canMove(int[][] grid, int dx, int dy) {
        for (Point p : blocks) {
            int newX = p.x + dx;
            int newY = p.y + dy;
            if (newX < 0 || newX >= grid[0].length || newY < 0 || newY >= grid.length) {
                return false;
            }
            if (grid[newY][newX] != 0) {
                return false;
            }
        }
        return true;
    }

    public void move(int dx, int dy) {
        for (Point p : blocks) {
            p.x += dx;
            p.y += dy;
        }
    }

    public void merge(int[][] grid) {
        for (Point p : blocks) {
            grid[p.y][p.x] = type + 1; // Color index (1-based)
        }
    }

    public void rotate(int[][] grid) {
        if (type == 0) return; // O block doesn't rotate

        Point pivot = blocks.get(1); // Use center or 2nd block as pivot
        List<Point> rotated = new ArrayList<>();
        for (Point p : blocks) {
            int relX = p.x - pivot.x;
            int relY = p.y - pivot.y;
            int newX = pivot.x - relY;
            int newY = pivot.y + relX;
            rotated.add(new Point(newX, newY));
        }

        for (Point p : rotated) {
            if (p.x < 0 || p.x >= grid[0].length || p.y < 0 || p.y >= grid.length || grid[p.y][p.x] != 0) {
                return; // Invalid rotation
            }
        }

        blocks = rotated;
    }
}
