package org.cwi.examine.internal;

import org.cwi.examine.internal.data.DataSet;
import org.cwi.examine.internal.visualization.Visualization;
import org.cwi.examine.internal.model.Model;
import org.cwi.examine.internal.molepan.dataread.DataRead;
import org.cwi.examine.internal.ui.controller.Main;


import java.io.IOException;


public class Option {
	private static boolean scel;

	public static boolean getScel(){
		return scel;
	}
	
	public void setScel (String a){

	if(a.contains("On")){

		scel = true;
	}
		
	else if(scel != true){
		scel = false;
	}
	
	}
	
	


}
