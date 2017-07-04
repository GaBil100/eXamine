package org.cwi.examine.presentation.main;

import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyMapProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlySetProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import org.cwi.examine.model.Network;
import org.cwi.examine.model.NetworkAnnotation;
import org.cwi.examine.model.NetworkCategory;
import org.cwi.examine.model.NetworkNode;
import org.jgrapht.graph.DefaultEdge;

import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.observableHashMap;
import static javafx.collections.FXCollections.observableSet;

/**
 * View model of the main section. Maintains exploration state of a network that is being viewed.
 */
public final class MainViewModel {

    private static final double DEFAULT_ANNOTATION_WEIGHT = 1;

    private final ObjectProperty<Network> activeNetwork = new SimpleObjectProperty<>(new Network());

    private final ObservableList<NetworkCategory> categories = observableArrayList();

    private final ListProperty<NetworkAnnotation> selectedAnnotations = new SimpleListProperty<>(observableArrayList());
    private final MapProperty<NetworkAnnotation, Double> annotationWeights = new SimpleMapProperty<>(observableHashMap());
    private final AnnotationColors annotationColors = new AnnotationColors();

    private final SetProperty<NetworkNode> highlightedNodes = new SimpleSetProperty<>(observableSet());
    private final SetProperty<DefaultEdge> highlightedLinks = new SimpleSetProperty<>(observableSet());
    private final SetProperty<NetworkAnnotation> highlightedAnnotations = new SimpleSetProperty<>(observableSet());

    // -- Actions.

    /**
     * Activate the given network as being explored. This clears the entire exploration state.
     *
     * @param network The network to activate as being explored.
     */
    public void activateNetwork(Network network) {

        activeNetwork.set(network);
        categories.setAll(network.getCategories());
        selectedAnnotations.clear();
        annotationWeights.clear();
        annotationColors.clear();
        highlightedNodes.clear();
        highlightedLinks.clear();
        highlightedAnnotations.clear();
    }

    /**
     * Toggle the selected state of the given annotation.
     *
     * @param annotation The annotation to toggle the selected state for.
     */
    public void toggleAnnotation(NetworkAnnotation annotation) {

        if (selectedAnnotations.contains(annotation)) {
            selectedAnnotationsProperty().remove(annotation);
            annotationWeightsProperty().remove(annotation);
            annotationColors.releaseColor(annotation);
        } else {
            annotationColors.assignColor(annotation);
            annotationWeightsProperty().put(annotation, DEFAULT_ANNOTATION_WEIGHT);
            selectedAnnotationsProperty().add(annotation);
        }
    }

    /**
     * Adjust the weight (importance) of the given annotation by the given weight change.
     *
     * @param annotation   The annotation to change the weight of.
     * @param weightChange The number by which to change the weight.
     */
    public void changeAnnotationWeight(NetworkAnnotation annotation, double weightChange) {

        final double currentWeight = annotationWeightsProperty().get(annotation);
        final double newWeight = Math.max(1f, currentWeight + weightChange);
        annotationWeightsProperty().put(annotation, newWeight);
    }

    /**
     * Highlight the given annotations, which also highlights the nodes and links
     * that are fully contained by individual annotations.
     *
     * @param annotations The annotation to highlight.
     */
    public void highlightAnnotations(NetworkAnnotation... annotations) {

        clearHighlights();

        for (NetworkAnnotation annotation : annotations) {
            highlightedNodes.addAll(annotation.getNodes());
            highlightedAnnotations.add(annotation);
        }
    }

    /**
     * Clears the highlighted state of nodes, links, and contours.
     */
    public void clearHighlights() {

        highlightedNodes.clear();
        highlightedLinks.clear();
        highlightedAnnotations.clear();
    }


    // -- Accessors.

    public ReadOnlyObjectProperty<Network> activeNetworkProperty() {
        return activeNetwork;
    }

    public ObservableList<NetworkCategory> getCategories() {
        return categories;
    }

    public ReadOnlyMapProperty<NetworkAnnotation, Double> annotationWeightsProperty() {
        return annotationWeights;
    }

    public ReadOnlyListProperty<NetworkAnnotation> selectedAnnotationsProperty() {
        return selectedAnnotations;
    }

    public ReadOnlyMapProperty<NetworkAnnotation, Color> annotationColorProperty() {
        return annotationColors.colorMapProperty();
    }

    public ReadOnlySetProperty<NetworkNode> highlightedNodesProperty() {
        return highlightedNodes;
    }

    public ReadOnlySetProperty<DefaultEdge> highlightedLinksProperty() {
        return highlightedLinks;
    }

    public ReadOnlySetProperty<NetworkAnnotation> highlightedAnnotationsProperty() {
        return highlightedAnnotations;
    }

}
