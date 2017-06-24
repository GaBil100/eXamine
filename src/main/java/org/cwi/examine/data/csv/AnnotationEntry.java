package org.cwi.examine.data.csv;

/**
 * Row annotation fields.
 */
public class AnnotationEntry extends ElementEntry {

    private String category;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "AnnotationEntry{" +
                "category='" + category +
                "} extends " + super.toString();
    }

}
