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

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 19 * hash + this.name.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final NetworkAnnotation other = (NetworkAnnotation) obj;
        if(!this.name.equals(other.name)) {
            return false;
        }
        return true;
    }
    
}
