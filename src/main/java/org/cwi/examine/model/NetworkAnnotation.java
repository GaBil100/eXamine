package org.cwi.examine.model;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections.SetUtils.unmodifiableSet;

/**
 * Annotation of a group of network nodes.
 */
public class NetworkAnnotation extends NetworkElement {

    private final Set<NetworkNode> nodes;

    public NetworkAnnotation(String identifier, String name, String url, double score, Set<NetworkNode> nodes) {
        super(identifier, name, url, score);

        this.nodes = unmodifiableSet(new HashSet<>(nodes));
    }

    public NetworkAnnotation filterNodes(Predicate<NetworkNode> predicate) {
        return new NetworkAnnotation(
                getIdentifier(),
                getName(),
                getUrl(),
                getScore(),
                getNodes().stream().filter(predicate).collect(toSet()));
    }

    public Set<NetworkNode> getNodes() {
        return nodes;
    }

}
