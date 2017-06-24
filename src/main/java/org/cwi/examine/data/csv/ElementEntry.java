package org.cwi.examine.data.csv;

/**
 * Row element fields that occur for both nodes and annotations.
 */
abstract class ElementEntry {

    private String identifier;  // Unique identifier.
    private String name;        // User-friendly name.
    private String url;         // Reference url.
    private double score;       // Some significance score.

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "ElementEntry{" +
                "identifier='" + identifier + '\'' +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", score=" + score +
                '}';
    }
}
