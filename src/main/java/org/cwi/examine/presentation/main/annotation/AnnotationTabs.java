package org.cwi.examine.presentation.main.annotation;

import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.control.TabPane;
import javafx.scene.paint.Color;
import org.cwi.examine.model.NetworkAnnotation;
import org.cwi.examine.model.NetworkCategory;

import java.util.List;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.observableHashMap;
import static javafx.collections.FXCollections.observableSet;

public class AnnotationTabs extends TabPane {

    private final ListProperty<NetworkCategory> categories = new SimpleListProperty<>(observableArrayList());
    private final MapProperty<NetworkAnnotation, Color> annotationColors = new SimpleMapProperty<>(observableHashMap());
    private final SetProperty<NetworkAnnotation> highlightedAnnotations = new SimpleSetProperty<>(observableSet());

    private final SimpleObjectProperty<Consumer<NetworkAnnotation>> onToggleAnnotation = new SimpleObjectProperty<>(c -> {
    });
    private final SimpleObjectProperty<Consumer<List<NetworkAnnotation>>> onHighlightAnnotations = new SimpleObjectProperty<>(c -> {
    });

    public AnnotationTabs() {
        getStyleClass().add("annotation-tabs");

        categories.addListener(this::onCategoriesChange);
    }

    private void onCategoriesChange(ListChangeListener.Change<? extends NetworkCategory> change) {

        final List<AnnotationTab> tabs = categories.stream().map(this::createAndBindTab).collect(toList());
        getTabs().setAll(tabs);
    }

    private AnnotationTab createAndBindTab(NetworkCategory category) {

        final AnnotationTab tab = new AnnotationTab(category);
        tab.annotationColorsProperty().bind(annotationColors);
        tab.highlightedAnnotationsProperty().bind(highlightedAnnotations);
        tab.onToggleAnnotationProperty().bind(onToggleAnnotation);
        tab.onHighlightAnnotationsProperty().bind(onHighlightAnnotations);

        return tab;
    }

    public ListProperty<NetworkCategory> categoriesProperty() {
        return categories;
    }

    public MapProperty<NetworkAnnotation, Color> annotationColorsProperty() {
        return annotationColors;
    }

    public SetProperty<NetworkAnnotation> highlightedAnnotationsProperty() {
        return highlightedAnnotations;
    }

    public SimpleObjectProperty<Consumer<NetworkAnnotation>> onToggleAnnotationProperty() {
        return onToggleAnnotation;
    }

    public SimpleObjectProperty<Consumer<List<NetworkAnnotation>>> onHighlightAnnotationsProperty() {
        return onHighlightAnnotations;
    }

    @Override
    public String getUserAgentStylesheet() {
        return AnnotationTabs.class.getResource("AnnotationTabs.css").toExternalForm();
    }
}
