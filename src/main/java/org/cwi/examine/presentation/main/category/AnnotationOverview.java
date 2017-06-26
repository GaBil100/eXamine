package org.cwi.examine.presentation.main.category;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TabPane;
import org.cwi.examine.model.NetworkCategory;

import java.util.List;
import java.util.stream.Collectors;

public class AnnotationOverview extends TabPane {

    private ListProperty<NetworkCategory> categories = new SimpleListProperty<>(FXCollections.observableArrayList());

    public AnnotationOverview() {
        categories.addListener(this::onCategoriesChange);
    }

    private void onCategoriesChange(ListChangeListener.Change<? extends NetworkCategory> change) {

        final List<CategoryTab> tabs = categories.stream()
                .map(CategoryTab::new)
                .collect(Collectors.toList());

        getTabs().setAll(tabs);

        System.out.println("Tabs set: " + categories);
    }

    public ObservableList<NetworkCategory> getCategories() {
        return categories.get();
    }

    public ListProperty<NetworkCategory> categoriesProperty() {
        return categories;
    }

    public void setCategories(ObservableList<NetworkCategory> categories) {
        this.categories.set(categories);
    }
}
