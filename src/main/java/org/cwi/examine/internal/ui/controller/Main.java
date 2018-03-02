package org.cwi.examine.internal.ui.controller;
	
import java.io.File;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.application.Application;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;


public class Main extends Application {
	Stage primaryStage;
	

	

	
	
	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		mainWindow();
		System.out.println("test main");
	
	}
	
	public void mainWindow() {
		
		try {
		
			FXMLLoader loader = new FXMLLoader(Main.class.getResource("/view/view.fxml"));
		
			AnchorPane pane = loader.load();
		
			primaryStage.setMinWidth(400.00);
		
			primaryStage.setMinHeight(325.00);
		
			Scene scene = new Scene(pane);
		
			MainController maincontroller = loader.getController(); 
			
			maincontroller.setMain(this);
			
			primaryStage.setScene(scene);
		
			primaryStage.show();
		
		
		}
		catch(IOException e) 
		{
			e.printStackTrace();
		}
	}  /**/
	 
	public static void main(String[] args) {
		launch(args);
	}
}
