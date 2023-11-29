///*
//NEU INFO 5100, Final Project - Number Recognizer
//Author:
//    Name: Xinzhe Yuan, NUID:
//    Name: Jia Xu, NUID:
//Date: 13 Nov 2023
//Version: 0.1
//
//References:
//[1]https://github.com/tensorflow/java/blob/master/tensorflow-core/tensorflow-core-api/src/test/java/org/tensorflow/SavedModelBundleTest.java#L106
//[2]https://towardsdatascience.com/running-savedmodel-in-java-1351e7bdf0a4
//[3]https://discuss.tensorflow.org/t/java-create-tensor-from-bufferedimage/13782
//[4]https://github.com/tensorflow/java-models/blob/master/tensorflow-examples/src/main/java/org/tensorflow/model/examples/cnn/fastrcnn/FasterRcnnInception.java
//[5]https://github.com/loretoparisi/tensorflow-java/blob/master/LabelImage.java
//[6]https://github.com/tensorflow/java
//[7]https://stackoverflow.com/questions/68935644/is-there-any-simpler-way-to-convert-a-tensor-to-java-array-in-java-tf-api
//[8]https://discourse.mozilla.org/t/retrieving-tensor-names-from-a-pre-trained-model/24574/8
//[9]https://stackoverflow.com/questions/43521439/tensorflow-model-import-to-java
// */
//
//
//package com.neu.info5100.numberrecognizer;
//
//
//import javafx.application.Platform;
//import org.tensorflow.SavedModelBundle;
//
//import java.io.File;
//
//import static org.tensorflow.SavedModelBundle.DEFAULT_TAG;
////Define Model Loader interface
//interface ModelLoader{
//     SavedModelBundle loadModel();
//}
////Implement Model Class
////abstract class Model implements ModelLoader{
////
////   protected SavedModelBundle model;
////
////    public void setModel(SavedModelBundle model) {
////        this.model = model;
////    }
////    public  SavedModelBundle getModel() {
////
////        return model;
////    }
////
////}
//public class TensorFlowModelLoader implements ModelLoader{
//    private static final String SAVED_MODEL_PATH = "./cnn/mnist_cnn_model";
//
//    @Override
//    public SavedModelBundle loadModel(){
//        // check if the path of CNN model exists
//        File folder = new File(SAVED_MODEL_PATH);
//        if(!folder.exists()) {
//            Platform.runLater(()->{
//                String errorMSG = "Error: No such CNN folder.";
//                NumberRecognizerController.errorAlert(errorMSG);
//            });
//            // check if the CNN model exists
//        }else if(folder.list().length <1){
//            Platform.runLater(()->{
//                String errorMSG = "Error: No such CNN models.";
//                NumberRecognizerController.errorAlert(errorMSG);
//            });
//        }
//
//        // load pre-trained CNN model
//        SavedModelBundle model = SavedModelBundle.load(SAVED_MODEL_PATH,DEFAULT_TAG);
//        return model;
//    }
//
//
//
//}
