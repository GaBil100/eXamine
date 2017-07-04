package org.cwi.examine.presentation.nodelinkcontour;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableMap;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import org.cwi.examine.model.Network;
import org.cwi.examine.model.NetworkAnnotation;
import org.cwi.examine.model.NetworkNode;
import org.cwi.examine.presentation.nodelinkcontour.layout.Contours;
import org.cwi.examine.presentation.nodelinkcontour.layout.Layout;
import org.jgrapht.graph.DefaultEdge;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static javafx.beans.binding.Bindings.createObjectBinding;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.observableHashMap;

/**
 * Node, link, and contour depiction of a network with annotations.
 */
public class NodeLinkContourView extends ScrollPane {

    private final ObjectProperty<Network> network = new SimpleObjectProperty<>();
    private final ListProperty<NetworkAnnotation> selectedAnnotations = new SimpleListProperty<>(observableArrayList());
    private final MapProperty<NetworkAnnotation, Double> annotationWeights = new SimpleMapProperty<>(observableHashMap());

    private Layout layout = null;
    private final ObservableMap<NetworkNode, Point2D> nodePositions = observableHashMap();
    private final ObservableMap<DefaultEdge, Point2D[]> linkPositions = observableHashMap();

    private final ContourLayer contourLayer = new ContourLayer();
    private final NetworkElementLayer<DefaultEdge, Node> linkLayer =
            new NetworkElementLayer<>("network-link", this::createLinkRepresentation);
    private final NetworkElementLayer<NetworkNode, Node> nodeLayer =
            new NetworkElementLayer<>("network-node", this::createNodeRepresentation);
    private final Group layerStack = new Group(contourLayer, linkLayer, nodeLayer);

    public NodeLinkContourView() {

        getStyleClass().add("node-link-contour-view");

        final BorderPane layerContainer = new BorderPane(layerStack);
        BorderPane.setAlignment(layerStack, Pos.CENTER);
        setFitToHeight(true);
        setFitToWidth(true);
        setContent(layerContainer);

        network.addListener(this::onNetworkChange);
        selectedAnnotationsProperty().addListener((ListChangeListener) change -> updateLayout());
        contourLayer.annotationsProperty().bind(selectedAnnotations);
    }

    private void onNetworkChange(
            final ObservableValue<? extends Network> observable,
            final Network old,
            final Network network) {

        updateLayout();
        linkLayer.elementProperty().setAll(network.getGraph().edgeSet());
        nodeLayer.elementProperty().setAll(network.getGraph().vertexSet());
    }

    private void updateLayout() {

        if(network == null) {
            layout = null;
        } else {
            layout = new Layout(network.get(), annotationWeights, layout);

            final Map<NetworkNode, Point2D> newPositions = new HashMap<>();
            networkProperty().get().getGraph().vertexSet().forEach(node -> newPositions.put(node, layout.position(node)));
            nodePositions.clear();
            nodePositions.putAll(newPositions);

            linkPositions.clear();
            linkPositions.putAll(layout.linkPositions());

            contourLayer.contoursProperty().clear();
            contourLayer.contoursProperty().putAll(selectedAnnotations.stream()
                    .map(annotation -> new Contours(annotation, layout))
                    .collect(Collectors.toMap(Contours::getAnnotation, Function.identity())));
        }
    }

    private Node createLinkRepresentation(final DefaultEdge edge) {

        final LinkRepresentation representation = new LinkRepresentation(edge);
        representation.controlPointsProperty().bind(createObjectBinding(
                () -> FXCollections.observableList(asList(linkPositions.getOrDefault(edge, new Point2D[]{}))),
                linkPositions));

        return representation;
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

    public ObjectProperty<Network> networkProperty() {
        return network;
    }

    public ListProperty<NetworkAnnotation> selectedAnnotationsProperty() {
        return selectedAnnotations;
    }

    public MapProperty<NetworkAnnotation, Double> annotationWeightsProperty() {
        return annotationWeights;
    }

    public MapProperty<NetworkAnnotation, Color> annotationColorsProperty() {
        return contourLayer.colorsProperty();
    }

    @Override
    public String getUserAgentStylesheet() {
        return NodeLinkContourView.class.getResource("NodeLinkContourView.css").toExternalForm();
    }
}
