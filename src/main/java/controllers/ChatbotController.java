package controllers;



import javafx.fxml.FXML;

import javafx.scene.control.*;

import javafx.scene.layout.*;

import javafx.geometry.Pos;

import javafx.animation.FadeTransition;

import javafx.util.Duration;

import services.ChatbotService;

import javafx.stage.FileChooser;

import javafx.stage.Stage;

import javafx.scene.image.Image;

import javafx.scene.image.ImageView;

import java.io.File;



public class ChatbotController {



    @FXML private VBox chatContainer;

    @FXML private TextField txtMessage;

    @FXML private ScrollPane scrollPane;

    @FXML private BorderPane rootPane; // lié via fx:id="rootPane"

    @FXML private ToggleButton darkModeToggle;



    private final ChatbotService chatbotService = new ChatbotService();



    @FXML

    public void initialize() {

        addMessage("Bonjour 👋 Je suis votre assistant Smart Farm.", false);

        rootPane.getStyleClass().add("light-mode");

    }



    @FXML

    void sendMessage() {

        String msg = txtMessage.getText().trim();

        if (msg.isEmpty()) return;



        addMessage(msg, true);

        String reply = chatbotService.sendMessage(msg);

        addMessage(reply, false);

        txtMessage.clear();

    }



    private void addMessage(String text, boolean isUser) {

        Label label = new Label(text);

        label.setWrapText(true);

        label.getStyleClass().add(isUser ? "user-bubble" : "bot-bubble");



        HBox box = new HBox(label);

        box.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);



        chatContainer.getChildren().add(box);



        FadeTransition fade = new FadeTransition(Duration.millis(250), box);

        fade.setFromValue(0);

        fade.setToValue(1);

        fade.play();



        scrollPane.setVvalue(1.0);

    }



    @FXML

    void toggleDarkMode() {

        if (darkModeToggle.isSelected()) {

            rootPane.getStyleClass().remove("light-mode");

            rootPane.getStyleClass().add("dark-mode");

        } else {

            rootPane.getStyleClass().remove("dark-mode");

            rootPane.getStyleClass().add("light-mode");

        }

    }



    @FXML

    void uploadImage() {

        FileChooser fc = new FileChooser();

        fc.getExtensionFilters().add(

                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")

        );

        File file = fc.showOpenDialog(chatContainer.getScene().getWindow());

        if (file != null) {

            Image img = new Image(file.toURI().toString(), 200, 200, true, true);

            ImageView iv = new ImageView(img);

            HBox box = new HBox(iv);

            box.setAlignment(Pos.CENTER_RIGHT);

            chatContainer.getChildren().add(box);

            scrollPane.setVvalue(1.0);

        }

    }



    @FXML

    void startVoiceInput() {

        txtMessage.setText("🎙️ Message dicté par la voix...");

    }

}