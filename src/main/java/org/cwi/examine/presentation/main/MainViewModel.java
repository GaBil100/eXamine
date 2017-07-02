package org.cwi.examine.presentation.main;

import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyMapProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
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
 * MainViewModel of network with interaction states.
 */
public final class MainViewModel {

    private static final double DEFAULT_ANNOTATION_WEIGHT = 1;

    private final ObjectProperty<Network> activeNetwork = new SimpleObjectProperty<>(new Network());

    private final ObservableList<NetworkCategory> categories = observableArrayList();

    private final ObservableSet<NetworkNode> highlightedNodes = observableSet();
    private final ObservableSet<DefaultEdge> highlightedLinks = observableSet();
    private final ObservableSet<NetworkAnnotation> highlightedAnnotations = observableSet();

    private final ListProperty<NetworkAnnotation> selectedAnnotations = new SimpleListProperty<>(observableArrayList());
    private final MapProperty<NetworkAnnotation, Double> annotationWeights = new SimpleMapProperty<>(observableHashMap());
    private final AnnotationColors annotationColors = new AnnotationColors();

    MainViewModel() {
        activeNetwork.addListener((obs, oldNetwork, network) -> categories.setAll(network.categories));
    }

    public void activateNetwork(final Network network) {
        this.activeNetwork.set(network);
    }

    /**
     * Add set with an initial weight, report on success
     * (there is a maximum number of selected sets).
     */
    public void toggleAnnotation(final NetworkAnnotation annotation) {

        if(selectedAnnotations.contains(annotation)) {
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
     * Adjust the weight of a set by the given change.
     */
    public void changeWeight(NetworkAnnotation proteinSet, double weightChange) {
        double currentWeight = annotationWeightsProperty().get(proteinSet);
        double newWeight = Math.max(1f, currentWeight + weightChange);
        annotationWeightsProperty().put(proteinSet, newWeight);
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

    public ReadOnlyObjectProperty<Network> activeNetworkProperty() {
        return activeNetwork;
    }

    public Network getActiveNetwork() {
        return activeNetwork.get();
    }

    public ObservableSet<NetworkNode> getHighlightedNodes() {
        return highlightedNodes;
    }

    public ObservableSet<DefaultEdge> getHighlightedLinksProperty() {
        return highlightedLinks;
    }

    public ObservableSet<NetworkAnnotation> getHighlightedAnnotations() {
        return highlightedAnnotations;
    }
}
