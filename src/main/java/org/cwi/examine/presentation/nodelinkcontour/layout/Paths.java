 package org.cwi.examine.presentation.nodelinkcontour.layout;

 import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.union.CascadedPolygonUnion;
import javafx.geometry.Point2D;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.PathElement;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.util.Collections.emptyList;

 /**
 * Geometry utility functions.
 */
public class Paths {

    public static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();  // JTS geometry factory.

    // Convert a JTS geometry to a Java shape.
    public static List<PathElement> geometryToShape(final Geometry geometry) {
        return geometry == null ? emptyList() : geometryToShape(geometry, 0);
    }

    public static List<PathElement> geometryToShape(final Geometry geometry, final double arcFactor) {

        final List<PathElement> pathElements = new ArrayList<>();
        geometryToShape(geometry, pathElements, arcFactor);
        return pathElements;
    }

    private static void geometryToShape(
            final Geometry geometry,
            final List<PathElement> pathElements,
            final double arcFactor) {

        if (geometry.getNumGeometries() > 1) {
            for (int i = 0; i < geometry.getNumGeometries(); i++) {
                geometryToShape(geometry.getGeometryN(i), pathElements, arcFactor);
            }
        } else if (geometry instanceof Polygon) {
            polygonToShape((Polygon) geometry, pathElements, arcFactor);
        }
    }

