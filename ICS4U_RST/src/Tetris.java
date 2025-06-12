import java.util.LinkedList;
import java.util.Queue;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import simpleIO.Console;

public class Tetris extends Application {
    private static final int TILE_SIZE = 30;
    private static final int COLUMNS = 10;
    private static final int ROWS = 20;

    private Canvas boardCanvas;
    private GraphicsContext boardGC;

    private Canvas holdCanvas;
    private GraphicsContext holdGC;

    private Canvas nextCanvas;
    private GraphicsContext nextGC;

    private Label scoreLabel;
    private int score = 0;

    private boolean leftPressed, rightPressed, downPressed, rotatePressed;
    private long lastMoveTime, lastDownTime, lastRotateTime;
    private static final long MOVE_DELAY_NS = 150_000_000;
    private static final long FAST_DROP_DELAY_NS = 60_000_000;
    private static final long ROTATE_DELAY_NS = 200_000_000;

    private Tetromino current;
    private Tetromino hold = null;
    private boolean canHold = true;

    private Queue<Integer> nextQueue = new LinkedList<>();

    private int[][] grid = new int[ROWS][COLUMNS];
    private Image[] blockImages;

    @Override
    public void start(Stage stage) {
        blockImages = new Image[] {
                new Image(getClass().getResource("/images/YellowTile.png").toString()), // O
                new Image(getClass().getResource("/images/CyanTile.png").toString()),   // I
                new Image(getClass().getResource("/images/GreenTile.png").toString()),  // S
                new Image(getClass().getResource("/images/RedTile.png").toString()),    // Z
                new Image(getClass().getResource("/images/PurpleTile.png").toString()), // T
                new Image(getClass().getResource("/images/BlueTile.png").toString()),   // J
                new Image(getClass().getResource("/images/OrangeTile.png").toString())  // L
        };

        for (int i = 0; i < 1; i++) {
            nextQueue.add((int) (Math.random() * 7));
        }

        current = new Tetromino(nextQueue.poll());
        nextQueue.add((int) (Math.random() * 7));

        boardCanvas = new Canvas(COLUMNS * TILE_SIZE, ROWS * TILE_SIZE);
        boardGC = boardCanvas.getGraphicsContext2D();

        holdCanvas = new Canvas(4 * TILE_SIZE, 4 * TILE_SIZE);
        holdGC = holdCanvas.getGraphicsContext2D();

        nextCanvas = new Canvas(4 * TILE_SIZE, 4 * TILE_SIZE);
        nextGC = nextCanvas.getGraphicsContext2D();

        scoreLabel = new Label("Score: 0");
        scoreLabel.setFont(new Font(18));
        scoreLabel.setTextFill(Color.WHITE);

        VBox sidePanel = new VBox(10, new Label("HOLD:"), holdCanvas, new Label("NEXT:"), nextCanvas, scoreLabel);
        sidePanel.setPadding(new Insets(10));
        sidePanel.setStyle("-fx-background-color: #222; -fx-text-fill: white;");
        ((Label) sidePanel.getChildren().get(0)).setTextFill(Color.WHITE);
        ((Label) sidePanel.getChildren().get(2)).setTextFill(Color.WHITE);

        HBox root = new HBox(10, boardCanvas, sidePanel);
        root.setPadding(new Insets(10));
        root.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Tetris");
        stage.show();

        Timeline gameLoop = new Timeline(new KeyFrame(Duration.millis(500), e -> {
            moveDown();
            drawAll();
        }));
        gameLoop.setCycleCount(Timeline.INDEFINITE);
        gameLoop.play();

        AnimationTimer inputLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (leftPressed && now - lastMoveTime > MOVE_DELAY_NS) {
                    move(-1);
                    drawAll();
                    lastMoveTime = now;
                } else if (rightPressed && now - lastMoveTime > MOVE_DELAY_NS) {
                    move(1);
                    drawAll();
                    lastMoveTime = now;
                }

                if (downPressed && now - lastDownTime > FAST_DROP_DELAY_NS) {
                    moveDown();
                    drawAll();
                    lastDownTime = now;
                }

                if (rotatePressed && now - lastRotateTime > ROTATE_DELAY_NS) {
                    rotate();
                    drawAll();
                    lastRotateTime = now;
                }
            }
        };
        inputLoop.start();

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.LEFT) leftPressed = true;
            else if (e.getCode() == KeyCode.RIGHT) rightPressed = true;
            else if (e.getCode() == KeyCode.DOWN) downPressed = true;
            else if (e.getCode() == KeyCode.UP) rotatePressed = true;
            else if (e.getCode() == KeyCode.Z && canHold) holdPiece();
        });

        scene.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.LEFT) leftPressed = false;
            else if (e.getCode() == KeyCode.RIGHT) rightPressed = false;
            else if (e.getCode() == KeyCode.DOWN) downPressed = false;
            else if (e.getCode() == KeyCode.UP) rotatePressed = false;
        });

        drawAll();
    }

    private void move(int dx) {
        if (current.canMove(grid, dx, 0)) {
            current.move(dx, 0);
        }
    }

    private void moveDown() {
        if (current.canMove(grid, 0, 1)) {
            current.move(0, 1);
        } else {
            current.merge(grid);
            clearLines();
            current = new Tetromino(nextQueue.poll());
            nextQueue.add((int) (Math.random() * 7));
            canHold = true;

            if (!current.canMove(grid, 0, 0)) {
                Console.print("Game Over");
                System.exit(0);
            }
        }
    }

    private void rotate() {
        current.rotate(grid);
    }


    private void holdPiece() {
        if (!canHold) return;
        if (hold == null) {
            hold = new Tetromino(current.type);
            current = new Tetromino(nextQueue.poll());
            nextQueue.add((int) (Math.random() * 7));
        } else {
            Tetromino temp = hold;
            hold = new Tetromino(current.type);
            current = new Tetromino(temp.type);
        }
        canHold = false;
    }

    private void clearLines() {
        for (int y = ROWS - 1; y >= 0; y--) {
            boolean full = true;
            for (int x = 0; x < COLUMNS; x++) {
                if (grid[y][x] == 0) {
                    full = false;
                    break;
                }
            }
            if (full) {
                for (int r = y; r > 0; r--) {
                    System.arraycopy(grid[r - 1], 0, grid[r], 0, COLUMNS);
                }
                y++;
                score += 100;
            }
        }
    }

    private void drawAll() {
        drawBoard();
        drawMini(holdGC, hold);
        drawMini(nextGC, new Tetromino(nextQueue.peek()));
        scoreLabel.setText("Score: " + score);
    }

    private void drawBoard() {
        boardGC.setFill(Color.BLACK);
        boardGC.fillRect(0, 0, COLUMNS * TILE_SIZE, ROWS * TILE_SIZE);

        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLUMNS; x++) {
                if (grid[y][x] != 0) {
                    boardGC.drawImage(blockImages[grid[y][x] - 1], x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }

        for (Point p : current.blocks) {
            boardGC.drawImage(blockImages[current.type], p.x * TILE_SIZE, p.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }
    }

    private void drawMini(GraphicsContext gc, Tetromino t) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, 4 * TILE_SIZE, 4 * TILE_SIZE);

        if (t == null) return;
        for (Point p : t.blocks) {
            int localX = p.x - 3;
            int localY = p.y;
            gc.drawImage(blockImages[t.type], localX * TILE_SIZE, localY * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
