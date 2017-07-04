package org.cwi.examine.model;

import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Pseudograph;
import org.jgrapht.graph.Subgraph;
import org.jgrapht.graph.UndirectedSubgraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.ListUtils.unmodifiableList;

/**
 * Network that wraps a graph with additional information.
 */
public class Network {

    private final UndirectedGraph<NetworkNode, DefaultEdge> graph;
    private final List<NetworkCategory> categories;
    private final List<NetworkAnnotation> annotations;
    private final NetworkCategory modules;
    private final double minNodeScore;
    private final double maxNodeScore;
    private final double minAnnotationScore;
    private final double maxAnnotationScore;

    public Network() {
        this(new Pseudograph<>(DefaultEdge.class), new ArrayList<>());
    }

    public Network(UndirectedGraph<NetworkNode, DefaultEdge> graph, List<NetworkCategory> categories) {

        this.graph = requireNonNull(graph);
        this.categories = unmodifiableList(categories.stream()
                .filter(category -> !category.getName().equals("Module"))
                .collect(toList()));
        this.annotations = unmodifiableList(categories.stream()
                .map(NetworkCategory::getAnnotations)
                .flatMap(List::stream)
                .collect(toList()));
        this.modules = categories.stream()
                .filter(category -> category.getName().equals("Module"))
                .findFirst()
                .orElse(new NetworkCategory("Module", Collections.emptyList()));

        Set<NetworkNode> nodes = graph.vertexSet();
        minNodeScore = nodes.stream().map(NetworkElement::getScore).min(Double::compare).orElse(0.);
        maxNodeScore = nodes.stream().map(NetworkElement::getScore).max(Double::compare).orElse(1.);
        minAnnotationScore = getAnnotations().stream().map(NetworkElement::getScore).min(Double::compare).orElse(0.);
        maxAnnotationScore = getAnnotations().stream().map(NetworkElement::getScore).max(Double::compare).orElse(1.);
    }

    /**
     * Induce sub network from super network.
     */
    public static Network induce(Set<NetworkNode> nodesToInclude, Network network) {

        // Verify whether entire subset is present in super network.
        nodesToInclude.stream()
                .filter(node -> !network.getGraph().containsVertex(node))
                .forEach(node -> System.err.println(
                        "Sub network node " + node + " not contained by super network."));

        final Graph<NetworkNode, DefaultEdge> subGraph = new Subgraph(network.getGraph(), nodesToInclude);
        final UndirectedGraph<NetworkNode, DefaultEdge> undirectedSubGraph =
                new UndirectedSubgraph(network.getGraph(), subGraph.vertexSet(), subGraph.edgeSet());

        final List<NetworkCategory> inducedCategories = network.getCategories().stream()
                .map(category -> category.filterNodes(nodesToInclude::contains))
                .collect(toList());

        return new Network(undirectedSubGraph, inducedCategories);
    }

    public static Network induce(NetworkCategory categoryToInclude, Network network) {
        Set<NetworkNode> unionNodes = new HashSet<>();
        categoryToInclude.getAnnotations().forEach(annotation -> unionNodes.addAll(annotation.getNodes()));
        return induce(unionNodes, network);
    }

    public static Network induce(NetworkAnnotation annotationToInclude, Network network) {
        return induce(annotationToInclude.getNodes(), network);
    }

    public UndirectedGraph<NetworkNode, DefaultEdge> getGraph() {
        return graph;
    }

    public List<NetworkCategory> getCategories() {
        return categories;
    }

    public List<NetworkAnnotation> getAnnotations() {
        return annotations;
    }

    public NetworkCategory getModules() {
        return modules;
    }

    public double getMinNodeScore() {
        return minNodeScore;
    }

    public double getMaxNodeScore() {
        return maxNodeScore;
    }

    public double getMinAnnotationScore() {
        return minAnnotationScore;
    }

    public double getMaxAnnotationScore() {
        return maxAnnotationScore;
    }
}
