/*
NEU INFO 5100, Final Project - Number Recognizer
Author:
    Name: Xinzhe Yuan, NUID:
    Name: Jia Xu, NUID:
Date: 13 Nov 2023
Version: 0.1

References:
[1]https://github.com/tensorflow/java/blob/master/tensorflow-core/tensorflow-core-api/src/test/java/org/tensorflow/SavedModelBundleTest.java#L106
[2]https://towardsdatascience.com/running-savedmodel-in-java-1351e7bdf0a4
[3]https://discuss.tensorflow.org/t/java-create-tensor-from-bufferedimage/13782
[4]https://github.com/tensorflow/java-models/blob/master/tensorflow-examples/src/main/java/org/tensorflow/model/examples/cnn/fastrcnn/FasterRcnnInception.java
[5]https://github.com/loretoparisi/tensorflow-java/blob/master/LabelImage.java
[6]https://github.com/tensorflow/java
[7]https://stackoverflow.com/questions/68935644/is-there-any-simpler-way-to-convert-a-tensor-to-java-array-in-java-tf-api
[8]https://discourse.mozilla.org/t/retrieving-tensor-names-from-a-pre-trained-model/24574/8
[9]https://stackoverflow.com/questions/43521439/tensorflow-model-import-to-java
 */


package com.neu.info5100.numberrecognizer;

//import org.tensorflow.*;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
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


public class TensorFlowModelLoader {
    private static final String SAVED_MODEL_PATH = "./cnn/mnist_cnn_model";
    private static final String IMAGE_PATH = "./tmp/image.png";

//    private static int argmax(float[] array) {
//        int argmax = 0;
//        for (int i = 1; i < array.length; i++) {
//            if (array[i] > array[argmax]) {
//                argmax = i;
//            }
//        }
//        return argmax;
//    }
    private static void errorAlert(String errorMSG){
        ButtonType goodButton = new ButtonType("Good");
        Alert alert = new Alert(Alert.AlertType.ERROR,"",goodButton);
        alert.setTitle("CNN Digit Recognizer");
        alert.setHeaderText("Error!");
        alert.setContentText(errorMSG);
        alert.showAndWait();

    }


    public static Prediction predict() throws Exception{
        // variables to store prediction results
        float max = Float.MIN_VALUE;//the chance of the most possible number in predictions
        int maxIndex = -1;//return value-predicted digit
        float secondMax = Float.MIN_VALUE; //the chance of the second most possible number in predictions
        int secondMaxIndex = -1;//return value - second predicted digit

        // check if the path of CNN model exists
        File folder = new File(SAVED_MODEL_PATH);
        if(!folder.exists()) {
            Platform.runLater(()->{
                String errorMSG = "Error: No such CNN folder.";
                errorAlert(errorMSG);
            });
            // check if the CNN model exists
        }else if(folder.list().length <1){
            Platform.runLater(()->{
                String errorMSG = "Error: No such CNN models.";
                errorAlert(errorMSG);
            });
        }

        // load pre-trained CNN model
        SavedModelBundle model = SavedModelBundle.load(SAVED_MODEL_PATH,DEFAULT_TAG);

        //for debug
//        SignatureDef signatureDef = model.metaGraphDef().getSignatureDefOrThrow("serving_default");
//        TensorInfo outputTensorInfo = signatureDef.getOutputsMap().get("dense_1");
//        long[] outputShape = outputTensorInfo.getTensorShape().getDimList().stream()
//                .mapToLong(TensorShapeProto.Dim::getSize)
//                .toArray();
//        System.out.println(signatureDef);
//        System.out.println("Output Tensor Shape: " + Arrays.toString(outputShape));

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
            //Shape imageShape = Shape.of(-1,28,28,1);
            Reshape<TFloat32> reshape = tf.reshape(normalizedTensor, tf.array(-1,28,28,1));
            try(TFloat32 reshapeTensor = (TFloat32) runner.fetch(reshape).run().get(0)){
                //debug
//                System.out.println("Reshaped Tensor Shape: " + Arrays.toString(reshapeTensor.shape().asArray()));
//                System.out.println("Reshaped Tensor Data Type: " + reshapeTensor.dataType());

                Map<String, Tensor> feedDict = new HashMap<>();
                //TFloat32 castedTensor = tf.dtypes.cast(reshapeTensor, TFloat32.DTYPE);
//                for (Map.Entry<String, TensorInfo> entry : signatureDef.getInputsMap().entrySet()) {
//                    System.out.println("Input Tensor Name: " + entry.getKey());
//                }
                //Define input tensor for the CNN model named 'conv2d_input'
                feedDict.put("conv2d_input",reshapeTensor);
                try {
                    Result outputTensorMap = model.function("serving_default").call(feedDict);
                    //Tensor outputTensor = model.session().runner().fetch("dense_1").feed("conv2d_input",reshapeTensor).run().get(0);
                    //System.out.println(outputTensor.asRawTensor());
                    try (TFloat32  denseClass= (TFloat32) outputTensorMap.get("dense_1").get()){
                        //float[][][] res = new float[][][]{StdArrays.array2dCopyOf(denseClass)};
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
//debug
//                        System.out.println(String.format("The Most Possible Number should be [%d] with %.2f%% Possibility", maxIndex, max * 100));
//                        System.out.println(String.format("The Second Possible Number should be [%d] with %.2f%% Possibility", secondMaxIndex, secondMax * 100));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

//                try (TFloat32 predictedProbabilities = (TFloat32) outputTensorMap.get("output_probabilities")) {
//                    float[] probabilities = StdArrays.array1dCopyOf(predictedProbabilities);
//                    int predictedDigit = argmax(probabilities);
//                }
            }
        }


//        BufferedImage image = ImageIO.read(new File(IMAGE_PATH));
//        int width = image.getWidth();
//        int height = image.getHeight();
//        DecodePng.Options options = DecodePng.channels(1L);
//        DecodePng decodePng = tf.image.dec
//
//
//        Shape shape = Shape.of(1,height,width,1);
//        ByteNdArray pixMatrix = NdArrays.ofBytes(shape);
//        //TUint8 inputTensor = TUint8.tensorOf(pixMatrix);
//        Tensor inputTensor = TUint8.tensorOf(pixMatrix);
////        Map<String, Tensor> feedDict = new HashMap<>();
////        feedDict.put("inputTensor",inputTensor);
//        System.out.println(model.function(DEFAULT_KEY).call(inputTensor));

        return new Prediction(maxIndex,max*100,secondMaxIndex,secondMax*100);
    }


}
