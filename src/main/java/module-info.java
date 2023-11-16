module com.neu.info5100.numberrecognizer {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.swing;
    requires org.tensorflow.core.api;
    requires org.tensorflow.ndarray;
    requires org.tensorflow;



    opens com.neu.info5100.numberrecognizer to javafx.fxml;
    exports com.neu.info5100.numberrecognizer;
}