package org.cwi.examine.presentation.nodelinkcontour;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.ListBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Rectangle;
import org.cwi.examine.model.Network;
import org.cwi.examine.model.NetworkAnnotation;
import org.cwi.examine.model.NetworkNode;
import org.cwi.examine.presentation.graphics.Geometry;
import org.cwi.examine.presentation.nodelinkcontour.layout.Layout;
import org.jgrapht.graph.DefaultEdge;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Node, link, and contour depiction of a network with annotations.
 */
public class NodeLinkContourView extends Pane {

    private final SimpleObjectProperty<Network> network = new SimpleObjectProperty<>();
    private final ObservableMap<NetworkAnnotation, Double> selectedAnnotations = FXCollections.observableHashMap();

    private Layout layout = null;
    private final ObservableMap<NetworkNode, Point2D> nodePositions = FXCollections.observableHashMap();
    private final ObservableMap<DefaultEdge, Point2D[]> linkPositions = FXCollections.observableHashMap();

    private final NetworkElementLayer<NetworkAnnotation, Node> annotationLayer =
            new NetworkElementLayer<>("network-annotation", this::createAnnotationRepresentation);
    private final NetworkElementLayer<DefaultEdge, Node> linkLayer =
            new NetworkElementLayer<>("network-link", this::createLinkRepresentation);
    private final NetworkElementLayer<NetworkNode, Node> nodeLayer =
            new NetworkElementLayer<>("network-node", this::createNodeRepresentation);

    public NodeLinkContourView() {

        getChildren().setAll(annotationLayer, linkLayer, nodeLayer);

        network.addListener(this::onNetworkChange);
        selectedAnnotations.addListener((MapChangeListener) change -> updateLayout());
    }

    private void onNetworkChange(
            final ObservableValue<? extends Network> observable,
            final Network old,
            final Network network) {

        updateLayout();
        linkLayer.getElements().setAll(network.graph.edgeSet());
        nodeLayer.getElements().setAll(network.graph.vertexSet());
    }

    private void updateLayout() {

        if(network == null) {
            layout = null;
        } else {
            layout = new Layout(network.get(), selectedAnnotations, layout);

            final Map<NetworkNode, Point2D> newPositions = new HashMap<>();
            getNetwork().graph.vertexSet().forEach(node -> newPositions.put(node, layout.position(node)));
            nodePositions.clear();
            nodePositions.putAll(newPositions);

            linkPositions.clear();
            linkPositions.putAll(layout.linkPositions());
        }
    }

    private Node createAnnotationRepresentation(final NetworkAnnotation annotation) {
        return new Rectangle(10, 10, 10, 10);
    }

    private Node createLinkRepresentation(final DefaultEdge edge) {

        final Path path = new Path();
        Bindings.bindContent(path.getElements(), new ListBinding<PathElement>() {

            {
                bind(linkPositions);
            }

            @Override
            protected ObservableList<PathElement> computeValue() {
                final Point2D[] positions = linkPositions.get(edge);
                return FXCollections.observableList(
                        positions == null ?
                                Collections.emptyList() :
                                Geometry.getArc(positions[0], positions[1], positions[2])
                );
            }
        });

        return path;
    }

    private Node createNodeRepresentation(final NetworkNode node) {

        final Label label = new Label(node.getName());

        // Bind coordinate to node layout.
        label.layoutXProperty().bind(bindNodeX(node));
        label.layoutYProperty().bind(bindNodeY(node));

        // Translate label to bring its layout coordinate to its center.
        label.translateXProperty().bind(label.widthProperty().multiply(-.5));
        label.translateYProperty().bind(label.heightProperty().multiply(-.5));

        return label;
    }

    private DoubleBinding bindNodeX(final NetworkNode node) {
        return Bindings.createDoubleBinding(() -> nodePositions.getOrDefault(node, Point2D.ZERO).getX(), nodePositions);
    }

    private DoubleBinding bindNodeY(final NetworkNode node) {
        return Bindings.createDoubleBinding(() -> nodePositions.getOrDefault(node, Point2D.ZERO).getY(), nodePositions);
    }

    public Network getNetwork() {
        return network.get();
    }

    public SimpleObjectProperty<Network> networkProperty() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network.set(network);
    }

    public ObservableMap<NetworkAnnotation, Double> getSelectedAnnotations() {
        return selectedAnnotations;
    }

    @Override
    public String getUserAgentStylesheet() {
        return NodeLinkContourView.class.getResource("NodeLinkContourView.css").toExternalForm();
    }
}
