package org.cwi.examine.presentation.main.annotation;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import org.cwi.examine.model.NetworkAnnotation;
import org.cwi.examine.model.NetworkCategory;

/**
 * Created by kdinkla on 10/22/16.
 */
public class AnnotationList extends HBox {

    private final NetworkCategory category;

    public AnnotationList(final NetworkCategory category) {
        this.category = category;

        ListView<NetworkAnnotation> listView = new ListView<>(
                new ObservableListWrapper<>(category.annotations));
        getChildren().add(listView);
    }
}
