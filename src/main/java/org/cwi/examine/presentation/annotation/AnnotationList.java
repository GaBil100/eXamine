package org.cwi.examine.presentation.annotation;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import org.cwi.examine.model.HAnnotation;
import org.cwi.examine.model.HCategory;

/**
 * Created by kdinkla on 10/22/16.
 */
public class AnnotationList extends HBox {

    private final HCategory category;

    public AnnotationList(final HCategory category) {
        this.category = category;

        ListView<HAnnotation> listView = new ListView<>(
                new ObservableListWrapper<>(category.annotations));
        getChildren().add(listView);
    }
}
