package org.cwi.examine.presentation.visualization;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.ArrayList;

import org.cwi.examine.graphics.PVector;
import org.cwi.examine.graphics.StaticGraphics;
import org.cwi.examine.graphics.draw.Layout;
import org.cwi.examine.graphics.draw.Representation;
import org.cwi.examine.model.NetworkCategory;

import java.util.List;

// Visual list of significantly expressed GO terms of a specific domain.
public class SetList extends Representation<NetworkCategory> {
    private final Visualization visualization;
    private final List<SetLabel> labels;
    private int positionScroll;                     // Internal set list scroll.
    
    public SetList(final Visualization visualization, NetworkCategory element, List<SetLabel> labels) {
        super(element);

        this.visualization = visualization;
        this.labels = labels;
        this.positionScroll = 0;
        
        for(SetLabel l: labels) {
            l.parentList = this;
        }
    }

    @Override
    public PVector dimensions() {
        PVector dimensions;
        
        StaticGraphics.textFont(org.cwi.examine.graphics.draw.Parameters.font);
        
        double space = org.cwi.examine.graphics.draw.Parameters.spacing;
        PVector domainBounds = PVector.v(0.75 * StaticGraphics.textHeight() + StaticGraphics.textWidth(element.toString()),
                                 StaticGraphics.textHeight() + space + OverviewConstants.LABEL_BAR_HEIGHT + space);
        
        if(isOpened()) {
            double termHeight = Layout.bounds(labels).y;
            dimensions = PVector.v(Math.max(domainBounds.x, Layout.maxWidth(labels)),
                           domainBounds.y + termHeight);
        } else {
            dimensions = PVector.v(Math.max(domainBounds.x, Layout.maxWidth(labels)), domainBounds.y);
        }
        
        return dimensions;
    }

    @Override
    public void draw() {
        PVector dim = dimensions();
        
        // Category label.
        StaticGraphics.pushTransform();
        StaticGraphics.translate(topLeft);
        
        // Background rectangle to enable scrolling.
        StaticGraphics.picking();
        StaticGraphics.color(Color.WHITE);
        StaticGraphics.drawRect(0, 0, dim.x, dim.y);
        
        StaticGraphics.translate(0, StaticGraphics.textHeight());
        
        StaticGraphics.textFont(org.cwi.examine.graphics.draw.Parameters.font);
        StaticGraphics.color(isOpened() ? org.cwi.examine.graphics.draw.Parameters.textColor: org.cwi.examine.graphics.draw.Parameters.textColor.brighter().brighter());
        StaticGraphics.text(element.toString(), 0.75 * StaticGraphics.textHeight(), 0);
        
        // Arrows.
        StaticGraphics.pushTransform();
        double arrowRad = 0.25 * StaticGraphics.textHeight();
        double arrowTrunc = 0.25 * 0.85 * StaticGraphics.textHeight();
        double arrowMargin = 0.33 * arrowTrunc;
        
        StaticGraphics.translate(arrowRad, -arrowRad);
        StaticGraphics.rotate(isOpened() ? 0.5 * Math.PI : 0);
        
        Path2D arrows = new Path2D.Double();
        arrows.moveTo(-arrowRad, 0);
        arrows.lineTo(-arrowMargin, -arrowTrunc);
        arrows.lineTo(-arrowMargin, arrowTrunc);
        arrows.closePath();
        arrows.moveTo(arrowRad, 0);
        arrows.lineTo(arrowMargin, arrowTrunc);
        arrows.lineTo(arrowMargin, -arrowTrunc);
        arrows.closePath();
        
        StaticGraphics.fill(arrows);
        StaticGraphics.popTransform();
        
        //noPicking();
        
        StaticGraphics.popTransform();
        
        // Layout tagged set labels.
        List<SetLabel> taggedLabels = new ArrayList<>();    // Tagged set label representations.
        List<SetLabel> remainderLabels = new ArrayList<>(); // Set label representations.
        for(SetLabel lbl: labels) {
            (visualization.model.activeAnnotationMapProperty().containsKey(lbl.element) ?
                    taggedLabels :
                    remainderLabels).add(lbl);
        }
        
        PVector domainBounds = PVector.v(StaticGraphics.textWidth(element.toString()),
                                 StaticGraphics.textHeight() + org.cwi.examine.graphics.draw.Parameters.spacing);
        PVector topTaggedPos = PVector.add(topLeft, domainBounds.Y());
        
        PVector labelPos = topTaggedPos;
        for(int i = 0; i < taggedLabels.size(); i++) {
            SetLabel label = taggedLabels.get(i);
            PVector labelDim = label.dimensions();
            
            label.opened = true;
            label.topLeft(labelPos);
            labelPos = PVector.add(labelPos, PVector.v(0, labelDim.y + 2));
        }
        
        // Layout remaining set labels.
        int skipCount = isOpened() ? positionScroll : remainderLabels.size();
        PVector topBarPos = PVector.add(labelPos,
                                        PVector.v(0, taggedLabels.isEmpty() ? 0 : StaticGraphics.textHeight()));
        PVector topListPos = PVector.add(topBarPos, PVector.v(0,
                    skipCount > 0 ? OverviewConstants.LABEL_BAR_HEIGHT + org.cwi.examine.graphics.draw.Parameters.spacing : 0));
        PVector bottomBarPos = null;
        
        double barIncrement = Math.min(
            2 * OverviewConstants.LABEL_MARKER_RADIUS + 2,
            dim.x / (double) remainderLabels.size()
        );
        
        int i;
        
        // Place in top bar.
        for(i = 0; i < skipCount && i < remainderLabels.size(); i++) {
            SetLabel label = remainderLabels.get(i);
            label.opened = false;
            label.topLeft(PVector.v(topBarPos.x + OverviewConstants.LABEL_MARKER_RADIUS + i * barIncrement,
                            topBarPos.y + OverviewConstants.LABEL_MARKER_RADIUS));
        }
        
        // Place in mid section, as full.
        labelPos = topListPos;
        for(i = skipCount; i < remainderLabels.size(); i++) {
            SetLabel label = remainderLabels.get(i);
            PVector labelDim = label.dimensions();
            
            label.opened =
                topLeft.y + labelPos.y + 2 * labelDim.y +
                OverviewConstants.LABEL_BAR_HEIGHT + org.cwi.examine.graphics.draw.Parameters.spacing < OverviewConstants.sceneHeight();
            
            if(label.opened) {
                label.topLeft(labelPos);
            }
            // Place in bottom bar.
            else {
                if(bottomBarPos == null) {
                    bottomBarPos = labelPos;
                }
                
                label.topLeft(PVector.v(topBarPos.x + OverviewConstants.LABEL_MARKER_RADIUS + i * barIncrement,
                                bottomBarPos.y + 2 * OverviewConstants.LABEL_MARKER_RADIUS));
            }
            
            labelPos = PVector.add(labelPos, PVector.v(0, labelDim.y + 2));
        }
        
        StaticGraphics.snippets(remainderLabels);
        StaticGraphics.snippets(taggedLabels);
    }
    
    public boolean isOpened() {
        return visualization.model.openedCategoriesProperty().get().contains(element);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if(isOpened()) {
            visualization.model.openedCategoriesProperty().remove(element);
        } else {
            visualization.model.openedCategoriesProperty().add(element);
        }
    }

    @Override
    public void mouseWheel(int rotation) {
        positionScroll = Math.max(0, Math.min(labels.size() - 1, positionScroll + rotation));
    }
}
