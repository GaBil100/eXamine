package org.cwi.examine.presentation.main;

import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import org.cwi.examine.presentation.main.category.AnnotationOverview;
import org.cwi.examine.presentation.nodelinkcontour.NodeLinkContourView;

/**
 * Primary pane of the application.
 */
public class MainView extends BorderPane {

    private final NodeLinkContourView nodeLinkContourView = new NodeLinkContourView();
    private final AnnotationOverview annotationOverview = new AnnotationOverview();

    public MainView() {

        // Center network view in a scroll pane.
        final ScrollPane nodeLinkContourScroll = new ScrollPane(nodeLinkContourView);
        BorderPane.setAlignment(nodeLinkContourView, Pos.CENTER);
        setCenter(nodeLinkContourScroll);

        // Annotation overview at the left side.
        annotationOverview.setSide(Side.RIGHT);
        setLeft(annotationOverview);
    }

    public NodeLinkContourView getNodeLinkContourView() {
        return nodeLinkContourView;
    }

    public AnnotationOverview getAnnotationOverview() {
        return annotationOverview;
    }

}
