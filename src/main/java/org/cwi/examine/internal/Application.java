package org.cwi.examine.internal;

import org.cwi.examine.internal.data.DataSet;
import org.cwi.examine.internal.visualization.Visualization;
import org.cwi.examine.internal.model.Model;
import org.cwi.examine.internal.molepan.dataread.DataRead;

import org.cwi.examine.internal.ui.controller.Main;

import org.cwi.examine.internal.Option;
import java.io.IOException;

/**
 * Application entry point.
 */
public class Application {

    public static void main(String[] args) throws IOException {
    
    
    	Option option = new Option();
    	option.setScel(args[0]);
    	System.out.println( option.getScel() );
    	
    	
    	
    	
    	DataRead dataread = new DataRead();
        final DataSet dataSet = new DataSet();
        final Model model = new Model(dataSet);
        final Visualization visualization = new Visualization(model);
        dataSet.load(); // Load from filesystem.
    }
}
