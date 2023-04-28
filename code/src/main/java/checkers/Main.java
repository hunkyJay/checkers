package checkers;

import checkers.model.GameBoard;
import checkers.view.GameWindow;
import checkers.view.WelcomeWindow;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    private final GameBoard model = new GameBoard();
    //private final GameWindow view = new GameWindow(model);
    private final WelcomeWindow view = new WelcomeWindow(model);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setScene(view.getScene());
        primaryStage.setTitle("Welcome to Checkers");
        primaryStage.show();
    }
}
