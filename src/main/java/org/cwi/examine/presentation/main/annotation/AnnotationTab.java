package org.cwi.examine.presentation.main.annotation;

import javafx.beans.binding.Bindings;
import javafx.beans.property.MapProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.cwi.examine.model.NetworkAnnotation;
import org.cwi.examine.model.NetworkCategory;

import java.util.Optional;
import java.util.function.Consumer;

import static javafx.collections.FXCollections.observableHashMap;
import static javafx.collections.FXCollections.observableList;

class AnnotationTab extends Tab {

    private final TableView<NetworkAnnotation> annotationTable;

    private final MapProperty<NetworkAnnotation, Color> annotationColors = new SimpleMapProperty<>(observableHashMap());

    private final TableColumn<NetworkAnnotation, Optional<Color>> colorColumn = new TableColumn<>();
    private final TableColumn<NetworkAnnotation, String> nameColumn = new TableColumn<>();
    private final TableColumn<NetworkAnnotation, Double> scoreColumn = new TableColumn<>("Score");

    private final SimpleObjectProperty<Consumer<NetworkAnnotation>> onToggleAnnotationProperty = new SimpleObjectProperty<>(c -> {
    });

    AnnotationTab(final NetworkCategory category) {

        // Tab.
        setClosable(false);
        setText(category.name);

        // Table.
        annotationTable = new TableView<>(observableList(category.annotations));

        final BorderPane content = new BorderPane(annotationTable);
        content.getStyleClass().add("annotation-tab");
        setContent(content);

        nameColumn.setText(category.name);

        // Cell value factories.
        colorColumn.setCellValueFactory(this::bindColorValue);
        nameColumn.setCellValueFactory(parameters -> new SimpleStringProperty(parameters.getValue().getName()));
        scoreColumn.setCellValueFactory(parameters -> new SimpleObjectProperty<>(parameters.getValue().getScore()));

        // Cell factories.
        colorColumn.setCellFactory(this::createColorCell);

        // Column layout and style.
        colorColumn.getStyleClass().add("color-column");
        nameColumn.getStyleClass().add("name-column");
        scoreColumn.getStyleClass().add("score-column");

        annotationTable.getColumns().setAll(colorColumn, nameColumn, scoreColumn);

        final AnnotationSelectionModel selectionModel = new AnnotationSelectionModel(annotationTable);
        annotationTable.setSelectionModel(selectionModel);
        selectionModel.onToggleAnnotationProperty().bind(onToggleAnnotationProperty);
    }

    private ObservableValue<Optional<Color>> bindColorValue(
            TableColumn.CellDataFeatures<NetworkAnnotation, Optional<Color>> parameters) {

        return Bindings.createObjectBinding(
                () -> Optional.ofNullable(annotationColors.get(parameters.getValue())),
                annotationColors
        );
    }

    private TableCell<NetworkAnnotation, Optional<Color>> createColorCell(TableColumn<NetworkAnnotation, Optional<Color>> column) {
        return new TableCell<NetworkAnnotation, Optional<Color>>() {

            {
                getStyleClass().add("color-cell");
            }

            @Override
            protected void updateItem(Optional<Color> optionalColor, boolean empty) {

                final Pane marker;

                if(empty || !optionalColor.isPresent()) {
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

    SimpleObjectProperty<Consumer<NetworkAnnotation>> onToggleAnnotationProperty() {
        return onToggleAnnotationProperty;
    }

    MapProperty<NetworkAnnotation, Color> annotationColorsProperty() {
        return annotationColors;
    }



}
