Digit Recognizer with TensorFlow and JavaFX
NEU INFO5100 Final Project

This project demonstrates a digit recognizer implemented using TensorFlow in Python for training and exporting the model, and Java with JavaFX for creating a user interface to draw digits and receive predictions.

Overview
The project consists of two main parts:

Training the Model in Python:
Utilized the TensorFlow library to create and train a convolutional neural network (CNN) on the MNIST dataset.
The trained model is then saved in TensorFlow format for later use.

JavaFX User Interface:
Developed a Java application with JavaFX to create a user-friendly interface for drawing digits.
Integrated TensorFlow Java API to load the pre-trained model and obtain predictions.
The application allows users to draw a digit on a canvas, and upon submission, the model predicts the digit.

Project Structure
|-- java
        |-- src
            |-- main
                |-- java
                    |-- com.neu.info5100.numberrecognizer
                        |-- Launcher.java   
                        |-- NumberRecognizerApplication 
                    |-- NumberRecognizerController.java
                        |-- NumberRecognizerController
                    |-- NumberRecognizerModel.java
                    |-- TensorFlowModelLoader.java
                    
                |-- com.neu.info5100.numberrecognizer
                    |--NumberRecognizer-view-fxml
        |--Test Screenshots
    |-- README.md                    

Run the Application
Open the project in your preferred Java IDE.
Build and run the Launcher.java file in src/main/java/com.neu.info5100.numberrecognizer
The JavaFX application window will appear, allowing you to draw digits.

  
