package org.cwi.examine.presentation.graphics;

import javafx.geometry.Point2D;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.PathElement;

import java.util.ArrayList;
import java.util.List;

public class Geometry {

    public static List<PathElement> getArc(final Point2D p1, final Point2D p2, final Point2D p3) {

        final List<PathElement> path = new ArrayList<>();
        path.add(new MoveTo(p1.getX(), p1.getY()));

        final Point2D v21 = p2.subtract(p1);
        final double d21 = v21.dotProduct(v21);
        final Point2D v31 = p3.subtract(p1);
        final double d31 = v31.dotProduct(v31);
        final double d13 = p1.distance(p3);

        final boolean wellFormed = p1.distance(p2) < d13 && p2.distance(p3) < d13;
        if(wellFormed) {
            final double a4 = 2 * v21.crossProduct(v31).getZ();
            double radius = Math.sqrt(d21 * d31 *
                    (Math.pow(p3.getX() - p2.getX(), 2) + Math.pow(p3.getY() - p2.getY(), 2))) / Math.abs(a4);

            boolean cross = p2.subtract(p1).crossProduct(p3.subtract(p2)).getZ() > 0;
            path.add(new ArcTo(radius, radius, 0, p3.getX(), p3.getY(), false, cross));
        }
        // There is no circle, so take a straight line between p0 and p1.
        else {
            path.add(new LineTo(p3.getX(), p3.getY()));
        }

        return path;
    }

}
