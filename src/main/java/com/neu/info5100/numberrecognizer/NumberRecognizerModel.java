/*
NEU INFO 5100, Final Project - Number Recognizer
 Author:
     Name: Xinzhe Yuan, NUID:002641096 , Email: yuan.xinz@northeastern.edu
     Name: Jia Xu, NUID:002222689， Email: jia.xu5@northeastern.edu
 Date: 5 Dec 2023
 Version: 1.0

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

 */


package com.neu.info5100.numberrecognizer;

import javafx.application.Platform;
import org.tensorflow.*;
import org.tensorflow.op.Ops;
import org.tensorflow.op.core.Constant;
import org.tensorflow.op.core.Reshape;
import org.tensorflow.op.image.DecodePng;
import org.tensorflow.op.io.ReadFile;
import org.tensorflow.op.math.Div;
import org.tensorflow.types.TFloat32;
import org.tensorflow.types.TString;
import org.tensorflow.types.TUint8;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.tensorflow.SavedModelBundle.DEFAULT_TAG;

//Define interface
interface ModelLoader{
    String SAVED_MODEL_PATH = "./cnn/mnist_cnn_model";
    String IMAGE_PATH = "./tmp/image.png";

    void loadModel();
    Prediction predict() throws Exception;

    interface PredictionInterface{
        int getMostPossibleNum();
        float getMostPossibleNumPossibility();
        int getSecondPossibleNum();
        float getSecondPossibleNumPossibility();
    }

    //Implement base class
    abstract class PredictionBase implements PredictionInterface{
        protected int mostPossibleNum;
        protected float mostPossibleNumPossibility;
        protected int secondPossibleNum;
        protected float secondPossibleNumPossibility;





        public int getMostPossibleNum() {
            return mostPossibleNum;
        }

        public float getMostPossibleNumPossibility() {
            return mostPossibleNumPossibility;
        }
        public int getSecondPossibleNum() {
            return secondPossibleNum;
        }
        public float getSecondPossibleNumPossibility() {
            return secondPossibleNumPossibility;
        }

    }

    class Prediction extends PredictionBase{
        public Prediction(int mostPossibleNum, float mostPossibleNumPossibility, int secondPossibleNum, float secondPossibleNumPossibility) {
            this.mostPossibleNum = mostPossibleNum;
            this.mostPossibleNumPossibility = mostPossibleNumPossibility;
            this.secondPossibleNum = secondPossibleNum;
            this.secondPossibleNumPossibility = secondPossibleNumPossibility;
        }
    }



}
abstract class NumberRecognizerModelBase implements ModelLoader{
    SavedModelBundle model;
    public abstract void loadModel();
    public abstract Prediction predict() throws Exception;


}
public class NumberRecognizerModel extends NumberRecognizerModelBase{
    public NumberRecognizerModel() {
        loadModel();
    }

    @Override
    public void loadModel(){
        // check if the path of CNN model exists
        File folder = new File(SAVED_MODEL_PATH);
        if(!folder.exists()) {
            Platform.runLater(()->{
                String errorMSG = "Error: No such CNN folder.";
                NumberRecognizerController.errorAlert(errorMSG);
            });
            // check if the CNN model exists
        }else if(folder.list().length <1){
            Platform.runLater(()->{
                String errorMSG = "Error: No such CNN models.";
                NumberRecognizerController.errorAlert(errorMSG);
            });
        }

        // load pre-trained CNN model
        this.model =  SavedModelBundle.load(SAVED_MODEL_PATH,DEFAULT_TAG);
    }
    @Override
    public synchronized Prediction predict() throws Exception{
        // variables to store prediction results
        float max = Float.MIN_VALUE;//the chance of the most possible number in predictions
        int maxIndex = -1;//return value-predicted digit
        float secondMax = Float.MIN_VALUE; //the chance of the second most possible number in predictions
        int secondMaxIndex = -1;//return value - second predicted digit

//      Initialization
        try(Graph g = new Graph(); Session s =new Session(g)){
            Ops tf = Ops.create(g);
            //Read image file
            Constant<TString> fileName = tf.constant(IMAGE_PATH);
            ReadFile readFile = tf.io.readFile(fileName);

            Session.Runner runner = s.runner();
            // Define PNG color channel as 1, as gray value image
            DecodePng.Options options = DecodePng.channels(1L);
            //Decode data from PNG to Tensor
            DecodePng<TUint8> decodePng = tf.image.decodePng(readFile.contents(), new DecodePng.Options[]{options});
            // Normalization, scales tensor range from [0,255] to [0,1].
            Div<TFloat32> normalizedTensor = tf.math.div(tf.dtypes.cast(decodePng, TFloat32.class), tf.constant(255.0f));
            //Fetch PNG image from file
            Reshape<TFloat32> reshape = tf.reshape(normalizedTensor, tf.array(-1,28,28,1));
            try(TFloat32 reshapeTensor = (TFloat32) runner.fetch(reshape).run().get(0)){

                Map<String, Tensor> feedDict = new HashMap<>();

                //Define input tensor for the CNN model named 'conv2d_input'
                feedDict.put("conv2d_input",reshapeTensor);
                try {
                    Result outputTensorMap = this.model.function("serving_default").call(feedDict);
                    try (TFloat32  denseClass= (TFloat32) outputTensorMap.get("dense_1").get()){
                        //Find the best and second-best digits.
                        for (int i = 0; i < 10; i++) {
                            float value = denseClass.getFloat(0, i);
                            if(value > max){
                                secondMax = max;
                                secondMaxIndex = maxIndex;
                                max = value;
                                maxIndex = i;
                            } else if (value > secondMax && value != max) {
                                secondMax = value;
                                secondMaxIndex = i;
                            }
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        return new Prediction(maxIndex,max*100,secondMaxIndex,secondMax*100);
    }




}



