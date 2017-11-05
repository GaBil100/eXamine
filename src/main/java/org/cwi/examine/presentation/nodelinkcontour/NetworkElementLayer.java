package org.cwi.examine.presentation.nodelinkcontour;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.SetChangeListener;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.observableSet;

class NetworkElementLayer<E, R extends Node> extends Pane {

    private static final String ELEMENT_HIGHLIGHTED_STYLE = "highlighted";

    private final String representationStyleClass;
    private final Function<E, R> representationFactory;

    private final SimpleListProperty<E> elements = new SimpleListProperty<>(observableArrayList());
    private final SimpleSetProperty<E> highlightedElements = new SimpleSetProperty<>(observableSet());

    private final Map<E, R> representations = new HashMap<>();

    public NetworkElementLayer(final String representationStyleClass, final Function<E, R> representationFactory) {
        this.representationStyleClass = requireNonNull(representationStyleClass);
        this.representationFactory = requireNonNull(representationFactory);

        elements.addListener(this::onElementChange);
        highlightedElements.addListener(this::onHighlightedElementsChange);
    }

    private void onElementChange(final ListChangeListener.Change<? extends E> change) {

        change.next();

        representations.keySet().removeAll(change.getRemoved());
        change.getAddedSubList().forEach(element -> representations.put(element, createRepresentation(element)));
        getChildren().setAll(elements.stream().map(representations::get).collect(toList()));
    }

    private void onHighlightedElementsChange(final SetChangeListener.Change<? extends E> change) {

        if(change.wasAdded()) {
            representations.get(change.getElementAdded()).getStyleClass().add(ELEMENT_HIGHLIGHTED_STYLE);
        } else if(change.wasRemoved()) {
            representations.get(change.getElementRemoved()).getStyleClass().remove(ELEMENT_HIGHLIGHTED_STYLE);
        }
    }

    private R createRepresentation(final E element) {
        final R representation = representationFactory.apply(element);
        representation.getStyleClass().add(representationStyleClass);
        return representation;
    }

    public SimpleListProperty<E> elementProperty() {
        return elements;
    }

    public SimpleSetProperty<E> highlightedElementsProperty() {
        return highlightedElements;
    }
}
