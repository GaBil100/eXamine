package org.cwi.examine.presentation;

import javafx.embed.swing.SwingNode;
import javafx.scene.layout.BorderPane;
import org.cwi.examine.model.Model;
import org.cwi.examine.presentation.visualization.Visualization;

/**
 * Primary pane of the application.
 */
public class MainPane extends BorderPane {

    private final Model model;
    private final Visualization visualization;
    private final CategoryOverview elementPane;

    public MainPane(final Model model) {
        this.model = model;

        // Side pane for element information and selection.
        elementPane = new CategoryOverview(model);
        setLeft(elementPane);

        // Network visualization.
        visualization = new Visualization(model);
        final SwingNode visualizationWrapper = new SwingNode();
        visualizationWrapper.setContent(visualization.getRootPanel());
        visualization.setupGraphics();
        setCenter(visualizationWrapper);
    }

    public Visualization getVisualization() {
        return visualization;
    }
}
