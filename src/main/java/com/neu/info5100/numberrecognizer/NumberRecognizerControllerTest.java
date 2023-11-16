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
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;


import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

public class NumberRecognizerControllerTest {

    public static void initialAlert() {
        ButtonType okButton = new ButtonType("Ok");
        Alert alert = new Alert(Alert.AlertType.INFORMATION,"",okButton);
        alert.setTitle("CNN Digit Recognizer");
        alert.setHeaderText("Draw a digit from [0-9] to be recognized.");
        //alert.setContentText("");
        alert.showAndWait();
    }

    interface ImageProcessor {
        Image process(Image image);
    }

    class InvertImageProcessor implements ImageProcessor {
        @Override
        public Image process(Image image) {
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
    }

    class ImageUtils {
        static Image resizeImage(Image image, int width, int height) {
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
            return imageView.snapshot(null, null);
        }

        static void saveImage(Image image, String path) {
            File file = new File(path);
            try {
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
                ImageIO.write(bufferedImage, "png", file);
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    class TensorFlowModel {
        static Prediction predict() throws Exception {
            // Implement TensorFlow prediction logic
            return new Prediction();
        }
    }

    public class NumberRecognizerController {

        private double lastX, lastY;

        @FXML
        private Canvas canvas;
        @FXML
        private GraphicsContext gc;
        @FXML
        private ProgressBar progressBar;

        private ImageProcessor imageProcessor;

        public NumberRecognizerController(ImageProcessor imageProcessor) {
            this.imageProcessor = imageProcessor;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> cleanTmp()));
        }

        private void cleanTmp() {
            String tmpPath = "./tmp";
            File tmp = new File(tmpPath);
            if (tmp.exists() && tmp.isDirectory()) {
                File[] files = tmp.listFiles();
                if (files != null) {
                    for (File f : files) {
                        f.delete();
                    }
                }
            }
        }

        @FXML
        protected void initialize() {
            gc = canvas.getGraphicsContext2D();
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1.0);

            canvas.setOnMousePressed(this::onMousePressed);
            canvas.setOnMouseDragged(this::onMouseDragged);

            Task<Void> loadModelTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    TensorFlowModel.predict();
                    return null;
                }
            };
            new Thread(loadModelTask).start();
        }



        @FXML
        protected void onMousePressed(MouseEvent event) {
            lastX = event.getX();
            lastY = event.getY();
            gc.beginPath();
            gc.moveTo(lastX, lastY);
            gc.stroke();
        }

        @FXML
        protected void onMouseDragged(MouseEvent event) {
            double newX = event.getX();
            double newY = event.getY();
            gc.fillOval(newX, newY, 15, 15);
            lastY = newY;
            lastX = newX;
        }

        @FXML
        protected void cleanCanvas() {
            gc.clearRect(0.0, 0.0, canvas.getWidth(), canvas.getHeight());
        }

        @FXML
        protected void submitBtnEvent() {
            progressBar.setVisible(true);
            Thread predictionThread = new Thread(() -> {
                try {
                    Prediction prediction = TensorFlowModel.predict();
                    Platform.runLater(() -> {
                        progressBar.setVisible(false);
                        showResult(prediction);
                        cleanCanvas();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            setProgressBar(progressBar);
            predictionThread.start();
        }

        private void showResult(Prediction prediction) {
            int mostPossibleNum = prediction.getMostPossibleNum();
            int secondPossibleNum = prediction.getSecondPossibleNum();
            float mostPossibleNumPossibility = prediction.getMostPossibleNumPossibility();
            float secondPossibleNumPossibility = prediction.getSecondPossibleNumPossibility();

            ButtonType goodButton = new ButtonType("Good");
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "", goodButton);
            alert.setTitle("CNN Digit Recognizer");
            alert.setHeaderText("Result");
            DecimalFormat df = new DecimalFormat("#.##");
            alert.setContentText("The most likely digit is [" + mostPossibleNum + "] with possibility " + df.format(mostPossibleNumPossibility) + "%.\n" +
                    "The second most likely digit is [" + secondPossibleNum + "]  with possibility " + df.format(secondPossibleNumPossibility) + "%.");

            alert.showAndWait();
        }

        private void setProgressBar(ProgressBar pb) {
            pb.setProgress(0.3);
            Timeline timeline = new Timeline();
            KeyValue keyValue = new KeyValue(pb.progressProperty(), 1);
            KeyFrame keyFrame = new KeyFrame(new Duration(100), keyValue);
            timeline.getKeyFrames().add(keyFrame);

            timeline.play();
        }

        @FXML
        protected void saveAsPNG() {
            Platform.runLater(() -> {
                Image image = canvas.snapshot(null, null);
                Image processedImage = imageProcessor.process(image);
                ImageUtils.saveImage(processedImage, "./tmp/image.png");
            });
        }
    }

}
