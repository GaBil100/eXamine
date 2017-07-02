package org.cwi.examine.presentation.main.annotation;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import org.cwi.examine.model.NetworkAnnotation;

import java.util.function.Consumer;

import static javafx.collections.FXCollections.emptyObservableList;
import static javafx.collections.FXCollections.observableArrayList;

class AnnotationSelectionModel extends TableView.TableViewSelectionModel<NetworkAnnotation> {

    private final ObservableList<NetworkAnnotation> annotations = observableArrayList();
    private final SimpleObjectProperty<Consumer<NetworkAnnotation>> onToggleAnnotationProperty = new SimpleObjectProperty<>(c -> {
    });

    /**
     * Builds a default TableViewSelectionModel instance with the provided
     * TableView.
     *
     * @param tableView The TableView upon which this selection model should
     *                  operate.
     * @throws NullPointerException TableView can not be null.
     */
    public AnnotationSelectionModel(final TableView<NetworkAnnotation> tableView) {
        super(tableView);
    }

    ObservableList<NetworkAnnotation> getAnnotations() {
        return annotations;
    }

    SimpleObjectProperty<Consumer<NetworkAnnotation>> onToggleAnnotationProperty() {
        return onToggleAnnotationProperty;
    }

    @Override
    public ObservableList<TablePosition> getSelectedCells() {
        return emptyObservableList();
    }

    @Override
    public boolean isSelected(int row, TableColumn<NetworkAnnotation, ?> column) {
        return false;
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
