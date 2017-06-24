package org.cwi.examine.presentation.visualization;

import com.sun.javafx.collections.ObservableSetWrapper;
import org.cwi.examine.graphics.draw.Representation;
import org.cwi.examine.model.HNode;
import org.cwi.examine.model.HAnnotation;

import java.awt.Desktop;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.cwi.examine.graphics.StaticGraphics.mouseEvent;

// ProteinSet representation.
public abstract class SetRepresentation extends Representation<HAnnotation> {

    protected final Visualization visualization;
    
    // Base constructor.
    public SetRepresentation(final Visualization visualization, HAnnotation element) {
        super(element);

        this.visualization = visualization;
    }

    public boolean highlight() {
        return visualization.model.highlightedAnnotations().get().contains(element);
    }

    @Override
    // Highlight term and its member proteins.
    public void beginHovered() {
        Set<HAnnotation> hT = new HashSet<>();
        hT.add(element);
        visualization.model.highlightedAnnotations().set(new ObservableSetWrapper<>(hT));
        
        Set<HNode> hP = new HashSet<>();
        hP.addAll(element.elements);
        visualization.model.highlightedNodesProperty().set(new ObservableSetWrapper<>(hP));
    }

    @Override
    public void endHovered() {
        visualization.model.highlightedAnnotations().clear();
        visualization.model.highlightedNodesProperty().clear();
    }

    @Override
    // Adjust weight if set is selected.
    public void mouseWheel(int rotation) {
        if(visualization.model.activeAnnotationMapProperty().keySet().contains(element)) {
            visualization.model.changeWeight(element, -rotation);
        }
    }

    @Override
    // Toggle selection state on mouse click.
    public void mouseClicked(MouseEvent e) {
        // Open website on ctrl click for relevant annotations.
        if(mouseEvent().isControlDown()) {
            // URL to open.
            String url = element.url;
            
            // Try to open browser if URL is specified.
            if(url != null && url.trim().length() > 0) {
                try {
                    Desktop.getDesktop().browse(URI.create(url.trim()));
                } catch(IOException ex) {
                    Logger.getLogger(SetRepresentation.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        // Select otherwise.
        else {
            visualization.model.select(element);
        }
    }
}
