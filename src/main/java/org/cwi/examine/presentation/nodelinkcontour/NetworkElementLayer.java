package org.cwi.examine.presentation.nodelinkcontour;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static javafx.collections.FXCollections.observableArrayList;

class NetworkElementLayer<E, R extends Node> extends Pane {

    private final String representationStyleClass;
    private final Function<E, R> representationFactory;

    private final SimpleListProperty<E> elements = new SimpleListProperty<>(observableArrayList());

    private final Map<E, R> representations = new HashMap<>();

    public NetworkElementLayer(final String representationStyleClass, final Function<E, R> representationFactory) {
        this.representationStyleClass = requireNonNull(representationStyleClass);
        this.representationFactory = requireNonNull(representationFactory);

        elements.addListener(this::onElementChange);
    }

    private void onElementChange(final ListChangeListener.Change<? extends E> change) {

        change.next();

        representations.keySet().removeAll(change.getRemoved());
        change.getAddedSubList().forEach(element -> representations.put(element, createRepresentation(element)));
        getChildren().setAll(elements.stream().map(representations::get).collect(toList()));
    }

    private R createRepresentation(final E element) {
        final R representation = representationFactory.apply(element);
        representation.getStyleClass().add(representationStyleClass);
        return representation;
    }

    public SimpleListProperty<E> elementProperty() {
        return elements;
    }

}
