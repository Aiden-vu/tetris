/**
 * ICS4U RST Final Project
 * Tetris.java
 * @author V. Aiden
 * 
 * Main class for tetris game
 * 
 * the goal of this project was to try to recreate the popular game Tetris in JavaFX,
 * graphics context was used as a primary render since it was easy to learn and implement
 * and allowed for smooth frame rendering
 * 
 * FEATURES (requirements):
 * Falling blocks (tetrominoes) that you can move left and right
 * 7 Tetromino types (O, I, T, S, Z, L and J)
 * Ability to rotate tetrominoes
 * Line clears when tetrominoes create a line
 * Points awarded when lines are cleared
 * High scores stored and loaded in a txt file
 * Game over when pieces reach top of screen
 * Main menu
 * Ability to store 1 tetromino/swap with stored tetromino
 * Color coded tetrominoes (ART CREDIT: Ryan Bolt)
 * Accelerate/instant block placements
 * Future tetrominoes shown
 * Increased movement overtime
 * 
 */

import java.util.LinkedList;
import java.util.Queue;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Tetris extends Application {
	//constants
    private static final int TILE_SIZE = 30;
    private static final int COLUMNS = 10;
    private static final int ROWS = 20;
    
    //java fx
    private Stage mainStage;
    private Scene menuScene, tutorialScene, gameScene, gameOverScene;

    //using graphics context and canvas because it has good performance and greater control
    private Canvas boardCanvas;
    private GraphicsContext boardGC;
    private Canvas holdCanvas;
    private GraphicsContext holdGC;
    private Canvas nextCanvas;
    private GraphicsContext nextGC;
    
    //score variables
    private Label scoreLabel;
    private Label gameOverLabel;
    private Label highScoreLabel;
    private int score = 0;
    private int highScore = 0;
    
    //variables related to controls and tetromino logic
    private boolean leftPressed, rightPressed, downPressed, rotatePressed;
    private long lastMoveTime, lastDownTime, lastRotateTime;
    
    //delay values (in nanoseconds for more precision)
    private static final long MOVE_DELAY_NS = 150_000_000; //150ms
    private static final long FAST_DROP_DELAY_NS = 60_000_000; //60ms
    private static final long ROTATE_DELAY_NS = 200_000_000; //200ms
    
    //tetromino dropping variables
    private double dropInterval = 500; // milliseconds
    private final double minDropInterval = 100; // minimum speed limit
    private final double dropAcceleration = 50; // how much to speed up each time (ms)
    //INFO: I know it ramps up FAST but I just did this to show that it actually is speeding up
    
    //tetromino class and logic
    private Tetromino current; //currently falling tetromino
    private Tetromino hold = null; // tetromino being held
    private boolean canHold = true; //prevent multi swaps
    
    //arrays
    private Queue<Integer> nextQueue = new LinkedList<>(); //queue for next tetrominos
    private int[][] grid = new int[ROWS][COLUMNS]; //game grid
    private Image[] blockImages; //images

    private Timeline gameLoop; //main game loop (aka drop timer)
    private AnimationTimer inputLoop; //frame-by-frame input handling

    @Override
    public void start(Stage stage) {
        this.mainStage = stage;
        
        //array for images to render, each are ordered depending on piece
        blockImages = new Image[] {
            new Image(getClass().getResource("/images/YellowTile.png").toString()), // O
            new Image(getClass().getResource("/images/CyanTile.png").toString()),   // I
            new Image(getClass().getResource("/images/GreenTile.png").toString()),  // S
            new Image(getClass().getResource("/images/RedTile.png").toString()),    // Z
            new Image(getClass().getResource("/images/PurpleTile.png").toString()), // T
            new Image(getClass().getResource("/images/BlueTile.png").toString()),   // J
            new Image(getClass().getResource("/images/OrangeTile.png").toString())  // L
        };
        
        //load all scenes
        setupMenuScene();
        setupTutorialScene();
        setupGameOverScene();
        setupGameScene();

        stage.setScene(menuScene);
        stage.setTitle("Tetris");
        stage.show();
    }

    private void setupMenuScene() { //start menu screen
    	//title
        Label title = new Label("TETRIS");
        title.setFont(new Font(40));
        title.setTextFill(Color.WHITE);
        
        //buttons
        Button startBtn = new Button("Start");
        Button exitBtn = new Button("Exit");

        startBtn.setOnAction(e -> mainStage.setScene(tutorialScene));
        exitBtn.setOnAction(e -> System.exit(0));
        
        //vbox layout
        VBox menuLayout = new VBox(20, title, startBtn, exitBtn);
        menuLayout.setAlignment(Pos.CENTER);
        menuLayout.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));

        menuScene = new Scene(menuLayout, 450, 620);
    }

    private void setupTutorialScene() { //menu for controls and instructions to game
    	//tutorial for controls
        Label tutorial = new Label("CONTROLS:\n\n← → ↓ : Move\n↑ : Rotate\nZ : Hold Piece\n\nPress 'Start Game' to begin.");
        tutorial.setFont(new Font(20));
        tutorial.setTextFill(Color.WHITE);
        tutorial.setAlignment(Pos.CENTER);

        //button to start game below instructions
        Button beginBtn = new Button("Start Game");
        beginBtn.setOnAction(e -> {
            resetGame();
            mainStage.setScene(gameScene);
        });

        //vbox layout
        VBox tutorialLayout = new VBox(30, tutorial, beginBtn);
        tutorialLayout.setAlignment(Pos.CENTER);
        tutorialLayout.setBackground(new Background(new BackgroundFill(Color.DARKSLATEGRAY, null, null)));

        tutorialScene = new Scene(tutorialLayout, 450, 620);
    }

    private void setupGameOverScene() { //game over screen when you lose
    	//labels
        gameOverLabel = new Label();
        gameOverLabel.setFont(new Font(24));
        gameOverLabel.setTextFill(Color.WHITE);
        
        //button to restart game after death
        Button retryBtn = new Button("Retry");
        retryBtn.setOnAction(e -> {
            resetGame();
            mainStage.setScene(gameScene);
        });

        Button exitBtn = new Button("Exit");
        exitBtn.setOnAction(e -> System.exit(0));
        
        //vbox layout
        VBox overLayout = new VBox(20, gameOverLabel, retryBtn, exitBtn);
        overLayout.setAlignment(Pos.CENTER);
        overLayout.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));

        gameOverScene = new Scene(overLayout, 450, 620);
    }

    private void setupGameScene() { //actual game screen
    	//using graphics context to draw the board (canvas)
    	//main canvas
        boardCanvas = new Canvas(COLUMNS * TILE_SIZE, ROWS * TILE_SIZE);
        boardGC = boardCanvas.getGraphicsContext2D();

        //canvas for block holding
        holdCanvas = new Canvas(4 * TILE_SIZE, 4 * TILE_SIZE);
        holdGC = holdCanvas.getGraphicsContext2D();

        //canvas for next tetromino in the queue
        nextCanvas = new Canvas(4 * TILE_SIZE, 4 * TILE_SIZE);
        nextGC = nextCanvas.getGraphicsContext2D();

        //label for current score
        scoreLabel = new Label("Score: 0");
        scoreLabel.setFont(new Font(18));
        scoreLabel.setTextFill(Color.WHITE);
        
        //label for all time high score (default 0)
        highScoreLabel = new Label("High Score: 0");
        highScoreLabel.setFont(new Font(18));
        highScoreLabel.setTextFill(Color.WHITE);

        //vbox layout for the side panel which contains hold, next, score and highscore
        VBox sidePanel = new VBox(10, new Label("HOLD:"), holdCanvas, new Label("NEXT:"), nextCanvas, scoreLabel, highScoreLabel);
        sidePanel.setPadding(new Insets(10));
        sidePanel.setStyle("-fx-background-color: #222; -fx-text-fill: white;");
        ((Label) sidePanel.getChildren().get(0)).setTextFill(Color.WHITE);
        ((Label) sidePanel.getChildren().get(2)).setTextFill(Color.WHITE);

        //hbox for the actual main canvas which contains side panel
        HBox root = new HBox(10, boardCanvas, sidePanel);
        root.setPadding(new Insets(10));
        root.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));

        gameScene = new Scene(root);
        setupControls();
    }

    private void resetGame() { //method to reset the game
    	//load high score
    	highScore = HighScoreHandler.loadHighScore();
    	
    	//intialize all variables
        score = 0;
        grid = new int[ROWS][COLUMNS];
        nextQueue.clear();
        hold = null;
        canHold = true;
        dropInterval = 500; // reset speed at game start

        //generate new tetrominos to kickstart the game
        for (int i = 0; i < 3; i++) {
        	nextQueue.add((int) (Math.random() * 7)); //add new from 7 tetromino shapes
        }
        
        current = new Tetromino(nextQueue.poll()); //get new current tetromino
        nextQueue.add((int) (Math.random() * 7)); //add new tetrominos to queue

        //start the game loop
        startGameLoop();
        
        //animation timer used for smooth tetromino controls movement (credit: Liam for the suggesting)
        inputLoop = new AnimationTimer() {
            @Override
            public void handle(long now) { //calls every frame (now is in nanoseconds)
            	//logic used to make super smooth controls, its like key buffering
                if (leftPressed && now - lastMoveTime > MOVE_DELAY_NS) { //if key is pressed and for more time than the allowed delay
                    move(-1); drawAll(); lastMoveTime = now; //move -> new drawing -> change last move time
                } else if (rightPressed && now - lastMoveTime > MOVE_DELAY_NS) {
                    move(1); drawAll(); lastMoveTime = now;
                }
                if (downPressed && now - lastDownTime > FAST_DROP_DELAY_NS) {
                    moveDown(); drawAll(); lastDownTime = now;
                }
                if (rotatePressed && now - lastRotateTime > ROTATE_DELAY_NS) {
                    rotate(); drawAll(); lastRotateTime = now;
                }
            }
        };
        //starts the animation timer
        inputLoop.start();
        
        //draws everything from graphics context
        drawAll();
    }

    private void startGameLoop() { //start a new game loop
        if (gameLoop != null) { //stops gameloop if there is one already
        	gameLoop.stop();
        }
        
        //create a new time line called gameloop that triggers moveDown() and drawlAll() every dropInterval
        gameLoop = new Timeline(new KeyFrame(Duration.millis(dropInterval), e -> {
            moveDown();
            drawAll();
        }));
        gameLoop.setCycleCount(Timeline.INDEFINITE);
        gameLoop.play();
    }


    private void setupControls() {
    	//using KeyCode, setup all controls
    	//on key pressed
        gameScene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.LEFT) {
            	leftPressed = true;
            } else if (e.getCode() == KeyCode.RIGHT) {
            	rightPressed = true;
            } else if (e.getCode() == KeyCode.DOWN) {
            	downPressed = true;
            } else if (e.getCode() == KeyCode.UP) {
            	rotatePressed = true;
            } else if (e.getCode() == KeyCode.Z && canHold) {
            	holdPiece();
            }
        });

        //on key released (only for movement)
        gameScene.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.LEFT) {
            	leftPressed = false;
            } else if (e.getCode() == KeyCode.RIGHT) {
            	rightPressed = false;
            } else if (e.getCode() == KeyCode.DOWN) {
            	downPressed = false;
            } else if (e.getCode() == KeyCode.UP) {
            	rotatePressed = false;
            }
        });
    }

    private void move(int dx) { //move current tetromino left/right
    	//if current tetromino can move left/right
        if (current.canMove(grid, dx, 0)) {
        	//move it left/right
        	current.move(dx, 0);
        }
    }

    private void moveDown() { //move down current tetromino
        if (current.canMove(grid, 0, 1)) { //if current tetromino can move down
        	//move it down
            current.move(0, 1);
        } else {
        	//lock it in place in the grid
            current.merge(grid);
            //clear line method
            clearLines();
            //get a new current tetromino from the array list queue
            current = new Tetromino(nextQueue.poll());
            //add a new tetromino to the queue
            nextQueue.add((int) (Math.random() * 7));
            //reset the hold restriction
            canHold = true;

            //if current tetromino cant move down and its at the top (cant move left or right) -> game over
            if (!current.canMove(grid, 0, 0)) {
            	//if the current score is higher than the old hgihscore
                if (score > highScore) {
                	//create a new highscore (for next game when restarted)
                    highScore = score;
                    //save high score to file
                    HighScoreHandler.saveHighScore(highScore);
                }
                //game over label
                gameOverLabel.setText("Game Over!\nFinal Score: " + score + "\nHigh Score: " + highScore);
                
                //stop the game to prevent bugs
                gameLoop.stop();
                inputLoop.stop();
                mainStage.setScene(gameOverScene);
            }
        }
    }

    private void rotate() { //rotate method
        current.rotate(grid);
    }

    private void holdPiece() { //hold method
        if (!canHold) { //if you cant hold
        	return; //end
        }
        
        if (hold == null) { //no tetromino in hold spot
        	//put the tetromino in the hold spot
            hold = new Tetromino(current.type);
            //get a new current tetromino from the array list queue
            current = new Tetromino(nextQueue.poll());
            //add a new tetromino to the queue
            nextQueue.add((int) (Math.random() * 7));
        } else {
        	//swap out the tetromino
            Tetromino temp = hold;
            //put the tetromino in the hold spot
            hold = new Tetromino(current.type);
            //change the current tetromino to the hold one
            current = new Tetromino(temp.type);
        }
        //remove hold capability to prevent hold spamming
        canHold = false;
    }

    private void clearLines() { //method for clearing lines
    	//reset line cleared to false
        boolean lineCleared = false;
        for (int y = ROWS - 1; y >= 0; y--) { //check the row
        	
        	//assume full
            boolean full = true;
            
            for (int x = 0; x < COLUMNS; x++) {//check in each column
                if (grid[y][x] == 0) { //if there is empty space
                	//change to false
                    full = false;
                    //break
                    break;
                }
            }
            
            if (full) { //if it remained full
                for (int r = y; r > 0; r--) {
                	//copy all the above lines and moves them down one
                	//arraycopy is highly efficient and very easy to implement with my current code
                    System.arraycopy(grid[r - 1], 0, grid[r], 0, COLUMNS);
                }
                y++; //check the row again after the sift
                
                //increase the score
                score += 100;
                lineCleared = true;
            }
        }
        
        //if line was cleared -> speed up the drop speed
        if (lineCleared) {
            speedUpDrop();
        }
    }

    private void speedUpDrop() { //method to speed up the drop speed
        if (dropInterval > minDropInterval) {
            dropInterval -= dropAcceleration;  // decrease delay to speed up
            if (dropInterval < minDropInterval) {
                dropInterval = minDropInterval;  // clamp to minimum speed
            }
            startGameLoop(); // restart game loop with new speed
        }
    }


    private void drawAll() { //draw method for graphics context
    	//main canvas
        drawBoard();
        
        //mini is the side bar
        drawMini(holdGC, hold);
        drawMini(nextGC, new Tetromino(nextQueue.peek()));
        scoreLabel.setText("Score: " + score);
        highScoreLabel.setText("High Score: " + highScore);
    }

    private void drawBoard() { //method to draw main board
    	//background
        boardGC.setFill(Color.BLACK);
        boardGC.fillRect(0, 0, COLUMNS * TILE_SIZE, ROWS * TILE_SIZE);
        
        //check row and column in the grid
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLUMNS; x++) {
                if (grid[y][x] != 0) { //if the cell is not 0 then that means a block is there
                    boardGC.drawImage(blockImages[grid[y][x] - 1], x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }
        for (Point p : current.blocks) { //draw the current falling tetromino
            boardGC.drawImage(blockImages[current.type], p.x * TILE_SIZE, p.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }
    }

    private void drawMini(GraphicsContext gc, Tetromino t) { // draw the hold and next tetrominos
    	//black background
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, 4 * TILE_SIZE, 4 * TILE_SIZE);
        if (t == null) { //if there is no tetromino then end method
        	return;
        }
        
        for (Point p : t.blocks) { //draw and positions the tetromino in a 4x4 mini grid
            int localX = p.x - 3;
            int localY = p.y;
            gc.drawImage(blockImages[t.type], localX * TILE_SIZE, localY * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
