package org.cwi.examine.presentation;

import javafx.scene.layout.Region;

/**
 * Section of the presentation.
 */
public interface Section {

    Region getView();

    void exit();

}