    // Attach JTS polygon to a shape.
    private static void polygonToShape(
            final Polygon polygon,
            final List<PathElement> path,
            final double arcFactor) {

        // Exterior ring.
        ringToShape(polygon.getExteriorRing(), path, arcFactor);

        // Interior rings.
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            ringToShape(polygon.getInteriorRingN(i), path, arcFactor);
        }
    }

    // Attach JTS ring to a shape.
    private static void ringToShape(
            final LineString string,
            final List<PathElement> pathElements,
            final double arcDegrees) {

        final Coordinate[] cs = string.getCoordinates();

        // Derive smooth arcs from sampled JTS arcs.
        if (arcDegrees > 0) {
            final Point2D[] vs = new Point2D[cs.length];
            for (int i = 0; i < cs.length; i++) {
                vs[i] = new Point2D(cs[cs.length - i - 1].x, cs[cs.length - i - 1].y);
            }

            // Similar region break points.
            final List<Integer> breakPoints = new ArrayList<>();
            for (int i = 0; i < vs.length - 1; i++) {

                final Point2D l = vs[i];
                final int mI = (i + 1) % (vs.length - 1);
                final Point2D m1 = vs[mI];
                final Point2D m2 = vs[(i + 2) % (vs.length - 1)];
                final Point2D r = vs[(i + 3) % (vs.length - 1)];

                final Point2D lm = m1.subtract(l);
                final Point2D m = m2.subtract(m1);
                final Point2D mr = r.subtract(m2);

                if (Math.abs(deltaAngle(lm, m) - deltaAngle(m, mr)) > arcDegrees) {
                    breakPoints.add(mI);
                }
            }

            // Construct path from similar regions.
            for (int i = 0; i < breakPoints.size(); i++) {

                // Breakpoint to ... + 1 is a straight line.
                final int firstBreak = breakPoints.get(i);
                final Point2D begin = vs[firstBreak % (vs.length - 1)];

                // Breakpoint to next breakpoint is arc, iff applicable.
                final int nextBreak = breakPoints.get((i + 1) % breakPoints.size()) % (vs.length - 1);
                int bD;
                for (bD = 0; (firstBreak + bD) % (vs.length - 1) != nextBreak; bD++) {
                }
                bD /= 2;
                final int midC = (firstBreak + bD) % (vs.length - 1);
                final Point2D mid = vs[midC];
                final Point2D end = vs[nextBreak];

                pathElements.addAll(getArc(begin, mid, end));
            }

            pathElements.add(new ClosePath());
        }
        // Path according to JTS samples.
        else {
            pathElements.add(new MoveTo(cs[0].x, cs[0].y));
            for (int j = 1; j < cs.length; j++) {
                pathElements.add(new LineTo(cs[j].x, cs[j].y));
            }
            pathElements.add(new ClosePath());
        }
    }

    // Shorthand for a fast and safe JTS union.
    public static Geometry fastUnion(final List<Geometry> gs) {
        return gs.isEmpty() ?
                GEOMETRY_FACTORY.createGeometryCollection(new Geometry[]{}) :
                new CascadedPolygonUnion(gs).union();
    }

    public static LineString circlePiece(
            final Point2D p1,
            final Point2D p2,
            final Point2D p3,
            final int segments) {

        final Point2D v21 = p2.subtract(p1);
        final double d21 = v21.dotProduct(v21);
        final Point2D v31 = p3.subtract(p1);
        final double d31 = v31.dotProduct(v31);
        final double a4 = 2 * v21.crossProduct(v31).getZ();

        final double d13 = p1.distance(p3);
        final boolean wellFormed = p1.distance(p2) < d13 && p2.distance(p3) < d13;

        LineString lS;
        if (false && wellFormed && Math.abs(a4) > 0.001) {
            final Point2D center = new Point2D(
                    p1.getX() + (v31.getY() * d21 - v21.getY() * d31) / a4,
                    p1.getY() + (v21.getX() * d31 - v31.getX() * d21) / a4
            );
            final double radius = Math.sqrt(
                    d21 * d31 *
                            (Math.pow(p3.getX() - p2.getX(), 2) + Math.pow(p3.getY() - p2.getY(), 2))
            ) / Math.abs(a4);

            double a1 = deltaAngle(center, p1);
            final double a2 = deltaAngle(center, p2);
            double a3 = deltaAngle(center, p3);
            if ((a2 < a1 && a2 < a3) || (a2 > a1 && a2 > a3)) {
                if (a1 < a3) {
                    a3 -= 2 * PI;
                } else {
                    a1 -= 2 * PI;
                }
            }

            final Coordinate[] cs = new Coordinate[segments];
            for (int i = 0; i < segments; i++) {
                final double fI = (double) i / (double) (segments - 1);
                final double aI = (1 - fI) * a1 + fI * a3;
                final Point2D vI = unitCirclePoint(aI).multiply(radius).add(center);
                cs[i] = new Coordinate(vI.getX(), vI.getY());
            }

            lS = GEOMETRY_FACTORY.createLineString(cs);
        }
        // There is no circle, so take a straight line between p0 and p1.
        else {
            final Coordinate[] cs = new Coordinate[2];
            cs[0] = new Coordinate(p1.getX(), p1.getY());
            cs[1] = new Coordinate(p3.getX(), p3.getY());
            lS = GEOMETRY_FACTORY.createLineString(cs);
        }

        return lS;
    }

    /**
     * Draw an arc through the three given points.
     *
     * @param p1 The start point of the arc.
     * @param p2 The point that the arc passes through.
     * @param p3 The end point of the arc.
     * @return An arc that passes through the three given points.
     */
    public static List<PathElement> getArc(final Point2D p1, final Point2D p2, final Point2D p3) {

        final List<PathElement> path = new ArrayList<>();
        path.add(new MoveTo(p1.getX(), p1.getY()));

        final Point2D v21 = p2.subtract(p1);
        final double d21 = v21.dotProduct(v21);
        final Point2D v31 = p3.subtract(p1);
        final double d31 = v31.dotProduct(v31);
        final double d13 = p1.distance(p3);

        final boolean wellFormed = p1.distance(p2) < d13 && p2.distance(p3) < d13;
        if (wellFormed) {
            final double a4 = 2 * v21.crossProduct(v31).getZ();
            final double radius = Math.sqrt(d21 * d31 *
                    (Math.pow(p3.getX() - p2.getX(), 2) + Math.pow(p3.getY() - p2.getY(), 2))) / Math.abs(a4);
            final boolean cross = p2.subtract(p1).crossProduct(p3.subtract(p2)).getZ() > 0;

            path.add(new ArcTo(radius, radius, 0, p3.getX(), p3.getY(), false, cross));
        }
        // There is no circle, so take a straight line between p0 and p1.
        else {
            path.add(new LineTo(p3.getX(), p3.getY()));
        }

        return path;
    }

    /**
     * Compute the angle, in radians, of the difference from origin to target.
     *
     * @param origin The point of origin.
     * @param target The target point; which lies at returned angle with respect to origin.
     * @return The angle from origin to target.
     */
    private static double deltaAngle(final Point2D origin, final Point2D target) {
        final Point2D delta = target.subtract(origin);
        return Math.atan2(delta.getX(), delta.getY());
    }

    /**
     * The point that lies on the unit circle at the given counter-clockwise angle with
     * respect to the origin and (0,1).
     *
     * @param angle The counter-clockwise angle between the returned point, the origin, and (0,1).
     * @return The point that lies on the unit circle.
     */
    private static Point2D unitCirclePoint(final double angle) {
        return new Point2D(cos(angle), sin(angle));
    }

}
