package org.cwi.examine.presentation.main;

import javafx.embed.swing.SwingNode;
import javafx.geometry.Side;
import javafx.scene.layout.BorderPane;
import org.cwi.examine.presentation.main.category.CategoryOverview;
import org.cwi.examine.presentation.visualization.Visualization;

/**
 * Primary pane of the application.
 */
public class MainView extends BorderPane {

    private final Visualization visualization;
    private final CategoryOverview categoryOverview;

    public MainView() {

        // Side pane for element information and selection.
        categoryOverview = new CategoryOverview();
        categoryOverview.setSide(Side.LEFT);
        setRight(categoryOverview);

        // Network visualization.
        visualization = new Visualization();
        final SwingNode visualizationWrapper = new SwingNode();
        visualizationWrapper.setContent(visualization.getRootPanel());
        visualization.setupGraphics();
        setCenter(visualizationWrapper);
    }

    public Visualization getVisualization() {
        return visualization;
    }

    public CategoryOverview getCategoryOverview() {
        return categoryOverview;
    }
}
