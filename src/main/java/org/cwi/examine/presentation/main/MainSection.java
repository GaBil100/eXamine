package org.cwi.examine.presentation.main;

import javafx.scene.layout.Region;
import org.cwi.examine.data.csv.DataSet;
import org.cwi.examine.presentation.Section;

import static javafx.beans.binding.Bindings.bindContent;

public class MainSection implements Section {

    private final MainView view;
    private final MainViewModel viewModel;

    public MainSection(final DataSet dataSet) {
        this.view = new MainView();
        this.viewModel = new MainViewModel(dataSet);

        bindViewAndModel();
    }

    private void bindViewAndModel() {

        bindContent(view.getCategoryOverview().getCategories(), viewModel.orderedCategoriesProperty().get());

        view.getVisualization().networkProperty().bind(viewModel.activeNetworkProperty());
        bindContent(view.getVisualization().getActiveAnnotations(), viewModel.activeAnnotationListProperty());
        bindContent(view.getVisualization().getOpenedCategories(), viewModel.orderedCategoriesProperty());
    }

    @Override
    public void exit() {
        view.getVisualization().stop();
    }

    @Override
    public Region getView() {
        return view;
    }

}
