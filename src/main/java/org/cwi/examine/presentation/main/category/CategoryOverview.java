package org.cwi.examine.presentation.main.category;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TabPane;
import org.cwi.examine.model.NetworkCategory;

import java.util.List;
import java.util.stream.Collectors;

public class CategoryOverview extends TabPane {

    private ObservableList<NetworkCategory> categories = FXCollections.observableArrayList();

    public CategoryOverview() {
        categories.addListener(this::onCategoriesChange);
    }

    private void onCategoriesChange(ListChangeListener.Change<? extends NetworkCategory> change) {

        final List<CategoryTab> tabs = categories.stream()
                .map(CategoryTab::new)
                .collect(Collectors.toList());

        getTabs().setAll(tabs);
    }

    public ObservableList<NetworkCategory> getCategories() {
        return categories;
    }
}
