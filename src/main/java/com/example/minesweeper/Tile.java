package com.example.minesweeper;

public class Tile {
    public int boardNumber;
    public boolean hasFlag = false;
    public boolean reveald = false;
    public int superBombCount = 0;
    public int regularBombCount = 0;

    public Tile(int numba){
        this.boardNumber = numba;
    }
}
