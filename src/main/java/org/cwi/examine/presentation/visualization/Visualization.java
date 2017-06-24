package org.cwi.examine.presentation.visualization;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.cwi.examine.graphics.Application;
import org.cwi.examine.graphics.PVector;
import org.cwi.examine.graphics.draw.Layout;
import org.cwi.examine.graphics.draw.Representation;
import org.cwi.examine.model.Network;
import org.cwi.examine.model.NetworkAnnotation;
import org.cwi.examine.model.NetworkCategory;
import org.cwi.examine.presentation.visualization.overview.Overview;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.cwi.examine.graphics.StaticGraphics.*;

// Visualization module.
public class Visualization extends Application {

    private ObjectProperty<Network> networkProperty = new SimpleObjectProperty<>();
    private ObservableList<NetworkCategory> openedCategories = FXCollections.observableArrayList();

    private List<SetList> setLists;  // GO Term lists (per domain).
    private Overview overview;       // Protein SOM overview.

    public SetColors setColors;      // Protein set coloring.

    public Visualization() {

        setColors = new SetColors();

        // Protein set listing, update on selection change.
        setLists = new ArrayList<>();
        networkProperty.addListener((observable, old, activeNetwork) -> setLists.clear());

        // Overview at bottom, dominant.
        overview = new Overview(this);
    }
    
    // Processing rootDraw.
    @Override
    public void draw() {
        // Catch thread exceptions (this is nasty, TODO: pretty fix).
        try {
            // Construct set lists.
            if(setLists.isEmpty()) {
                for(NetworkCategory<NetworkAnnotation> d: networkProperty.get().categories) {
                    List<SetLabel> labels = new ArrayList<>();

                    for(NetworkAnnotation t: d.annotations) {
                        String text = t.toString();
                        labels.add(new SetLabel(this, t, text));
                    }

                    setLists.add(new SetList(this, d, labels));
                }
            }
            
            // Enforce side margins.
            translate(OverviewConstants.MARGIN, OverviewConstants.MARGIN);

            // Black fill.
            color(Color.BLACK);

            // Normal face.
            textFont(org.cwi.examine.graphics.draw.Parameters.font);

            // Downward shifting position.
            PVector shiftPos = PVector.v();

            // Left side option snippets (includes set lists).
            List<Representation> sideSnippets = new ArrayList<>();
            
            List<SetList> openSl = new ArrayList<>();
            List<SetList> closedSl = new ArrayList<>();
            for(SetList sl: setLists) {
                (openedCategories.contains(sl.element) ? openSl : closedSl).add(sl);
            }
            sideSnippets.addAll(openSl);
            sideSnippets.addAll(closedSl);

            Layout.placeBelowLeftToRight(shiftPos, sideSnippets, OverviewConstants.MARGIN, OverviewConstants.sceneHeight());
            PVector termBounds = Layout.bounds(sideSnippets);

            shiftPos.x += termBounds.x + OverviewConstants.MARGIN;
        
            // Draw protein overview.
            overview.bounds = PVector.v(OverviewConstants.sceneWidth() - shiftPos.x - 2 * OverviewConstants.MARGIN, OverviewConstants.sceneHeight());
            overview.topLeft(shiftPos);
            snippet(overview);
            
            // Occlude any overview overflow for side lists.
            color(Color.WHITE);
            fillRect(-OverviewConstants.MARGIN, -OverviewConstants.MARGIN, shiftPos.x + OverviewConstants.MARGIN, sketchHeight());
            snippets(sideSnippets);

            try {
                Thread.sleep(10);
            } catch(InterruptedException ex) {
                Logger.getLogger(Visualization.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch(Exception ex) {
            
        }
    }

    // Terminate overview on disposal.
    public void stop() {
        overview.stop();
    }

    public ObjectProperty<Network> networkProperty() {
        return networkProperty;
    }

    public ObservableList<NetworkAnnotation> getActiveAnnotations() {
        return setColors.getActiveAnnotations();
    }

    public ObservableList<NetworkCategory> getOpenedCategories() {
        return openedCategories;
    }
}
