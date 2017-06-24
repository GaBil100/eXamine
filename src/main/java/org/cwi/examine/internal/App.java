package org.cwi.examine.internal;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.cwi.examine.internal.data.DataSet;
import org.cwi.examine.internal.model.Model;
import org.cwi.examine.internal.presentation.MainPane;

import java.io.IOException;

public class App extends Application {

    public static final String TITLE = "eXamineS";

    final DataSet dataSet = new DataSet();
    final Model model = new Model(dataSet);
    MainPane mainPane;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

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
