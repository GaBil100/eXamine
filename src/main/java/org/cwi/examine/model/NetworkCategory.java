package org.cwi.examine.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Category of elements.
 */
public class NetworkCategory {

    public static int MAXIMUM_SIZE = 50;

    public final String name;
    public final List<NetworkAnnotation> annotations;

    public NetworkCategory(final String name, final List<NetworkAnnotation> annotations) {
        this.name = name;

        // Sort annotations by score, then alphabet.
        final List<NetworkAnnotation> topAnnotations = new ArrayList<>(annotations);
        Collections.sort(topAnnotations, (lS, rS) -> {
            int result;

            if(lS.getScore() == rS.getScore()) {
                result = lS.getName().compareTo(rS.getName());
            } else {
                result = Double.isNaN(lS.getScore()) || lS.getScore() > rS.getScore() ? 1 : -1;
            }

            return result;
        });

        this.annotations = topAnnotations.subList(0, Math.min(topAnnotations.size(), MAXIMUM_SIZE));
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + (this.name != null ? this.name.hashCode() : 0);
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
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        return true;
    }
}
