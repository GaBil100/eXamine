package org.cwi.examine.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a node of interest.
 */
public class NetworkNode extends NetworkElement {

    public final Set<NetworkAnnotation> annotations;

    public NetworkNode(final String id, final String name, final String url, final double score) {
        super(id, name, url, score);
        this.annotations = new HashSet<>();
    }

}
