package org.cwi.examine.internal.model;

import com.sun.javafx.collections.ObservableListWrapper;
import com.sun.javafx.collections.ObservableMapWrapper;
import com.sun.javafx.collections.ObservableSetWrapper;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import org.cwi.examine.internal.data.*;
import org.cwi.examine.internal.visualization.SetColors;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Model of network with interaction states.
 */
public final class Model {

    private final DataSet dataSet;
    private final SetProperty<HCategory> openedCategories;
    private final ListProperty<HCategory> orderedCategories;
    private final SetProperty<HNode> highlightedNodes;
    private final SetProperty<DefaultEdge> highlightedLinks;
    private final SetProperty<HAnnotation> highlightedAnnotations;
    private final ObjectProperty<Network> activeNetwork;

    // Included sets and the weight that has been assigned to them.
    private final ObservableMap<HAnnotation, Double> activeSetMap;

    // List of active sets with a somewhat stable ordering.
    private final ObservableList<HAnnotation> activeSetList;

    // Selected set or protein.
    private ObjectProperty<HElement> selected;

    public Model(final DataSet dataSet) {
        this.dataSet = dataSet;
        this.openedCategories = new SimpleSetProperty<>(new ObservableSetWrapper<>(new HashSet<>()));
        this.orderedCategories = new SimpleListProperty<>(new ObservableListWrapper<>(new ArrayList<>()));
        this.highlightedNodes = new SimpleSetProperty<>(new ObservableSetWrapper<>(new HashSet<>()));
        this.highlightedLinks = new SimpleSetProperty<>(new ObservableSetWrapper<>(new HashSet<>()));
        this.highlightedAnnotations = new SimpleSetProperty<>(new ObservableSetWrapper<>(new HashSet<>()));
        this.activeNetwork = new SimpleObjectProperty<>(new Network());
        this.activeSetMap = new ObservableMapWrapper<>(new HashMap<>());
        this.activeSetList = new ObservableListWrapper<>(new ArrayList<>());
        this.selected = new SimpleObjectProperty<>(null);

        // Update active network that is to be visualized.
        // For now, it is the union of all known modules.
        dataSet.superNetwork.addListener((obs, old, categories) -> {
            final Network superNetwork = dataSet.superNetwork.get();
            final Network moduleNetwork = Network.induce(superNetwork.modules, superNetwork);
            activeNetworkProperty().set(moduleNetwork);

            System.out.println(superNetwork.graph.vertexSet().size());
            System.out.println(moduleNetwork.graph.vertexSet().size());
        });

        // Update ordered category list.
        Runnable categoryObserver = () -> {
            List<HCategory> openedCat = new ArrayList<>();
            List<HCategory> closedCat = new ArrayList<>();
            for(HCategory c: dataSet.superNetwork.get().categories) {
                (openedCategoriesProperty().contains(c) ? openedCat : closedCat).add(c);
            }

            openedCat.addAll(closedCat);
            orderedCategoriesProperty().set(new ObservableListWrapper<>(openedCat));
        };

        openedCategoriesProperty().addListener((obs, old, categories) -> categoryObserver.run());
        dataSet.superNetwork.addListener((obs, old, categories) -> categoryObserver.run());
    }

    public DataSet getDataSet() {
        return dataSet;
    }

    public SetProperty<HCategory> openedCategoriesProperty() {
        return openedCategories;
    }

    public ListProperty<HCategory> orderedCategoriesProperty() {
        return orderedCategories;
    }

    public SetProperty<HNode> highlightedNodesProperty() {
        return highlightedNodes;
    }

    public SetProperty<DefaultEdge> highlightedLinksProperty() {
        return highlightedLinks;
    }

    public SetProperty<HAnnotation> highlightedAnnotations() {
        return highlightedAnnotations;
    }

    public ObjectProperty<Network> activeNetworkProperty() {
        return activeNetwork;
    }

    /**
     * Add set with an initial weight, report on success
     * (there is a maximum number of selected sets).
     */
    public boolean add(HAnnotation proteinSet, double weight) {
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
    public void remove(HAnnotation proteinSet) {
        activeAnnotationMapProperty().remove(proteinSet);
        activeAnnotationListProperty().remove(proteinSet);
    }

    /**
     * Adjust the weight of a set by the given change.
     */
    public void changeWeight(HAnnotation proteinSet, double weightChange) {
        double currentWeight = activeAnnotationMapProperty().get(proteinSet);
        double newWeight = Math.max(1f, currentWeight + weightChange);
        activeAnnotationMapProperty().put(proteinSet, newWeight);
    }

    public ObservableMap<HAnnotation, Double> activeAnnotationMapProperty() {
        return activeSetMap;
    }

    public ObservableList<HAnnotation> activeAnnotationListProperty() {
        return activeSetList;
    }

    public ObjectProperty<HElement> selectedElementProperty() {
        return selected;
    }

    /**
     * Select a set or protein, null iff no element is selected.
     */
    public void select(HElement element) {
        selected.set(element);

        // Element is a set -> remove from or include in active sets.
        if(element != null && element instanceof HAnnotation) {
            HAnnotation elSet = (HAnnotation) element;

            if(activeAnnotationListProperty().contains(elSet)) {
                remove(elSet);
            } else {
                add(elSet, 1);
            }
        }
    }
}
