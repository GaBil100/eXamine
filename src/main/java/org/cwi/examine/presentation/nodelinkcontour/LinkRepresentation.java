package org.cwi.examine.presentation.nodelinkcontour;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.shape.Path;
import org.cwi.examine.presentation.nodelinkcontour.layout.Paths;
import org.jgrapht.graph.DefaultEdge;

import static java.util.Collections.emptyList;
import static javafx.collections.FXCollections.observableArrayList;

class LinkRepresentation extends Path {

    private final DefaultEdge edge;

    private final ListProperty<Point2D> controlPoints = new SimpleListProperty<>(observableArrayList());

    LinkRepresentation(final DefaultEdge edge) {
        this.edge = edge;

        controlPoints.addListener((ListChangeListener) c ->
                getElements().setAll(
                        controlPoints.size() == 3 ?
                                Paths.getArc(controlPoints.get(0), controlPoints.get(1), controlPoints.get(2)) :
                                emptyList()
                ));
    }

    ListProperty<Point2D> controlPointsProperty() {
        return controlPoints;
    }
}
