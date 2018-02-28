package org.cwi.examine.internal.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class MainController {
	
	//VIEW Window
	@FXML 
	private Button start_eXamole;
	private Button choose_button;
	private Button create_button;
	

	private CheckBox show_c;
	private CheckBox show_h;
	
	private Main main;
	
	public void setMain(Main main) {
		this.main = main;
		
	}
	
	@FXML
	public void handleButtonChoose() {
		
	
		System.out.println("test button_choose");
		//label.setText("H Off");
		//if(hydrogen == true) {
		// start_eXamole.setText("Remove Hydrogen");
	
	}
	
	@FXML
	public void handleButtonStart() {
		
	
		System.out.println("test button_start");
		//label.setText("H Off");
		//if(hydrogen == true) {
		// start_eXamole.setText("Remove Hydrogen");
	
	}
	
	@FXML
	public void handleButtonSkeletal() {
		
	
		System.out.println("test button_skeletal");
		//label.setText("H Off");
		//if(hydrogen == true) {
		// start_eXamole.setText("Remove Hydrogen");
	
	}

}
