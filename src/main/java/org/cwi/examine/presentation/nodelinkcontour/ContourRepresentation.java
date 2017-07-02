package org.cwi.examine.presentation.nodelinkcontour;

import com.vividsolutions.jts.geom.Geometry;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.shape.Path;
import org.cwi.examine.model.NetworkAnnotation;
import org.cwi.examine.presentation.nodelinkcontour.layout.Paths;

import static java.util.Collections.emptyList;

class ContourRepresentation extends Path {

    private final NetworkAnnotation annotation;

    private final ObjectProperty<Geometry> geometry =
            new SimpleObjectProperty<>(Paths.GEOMETRY_FACTORY.buildGeometry(emptyList()));

    ContourRepresentation(NetworkAnnotation annotation) {
        this.annotation = annotation;

        geometry.addListener(this::geometryChange);
    }

    private void geometryChange(ObservableValue<? extends Geometry> observable, Geometry oldGeometry, Geometry geometry) {
        getElements().setAll(Paths.geometryToShape(geometry));
    }

    ObjectProperty<Geometry> geometryProperty() {
        return geometry;
    }
}
