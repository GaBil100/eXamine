package org.cwi.examine.presentation.visualization;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.cwi.examine.model.NetworkAnnotation;

import java.awt.*;
import java.util.*;

// Selected set to getColor map.
public class SetColors implements ListChangeListener<NetworkAnnotation> {

    private final ObservableList<NetworkAnnotation> activeAnnotations = FXCollections.observableArrayList();

    private final Map<NetworkAnnotation, Color> predefinedColorMap;  // Predefined protein set to getColor mapping.
    private final Map<NetworkAnnotation, Color> colorMap;            // Dynamic protein set to getColor mapping.
    private final ArrayList<Color> availableColors;            // Available set colors.
    
    // Source getColor pool.
    public static final Color[] palette = new Color[] {
            new Color(141, 211, 199),
            new Color(255, 255, 179),
            new Color(190, 186, 218),
            new Color(251, 128, 114),
            new Color(128, 177, 211),
            new Color(253, 180, 98),
            new Color(252, 205, 229),
            new Color(188, 128, 189),
            new Color(204, 235, 197),
            new Color(255, 237, 111)
    };
    
    public SetColors() {

        colorMap = new HashMap<>();
        availableColors = new ArrayList<>();
        availableColors.addAll(Arrays.asList(SetColors.palette));
        
        // Predefined colors for expression sets (log FC and score derived).
        predefinedColorMap = new HashMap<>();
        
        // Listen to model and parameter changes.
        activeAnnotations.addListener(this);
    }

    public void onChanged(ListChangeListener.Change<? extends NetworkAnnotation> change) {
        Set<NetworkAnnotation> newActiveSets = new HashSet<>();
        newActiveSets.addAll(activeAnnotations);
        newActiveSets.removeAll(colorMap.keySet());
        newActiveSets.removeAll(predefinedColorMap.keySet());
        
        Set<NetworkAnnotation> newDormantSets = new HashSet<>();
        newDormantSets.addAll(colorMap.keySet());
        newDormantSets.removeAll(activeAnnotations);
        newDormantSets.removeAll(predefinedColorMap.keySet());
        
        // Assign colors to new active sets.
        for(NetworkAnnotation pS: newActiveSets) {
            colorMap.put(pS, availableColors.remove(0));
        }
        
        // Release colors of new dormant sets.
        for(NetworkAnnotation pS: newDormantSets) {
            availableColors.add(colorMap.remove(pS));
        }
    }
    
    // Get the getColor that has been assigned to the given set.
    public Color getColor(NetworkAnnotation annotation) {
        Color result = predefinedColorMap.get(annotation);
        
        if(result == null) {
            result = colorMap.get(annotation);
        }
        
        return result;
    }

    public ObservableList<NetworkAnnotation> getActiveAnnotations() {
        return activeAnnotations;
    }

}
