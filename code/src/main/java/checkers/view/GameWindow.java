package checkers.view;

import checkers.model.GameBoard;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

/**
 * This is the overall window scene for the application. It creates and contains the different elements in the
 * top, bottom, center, and right side of the window, along with linking them to the model.
 *
 * Identify the mutable couplings between the View and the Model: This class and the BoardPane are the only 2 View
 * classes that mutate the Model, and all mutations go first to the GameBoard. There is coupling in other ways
 * from the View to the Model, but they are accessor methods only.
 *
 * Also note that while this represents the game window, it *contains* the Scene that JavaFX needs, and does not
 * inherit from Scene. This is true for all JavaFX components in this application, they are contained, not extended.
 */
public class GameWindow {
    private final BoardPane boardPane;
    private final Scene scene;
    private MenuBar menuBar;
    private VBox sideButtonBar;

    private final GameBoard model;

    public GameWindow(GameBoard model) {
        this.model = model;

        BorderPane pane = new BorderPane();
        this.scene = new Scene(pane);

        this.boardPane = new BoardPane(model);
        StatusBarPane statusBar = new StatusBarPane(model);
        buildMenu();
        buildSideButtons();
        buildKeyListeners();

        pane.setCenter(boardPane.getPane());
        pane.setTop(menuBar);
        pane.setRight(sideButtonBar);
        pane.setBottom(statusBar.getStatusBar());

    }

    private void buildKeyListeners() {
        // This allows keyboard input. Note that the scene is used, so any time
        // the window is in focus the keyboard input will be registered.
        // More often, keyboard input is more closely linked to a specific
        // node that must have focus, i.e. the Enter key in a text input to submit
        // a form.

        scene.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
            if (event.isControlDown() && event.getCode() == KeyCode.N) {
                newGameAction();
            }
            if (event.isControlDown() && event.getCode() == KeyCode.S) {
                serialiseAction();
            }
            if (event.isControlDown() && event.getCode() == KeyCode.L) {
                deserialiseAction();
            }
        });
    }

    private void buildSideButtons() {
        Button newGameBtn = new Button("New Game");
        newGameBtn.setOnAction((event) -> newGameAction());

        Button serialiseBtn = new Button("Save Game");
        serialiseBtn.setOnAction((event) -> serialiseAction());

        Button deserialiseBtn = new Button("Load Game");
        deserialiseBtn.setOnAction((event) -> deserialiseAction());

        this.sideButtonBar = new VBox(newGameBtn, serialiseBtn, deserialiseBtn);
        sideButtonBar.setSpacing(10);
    }

    private void buildMenu() {
        Menu actionMenu = new Menu("Actions");

        MenuItem newGameItm = new MenuItem("New Game");
        newGameItm.setOnAction((event)-> newGameAction());

        MenuItem serialiseItm = new MenuItem("Save Game");
        serialiseItm.setOnAction((event)-> serialiseAction());

        MenuItem deserialiseItm = new MenuItem("Load Game");
        deserialiseItm.setOnAction((event)-> deserialiseAction());

        actionMenu.getItems().addAll(newGameItm, serialiseItm, deserialiseItm);

        this.menuBar = new MenuBar();
        menuBar.getMenus().add(actionMenu);
    }

    private void newGameAction() {
        // Note the separation here between newGameAction and doNewGame. This allows
        // for the validation aspects to be separated from the operation itself.

        if (null == model.getCurrentTurn()) { // no current game
            doNewGame();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("New Game Warning");
        alert.setHeaderText("Starting a new game now will lose all current progress.");
        alert.setContentText("Are you ok with this?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK){
         doNewGame();
        }
    }

    private void serialiseAction() {
        // Serialisation is a way of turning some data into a communicable form.
        // In Java it has a library to support it, but here we are just manually converting the field
        // we know we need into a string (in the model). We can then use that string in reverse to get that state back

        if (null == model.getCurrentTurn()) { // no current game
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Save Game Error");
            alert.setHeaderText("There is no game to save!");

            alert.showAndWait();
            return;
        }

        TextInputDialog textInput = new TextInputDialog();
        textInput.setTitle("Save Game");
        textInput.setHeaderText("Please input the game name to record");
        Optional<String> inputResult = textInput.showAndWait();

        if(inputResult.isPresent()) {
            String gameRecordName = inputResult.get();
            if (gameRecordName.length() == 0) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Save Error");
                alert.setHeaderText("Please input a game name to record!");
                alert.showAndWait();
                return;
            }

            boolean gameRecordExist = !model.saveGameRecord(gameRecordName);

            if (gameRecordExist) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Game Record Name Already Exists");
                alert.setHeaderText("Saving this record will overwrite the last one.");
                alert.setContentText("Are you ok with this?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    model.updateGameRecord(gameRecordName);
                }
            }
        }

    }

    private void deserialiseAction() {
        // Here we take an existing serialisation string and feed it back into the model to retrieve that state.
        // We don't do any validation here, as that would leak model knowledge into the view.

        List<String> recordList = model.getAvailableRecords();

        if(recordList.size() == 0){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("No Game Records saved!");
            alert.setHeaderText("User has not saved any game");
            alert.showAndWait();
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(null,recordList);
        dialog.setTitle("Load Game");
        dialog.setHeaderText("Select a game record to load");
        dialog.setContentText("Please select:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String recordName = result.get();
            try {
                model.loadGameRecord(recordName);
            } catch (IllegalArgumentException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Load Game Error");
                alert.setHeaderText(e.getMessage());
                alert.showAndWait();
                return;
            }

            boardPane.updateBoard();
        }

    }

    private void doNewGame() {
        // Here we have an action that we know would likely mutate the state of the model, and so the view should
        // update. Unlike the StatusBarPane that uses the observer pattern to do this, here we can just trigger it
        // because we know the model will mutate as a result of our call to it.
        // Generally speaking the observer pattern is superior - I would recommend using it instead of
        // doing it this way.

        model.newGame();
        boardPane.updateBoard();
    }

    public Scene getScene() {
        return this.scene;
    }
}
