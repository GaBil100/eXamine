package org.cwi.examine.presentation.main;

import javafx.beans.property.MapProperty;
import javafx.beans.property.ReadOnlyMapProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.scene.paint.Color;
import org.cwi.examine.model.NetworkAnnotation;

import java.util.LinkedList;
import java.util.Optional;

import static java.util.Arrays.asList;
import static javafx.collections.FXCollections.observableHashMap;
import static javafx.scene.paint.Color.rgb;

class AnnotationColors {

    private static final Color[] COLOR_PALETTE = new Color[]{
            rgb(141, 211, 199),
            rgb(255, 255, 179),
            rgb(190, 186, 218),
            rgb(251, 128, 114),
            rgb(128, 177, 211),
            rgb(253, 180, 98),
            rgb(252, 205, 229),
            rgb(188, 128, 189),
            rgb(204, 235, 197),
            rgb(255, 237, 111)
    };

    private final LinkedList<Color> availableColors = new LinkedList<>(asList(COLOR_PALETTE));
    private final MapProperty<NetworkAnnotation, Color> colorMap = new SimpleMapProperty<>(observableHashMap());

    /**
     * Fetch an available color and assign it to the given annotation.
     * No result if no more colors are available.
     *
     * @param annotation The annotation to assign a color to.
     * @return A unique color for the given annotation, or Empty if no color is available.
     */
    Optional<Color> assignColor(final NetworkAnnotation annotation) {

        assert !colorMap.containsKey(annotation);

        final Optional<Color> optionalColor = Optional.of(availableColors.poll());
        optionalColor.ifPresent(color -> colorMap.put(annotation, color));
        return optionalColor;
    }

    /**
     * Release the color that has been assigned to the given annotation.
     * This color will be available for future use.
     *
     * @param annotation The annotation to release the color of.
     */
    void releaseColor(final NetworkAnnotation annotation) {

        assert colorMap.containsKey(annotation);

        availableColors.add(colorMap.remove(annotation));
    }

    /**
     * Release the color of the given annotation if it has an assigned color.
     * Assign a color to the given annotation if it does not have an assigned color.
     *
     * @param annotation The annotation to toggle a color for.
     */
    void toggleColor(final NetworkAnnotation annotation) {

        if(colorMap.containsKey(annotation)) {
            releaseColor(annotation);
        } else {
            assignColor(annotation);
        }
    }

    /**
     * @return The mapping of annotations and their assigned colors.
     */
    ReadOnlyMapProperty<NetworkAnnotation, Color> colorMapProperty() {
        return colorMap;
    }

}
