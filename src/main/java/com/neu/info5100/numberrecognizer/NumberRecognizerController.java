/*
NEU INFO 5100, Final Project - Number Recognizer
Author:
    Name: Xinzhe Yuan, NUID:
    Name: Jia Xu, NUID:
Date: 13 Nov 2023
Version: 0.1

Reference
https://stackoverflow.com/questions/49343256/threads-in-javafx-not-on-fx-application-thread
https://stackoverflow.com/questions/34198190/javafx-progressbar-animation-or-transition
https://www.tensorflow.org/jvm
https://stackoverflow.com/questions/52472046/alerts-in-javafx-do-not-close-when-x-button-is-pressed


 */

package com.neu.info5100.numberrecognizer;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;

// Abstract class serving as the base for NumberRecognizerController and providing common methods
abstract class NumberRecognizerBase {
    NumberRecognizerModel model;

    // Abstract methods to be implemented by subclasses
    protected abstract void setProgressBar(ProgressBar pb);
    protected abstract void saveAsPNG();

    // Method to clean temporary directory
    public void cleanTmp() {
        // Path to the temporary directory
        String tmpPath = "./tmp";
        File tmp = new File(tmpPath);

        // Check if the directory exists and is a directory
        if (tmp.exists() && tmp.isDirectory()) {
            File[] files = tmp.listFiles();
            // Delete all files in the directory
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
        }
    }

    // Method to resize an image
    protected Image resizeImage(Image image, int width, int height) {
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        return imageView.snapshot(null, null);
    }

    // Method to save an image
    protected void saveImage(Image image, String path) {
        File file = new File(path);
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Method to invert colors of an image
    protected Image invertImage(Image image) {
        WritableImage invertedImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelReader pixelReader = image.getPixelReader();
        PixelWriter pixelWriter = invertedImage.getPixelWriter();

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color color = pixelReader.getColor(x, y);
                pixelWriter.setColor(x, y, color.invert());
            }
        }
        return invertedImage;
    }

    // Abstract method to show the result of a prediction
    protected abstract void showResult(ModelLoader.Prediction p);
}

// Class for the main controller handling the JavaFX components
public class NumberRecognizerController extends NumberRecognizerBase {
    // Constructor initializes the model and sets a shutdown hook to clean temporary files
    public NumberRecognizerController() {
        model = new NumberRecognizerModel();
        Runtime.getRuntime().addShutdownHook(new Thread(this::cleanTmp));
    }

    // FXML annotations to inject components from the FXML file
    @FXML
    private Canvas canvas;
    @FXML
    private GraphicsContext gc;
    @FXML
    private ProgressBar progressBar;

