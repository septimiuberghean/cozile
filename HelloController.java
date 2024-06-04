package com.example.queueservice;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;

public class HelloController implements Initializable {
    @FXML
    public TextArea logTextArea;
    @FXML
    private TextField clientsTextField;
    @FXML
    private TextField queuesTextField;
    @FXML
    private TextField minArrivalTextField;
    @FXML
    private TextField maxArrivalTextField;
    @FXML
    private TextField minServiceTextField;
    @FXML
    private TextField maxServiceTextField;
    @FXML
    private TextField simulationTextField;
    @FXML
    private ImageView queueImageView;
    @FXML
    private Button generateButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        File lit = new File("Q/queue.jpg");
        Image litImage = new Image(lit.toURI().toString());
        this.queueImageView.setImage(litImage);
    }

    @FXML
    public void generateButtonOnAction(ActionEvent actionEvent) {
        int numClients = Integer.parseInt(clientsTextField.getText());
        int numQueues = Integer.parseInt(queuesTextField.getText());
        int minArrivalTime = Integer.parseInt(minArrivalTextField.getText());
        int maxArrivalTime = Integer.parseInt(maxArrivalTextField.getText());
        int minServiceTime = Integer.parseInt(minServiceTextField.getText());
        int maxServiceTime = Integer.parseInt(maxServiceTextField.getText());
        int simulationMaxTime = Integer.parseInt(simulationTextField.getText());

        Simulation simulation = new Simulation(numClients, numQueues, simulationMaxTime,
                minArrivalTime, maxArrivalTime, minServiceTime, maxServiceTime, logTextArea);

        Thread simulationThread = new Thread(simulation);
        simulationThread.start();
    }

    public void sizeButtonOnAction(ActionEvent actionEvent) {
    }
}