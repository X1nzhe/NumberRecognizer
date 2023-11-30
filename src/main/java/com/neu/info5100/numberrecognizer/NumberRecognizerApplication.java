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

// Package declaration specifying the package structure
package com.neu.info5100.numberrecognizer;

// Import statements for necessary JavaFX and IO classes
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

// Class definition for the main application
public class NumberRecognizerApplication extends Application {
    // Override the start method from the Application class
    @Override
    public void start(Stage stage) throws IOException {
        try {
            // Create an instance of FXMLLoader to load the FXML file
            FXMLLoader fxmlLoader = new FXMLLoader(NumberRecognizerApplication.class.getResource("NumberRecognizer-view.fxml"));

            // Create a scene using the loaded FXML file and set dimensions
            Scene scene = new Scene(fxmlLoader.load(), 330, 330);

            // Set properties for the main stage
            stage.setTitle("CNN Digit Recognizer");
            stage.setScene(scene);
            stage.setHeight(360);
            stage.setWidth(360);
            stage.setResizable(false);
            stage.show();

            // Run the specified method on the JavaFX Application thread
            Platform.runLater(NumberRecognizerController::initialAlert);

        } catch (Exception e) {
            // Print the stack trace in case of an exception
            e.printStackTrace();
        }
    }

    // Main method to launch the JavaFX application
    public static void main(String[] args) {
        launch();
    }
}
