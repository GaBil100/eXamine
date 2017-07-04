package org.cwi.examine.presentation.nodelinkcontour.layout;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.operation.union.CascadedPolygonUnion;
import javafx.geometry.Point2D;
import org.cwi.examine.model.NetworkAnnotation;
import org.cwi.examine.model.NetworkNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.cwi.examine.presentation.nodelinkcontour.layout.Paths.GEOMETRY_FACTORY;

/**
 * Generates annotation contours from a network layout.
 */
public class Contours {

    private final NetworkAnnotation annotation;
    private final Geometry ribbon;
    private final Geometry outline;

    public Contours(NetworkAnnotation annotation) {
        this.annotation = annotation;
        this.ribbon = GEOMETRY_FACTORY.buildGeometry(emptyList());
        this.outline = GEOMETRY_FACTORY.buildGeometry(emptyList());
    }

    public Contours(NetworkAnnotation annotation, Layout layout) {
        this.annotation = annotation;

        // Radius for smoothening contours.
        final double smoothRadius = 4 * Layout.RIBBON_EXTENT;

        final List<Geometry> vertexHulls = new ArrayList<>();
        for (NetworkNode v : annotation.getNodes()) {
            // Radius of set around vertex.
            final double vertexIndex = 1.01 + layout.nodeMemberships.get(v).indexOf(annotation);
            final double edgeRadius = vertexIndex * Layout.RIBBON_EXTENT + smoothRadius;

            // Radius of vertex (assuming rounded rectangle).
            final Point2D vertexBounds = Layout.labelDimensions(v, false);
            final Point2D vertexPos = layout.position(v);
            final double vertexRadius = 0.5 * vertexBounds.getY() + Layout.NODE_MARGIN;
            final double totalRadius = vertexRadius + edgeRadius;

            final Geometry line = GEOMETRY_FACTORY.createLineString(
                    new Coordinate[]{
                            new Coordinate(vertexPos.getX() - 0.5 * vertexBounds.getX(), vertexPos.getY()),
                            new Coordinate(vertexPos.getX() + 0.5 * vertexBounds.getX(), vertexPos.getY())
                    });
            final Geometry hull = line.buffer(totalRadius, Layout.BUFFER_SEGMENTS);

            vertexHulls.add(hull);
        }

        final List<Geometry> linkHulls = new ArrayList<>();
        for (final Layout.RichEdge e : layout.richGraph.edgeSet()) {
            int ind = e.memberships.indexOf(annotation);

            if (ind >= 0) {
                final Layout.RichNode sN = layout.richGraph.getEdgeSource(e);
                final Point2D sP = layout.position(sN.element);
                final Layout.RichNode tN = layout.richGraph.getEdgeTarget(e);
                final Point2D tP = layout.position(tN.element);
                final Layout.RichNode dN = e.subNode;
                final Point2D dP = layout.position(dN);
                final boolean hasCore = layout.network.getGraph().containsEdge(sN.element, tN.element);

                // Radius of set around vertex.
                final double edgeIndex = 0.51 + ind;
                final double edgeRadius = edgeIndex * Layout.RIBBON_EXTENT + smoothRadius +
                        (hasCore ? Layout.LINK_WIDTH + Layout.RIBBON_SPACE : 0);  // Widen for contained edge.

                final Geometry line = Paths.circlePiece(sP, dP, tP, Layout.LINK_SEGMENTS);
                final Geometry hull = line.buffer(edgeRadius, Layout.BUFFER_SEGMENTS);

                linkHulls.add(hull);
            }
        }

        // Vertex anti-membership hulls.
        final Set<NetworkNode> antiVertices = new HashSet<>(layout.network.getGraph().vertexSet());
        antiVertices.removeAll(annotation.getNodes());
        final List<Geometry> vertexAntiHulls = new ArrayList<Geometry>();
        for (final NetworkNode v : antiVertices) {
            // Radius of vertex (assuming rounded rectangle).
            final Point2D bounds = Layout.labelDimensions(v, false);
            final Point2D pos = layout.position(v);
            double radius = 0.5 * bounds.getY() + Layout.NODE_OUTLINE;

            final Geometry line = GEOMETRY_FACTORY.createLineString(
                    new Coordinate[]{
                            new Coordinate(pos.getX() - 0.5 * bounds.getX(), pos.getY()),
                            new Coordinate(pos.getX() + 0.5 * bounds.getX(), pos.getY())
                    });
            final Geometry hull = line.buffer(radius, Layout.BUFFER_SEGMENTS);

            vertexAntiHulls.add(hull);
        }

        Geometry vertexContour = convexHulls(Paths.fastUnion(vertexHulls));
        Geometry linkContour = Paths.fastUnion(linkHulls);
        Geometry fullContour = vertexContour.union(linkContour);
        Geometry smoothenedContour = fullContour.buffer(-smoothRadius, Layout.BUFFER_SEGMENTS);

        if (!vertexAntiHulls.isEmpty()) {
            Geometry antiContour = new CascadedPolygonUnion(vertexAntiHulls).union();
            smoothenedContour = smoothenedContour.difference(antiContour);
            // Safeguard link contours, TODO: fix anti-hull vs link cases.
            smoothenedContour = smoothenedContour.union(
                    linkContour.buffer(-smoothRadius, Layout.BUFFER_SEGMENTS));
        }

        Geometry innerContour = smoothenedContour.buffer(-Layout.RIBBON_WIDTH, Layout.BUFFER_SEGMENTS);
        Geometry ribbon = smoothenedContour.difference(innerContour);

        this.ribbon = ribbon;
        this.outline = smoothenedContour;
    }

    private static Geometry convexHulls(Geometry g) {
        int gN = g.getNumGeometries();

        List<Geometry> sG = new ArrayList<Geometry>();
        for (int i = 0; i < gN; i++) {
            sG.add(g.getGeometryN(i).convexHull());
        }

        return new CascadedPolygonUnion(sG).union();
    }

    public NetworkAnnotation getAnnotation() {
        return annotation;
    }

    public Geometry getRibbon() {
        return ribbon;
    }

    public Geometry getOutline() {
        return outline;
    }

}
