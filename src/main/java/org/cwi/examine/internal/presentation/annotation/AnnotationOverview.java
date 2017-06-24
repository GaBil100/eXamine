package org.cwi.examine.internal.presentation.annotation;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.cwi.examine.internal.data.DataSet;
import org.cwi.examine.internal.data.Network;
import org.cwi.examine.internal.model.Model;
import org.cwi.examine.internal.presentation.CategoryTable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by kdinkla on 10/22/16.
 */
public class AnnotationOverview extends HBox {

    private final Model model;

    public AnnotationOverview(final Model model) {
        this.model = model;

        final DataSet dataSet = model.getDataSet();
        dataSet.superNetwork.addListener((observable, oldValue, network) -> setupTables(network));
    }

    private void setupTables(final Network network) {
        final List<CategoryTable> tables =
                network.categories
                        .stream()
                        .map(category -> new CategoryTable(category))
                        .collect(Collectors.toList());
        tables.forEach(table -> VBox.setVgrow(table, Priority.ALWAYS));
        getChildren().setAll(tables);
    }
}
