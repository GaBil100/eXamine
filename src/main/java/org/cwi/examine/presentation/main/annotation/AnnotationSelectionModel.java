package org.cwi.examine.presentation.main.annotation;

import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import org.cwi.examine.model.NetworkAnnotation;

import java.util.List;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.observableSet;

class AnnotationSelectionModel extends TableView.TableViewSelectionModel<NetworkAnnotation> {

    private final SetProperty<NetworkAnnotation> highlightedAnnotations = new SimpleSetProperty<>(observableSet());
    private final SimpleObjectProperty<Consumer<NetworkAnnotation>> onToggleAnnotationProperty = new SimpleObjectProperty<>(c -> {
    });

    private final ObservableList<TablePosition> highlightedTablePositions = observableArrayList();

    /**
     * Builds a default TableViewSelectionModel instance with the provided
     * TableView.
     *
     * @param tableView The TableView upon which this selection model should
     *                  operate.
     * @throws NullPointerException TableView can not be null.
     */
    AnnotationSelectionModel(final TableView<NetworkAnnotation> tableView) {
        super(tableView);

        highlightedAnnotations.addListener((SetChangeListener) change -> updateHighlightedTablePositions());
    }

    private void updateHighlightedTablePositions() {

        final List<TablePosition> newPositions = highlightedAnnotations.stream()
                .filter(getTableModel()::contains)
                .map(annotation -> new TablePosition<>(getTableView(), getTableModel().indexOf(annotation), null))
                .collect(toList());

        highlightedTablePositions.setAll(newPositions);
    }

    SetProperty<NetworkAnnotation> highlightedAnnotationsProperty() {
        return highlightedAnnotations;
    }

    SimpleObjectProperty<Consumer<NetworkAnnotation>> onToggleAnnotationProperty() {
        return onToggleAnnotationProperty;
    }

    @Override
    public ObservableList<TablePosition> getSelectedCells() {
        return highlightedTablePositions;
    }

    @Override
    public boolean isSelected(int row, TableColumn<NetworkAnnotation, ?> column) {
        return isSelected(row);
    }

    @Override
    public boolean isSelected(int row) {
        return row < getTableModel().size() && highlightedAnnotations.contains(getTableModel().get(row));
    }

    @Override
    public void select(int row, TableColumn<NetworkAnnotation, ?> column) {
    }

    @Override
    public void clearAndSelect(int row, TableColumn<NetworkAnnotation, ?> column) {
        onToggleAnnotationProperty().get().accept(getTableModel().get(row));
    }

    @Override
    public void clearSelection(int row, TableColumn<NetworkAnnotation, ?> column) {

    }

    @Override
    public void selectLeftCell() {

    }

    @Override
    public void selectRightCell() {

    }

    @Override
    public void selectAboveCell() {

    }

    @Override
    public void selectBelowCell() {

    }
}