    // Method called during initialization of the controller
    @FXML
    protected void initialize() {
        gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(15.0);

        // Set mouse event handlers for drawing on the canvas
        canvas.setOnMousePressed(this::onMousePressed);
        canvas.setOnMouseDragged(this::onMouseDragged);

        // Task for loading the machine learning model in a separate thread
        Task<Void> loadModelTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                loadModel();
                return null;
            }
        };
        // Start the thread for loading the model
        new Thread(loadModelTask).start();
    }

    // Static method to display an initial alert
    protected static void initialAlert() {
        ButtonType okButton = new ButtonType("Ok");
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "", okButton);
        alert.setTitle("CNN Digit Recognizer");
        alert.setHeaderText("Draw a digit from [0-9] to be recognized.");
        alert.showAndWait();
    }

    // Static method to display an error alert with a custom message
    protected static void errorAlert(String errorMSG) {
        ButtonType goodButton = new ButtonType("Good");
        Alert alert = new Alert(Alert.AlertType.ERROR, "", goodButton);
        alert.setTitle("CNN Digit Recognizer");
        alert.setHeaderText("Error!");
        alert.setContentText(errorMSG);
        alert.showAndWait();
    }

    // Variables to track last mouse coordinates for drawing
    @FXML
    private double lastX, lastY;

    // Method called when the mouse is pressed on the canvas
    @FXML
    private void onMousePressed(MouseEvent event) {
        lastX = event.getX();
        lastY = event.getY();
        gc.beginPath();
        gc.moveTo(lastX, lastY);
    }

    // Method called when the mouse is dragged on the canvas
    @FXML
    private void onMouseDragged(MouseEvent event) {
        double newX = event.getX();
        double newY = event.getY();

        // Set line properties for drawing
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);
        gc.lineTo(newX, newY);
        gc.stroke();

        lastY = newY;
        lastX = newX;
    }

    // Method to clear the canvas
    @FXML
    private void cleanCanvas() {
        gc.clearRect(0.0, 0.0, canvas.getWidth(), canvas.getHeight());
    }

    // Method called when the submit button is pressed
    @FXML
    private void submitBtnEvent() {
        progressBar.setVisible(true);
        // Thread for performing digit recognition
        Thread predictionThread = new Thread(() -> {
            try {
                ModelLoader.Prediction prediction = recognizeNumber();
                // Update UI on the JavaFX Application thread
                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    showResult(prediction);
                    cleanCanvas();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Set progress bar and start the prediction thread
        setProgressBar(progressBar);
        predictionThread.start();
    }

    // Method to show the result of a prediction
    @Override
    public void showResult(ModelLoader.Prediction prediction) {
        int mostPossibleNum = prediction.getMostPossibleNum();
        int secondPossibleNum = prediction.getSecondPossibleNum();
        float mostPossibleNumPossibility = prediction.getMostPossibleNumPossibility();
        float secondPossibleNumPossibility = prediction.getSecondPossibleNumPossibility();

        // Create an information alert with the prediction result
        ButtonType goodButton = new ButtonType("Good");
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "", goodButton);
        alert.setTitle("CNN Digit Recognizer");
        alert.setHeaderText("Result");
        DecimalFormat df = new DecimalFormat("#.##");
        alert.setContentText("The most likely digit is [" + mostPossibleNum + "] with possibility " + df.format(mostPossibleNumPossibility) + "%.\n" +
                "The second most likely digit is [" + secondPossibleNum + "]  with possibility " + df.format(secondPossibleNumPossibility) + "%.");

        // Show the alert and wait for user interaction
        alert.showAndWait();
    }
    // Method to save the drawing as a PNG image
    @Override
    public void saveAsPNG() {
        // Clean the temporary directory and perform image processing on the JavaFX Application thread
        cleanTmp();
        Platform.runLater(() -> {
            // Snapshot the canvas to capture the drawing
            Image image = canvas.snapshot(null, null);
            // Resize the image to a specific dimension
            Image resizedImage = resizeImage(image, 28, 28);
            // Invert the colors of the resized image
            Image invertedImage = invertImage(resizedImage);

            // Create a temporary directory if it doesn't exist
            File tmpDir = new File("tmp");
            if (!tmpDir.exists()) {
                boolean created = tmpDir.mkdir();
                if (!created) {
                    System.out.println("Error: Cannot create tmp dir");
                    return;
                }
            }
            // Save the inverted image as a PNG file in the temporary directory
            saveImage(invertedImage, "./tmp/image.png");
        });
    }

    // Method to recognize the digit in the drawing using the machine learning model
    public synchronized ModelLoader.Prediction recognizeNumber() throws Exception {
        // Save the drawing as a PNG image
        saveAsPNG();
        // Path to the image file used for recognition
        Path imagePath = Paths.get(ModelLoader.IMAGE_PATH);

        // Wait until the image file exists (indicating the image is saved)
        while (!Files.exists(imagePath)) {
            Thread.sleep(100);
        }

        // Perform prediction using the machine learning model
        return model.predict();
    }

    // Method to load the machine learning model
    public synchronized void loadModel() throws Exception {
        // Save a blank drawing as a PNG image
        saveAsPNG();
        // Path to the image file used for loading the model
        Path imagePath = Paths.get(ModelLoader.IMAGE_PATH);

        // Wait until the image file exists (indicating the image is saved)
        while (!Files.exists(imagePath)) {
            Thread.sleep(100);
        }

        // Load the machine learning model
        model.predict();
    }

    // Method to set the progress bar with a timeline animation
    @Override
    public void setProgressBar(ProgressBar pb) {
        // Set an initial progress value
        pb.setProgress(0.3);

        // Create a timeline animation to smoothly transition the progress bar from 0.3 to 1
        Timeline timeline = new Timeline();
        KeyValue keyValue = new KeyValue(pb.progressProperty(), 1);
        KeyFrame keyFrame = new KeyFrame(new Duration(300), keyValue);
        timeline.getKeyFrames().add(keyFrame);

        // Play the timeline animation
        timeline.play();
    }
}
