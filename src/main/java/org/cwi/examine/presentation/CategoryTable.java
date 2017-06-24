package org.cwi.examine.presentation;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import org.cwi.examine.model.HAnnotation;
import org.cwi.examine.model.HCategory;

/**
 *
 */
public class CategoryTable extends TitledPane {

    private final HCategory category;

    public CategoryTable(final HCategory category) {
        this.category = category;

        setExpanded(false);
        setText(category.name);

        ListView<HAnnotation> listView = new ListView<>(
                new ObservableListWrapper<>(category.annotations));
        setContent(listView);
        listView.setMaxHeight(Double.MAX_VALUE);
    }
}
