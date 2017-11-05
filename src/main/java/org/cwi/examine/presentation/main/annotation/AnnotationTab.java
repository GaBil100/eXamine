package org.cwi.examine.presentation.main.annotation;

import javafx.beans.binding.Bindings;
import javafx.beans.property.MapProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.cwi.examine.model.NetworkAnnotation;
import org.cwi.examine.model.NetworkCategory;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static javafx.collections.FXCollections.observableHashMap;
import static javafx.collections.FXCollections.observableList;

class AnnotationTab extends Tab {

    private final TableView<NetworkAnnotation> annotationTable;
    private final AnnotationSelectionModel annotationSelectionModel;

    private final MapProperty<NetworkAnnotation, Color> annotationColors = new SimpleMapProperty<>(observableHashMap());

    private final TableColumn<NetworkAnnotation, Optional<Color>> colorColumn = new TableColumn<>();
    private final TableColumn<NetworkAnnotation, String> nameColumn = new TableColumn<>();
    private final TableColumn<NetworkAnnotation, Double> scoreColumn = new TableColumn<>("Score");

    private final SimpleObjectProperty<Consumer<NetworkAnnotation>> onToggleAnnotation = new SimpleObjectProperty<>(c -> {
    });
    private final SimpleObjectProperty<Consumer<List<NetworkAnnotation>>> onHighlightAnnotations = new SimpleObjectProperty<>(c -> {
    });

    AnnotationTab(final NetworkCategory category) {

        // Tab.
        setClosable(false);
        setText(category.getName());

        // Table.
        annotationTable = new TableView<>(observableList(category.getAnnotations()));

        final BorderPane content = new BorderPane(annotationTable);
        content.getStyleClass().add("annotation-tab");
        setContent(content);

        nameColumn.setText(category.getName());

        // Cell value factories.
        colorColumn.setCellValueFactory(this::bindColorValue);
        nameColumn.setCellValueFactory(parameters -> new SimpleStringProperty(parameters.getValue().getName()));
        scoreColumn.setCellValueFactory(parameters -> new SimpleObjectProperty<>(parameters.getValue().getScore()));

        // Row and cell factories.
        annotationTable.setRowFactory(this::createRow);
        colorColumn.setCellFactory(this::createColorCell);

        // Column layout and style.
        colorColumn.getStyleClass().add("color-column");
        nameColumn.getStyleClass().add("name-column");
        scoreColumn.getStyleClass().add("score-column");

        annotationTable.getColumns().setAll(colorColumn, nameColumn, scoreColumn);

        annotationSelectionModel = new AnnotationSelectionModel(annotationTable);
        annotationTable.setSelectionModel(annotationSelectionModel);
        annotationSelectionModel.onToggleAnnotationProperty().bind(onToggleAnnotation);

        annotationTable.setRowFactory(this::createRow);
    }

    private ObservableValue<Optional<Color>> bindColorValue(
            TableColumn.CellDataFeatures<NetworkAnnotation, Optional<Color>> parameters) {

        return Bindings.createObjectBinding(
                () -> Optional.ofNullable(annotationColors.get(parameters.getValue())),
                annotationColors
        );
    }

    private TableRow<NetworkAnnotation> createRow(TableView<NetworkAnnotation> tableView) {

        final TableRow<NetworkAnnotation> tableRow = new TableRow<>();
        tableRow.setOnMouseEntered(event -> onHighlightAnnotations.get().accept(asList(tableRow.getItem())));
        tableRow.setOnMouseExited(event -> onHighlightAnnotations.get().accept(emptyList()));

        return tableRow;
    }

    private TableCell<NetworkAnnotation, Optional<Color>> createColorCell(TableColumn<NetworkAnnotation, Optional<Color>> column) {
        return new TableCell<NetworkAnnotation, Optional<Color>>() {

            {
                getStyleClass().add("color-cell");
            }

            @Override
            protected void updateItem(Optional<Color> optionalColor, boolean empty) {

                final Pane marker;

                if (empty || !optionalColor.isPresent()) {
                    marker = null;
                } else {
                    marker = new Pane();
                    marker.getStyleClass().add("marker");
                    marker.setStyle("-fx-background-color: " + rgbString(optionalColor.get()));
                }

                setGraphic(marker);
            }
        };
    }

    private String rgbString(Color color) {
        return "rgba(" +
                (int) (255 * color.getRed()) + "," +
                (int) (255 * color.getGreen()) + "," +
                (int) (255 * color.getBlue()) + "," +
                color.getOpacity() + ")";
    }

    MapProperty<NetworkAnnotation, Color> annotationColorsProperty() {
        return annotationColors;
    }

    SetProperty<NetworkAnnotation> highlightedAnnotationsProperty() {
        return annotationSelectionModel.highlightedAnnotationsProperty();
    }

    SimpleObjectProperty<Consumer<NetworkAnnotation>> onToggleAnnotationProperty() {
        return onToggleAnnotation;
    }

    SimpleObjectProperty<Consumer<List<NetworkAnnotation>>> onHighlightAnnotationsProperty() {
        return onHighlightAnnotations;
    }

}
