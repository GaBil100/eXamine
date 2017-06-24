package org.cwi.examine.presentation.visualization;

import org.cwi.examine.graphics.Colors;
import org.cwi.examine.graphics.PVector;
import org.cwi.examine.graphics.StaticGraphics;
import org.cwi.examine.model.NetworkAnnotation;
import org.cwi.examine.model.Network;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static org.cwi.examine.graphics.StaticGraphics.*;

// GOTerm set label.
public class SetLabel extends SetRepresentation {

    public boolean opened;
    private final String text;
    private final String[] linedText;
    private final SetText setText;
    
    private String shortExponent;
    
    protected SetList parentList;

    public SetLabel(final Visualization visualization, final NetworkAnnotation element, String text) {
        super(visualization, element);

        this.opened = false;
        
        if(text == null) {
            text = element.toString();
        }
        
        textFont(org.cwi.examine.graphics.draw.Parameters.labelFont);
        String[] words = text.split(" ");
        ArrayList<String> lines = new ArrayList<>();
        for(int i = 0; i < words.length; i++) {
            String w = words[i];
            
            if(lines.size() < OverviewConstants.SET_LABEL_MAX_LINES) {
                if (textWidth(w) > OverviewConstants.SET_LABEL_MAX_WIDTH) {
                    lines.add(w.substring(0, OverviewConstants.SET_LABEL_MAX_WIDTH / (int) (0.75 * textHeight())) + "...");
                } else if(lines.isEmpty()) {
                    lines.add(w);
                } else {
                    String extW = lines.get(lines.size() - 1) + " " + w;
                    if(textWidth(extW) < OverviewConstants.SET_LABEL_MAX_WIDTH) {
                        lines.set(lines.size() - 1, extW);
                    } else {
                        lines.add(w);
                    }
                }
            }
        }
        this.linedText = lines.toArray(new String[]{});
        
        String txt = text;
        DecimalFormat df = new DecimalFormat("0.0E0");
        txt = df.format(element.score) + "  " + txt;
        this.text = txt;
        
        this.shortExponent = "-" + Double.toString(exponent(element.score));
        this.shortExponent = shortExponent.substring(0, shortExponent.length() - 2);
        
        this.setText = new SetText(element);
    }

    @Override
    public PVector dimensions() {
        textFont(org.cwi.examine.graphics.draw.Parameters.labelFont);
        
        return PVector.v(OverviewConstants.LABEL_PADDING + 2 * OverviewConstants.LABEL_MARKER_RADIUS + 2 * OverviewConstants.LABEL_DOUBLE_PADDING
                 + OverviewConstants.SET_LABEL_MAX_WIDTH
                 + OverviewConstants.LABEL_PADDING,
                 linedText.length * textHeight() + OverviewConstants.LABEL_DOUBLE_PADDING);
    }

    @Override
    public void draw() {
        PVector dim = dimensions();
        boolean hL = highlight();
        
        textFont(org.cwi.examine.graphics.draw.Parameters.labelFont);
        
        translate(topLeft);
        
        if(opened) {
            snippet(setText);
        }
        
        // Set marker.
        if(!opened && hL) {
            translate(0, !opened && hL ? 2 * OverviewConstants.LABEL_MARKER_RADIUS : 0);
        } else if(!opened) {
            translate(0, 0);
        } else {
            translate(OverviewConstants.LABEL_PADDING + OverviewConstants.LABEL_MARKER_RADIUS, 0.5 * dim.y);
        }

        final Network network = visualization.model.activeNetworkProperty().get();
        double maxRadius = 0.5 * textHeight() - 2;
        double minScoreExp = exponent(network.minAnnotationScore);
        double maxScoreExp = exponent(network.maxAnnotationScore);
        double scoreExp = exponent(element.score);
        double normScore = (scoreExp - maxScoreExp) / (minScoreExp - maxScoreExp);
        double radius = OverviewConstants.SCORE_MIN_RADIUS + (maxRadius - OverviewConstants.SCORE_MIN_RADIUS) * normScore;
        color(hL ? org.cwi.examine.graphics.draw.Parameters.containmentColor : Colors.grey(0.7));
        fillEllipse(0, 0, radius, radius);
        
        color(Color.WHITE);
        StaticGraphics.strokeWeight(1);
        drawEllipse(0, 0, radius, radius);
    }
    
    private double exponent(double value) {
        String[] formatStrings = new DecimalFormat("0.0E0").format(value).split("E");
        return formatStrings.length > 1 ? Math.abs(Double.valueOf(formatStrings[1])) : 0;
    }

    private boolean selected() { 
        return visualization.model.activeAnnotationMapProperty().containsKey(element);
    }

    // Delegate mouse wheel to parent list for scrolling.
    @Override
    public void mouseWheel(int rotation) {
        if(parentList != null) parentList.mouseWheel(rotation);
    }

    @Override
    public String toolTipText() {
        return text;
    }
    
    private class SetText extends SetRepresentation {
        
        public SetText(NetworkAnnotation element) {
            super(SetLabel.this.visualization, element);
        }

        @Override
        public PVector dimensions() {
            return PVector.v();
        }

        @Override
        public void draw() {
            PVector dim = SetLabel.this.dimensions();
            boolean hL = highlight();
            boolean selected = selected();
        
            // Background bubble.
            color(hL ? org.cwi.examine.graphics.draw.Parameters.containmentColor :
                 selected ? visualization.setColors.color(element) : Colors.grey(1f),
                 selected || hL ? 1f : 0f);
            fillRect(0f, 0f, dim.x, dim.y, OverviewConstants.LABEL_ROUNDING);
            
            // Score label.
            color(hL ? org.cwi.examine.graphics.draw.Parameters.textContainedColor :
                  selected ? org.cwi.examine.graphics.draw.Parameters.textHighlightColor : org.cwi.examine.graphics.draw.Parameters.textColor);

            textFont(org.cwi.examine.graphics.draw.Parameters.noteFont);
            text(shortExponent, 2 * OverviewConstants.LABEL_MARKER_RADIUS + 3, 0.5 * dim.y - OverviewConstants.LABEL_MARKER_RADIUS);

            // Set label.
            picking();
            textFont(org.cwi.examine.graphics.draw.Parameters.labelFont);
            translate(OverviewConstants.LABEL_PADDING + 2 * OverviewConstants.LABEL_MARKER_RADIUS + 2 * OverviewConstants.LABEL_DOUBLE_PADDING,
                      OverviewConstants.LABEL_PADDING);
            for(String line: linedText) {
                text(line);
                translate(0, textHeight());
            }
        }

        // Delegate to parent.
        @Override
        public void mouseWheel(int rotation) {
            if(selected()) {
                super.mouseWheel(rotation);
            } else {
                SetLabel.this.mouseWheel(rotation);
            }
        }
    }
}
