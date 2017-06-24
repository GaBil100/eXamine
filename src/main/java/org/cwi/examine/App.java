package org.cwi.examine;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.cwi.examine.data.csv.DataSet;
import org.cwi.examine.model.Model;
import org.cwi.examine.presentation.MainPane;

import java.io.IOException;

/**
 * Primary application entry point.
 */
public class App extends Application {

    private static final String TITLE = "eXamineS";

    private static final String USER_AGENT_STYLESHEET = "UserAgentStylesheet.css";

    private final DataSet dataSet = new DataSet();
    private final Model model = new Model(dataSet);
    private MainPane mainPane;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        // Apply custom, overall style.
        Application.setUserAgentStylesheet(App.class.getResource(USER_AGENT_STYLESHEET).toExternalForm());

        primaryStage.setTitle(TITLE);

        mainPane = new MainPane(model);

        final Scene scene = new Scene(mainPane);
        primaryStage.setScene(scene);
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);
        primaryStage.show();

        dataSet.load(); // Load from filesystem.
    }

    @Override
    public void stop() throws Exception {
        mainPane.getVisualization().stop();
    }

}
