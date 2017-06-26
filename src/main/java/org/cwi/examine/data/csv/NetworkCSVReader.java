package org.cwi.examine.data.csv;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;
import org.cwi.examine.model.*;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Pseudograph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CSV network loader.
 */
public class NetworkCSVReader {

    private static final String NODES_POSTFIX = ".nodes";
    private static final String LINKS_POSTFIX = ".links";
    private static final String ANNOTATIONS_POSTFIX = ".annotations";

    private final String filePath;

    public NetworkCSVReader(final String filePath) {
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

        // Annotations and categories.
        final Map<String, NetworkAnnotation> idToAnnotation = new HashMap<>();
        final Map<String, List<NetworkAnnotation>> categoryToAnnotations = new HashMap<>();
        for (final File file : resolveFiles(ANNOTATIONS_POSTFIX)) {
            loadAnnotations(file, idToAnnotation, categoryToAnnotations);
        }

        // Categories.
        final List<NetworkCategory> categories = new ArrayList<>();
        categoryToAnnotations.forEach((id, hAnnotations) ->
                categories.add(new NetworkCategory(id, hAnnotations)));

        // Links, for both node <-> node and node <-> annotation.
        for (final File file : resolveFiles(LINKS_POSTFIX)) {
            loadLinks(file, idToNode, idToAnnotation, superGraph);
        }

        return new Network(superGraph, categories);
    }

    private void loadNodes(final File file, final Map<String, NetworkNode> idToNode)
            throws FileNotFoundException {

        final Map<String, String> nodeColumns = new HashMap<>();
        nodeColumns.put("Identifier", "identifier");
        nodeColumns.put("Symbol", "name");
        nodeColumns.put("URL", "url");
        nodeColumns.put("Score", "logFC");

        final List<NodeEntry> nodeEntryBeans = csvToBean(file, NodeEntry.class, nodeColumns);
        //nodes.forEach(System.out::println);

        final List<NetworkNode> graphNodes = nodeEntryBeans.stream()
                .map(nodeEntry -> new NetworkNode(nodeEntry.getIdentifier(), nodeEntry.getName(), nodeEntry.getUrl(), nodeEntry.getScore()))
                .collect(Collectors.toList());

        mapIdToElement(graphNodes, idToNode);
    }

    private void loadAnnotations(final File file,
                                 final Map<String, NetworkAnnotation> idToAnnotation,
                                 final Map<String, List<NetworkAnnotation>> categoryToAnnotations)
            throws FileNotFoundException {

        final Map<String, String> annotationColumns = new HashMap<>();
        annotationColumns.put("Identifier", "identifier");
        annotationColumns.put("Symbol", "name");
        annotationColumns.put("URL", "url");
        annotationColumns.put("Score", "score");
        annotationColumns.put("Category", "category");

        final List<AnnotationEntry> annotationEntries = csvToBean(file, AnnotationEntry.class, annotationColumns);
        //annotationEntries.forEach(System.out::println);

        // Category <-> annotationEntries.
        annotationEntries.forEach(annotationEntry -> {
            final NetworkAnnotation hAnnotation = new NetworkAnnotation(
                    annotationEntry.getIdentifier(),
                    annotationEntry.getName(),
                    annotationEntry.getUrl(),
                    annotationEntry.getScore());
            final String category = annotationEntry.getCategory();

            idToAnnotation.put(annotationEntry.getIdentifier(), hAnnotation);
            categoryToAnnotations
                    .computeIfAbsent(category, k -> new ArrayList<>())
                    .add(hAnnotation);
        });
    }

    private void loadLinks(final File linkFile,
                           final Map<String, NetworkNode> idToNode,
                           final Map<String, NetworkAnnotation> idToAnnotation,
                           final UndirectedGraph<NetworkNode, DefaultEdge> graph)
            throws FileNotFoundException {

        final CSVReader csvReader = new CSVReader(new FileReader(linkFile), '\t');
        csvReader.forEach(ids -> {
            final String sourceId = ids[0]; // First column is link source.
            final NetworkNode sourceNode = idToNode.get(sourceId);
            final NetworkAnnotation sourceAnnotation = idToAnnotation.get(sourceId);

            // Remaining columns are link targets; one link per target.
            for (int i = 1; i < ids.length; i++) {
                final String targetId = ids[i];
                final NetworkNode targetNode = idToNode.get(targetId);
                final NetworkAnnotation targetAnnotation = idToAnnotation.get(targetId);

                // NodeEntry -> node.
                if (sourceNode != null && targetNode != null) {
                    graph.addEdge(sourceNode, targetNode);
                }
                // NodeEntry -> annotation.
                else if (sourceNode != null && targetAnnotation != null) {
                    sourceNode.addAnnotation(targetAnnotation);
                    targetAnnotation.addMember(sourceNode);
                }
                // AnnotationEntry -> node.
                else if (sourceAnnotation != null && targetNode != null) {
                    sourceAnnotation.addMember(targetNode);
                    targetNode.addAnnotation(sourceAnnotation);
                }
                // Invalid link: annotation -> annotation, or unknown identifiers.
                else {
                    System.err.println("Invalid link: " + sourceId + " -> " + targetId);
                }
            }
        });
    }

    private <T extends NetworkElement> void mapIdToElement(final List<T> elements, final Map<String, T> idToElement) {
        elements.forEach(e -> idToElement.put(e.getIdentifier(), e));
    }

    private List<File> resolveFiles(final String postFix) {

        final File dataRoot = new File(filePath);
        final File[] files = dataRoot.listFiles(file -> file.getName().endsWith(postFix));
        return Arrays.asList(files);
    }

    private <T> List<T> csvToBean(final File csvFile, final HeaderColumnNameMappingStrategy<T> strategy)
            throws FileNotFoundException {

        final CsvToBean<T> csvToBean = new CsvToBean<>();
        final CSVReader csvReader = new CSVReader(new FileReader(csvFile), '\t');
        return csvToBean.parse(strategy, csvReader);
    }

    private <T> List<T> csvToBean(
            final File csvFile,
            final Class<T> classToMap,
            final Map<String, String> columnToBeanNames)
            throws FileNotFoundException {

        final HeaderColumnNameTranslateMappingStrategy<T> strategy = new HeaderColumnNameTranslateMappingStrategy<>();
        strategy.setType(classToMap);
        strategy.setColumnMapping(columnToBeanNames);
        return csvToBean(csvFile, strategy);
    }

}
