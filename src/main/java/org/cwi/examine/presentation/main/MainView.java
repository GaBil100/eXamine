package org.cwi.examine.presentation.main;

import javafx.geometry.Side;
import javafx.scene.layout.BorderPane;
import org.cwi.examine.presentation.main.annotation.AnnotationTabs;
import org.cwi.examine.presentation.nodelinkcontour.NodeLinkContourView;

/**
 * Primary pane of the application.
 */
public class MainView extends BorderPane {

    private final NodeLinkContourView nodeLinkContourView = new NodeLinkContourView();
    private final AnnotationTabs annotationOverview = new AnnotationTabs();

    public MainView() {

        getStyleClass().add("main-view");

        final BorderPane nodeLinkContourContainer = new BorderPane(nodeLinkContourView);
        nodeLinkContourContainer.getStyleClass().add("node-link-contour-container");
        setCenter(nodeLinkContourContainer);

        annotationOverview.setSide(Side.RIGHT);
        setLeft(annotationOverview);
    }

    public NodeLinkContourView getNodeLinkContourView() {
        return nodeLinkContourView;
    }

    public AnnotationTabs getAnnotationOverview() {
        return annotationOverview;
    }

    @Override
    public String getUserAgentStylesheet() {
        return MainView.class.getResource("MainView.css").toExternalForm();
    }
}
