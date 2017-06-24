package org.cwi.examine.presentation.visualization;

import org.cwi.examine.graphics.Application;
import org.cwi.examine.graphics.PVector;
import org.cwi.examine.graphics.draw.Layout;
import org.cwi.examine.graphics.draw.Representation;
import org.cwi.examine.model.HAnnotation;
import org.cwi.examine.model.HCategory;
import org.cwi.examine.model.Model;
import org.cwi.examine.presentation.visualization.overview.Overview;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.cwi.examine.graphics.StaticGraphics.*;

// Visualization module.
public class Visualization extends Application {

    public final Model model;
    private List<SetList> setLists;  // GO Term lists (per domain).
    private Overview overview;       // Protein SOM overview.

    public SetColors setColors;      // Protein set coloring.

    public Visualization(final Model model) {
        this.model = model;

        setColors = new SetColors(model);

        // Protein set listing, update on selection change.
        setLists = new ArrayList<>();
        model.activeNetworkProperty().addListener((observable, old, activeNetwork) -> setLists.clear());

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
                for(HCategory<HAnnotation> d: model.activeNetworkProperty().get().categories) {
                    List<SetLabel> labels = new ArrayList<>();

                    for(HAnnotation t: d.annotations) {
                        String text = t.toString();
                        labels.add(new SetLabel(this, t, text));
                    }

                    setLists.add(new SetList(this, d, labels));
                }
            }
            
            // Enforce side margins.
            translate(Parameters.MARGIN, Parameters.MARGIN);

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
                (model.openedCategoriesProperty().contains(sl.element) ? openSl : closedSl)
                .add(sl);
            }
            sideSnippets.addAll(openSl);
            sideSnippets.addAll(closedSl);

            Layout.placeBelowLeftToRight(shiftPos, sideSnippets, Parameters.MARGIN, Parameters.sceneHeight());
            PVector termBounds = Layout.bounds(sideSnippets);

            shiftPos.x += termBounds.x + Parameters.MARGIN;
        
            // Draw protein overview.
            overview.bounds = PVector.v(Parameters.sceneWidth() - shiftPos.x - 2 * Parameters.MARGIN, Parameters.sceneHeight());
            overview.topLeft(shiftPos);
            snippet(overview);
            
            // Occlude any overview overflow for side lists.
            color(Color.WHITE);
            fillRect(-Parameters.MARGIN, -Parameters.MARGIN, shiftPos.x + Parameters.MARGIN, sketchHeight());
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
}
