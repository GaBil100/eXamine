package org.cwi.examine.data.csv;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;
import org.cwi.examine.model.Network;
import org.cwi.examine.model.NetworkAnnotation;
import org.cwi.examine.model.NetworkCategory;
import org.cwi.examine.model.NetworkElement;
import org.cwi.examine.model.NetworkNode;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Pseudograph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * CSV network loader.
 */
public class NetworkCSVReader {

    private static final String NODES_POSTFIX = ".nodes";
    private static final String LINKS_POSTFIX = ".links";
    private static final String ANNOTATIONS_POSTFIX = ".annotations";

    private final String filePath;

    public NetworkCSVReader(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Load network from the csv files that can be found at filePath.
     */
    public Network readNetwork() throws FileNotFoundException {

        // Nodes.
        final Map<String, NetworkNode> idToNode = new HashMap<>();
        for (final File file : resolveFiles(NODES_POSTFIX)) {
            loadNodes(file, idToNode);
        }
        final UndirectedGraph<NetworkNode, DefaultEdge> superGraph = new Pseudograph<>(DefaultEdge.class);
        idToNode.values().forEach(node -> superGraph.addVertex(node));

        // Id to id links, for both node <-> node and node <-> annotation.
        final UndirectedGraph<String, DefaultEdge> idGraph = new Pseudograph<>(DefaultEdge.class);
        for (final File file : resolveFiles(LINKS_POSTFIX)) {
            loadLinks(file, idGraph);
        }

        // Resolve node <-> node links.
        idGraph.edgeSet().stream()
                .filter(edge -> idToNode.containsKey(idGraph.getEdgeSource(edge)) &&
                        idToNode.containsKey(idGraph.getEdgeTarget(edge)))
                .forEach(edge -> superGraph.addEdge(
                        idToNode.get(idGraph.getEdgeSource(edge)),
                        idToNode.get(idGraph.getEdgeTarget(edge)))
                );

        // Annotations and categories.
        final Map<String, NetworkAnnotation> idToAnnotation = new HashMap<>();
        final Map<String, List<NetworkAnnotation>> categoryToAnnotations = new HashMap<>();
        for (final File file : resolveFiles(ANNOTATIONS_POSTFIX)) {
            loadAnnotations(file, idToNode, idGraph, idToAnnotation, categoryToAnnotations);
        }

        // Categories.
        final List<NetworkCategory> categories = new ArrayList<>();
        categoryToAnnotations.forEach((id, hAnnotations) -> categories.add(new NetworkCategory(id, hAnnotations)));

        return new Network(superGraph, categories);
    }

    private void loadNodes(File file, Map<String, NetworkNode> idToNode)
            throws FileNotFoundException {

        final Map<String, String> nodeColumns = new HashMap<>();
        nodeColumns.put("Identifier", "identifier");
        nodeColumns.put("Symbol", "name");
        nodeColumns.put("URL", "url");
        nodeColumns.put("Score", "logFC");

        final List<NodeEntry> nodeEntryBeans = csvToBean(file, NodeEntry.class, nodeColumns);

        final List<NetworkNode> graphNodes = nodeEntryBeans.stream()
                .map(nodeEntry -> new NetworkNode(
                        nodeEntry.getIdentifier(),
                        nodeEntry.getName(),
                        nodeEntry.getUrl(),
                        nodeEntry.getScore()))
                .collect(toList());

        mapIdToElement(graphNodes, idToNode);
    }

    private void loadLinks(File linkFile, UndirectedGraph<String, DefaultEdge> idGraph) throws FileNotFoundException {

        final CSVReader csvReader = new CSVReader(new FileReader(linkFile), '\t');
        csvReader.forEach(ids -> {
            idGraph.addVertex(ids[0]);

            for (int i = 1; i < ids.length; i++) {
                idGraph.addVertex(ids[i]);
                idGraph.addEdge(ids[0], ids[i]);
            }
        });
    }

    private void loadAnnotations(File file,
                                 Map<String, NetworkNode> idToNode,
                                 UndirectedGraph<String, DefaultEdge> idGraph,
                                 Map<String, NetworkAnnotation> idToAnnotation,
                                 Map<String, List<NetworkAnnotation>> categoryToAnnotations)
            throws FileNotFoundException {

        final Map<String, String> annotationColumns = new HashMap<>();
        annotationColumns.put("Identifier", "identifier");
        annotationColumns.put("Symbol", "name");
        annotationColumns.put("URL", "url");
        annotationColumns.put("Score", "score");
        annotationColumns.put("Category", "category");

        final List<AnnotationEntry> annotationEntries = csvToBean(file, AnnotationEntry.class, annotationColumns);

        // Category <-> annotationEntries.
        annotationEntries.forEach(annotationEntry -> {

            final Set<NetworkNode> members = idGraph.containsVertex(annotationEntry.getIdentifier()) ?
                    Graphs.neighborListOf(idGraph, annotationEntry.getIdentifier())
                            .stream()
                            .filter(idToNode::containsKey)
                            .map(idToNode::get)
                            .collect(toSet()) :
                    emptySet();

            final NetworkAnnotation annotation = new NetworkAnnotation(
                    annotationEntry.getIdentifier(),
                    annotationEntry.getName(),
                    annotationEntry.getUrl(),
                    annotationEntry.getScore(),
                    members);

            idToAnnotation.put(annotationEntry.getIdentifier(), annotation);
            categoryToAnnotations
                    .computeIfAbsent(annotationEntry.getCategory(), k -> new ArrayList<>())
                    .add(annotation);
        });
    }

    private <T extends NetworkElement> void mapIdToElement(List<T> elements, Map<String, T> idToElement) {
        elements.forEach(e -> idToElement.put(e.getIdentifier(), e));
    }

    private List<File> resolveFiles(String postFix) {

        final File dataRoot = new File(filePath);
        final File[] files = dataRoot.listFiles(file -> file.getName().endsWith(postFix));
        return Arrays.asList(files);
    }

    private <T> List<T> csvToBean(File csvFile, HeaderColumnNameMappingStrategy<T> strategy)
            throws FileNotFoundException {

        final CsvToBean<T> csvToBean = new CsvToBean<>();
        final CSVReader csvReader = new CSVReader(new FileReader(csvFile), '\t');
        return csvToBean.parse(strategy, csvReader);
    }

    private <T> List<T> csvToBean(
            File csvFile,
            Class<T> classToMap,
            Map<String, String> columnToBeanNames)
            throws FileNotFoundException {

        final HeaderColumnNameTranslateMappingStrategy<T> strategy = new HeaderColumnNameTranslateMappingStrategy<>();
        strategy.setType(classToMap);
        strategy.setColumnMapping(columnToBeanNames);
        return csvToBean(csvFile, strategy);
    }

}
