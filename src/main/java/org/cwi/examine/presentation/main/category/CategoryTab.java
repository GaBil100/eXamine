package org.cwi.examine.presentation.main.category;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import org.cwi.examine.model.NetworkCategory;

/**
 *
 */
public class CategoryTab extends Tab {

    private final NetworkCategory category;

    private final ListView categoryList;

    public CategoryTab(final NetworkCategory category) {
        this.category = category;

        setClosable(false);

        setText(category.name);

        categoryList = new ListView<>(new ObservableListWrapper<>(category.annotations));

        setContent(categoryList);
    }

}
