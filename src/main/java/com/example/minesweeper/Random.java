package com.example.minesweeper;

public class Random {
    public static int getRandomNumber(int lowest, int highest){
        highest -= lowest -1;
        return (int)(Math.random()*highest+lowest);
    }

    public static double getRandomDouble(double lowest, double highest){
        highest -= lowest -1;
        return (double)(Math.random()*highest+lowest);
    }
}
