package org.cwi.examine.model;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.comparingDouble;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Category of network annotations.
 */
public class NetworkCategory {

    private static final Comparator<NetworkAnnotation> ANNOTATION_COMPARATOR =
            comparingDouble(NetworkAnnotation::getScore).thenComparing(NetworkAnnotation::getName);

    private final String name;   // Serves as unique identifier.
    private final List<NetworkAnnotation> annotations;

    public NetworkCategory(String name, List<NetworkAnnotation> annotations) {
        this.name = requireNonNull(name);
        this.annotations = unmodifiableList(annotations.stream().sorted(ANNOTATION_COMPARATOR).collect(toList()));
    }

    /**
     * Filter annotations to only contain nodes that pass the given predicate.
     * Annotations that have no nodes left will be filtered out as well.
     *
     * @param predicate Defines whether a node is to be included or not.
     * @return Category with non-empty annotations that contain only nodes that pass the given filter.
     */
    public NetworkCategory filterNodes(Predicate<NetworkNode> predicate) {
        return new NetworkCategory(
                getName(),
                getAnnotations().stream()
                        .map(annotation -> annotation.filterNodes(predicate))
                        .filter(annotation -> !annotation.getNodes().isEmpty())
                        .collect(toList()));
    }

    public String getName() {
        return name;
    }

    public List<NetworkAnnotation> getAnnotations() {
        return annotations;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + (this.getName() != null ? this.getName().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NetworkCategory other = (NetworkCategory) obj;
        if ((this.getName() == null) ? (other.getName() != null) : !this.getName().equals(other.getName())) {
            return false;
        }
        return true;
    }
}
