package org.cwi.examine.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import java.util.Set;

/**
 * Annotation of a group of network nodes.
 */
public class NetworkAnnotation extends NetworkElement {

    public final List<NetworkNode> elements = new ArrayList<>();
    public final Set<NetworkNode> set = new HashSet<>();

    public NetworkAnnotation(final String identifier, final String name, final String url, final double score) {
        super(identifier, name, url, score);
    }

    public void addMember(final NetworkNode node) {
        elements.add(node);
        set.add(node);
    }
    
}
