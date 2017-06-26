package org.cwi.examine.model;

public abstract class NetworkElement {

    private final String identifier;
    private final String name;
    private final String url;
    private final double score;

    NetworkElement(
            final String identifier,
            final String name,
            final String url,
            final double score) {
        this.identifier = identifier;
        this.name = name;
        this.url = url;
        this.score = score;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public double getScore() {
        return score;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NetworkElement)) return false;

        final NetworkElement that = (NetworkElement) o;

        return identifier.equals(that.identifier);
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }
}
