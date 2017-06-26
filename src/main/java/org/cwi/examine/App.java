package org.cwi.examine;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.cwi.examine.data.csv.NetworkCSVReader;
import org.cwi.examine.model.Network;
import org.cwi.examine.presentation.main.MainSection;

import java.io.IOException;

/**
 * Primary application entry point.
 */
public class App extends Application {

    private static final String TITLE = "eXamineS";
    private static final String CSV_FILE_PATH = "data/";
    private static final String USER_AGENT_STYLESHEET = "UserAgentStylesheet.css";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        // Apply custom, overall style.
        Application.setUserAgentStylesheet(App.class.getResource(USER_AGENT_STYLESHEET).toExternalForm());

        primaryStage.setTitle(TITLE);

        // Load network from standard path and show main section.
        final Network network = new NetworkCSVReader(CSV_FILE_PATH).readNetwork();
        final MainSection mainSection = new MainSection(network);

        final Scene scene = new Scene(mainSection.getView());
        primaryStage.setScene(scene);
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);
        primaryStage.show();
    }

}
