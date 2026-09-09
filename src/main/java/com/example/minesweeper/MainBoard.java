package com.example.minesweeper;

import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class MainBoard {
    private GridPane gPane;
    private Label minesLbl;
    private int boardWidth;
    private int boardHeight;
    private int buttonImageSize = 30;

    private Button[][] board;
    private Tile[][] tiles;
    private ImageView[][] flagImages; // ImageViews for flag graphics

    private int minesLeft;
    private int tilesLeftToBeRevealed;

    private boolean isSupermines;
    private boolean canWrapAround;
    private boolean canOpenMore;
    private boolean showMines;
    private boolean firstClick; // Tracks if this is the first click

    public boolean gameOver = false;

    private Image flagImage; // Loaded once and reused
    private Image mineImage; // Mine image for when you lose
    private Image superMine;

    public MainBoard(GridPane gPane, Label minesLeft, int width, int height, boolean supermines, boolean wrapAround, boolean openMore, boolean showMines){
        this.gPane = gPane;
        this.minesLbl = minesLeft;
        this.boardWidth = width;
        this.boardHeight = height;
        board = new Button[this.boardWidth][this.boardHeight];
        tiles = new Tile[this.boardWidth][this.boardHeight];
        flagImages = new ImageView[this.boardWidth][this.boardHeight];
        isSupermines = supermines;
        canWrapAround = wrapAround;
        canOpenMore = openMore;
        this.showMines = showMines;
        firstClick = true; // Set to true initially

        try {
            flagImage = new Image(getClass().getResourceAsStream("/images/flag.png"));
            mineImage = new Image(getClass().getResourceAsStream("/images/mine.png"));
            superMine = new Image(getClass().getResourceAsStream("/images/supermine.png"));
        } catch (Exception e) {
            System.out.println("Could not load images");
            flagImage = null;
            mineImage = null;
        }

        createBoard();
    }

    public void createBoard(){
        // Initialize the board with all empty tiles (no mines yet)
        for (int i = 0; i < boardWidth; i++){
            for (int j = 0; j < boardHeight; j++){
                board[i][j] = new Button();
                board[i][j].setMaxSize(buttonImageSize,buttonImageSize);
                board[i][j].setMinSize(buttonImageSize,buttonImageSize);
                board[i][j].setStyle("-fx-background-color: #4CAF50;");
                tiles[i][j] = new Tile(0); // All tiles start as empty

                // deals with images
                flagImages[i][j] = new ImageView();
                flagImages[i][j].setFitWidth(buttonImageSize);
                flagImages[i][j].setFitHeight(buttonImageSize);
                flagImages[i][j].setPreserveRatio(true);
                flagImages[i][j].setVisible(false);
                board[i][j].setGraphic(flagImages[i][j]);

                board[i][j].setText("");
                if (isSupermines){
                    board[i][j].setFont(new Font(8));
                }else{
                    board[i][j].setFont(new Font(10));
                }
                board[i][j].setTextFill(Color.web("#FFFFFF"));
                gPane.add(board[i][j],i,j);
                board[i][j].setOnMouseClicked(revealTile);
            }
        }
        minesLeft = 0; // No mines yet
        tilesLeftToBeRevealed = boardHeight * boardWidth;
        minesLbl.setText("Mines Left: " + minesLeft + "    Tiles Left: " + tilesLeftToBeRevealed);
    }

    // Generate mines after first click, ensuring the clicked area is safe
    public void generateMines(int clickedI, int clickedJ){
        int count = 0;

        // Create a list of protected tiles (clicked tile + all 8 neighbors)
        boolean[][] protectedTiles = new boolean[boardWidth][boardHeight];

        // Mark the clicked tile and its neighbors as protected
        for (int di = -1; di <= 1; di++) {
            for (int dj = -1; dj <= 1; dj++) {
                int ni = clickedI + di;
                int nj = clickedJ + dj;

                // Handle wrapping or bounds
                if (canWrapAround) {
                    ni = (ni + boardWidth) % boardWidth;
                    nj = (nj + boardHeight) % boardHeight;
                    protectedTiles[ni][nj] = true;
                } else {
                    if (ni >= 0 && ni < boardWidth && nj >= 0 && nj < boardHeight) {
                        protectedTiles[ni][nj] = true;
                    }
                }
            }
        }

        // Generate mines randomly, avoiding protected tiles
        for (int i = 0; i < boardWidth; i++){
            for (int j = 0; j < boardHeight; j++){
                // Skip protected tiles
                if (protectedTiles[i][j]) {
                    continue;
                }

                // 1 in 6 chance of mine (same as before)
                if (Random.getRandomNumber(1,6) == 1){
                    // 25% chance of super bomb if it's a bomb
                    if (Random.getRandomNumber(1,4) == 1 && isSupermines){
                        tiles[i][j] = new Tile(-2); // Super bomb
                        // Show mines if enabled, otherwise hide them
                        if (showMines) {
                            board[i][j].setStyle("-fx-background-color: #800080;"); // Purple for super bomb
                        } else {
                            board[i][j].setStyle("-fx-background-color: #4CAF50;"); // Hidden
                        }
                    } else {
                        tiles[i][j] = new Tile(-1); // Regular bomb
                        // Show mines if enabled, otherwise hide them
                        if (showMines) {
                            board[i][j].setStyle("-fx-background-color: #FF0000;"); // Red for regular bomb
                        } else {
                            board[i][j].setStyle("-fx-background-color: #4CAF50;"); // Hidden
                        }
                    }
                    count++;
                }
            }
        }

        minesLeft = count;
        tilesLeftToBeRevealed = (boardWidth*boardHeight) - minesLeft;
        minesLbl.setText("Mines Left: " + minesLeft + "    Tiles Left: " + tilesLeftToBeRevealed);

        // Now calculate the numbers around all bombs
        checkAdjacentTiles();
    }

    public void checkAdjacentTiles(){
        for (int i = 0; i < boardWidth; i++) {
            for (int j = 0; j < boardHeight; j++) {
                incrementAroundBomb(i,j);
            }
        }
    }

    public void updateBoard(){
        for (int i = 0; i < boardWidth; i++) {
            for (int j = 0; j < boardHeight; j++) {
                if (tiles[i][j].reveald && !tiles[i][j].hasFlag){
                    board[i][j].setGraphic(null);

                    // Display as "regularCount|superCount"
                    if (tiles[i][j].boardNumber >= 0){
                        if (isSupermines){
                            board[i][j].setText(tiles[i][j].regularBombCount + "|" + tiles[i][j].superBombCount);
                        }else{
                            board[i][j].setText(Integer.toString(tiles[i][j].regularBombCount));
                        }
                    } else {
                        board[i][j].setText(Integer.toString(tiles[i][j].boardNumber));
                    }

                    // Bombs keep their red/purple background
                    if (tiles[i][j].boardNumber == -1){
                        board[i][j].setStyle("-fx-background-color: #FF0000;");
                        board[i][j].setTextFill(Color.web("#FFFFFF"));
                    }
                    else if (tiles[i][j].boardNumber == -2){
                        board[i][j].setStyle("-fx-background-color: #800080;"); // Purple for super bomb
                        board[i][j].setTextFill(Color.web("#FFFFFF"));
                    }
                    // All other revealed tiles have white background with colored numbers
                    else {
                        board[i][j].setStyle("-fx-background-color: #FFFFFF;");

                        // Color the text based on total danger level
                        int totalBombs = tiles[i][j].regularBombCount + tiles[i][j].superBombCount;

                        if (totalBombs == 0){
                            board[i][j].setTextFill(Color.web("#C0C0C0")); // Light gray for zeros
                        }
                        else if (totalBombs == 1){
                            board[i][j].setTextFill(Color.web("#0000FF")); // Blue
                        }
                        else if (totalBombs == 2){
                            board[i][j].setTextFill(Color.web("#008000")); // Green
                        }
                        else if (totalBombs == 3){
                            board[i][j].setTextFill(Color.web("#FF0000")); // Red
                        }
                        else if (totalBombs == 4){
                            board[i][j].setTextFill(Color.web("#000080")); // Dark Blue
                        }
                        else if (totalBombs == 5){
                            board[i][j].setTextFill(Color.web("#800000")); // Maroon
                        }
                        else if (totalBombs == 6){
                            board[i][j].setTextFill(Color.web("#008080")); // Teal
                        }
                        else if (totalBombs == 7){
                            board[i][j].setTextFill(Color.web("#000000")); // Black
                        }
                        else if (totalBombs >= 8){
                            board[i][j].setTextFill(Color.web("#808080")); // Gray
                        }
                    }
                }else{
                    board[i][j].setText("");
                    board[i][j].setTextFill(Color.web("#FFFFFF")); // Reset text color for unrevealed
                }

                // Handle flags
                if (tiles[i][j].hasFlag){
                    if (flagImage != null) {
                        flagImages[i][j].setImage(flagImage);
                        flagImages[i][j].setVisible(true);
                        board[i][j].setGraphic(flagImages[i][j]); // SET the graphic
                        board[i][j].setStyle("-fx-background-color: #4CAF50;");
                    } else {
                        board[i][j].setGraphic(null); // REMOVE graphic
                        board[i][j].setStyle("-fx-background-color: #FFFF00;");
                    }
                }
                // Handle unrevealed tiles (show mines if enabled)
                else if (!tiles[i][j].hasFlag && !tiles[i][j].reveald) {
                    board[i][j].setGraphic(null);

                    if (showMines && tiles[i][j].boardNumber == -1){
                        board[i][j].setStyle("-fx-background-color: #FF0000;"); // Show regular bombs
                    } else if (showMines && tiles[i][j].boardNumber == -2){
                        board[i][j].setStyle("-fx-background-color: #800080;"); // Show super bombs
                    } else if (tiles[i][j].boardNumber >= 0) {
                        board[i][j].setStyle("-fx-background-color: #4CAF50;"); // Normal unrevealed tile
                    } else if (!showMines && (tiles[i][j].boardNumber == -1 || tiles[i][j].boardNumber == -2)) {
                        board[i][j].setStyle("-fx-background-color: #4CAF50;"); // Hide bombs
                    }
                }
            }
        }
        minesLbl.setText("Mines Left: " + minesLeft + "    Tiles Left: " + tilesLeftToBeRevealed);
        if (tilesLeftToBeRevealed == 0){
            winGame();
        }
    }

    // RECURSIVE VERSION - For educational purposes / advanced students
    // This is a more elegant solution but uses recursion (not typically covered deeply in AP CSA)
    public void floodFillRevealWithRecursion(int i, int j){
        // Base cases: stop if out of bounds (or wrap if needed)
        int ni = i;
        int nj = j;

        if (!canWrapAround) {
            // Normal bounds checking
            if (ni < 0 || ni >= boardWidth || nj < 0 || nj >= boardHeight) {
                return;
            }
        } else {
            // Wrap around if enabled
            ni = (i + boardWidth) % boardWidth;
            nj = (j + boardHeight) % boardHeight;
        }

        // Stop if already revealed, flagged, or is a bomb
        if (tiles[ni][nj].reveald || tiles[ni][nj].hasFlag || tiles[ni][nj].boardNumber < 0) {
            return;
        }

        // Reveal this tile
        tiles[ni][nj].reveald = true;
        tilesLeftToBeRevealed--;

        // If this tile has bombs nearby (boardNumber > 0), stop here
        // This creates the "border" of numbered tiles around the zeros
        if (tiles[ni][nj].boardNumber > 0) {
            return;
        }

        // If we got here, this is a zero tile - recursively reveal all 8 neighbors
        for (int di = -1; di <= 1; di++) {
            for (int dj = -1; dj <= 1; dj++) {
                if (di == 0 && dj == 0) continue; // Skip center tile
                floodFillRevealWithRecursion(ni + di, nj + dj);
            }
        }
    }

    // ITERATIVE VERSION - Uses only AP CSA concepts (arrays, loops, booleans)
    // This is the recommended version for students to understand
    public void floodFillReveal(int i, int j){
        // Create a 2D boolean array to track which tiles we've added to check
        boolean[][] needsCheck = new boolean[boardWidth][boardHeight];

        // Mark the starting tile
        needsCheck[i][j] = true;

        // Keep looping until no more tiles need checking
        boolean foundNewTiles = true;
        while (foundNewTiles) {
            foundNewTiles = false;

            // Go through entire board looking for tiles that need checking
            for (int ci = 0; ci < boardWidth; ci++) {
                for (int cj = 0; cj < boardHeight; cj++) {

                    // Skip if this tile doesn't need checking
                    if (!needsCheck[ci][cj]) {
                        continue;
                    }

                    // Mark as checked
                    needsCheck[ci][cj] = false;

                    // Skip if already revealed, flagged, or is a bomb
                    if (tiles[ci][cj].reveald || tiles[ci][cj].hasFlag || tiles[ci][cj].boardNumber < 0) {
                        continue;
                    }

                    // Reveal this tile
                    tiles[ci][cj].reveald = true;
                    tilesLeftToBeRevealed--;

                    // If this tile has bombs nearby (boardNumber > 0), don't check neighbors
                    // This creates the "border" of numbered tiles around the zeros
                    if (tiles[ci][cj].boardNumber > 0) {
                        continue;
                    }

                    // If we got here, this is a zero tile - mark all 8 neighbors to be checked
                    for (int di = -1; di <= 1; di++) {
                        for (int dj = -1; dj <= 1; dj++) {
                            if (di == 0 && dj == 0) continue; // Skip center tile

                            int ni = ci + di;
                            int nj = cj + dj;

                            // Handle wrapping or bounds
                            if (canWrapAround) {
                                ni = (ni + boardWidth) % boardWidth;
                                nj = (nj + boardHeight) % boardHeight;
                            } else {
                                // Skip if out of bounds
                                if (ni < 0 || ni >= boardWidth || nj < 0 || nj >= boardHeight) {
                                    continue;
                                }
                            }

                            // Mark this neighbor to be checked next
                            if (!needsCheck[ni][nj]) {
                                needsCheck[ni][nj] = true;
                                foundNewTiles = true; // We found new tiles, so keep looping
                            }
                        }
                    }
                }
            }
        }
    }

    private EventHandler<MouseEvent> revealTile = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent mouseEvent) {
            int row = GridPane.getRowIndex((Button)mouseEvent.getSource());
            int col = GridPane.getColumnIndex((Button)mouseEvent.getSource());

            if (gameOver){
                return;
            }

            if (mouseEvent.getButton() == MouseButton.PRIMARY && !tiles[col][row].hasFlag){
                // If this is the first click, generate mines now
                if (firstClick) {
                    firstClick = false;
                    generateMines(col, row);
                    // The clicked tile is guaranteed to be a zero now
                }

                // Check if it's a bomb first
                if (tiles[col][row].boardNumber == -1){
                    tiles[col][row].reveald = true;
                    revealAroundBomb(col, row, 1); // 3x3 for regular bomb
                    loseGame();
                    return;
                } else if (tiles[col][row].boardNumber == -2){
                    tiles[col][row].reveald = true;
                    revealAroundBomb(col, row, 2); // 5x5 for super bomb
                    loseGame();
                    return;
                } else {
                    // Not a bomb - use flood fill if enabled and tile is zero
                    if (canOpenMore && tiles[col][row].boardNumber == 0) {
                        floodFillReveal(col, row);
                    } else {
                        // Just reveal this single tile
                        if (!tiles[col][row].reveald) {
                            tiles[col][row].reveald = true;
                            tilesLeftToBeRevealed--;
                        }
                    }
                }
            }

            if (mouseEvent.getButton() == MouseButton.SECONDARY && !tiles[col][row].reveald){
                if (!tiles[col][row].hasFlag && minesLeft > 0){
                    tiles[col][row].hasFlag = true;
                    minesLeft--;
                } else if (tiles[col][row].hasFlag){
                    tiles[col][row].hasFlag = false;
                    minesLeft++;
                }
            }
            updateBoard();
        }
    };

    public void revealAroundBomb(int i, int j, int radius){
        for (int di = -radius; di <= radius; di++) {
            for (int dj = -radius; dj <= radius; dj++) {
                int ni, nj;

                if (canWrapAround) {
                    ni = (i + di + boardWidth) % boardWidth;
                    nj = (j + dj + boardHeight) % boardHeight;
                } else {
                    ni = i + di;
                    nj = j + dj;
                    if (ni < 0 || ni >= boardWidth || nj < 0 || nj >= boardHeight) {
                        continue;
                    }
                }

                tiles[ni][nj].reveald = true;
            }
        }
    }

    public void loseGame(){
        gameOver = true;
        for (int i = 0; i < boardWidth; i++) {
            for (int j = 0; j < boardHeight; j++) {
                tiles[i][j].reveald = true;

                // If it's a mine
                if ((tiles[i][j].boardNumber == -1 || tiles[i][j].boardNumber == -2) && !tiles[i][j].hasFlag) {

                    // Show mine image if available
                    if (mineImage != null) {
                        ImageView mineView;
                        if (tiles[i][j].boardNumber == -1){
                            mineView = new ImageView(mineImage);
                        }else{
                            mineView = new ImageView(superMine);
                        }
                        mineView.setFitWidth(buttonImageSize);
                        mineView.setFitHeight(buttonImageSize);
                        mineView.setPreserveRatio(true);
                        board[i][j].setGraphic(mineView);
                    }
                }
                // If it's flagged but NOT a mine (incorrect flag)
                else if (tiles[i][j].hasFlag && tiles[i][j].boardNumber >= 0) {
                    System.out.println("has flag but not right");
                    board[i][j].setGraphic(null); // Remove incorrect flag
                }
            }
        }
    }

    public void winGame(){
        gameOver = true;
        for (int i = 0; i < boardWidth; i++) {
            for (int j = 0; j < boardHeight; j++) {
                tiles[i][j].reveald = true;
                minesLbl.setText("YOU WIN!!!");
            }
        }
    }

    // Increments counters around bombs
    public void incrementAroundBomb(int i, int j){
        // Only process if this tile is a bomb
        if (tiles[i][j].boardNumber >= 0){
            return;
        }

        int radius;
        // 1 for 3x3, 2 for 5x5
        if (tiles[i][j].boardNumber == -1){
            radius = 1;
        }else {
            radius = 2;
        }
        boolean isSuperBomb = (tiles[i][j].boardNumber == -2);

        // Check all tiles in radius
        for (int di = -radius; di <= radius; di++) {
            for (int dj = -radius; dj <= radius; dj++) {
                if (di == 0 && dj == 0) continue; // Skip the bomb itself

                int ni, nj;

                if (canWrapAround) {
                    // Wrap-around logic: use modulo to wrap coordinates
                    ni = (i + di + boardWidth) % boardWidth;
                    nj = (j + dj + boardHeight) % boardHeight;
                } else {
                    // Normal logic: calculate coordinates
                    ni = i + di;
                    nj = j + dj;

                    // Skip if out of bounds
                    if (ni < 0 || ni >= boardWidth || nj < 0 || nj >= boardHeight) {
                        continue;
                    }
                }

                // Now ni and nj are guaranteed to be valid
                if (tiles[ni][nj].boardNumber >= 0) {
                    // Increment appropriate counter
                    if (isSuperBomb) {
                        tiles[ni][nj].superBombCount++;
                    } else {
                        tiles[ni][nj].regularBombCount++;
                    }
                    // Update boardNumber to reflect total nearby bombs
                    tiles[ni][nj].boardNumber = tiles[ni][nj].regularBombCount + tiles[ni][nj].superBombCount;
                }
            }
        }
    }
}