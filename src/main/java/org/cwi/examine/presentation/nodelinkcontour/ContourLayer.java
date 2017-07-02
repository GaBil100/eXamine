package org.cwi.examine.presentation.nodelinkcontour;

import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.cwi.examine.model.NetworkAnnotation;
import org.cwi.examine.presentation.nodelinkcontour.layout.Contours;

import static javafx.beans.binding.Bindings.createObjectBinding;
import static javafx.beans.binding.Bindings.valueAt;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.observableHashMap;

class ContourLayer extends StackPane {

    private final ListProperty<NetworkAnnotation> annotations = new SimpleListProperty<>(observableArrayList());
    private final MapProperty<NetworkAnnotation, Contours> contours = new SimpleMapProperty<>(observableHashMap());
    private final MapProperty<NetworkAnnotation, Color> colors = new SimpleMapProperty<>(observableHashMap());

    private final NetworkElementLayer<NetworkAnnotation, Node> ribbonLayer =
            new NetworkElementLayer<>("network-contour", this::createAnnotationRibbon);

    ContourLayer() {

        getChildren().setAll(ribbonLayer);

        ribbonLayer.elementProperty().bind(annotations);
    }

    private Node createAnnotationRibbon(final NetworkAnnotation annotation) {

        final ContourRepresentation ribbon = new ContourRepresentation(annotation);
        ribbon.getStyleClass().add("network-contour-ribbon");
        ribbon.geometryProperty().bind(createObjectBinding(
                () -> contours.getOrDefault(annotation, new Contours(annotation)).getRibbon(),
                contours));
        ribbon.fillProperty().bind(valueAt(colors, annotation));

        final ContourRepresentation hardOutline = new ContourRepresentation(annotation);
        hardOutline.getStyleClass().add("network-contour-hard-outline");
        hardOutline.geometryProperty().bind(createObjectBinding(
                () -> contours.getOrDefault(annotation, new Contours(annotation)).getOutline(),
                contours
        ));

        return new Pane(ribbon, hardOutline);
    }

    ListProperty<NetworkAnnotation> annotationsProperty() {
        return annotations;
    }

    MapProperty<NetworkAnnotation, Contours> contoursProperty() {
        return contours;
    }

    MapProperty<NetworkAnnotation, Color> colorsProperty() {
        return colors;
    }

}
