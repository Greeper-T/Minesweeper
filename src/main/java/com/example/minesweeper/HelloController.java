package com.example.minesweeper;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;

import java.util.Timer;
import java.util.TimerTask;

public class HelloController {
    public GridPane gPane;
    public Button spawnGridButton;

    public MainBoard board;
    public Label minesLeftLbl, timeLbl;
    public TextField boardHeightTxtField, boardWidthTxtField;
    public CheckBox superminesTextBox, wrapAroundTextBox, openMoreCheckbox, speedrunModeCheckbox, showMinesCheckbox;

    private Timer timer;
    private TimerTask timerTask;
    private int secounds = 0;

    public HelloController(){

    }
    @FXML
    public void spawnGrid(ActionEvent actionEvent) {
        gPane.getChildren().clear();
        board = new MainBoard(gPane, minesLeftLbl, Integer.parseInt(boardWidthTxtField.getText()),Integer.parseInt(boardHeightTxtField.getText()),superminesTextBox.isSelected(), wrapAroundTextBox.isSelected(), openMoreCheckbox.isSelected(), showMinesCheckbox.isSelected());
        startTimer();
    }

    private void startTimer(){
        stopTimer();
        secounds = 0;

        timer = new Timer();

        timerTask = new TimerTask() {
            @Override
            public void run() {
                secounds++;

                if (board.gameOver){
                    stopTimer();
                }

                Platform.runLater(() -> {
                    if (speedrunModeCheckbox.isSelected()){
                        if (secounds%60 <10){
                            timeLbl.setText("Time  " +  secounds /60 + ":0" + secounds%60);
                        }else{
                            timeLbl.setText("Time  " +  secounds /60 + ":" + secounds%60);
                        }
                    }
                });
            }
        };

        timer.scheduleAtFixedRate(timerTask, 1000, 1000);
    }

    private void stopTimer() {
        if (timerTask != null) {
            timerTask.cancel();
        }
        if (timer != null) {
            timer.cancel();
        }
    }

}
