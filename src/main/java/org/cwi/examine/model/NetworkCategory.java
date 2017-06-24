package org.cwi.examine.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Category of elements.
 */
public class NetworkCategory<E extends NetworkElement> {

    public static int MAXIMUM_SIZE = 50;

    public final String name;
    public final List<E> annotations;

    public NetworkCategory(final String name, final List<E> annotations) {
        this.name = name;

        // Sort annotations by score, then alphabet.
        final List<E> topAnnotations = new ArrayList<>(annotations);
        Collections.sort(topAnnotations, (lS, rS) -> {
            int result;

            if(lS.score == rS.score) {
                result = lS.name.compareTo(rS.name);
            } else {
                result = Double.isNaN(lS.score) || lS.score > rS.score ? 1 : -1;
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
