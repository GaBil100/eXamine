package org.cwi.examine.presentation.main;

import javafx.scene.layout.Region;
import org.cwi.examine.model.Network;
import org.cwi.examine.presentation.Section;
import org.cwi.examine.presentation.main.annotation.AnnotationTabs;
import org.cwi.examine.presentation.nodelinkcontour.NodeLinkContourView;

public class MainSection implements Section {

    private final Network superNetwork;

    private final MainView view;
    private final MainViewModel viewModel;

    public MainSection(final Network superNetwork) {
        this.superNetwork = superNetwork;

        this.view = new MainView();
        this.viewModel = new MainViewModel();

        bindViewModel();

        initializeActiveNetwork();
    }

    private void bindViewModel() {

        final AnnotationTabs annotationTabs = view.getAnnotationOverview();
        annotationTabs.categoriesProperty().bindContent(viewModel.getCategories());
        annotationTabs.annotationColorsProperty().bind(viewModel.annotationColorProperty());
        annotationTabs.highlightedAnnotationsProperty().bind(viewModel.highlightedAnnotationsProperty());
        annotationTabs.onToggleAnnotationProperty().set(viewModel::toggleAnnotation);
        annotationTabs.onHighlightAnnotationsProperty().set(viewModel::highlightAnnotations);

        final NodeLinkContourView nodeLinkContourView = view.getNodeLinkContourView();
        nodeLinkContourView.networkProperty().bind(viewModel.activeNetworkProperty());
        nodeLinkContourView.selectedAnnotationsProperty().bind(viewModel.selectedAnnotationsProperty());
        nodeLinkContourView.annotationWeightsProperty().bind(viewModel.annotationWeightsProperty());
        nodeLinkContourView.annotationColorsProperty().bind(viewModel.annotationColorProperty());
        nodeLinkContourView.highlightedNodesProperty().bind(viewModel.highlightedNodesProperty());
        nodeLinkContourView.highlightedLinksProperty().bind(viewModel.highlightedLinksProperty());
    }

    /**
     * Initialize active network that is visualized. For now it is the union of all known modules.
     */
    private void initializeActiveNetwork() {

        final Network moduleNetwork = Network.induce(superNetwork.getModules(), superNetwork);

        viewModel.activateNetwork(moduleNetwork);
    }

    @Override
    public Region getView() {
        return view;
    }

}
