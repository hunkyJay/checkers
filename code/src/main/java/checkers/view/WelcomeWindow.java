package checkers.view;

import checkers.model.GameBoard;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;


public class WelcomeWindow {
    private final Scene scene;
    private final GameBoard model;
    private Label gameLabel;
    private final TextField userNameField;

    public WelcomeWindow(GameBoard model) {
        this.model = model;
        gameLabel = new Label("Checkers");
        gameLabel.setTooltip(new Tooltip("Please enter your username to start the game"));
        gameLabel.setFont(Font.font("Verdana",20));
        gameLabel.setLayoutX(120);
        gameLabel.setLayoutY(30);
        gameLabel.setTextFill(Color.RED);

        Label userLabel = new Label("Username");
        userLabel.setFont(new Font(15));
        userLabel.setTooltip(new Tooltip("Please enter your username to start the game"));
        userLabel.setLayoutX(15);
        userLabel.setLayoutY(150);

        userNameField = new TextField();
        userNameField.setPromptText("Enter your username");
        userNameField.setLayoutX(100);
        userNameField.setLayoutY(150);

        Button enterButton =  new Button("Enter Game");
        enterButton.setOnAction((event -> startGame()));
        enterButton.setPrefHeight(30);
        enterButton.setPrefWidth(150);
        enterButton.setLayoutX(100);
        enterButton.setLayoutY(230);


        AnchorPane pane = new AnchorPane();
        pane.getChildren().addAll(gameLabel, userLabel, userNameField, enterButton);

        this.scene = new Scene(pane, 350, 400);
    }

    public void startGame(){
        String username = userNameField.getText();
        if(username == null || username.length() == 0){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Login Error");
            alert.setHeaderText("Invalid username, input should not be empty");
            alert.setContentText("Please enter a username.");
            alert.showAndWait();
        }else {
            model.enterUser(username);
            loadGameWindow();
        }
    }

    public void loadGameWindow(){
        Stage primaryStage = (Stage)gameLabel.getScene().getWindow();

        GameWindow view = new GameWindow(model);
        Stage stage = new Stage();
        stage.setScene(view.getScene());
        stage.setTitle("Checkers  " + "User: " + model.getCurrentUser().getName());
        stage.show();

        primaryStage.close();
    }

    public Scene getScene(){
        return this.scene;
    }
}
