package org.cwi.examine.presentation.main;

import com.sun.javafx.collections.ObservableListWrapper;
import com.sun.javafx.collections.ObservableMapWrapper;
import com.sun.javafx.collections.ObservableSetWrapper;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import org.cwi.examine.model.*;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static javafx.beans.binding.Bindings.createObjectBinding;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.observableList;

/**
 * MainViewModel of network with interaction states.
 */
public final class MainViewModel {

    private final ObjectProperty<Network> activeNetwork = new SimpleObjectProperty<>(new Network());
    private final ListProperty<NetworkCategory> activeCategories = new SimpleListProperty<>(observableArrayList());

    private final SetProperty<NetworkNode> highlightedNodes;
    private final SetProperty<DefaultEdge> highlightedLinks;
    private final SetProperty<NetworkAnnotation> highlightedAnnotations;

    // Included sets and the weight that has been assigned to them.
    private final ObservableMap<NetworkAnnotation, Double> activeSetMap;

    // List of active sets with a somewhat stable ordering.
    private final ObservableList<NetworkAnnotation> activeSetList;

    // Selected set or protein.
    private ObjectProperty<NetworkElement> selected;

    public MainViewModel() {

        activeCategories.bind(createObjectBinding(() -> observableList(getActiveNetwork().categories), activeNetwork));

        this.highlightedNodes = new SimpleSetProperty<>(new ObservableSetWrapper<>(new HashSet<>()));
        this.highlightedLinks = new SimpleSetProperty<>(new ObservableSetWrapper<>(new HashSet<>()));
        this.highlightedAnnotations = new SimpleSetProperty<>(new ObservableSetWrapper<>(new HashSet<>()));
        this.activeSetMap = new ObservableMapWrapper<>(new HashMap<>());
        this.activeSetList = new ObservableListWrapper<>(new ArrayList<>());
        this.selected = new SimpleObjectProperty<>(null);
    }

    public void activateNetwork(final Network network) {
        this.activeNetwork.set(network);
    }

    public ReadOnlyObjectProperty<Network> activeNetworkProperty() {
        return activeNetwork;
    }

    public Network getActiveNetwork() {
        return activeNetwork.get();
    }

    public ObservableList<NetworkCategory> getActiveCategories() {
        return activeCategories.get();
    }

    public ReadOnlyListProperty<NetworkCategory> activeCategoriesProperty() {
        return activeCategories;
    }

    public SetProperty<NetworkNode> highlightedNodesProperty() {
        return highlightedNodes;
    }

    public SetProperty<DefaultEdge> highlightedLinksProperty() {
        return highlightedLinks;
    }

    public SetProperty<NetworkAnnotation> highlightedAnnotations() {
        return highlightedAnnotations;
    }

    /**
     * Add set with an initial weight, report on success
     * (there is a maximum number of selected sets).
     */
    public boolean add(NetworkAnnotation proteinSet, double weight) {
        boolean added = activeAnnotationListProperty().size() < SetColors.palette.length;

        if(added) {
            activeAnnotationMapProperty().put(proteinSet, weight);
            activeAnnotationListProperty().add(proteinSet);
        }

        return added;
    }

    /**
     * Remove set.
     */
    public void remove(NetworkAnnotation proteinSet) {
        activeAnnotationMapProperty().remove(proteinSet);
        activeAnnotationListProperty().remove(proteinSet);
    }

    /**
     * Adjust the weight of a set by the given change.
     */
    public void changeWeight(NetworkAnnotation proteinSet, double weightChange) {
        double currentWeight = activeAnnotationMapProperty().get(proteinSet);
        double newWeight = Math.max(1f, currentWeight + weightChange);
        activeAnnotationMapProperty().put(proteinSet, newWeight);
    }

    public ObservableMap<NetworkAnnotation, Double> activeAnnotationMapProperty() {
        return activeSetMap;
    }

    public ObservableList<NetworkAnnotation> activeAnnotationListProperty() {
        return activeSetList;
    }

    public ObjectProperty<NetworkElement> selectedElementProperty() {
        return selected;
    }

    /**
     * Select a set or protein, null iff no element is selected.
     */
    public void select(NetworkElement element) {
        selected.set(element);

        // Element is a set -> remove from or include in active sets.
        if(element != null && element instanceof NetworkAnnotation) {
            NetworkAnnotation elSet = (NetworkAnnotation) element;

            if(activeAnnotationListProperty().contains(elSet)) {
                remove(elSet);
            } else {
                add(elSet, 1);
            }
        }
    }
}
