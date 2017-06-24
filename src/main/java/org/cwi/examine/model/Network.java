package org.cwi.examine.model;

import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Pseudograph;
import org.jgrapht.graph.Subgraph;
import org.jgrapht.graph.UndirectedSubgraph;

import java.util.*;

/**
 * Network that wraps a graph with additional information.
 */
public class Network {

    public final UndirectedGraph<NetworkNode, DefaultEdge> graph;
    public final List<NetworkCategory<NetworkAnnotation>> categories;
    public final List<NetworkAnnotation> annotations;
    public final NetworkCategory modules;
    public final double minNodeScore, maxNodeScore;
    public final double minAnnotationScore, maxAnnotationScore;

    public Network() {
        this(new Pseudograph<>(DefaultEdge.class), new ArrayList<>());
    }

    public Network(final UndirectedGraph<NetworkNode, DefaultEdge> graph,
                   final List<NetworkCategory<NetworkAnnotation>> categories) {
        this.graph = graph;
        this.categories = new ArrayList<>(categories);
        this.categories.removeIf(category -> category.name.equals("Module"));
        this.annotations = new ArrayList<>();
        categories.forEach(category -> annotations.addAll(category.annotations));
        this.modules = categories.stream()
                .filter(category -> category.name.equals("Module"))
                .findFirst()
                .orElse(new NetworkCategory("Module", Collections.emptyList()));

        Set<NetworkNode> nodes = graph.vertexSet();
        minNodeScore = nodes.stream().map(n -> n.score).min(Double::compare).orElse(0.);
        maxNodeScore = nodes.stream().map(n -> n.score).max(Double::compare).orElse(1.);
        minAnnotationScore = annotations.stream().map(a -> a.score).min(Double::compare).orElse(0.);
        maxAnnotationScore = annotations.stream().map(a -> a.score).max(Double::compare).orElse(1.);
    }
    
    /**
     * Induce sub network from super network.
     */
    public static Network induce(final Set<NetworkNode> nodesToInclude, final Network network) {
        // Verify whether entire subset is present in super network.
        nodesToInclude.stream()
                .filter(node -> !network.graph.containsVertex(node))
                .forEach(node -> System.err.println(
                        "Sub network node " + node + " not contained by super network."));

        final Graph<NetworkNode, DefaultEdge> subGraph = new Subgraph(network.graph, nodesToInclude);
        final UndirectedGraph<NetworkNode, DefaultEdge> undirectedSubGraph =
                new UndirectedSubgraph(network.graph, subGraph.vertexSet(), subGraph.edgeSet());

        return new Network(undirectedSubGraph, network.categories);
    }

    public static Network induce(final NetworkCategory<NetworkAnnotation> categoryToInclude,
                                 final Network network) {
        Set<NetworkNode> unionNodes = new HashSet<>();
        categoryToInclude.annotations.forEach(annotation -> unionNodes.addAll(annotation.set));
        return induce(unionNodes, network);
    }

    public static Network induce(final NetworkAnnotation annotationToInclude, final Network network) {
        return induce(annotationToInclude.set, network);
    }
}
