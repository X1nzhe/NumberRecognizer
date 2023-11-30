/*
NEU INFO 5100, Final Project - Number Recognizer
Author:
    Name: Xinzhe Yuan, NUID: , Email: yuan.xinz@northeastern.edu
    Name: Jia Xu, NUID:
Date: 13 Nov 2023
Version: 0.1

Reference:
https://openjfx.io/index.html
 */

package com.neu.info5100.numberrecognizer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class NumberRecognizerApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(NumberRecognizerApplication.class.getResource("NumberRecognizer-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 330, 330);
            stage.setTitle("CNN Digit Recognizer");
            stage.setScene(scene);
            stage.setHeight(360);
            stage.setWidth(360);
            stage.setResizable(false);
            stage.show();
            Platform.runLater(NumberRecognizerController::initialAlert);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}