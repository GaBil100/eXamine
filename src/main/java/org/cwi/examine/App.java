package org.cwi.examine;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.cwi.examine.data.csv.DataSet;
import org.cwi.examine.presentation.main.MainSection;

import java.io.IOException;

/**
 * Primary application entry point.
 */
public class App extends Application {

    private static final String TITLE = "eXamineS";

    private static final String USER_AGENT_STYLESHEET = "UserAgentStylesheet.css";

    private final DataSet dataSet = new DataSet();
    private MainSection mainSection = new MainSection(dataSet);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        // Apply custom, overall style.
        Application.setUserAgentStylesheet(App.class.getResource(USER_AGENT_STYLESHEET).toExternalForm());

        primaryStage.setTitle(TITLE);

        final Scene scene = new Scene(mainSection.getView());
        primaryStage.setScene(scene);
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);
        primaryStage.show();

        dataSet.load(); // Load from filesystem.
    }

    @Override
    public void stop() throws Exception {
        mainSection.exit();
    }

}
