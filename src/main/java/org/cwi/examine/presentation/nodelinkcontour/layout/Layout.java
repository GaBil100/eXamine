package org.cwi.examine.presentation.nodelinkcontour.layout;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.text.Text;
import org.cwi.examine.model.Network;
import org.cwi.examine.model.NetworkAnnotation;
import org.cwi.examine.model.NetworkNode;
import org.cwi.examine.presentation.nodelinkcontour.layout.dwyer.cola.Descent;
import org.cwi.examine.presentation.nodelinkcontour.layout.dwyer.vpsc.Constraint;
import org.cwi.examine.presentation.nodelinkcontour.layout.dwyer.vpsc.Solver;
import org.cwi.examine.presentation.nodelinkcontour.layout.dwyer.vpsc.Variable;
import org.jgrapht.Graph;
import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.alg.PrimMinimumSpanningTree;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.*;

import static java.util.Comparator.comparingInt;

public class Layout {

    public static final double RIBBON_WIDTH = 8;
    public static final double RIBBON_SPACE = 2;
    public static final double RIBBON_EXTENT = RIBBON_WIDTH + RIBBON_SPACE;
    public static final double LINK_WIDTH = 3;
    public static final double NODE_OUTLINE = 4;
    public static final double NODE_SPACE = 2;
    public static final double NODE_MARGIN = 0.5 * NODE_OUTLINE + NODE_SPACE;
    public static final int BUFFER_SEGMENTS = 5;
    public static final int LINK_SEGMENTS = 10; //20;

    private static final double EDGE_SPACE = 50;
    private static final int INITIAL_ITERATIONS = 100000;
    private static final int PHASE_ITERATIONS = 10000;
    private static final double SET_EDGE_CONTRACTION = 0.5;

    // Network and set topology.
    public final Network network;
    public final Map<NetworkAnnotation, Double> weightedAnnotations = new HashMap<>();

    public final List<NetworkAnnotation> sets = new ArrayList<>();
    public final NetworkNode[] nodes;
    public final Map<NetworkNode, List<NetworkAnnotation>> nodeMemberships = new HashMap<>();

    // Spanning set graphs.
    private WeightedGraph<NetworkNode, DefaultEdge> minDistGraph;
    private List<Graph<NetworkNode, DefaultEdge>> spanGraphs;
    public WeightedGraph<RichNode, RichEdge> richGraph;
    private WeightedGraph<RichNode, RichEdge> extRichGraph;
    private RichNode[] richNodes;

    // Descent layout.
    private Map<NetworkNode, Integer> index;
    private Map<RichNode, Integer> richIndex;
    private double[] baseDilations;
    private double[] radii;
    private double[][] mD;
    private double[][] P;
    private double[][] D;
    private double[][] G;
    private Descent descent;

    public Layout(Network network, Map<NetworkAnnotation, Double> weightedAnnotations, Layout oldLayout) {
        this.network = network;
        this.weightedAnnotations.putAll(weightedAnnotations);

        // Order annotations by size.
        this.sets.addAll(weightedAnnotations.keySet());
        Collections.sort(this.sets, comparingInt(s2 -> s2.getNodes().size()));

        // Invert set membership for vertices.
        this.nodes = network.getGraph().vertexSet().toArray(new NetworkNode[]{});
        for (NetworkNode n : nodes) {
            nodeMemberships.put(n, new ArrayList<>());
        }
        for (NetworkAnnotation s : sets) {
            for (NetworkNode n : s.getNodes()) {
                nodeMemberships.get(n).add(s);
            }
        }

        updatePositions(oldLayout);
    }

    public boolean updatePositions() {
        return updatePositions(null);
    }

    private final boolean updatePositions(final Layout oldLayout) {
        boolean converged;
        int vN = nodes.length;

        if (index == null) {
            index = new HashMap<>();
            for (int i = 0; i < vN; i++) index.put(nodes[i], i);

            // Vertex line radii (width / 2) and base dilations (based on bounds height).
            baseDilations = new double[vN];
            radii = new double[vN];
            for (int i = 0; i < vN; i++) {
                baseDilations[i] = 0.5 * labelSpacedDimensions(nodes[i]).getY();
                radii[i] = 0.5 * labelSpacedDimensions(nodes[i]).getX();
            }

            // Vertex to vertex minimum distance (based on set memberships).
            mD = new double[vN][vN];
            for (int i = 0; i < vN; i++) {
                double dil1 = baseDilations[i];
                for (int j = i + 1; j < vN; j++) {
                    double dil2 = baseDilations[j];
                    mD[i][j] = mD[j][i] =
                            dil1 + dil2 + 2 * NODE_SPACE +
                                    RIBBON_EXTENT * membershipDiscrepancy(nodes[i], nodes[j]);
                }
            }

            // Construct set spanning graphs.
            initializeSetGraphs();

            // Update shortest path matrix to rich graph.
            vN = richNodes.length;
            FloydWarshallShortestPaths paths = new FloydWarshallShortestPaths(extRichGraph);
            D = new double[vN][vN];
            for (int i = 0; i < vN; i++)
                for (int j = i + 1; j < vN; j++)
                    D[i][j] = D[j][i] = paths.shortestDistance(richNodes[i], richNodes[j]);

            // Vertex positions start at (0,0), or at position of previous layout.
            P = new double[2][vN];
            for (int i = 0; i < nodes.length; i++) {
                Point2D pos = oldLayout == null ? new Point2D(0, 0) : oldLayout.position(richNodes[i]);
                P[0][i] = pos.getX();
                P[1][i] = pos.getY();
            }

            // Gradient descent.
            G = new double[vN][vN];
            for (int i = 0; i < vN; i++)
                for (int j = i; j < vN; j++)
                    G[i][j] = G[j][i] =
                            extRichGraph.containsEdge(richNodes[i], richNodes[j]) ||
                                    network.getGraph().containsEdge(richNodes[i].element, richNodes[j].element) ? 1 : 2;
            descent = new Descent(P, D, null);

            // Apply initialIterations without user constraints or non-overlap constraints.
            descent.run(INITIAL_ITERATIONS);

            // Initialize vertex and contour bound respecting projection.
            // TODO: convert to rich graph form.
            descent.project = new BoundProjection(radii, mD).projectFunctions();

            // Allow not immediately connected (by direction) nodes to relax apart (p-stress).
            descent.G = G;
            descent.run(PHASE_ITERATIONS);

            converged = false;
        }
        // Improve layout.
        else {
            converged = descent.run(PHASE_ITERATIONS);
        }

        // Measure span and shift nodes top left to (0,0).
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;
        for (int i = 0; i < vN; i++) {
            minX = Math.min(minX, P[0][i]);
            minY = Math.min(minY, P[1][i]);
            maxX = Math.max(maxX, P[0][i]);
            maxY = Math.max(maxY, P[1][i]);
        }

        for (int i = 0; i < vN; i++) {
            P[0][i] -= minX;
            P[1][i] -= minY;
        }

        return converged;
    }

    // Position of the given node, (0,0) iff null.
    public Point2D position(final NetworkNode node) {
        Point2D result;

        if (index == null) {
            result = new Point2D(0, 0);
        } else {
            Integer i = index.get(node);
            result = i == null ? new Point2D(0, 0) : new Point2D(P[0][i], P[1][i]);
        }

        return result;
    }


    // Position of the given node, (0,0) iff null.
    public Point2D position(final RichNode node) {
        Point2D result;

        if (richIndex == null) {
            result = Point2D.ZERO;
        } else {
            Integer i = richIndex.get(node);
            result = i == null ? Point2D.ZERO : new Point2D(P[0][i], P[1][i]);
        }

        return result;
    }

    public Map<DefaultEdge, Point2D[]> linkPositions() {

        final Map<DefaultEdge, Point2D[]> positionMap = new HashMap<>();

        for (final RichEdge richEdge : richGraph.edgeSet()) {

            final Point2D[] positions = new Point2D[3];
            positions[0] = position(richGraph.getEdgeSource(richEdge).element);
            positions[1] = position(richEdge.subNode);
            positions[2] = position(richGraph.getEdgeTarget(richEdge).element);

            positionMap.put(richEdge.edge, positions);
        }

        return positionMap;
    }

    // Position of the given node, (0,0) iff null.
//    public Point2D position(RichNode node) {
//        Point2D result;
//
//        if(richIndex == null) {
//            result = new Point2D(0, 0);
//        } else {
//            Integer i = richIndex.get(node);
//            result = i == null ? new Point2D(0, 0) : new Point2D(P[0][i], P[1][i]);
//        }
//
//        return result;
//    }

    private void initializeSetGraphs() {
        int vN = nodes.length;

        // Minimum guaranteed distance graph.
        minDistGraph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        for (NetworkNode v : network.getGraph().vertexSet()) {
            minDistGraph.addVertex(v);
        }
        for (DefaultEdge e : network.getGraph().edgeSet()) {
            NetworkNode s = network.getGraph().getEdgeSource(e);
            int sI = index.get(s);
            NetworkNode t = network.getGraph().getEdgeTarget(e);
            int tI = index.get(t);
            DefaultEdge nE = minDistGraph.addEdge(s, t);
            minDistGraph.setEdgeWeight(nE, EDGE_SPACE + mD[sI][tI]);
        }

        // Construct shortest path distance matrix on original graph,
        // for distance graph and node overlap constraints.
        FloydWarshallShortestPaths paths = new FloydWarshallShortestPaths(minDistGraph);
        D = new double[vN][vN];
        for (int i = 0; i < vN; i++)
            for (int j = i + 1; j < vN; j++)
                D[i][j] = D[j][i] = paths.shortestDistance(nodes[i], nodes[j]);

        // Spanning graph per set.
        spanGraphs = new ArrayList<>();
        for (NetworkAnnotation set : sets) {
            SimpleWeightedGraph<NetworkNode, DefaultEdge> weightedSubGraph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
            for (NetworkNode v : set.getNodes()) {
                weightedSubGraph.addVertex(v);
            }
            Set<DefaultEdge> coreEdges = new HashSet<>();
            final List<NetworkNode> nodes = new ArrayList<>(set.getNodes());
            for (int i = 0; i < nodes.size(); i++) {
                final NetworkNode s = nodes.get(i);

                for (int j = i + 1; j < set.getNodes().size(); j++) {
                    final NetworkNode t = nodes.get(j);
                    final DefaultEdge nE = weightedSubGraph.addEdge(s, t);

                    // Guarantee MST along already present edges.
                    final boolean isCore = network.getGraph().containsEdge(s, t);
                    weightedSubGraph.setEdgeWeight(nE, isCore ? 0 : D[index.get(s)][index.get(t)]);
                    if (isCore) {
                        coreEdges.add(nE);
                    }
                }
            }

            // Combine spanning and core edges into set spanning graph.
            SimpleGraph<NetworkNode, DefaultEdge> spanGraph = new SimpleGraph<>(DefaultEdge.class);
            for (NetworkNode v : set.getNodes()) {
                spanGraph.addVertex(v);
            }
            for (DefaultEdge e : coreEdges) {
                spanGraph.addEdge(weightedSubGraph.getEdgeSource(e), weightedSubGraph.getEdgeTarget(e));
            }

            if (!weightedSubGraph.edgeSet().isEmpty()) {
                Set<DefaultEdge> spanningEdges = new PrimMinimumSpanningTree<>(weightedSubGraph).getMinimumSpanningTreeEdgeSet();
                for (DefaultEdge e : spanningEdges) {
                    spanGraph.addEdge(weightedSubGraph.getEdgeSource(e), weightedSubGraph.getEdgeTarget(e));
                }
            }

            spanGraphs.add(spanGraph);
        }

        // Construct rich graph (containing all membership information).
        richGraph = new SimpleWeightedGraph<RichNode, RichEdge>(RichEdge.class);
        richIndex = new HashMap<RichNode, Integer>();

        // Base nodes.
        for (int i = 0; i < nodes.length; i++) {
            NetworkNode n = nodes[i];
            RichNode rN = new RichNode(n);
            rN.memberships.addAll(nodeMemberships.get(n));
            richGraph.addVertex(rN);
        }
        // Add all core edges.
        for (DefaultEdge e : network.getGraph().edgeSet()) {
            RichNode rSN = new RichNode(network.getGraph().getEdgeSource(e));
            RichNode rTN = new RichNode(network.getGraph().getEdgeTarget(e));
            RichEdge rE = richGraph.addEdge(rSN, rTN);
            rE.edge = e;
            rE.core = true;
            richGraph.setEdgeWeight(rE, D[index.get(rSN.element)][index.get(rTN.element)]);
        }
        // Add all set span edges.
        for (int i = 0; i < sets.size(); i++) {
            NetworkAnnotation annotation = sets.get(i);
            Graph<NetworkNode, DefaultEdge> sG = spanGraphs.get(i);

            for (DefaultEdge e : sG.edgeSet()) {
                RichNode rSN = new RichNode(sG.getEdgeSource(e));
                RichNode rTN = new RichNode(sG.getEdgeTarget(e));
                RichEdge rE = richGraph.addEdge(rSN, rTN);

                if (rE == null) {
                    rE = richGraph.getEdge(rSN, rTN);
                } else {
                    rE.core = false;
                    int rSI = index.get(rSN.element);
                    int rTI = index.get(rTN.element);
                    richGraph.setEdgeWeight(rE, Math.max(mD[rSI][rTI],
                            (SET_EDGE_CONTRACTION / weightedAnnotations.get(annotation)) * D[rSI][rTI]));
                }
                //rE.memberships.add(s);
            }
        }
        // Infer edge to set memberships from matching vertices.
        for (RichEdge e : richGraph.edgeSet()) {
            RichNode rSN = richGraph.getEdgeSource(e);
            RichNode rTN = richGraph.getEdgeTarget(e);
            e.memberships.addAll(this.nodeMemberships.get(rSN.element));
            e.memberships.retainAll(this.nodeMemberships.get(rTN.element));
        }

        // Construct rich graph that has been extended by one dummy node per edge.
        richNodes = new RichNode[vN + richGraph.edgeSet().size()];
        extRichGraph = new SimpleWeightedGraph<RichNode, RichEdge>(RichEdge.class);
        // Base nodes.
        for (int i = 0; i < nodes.length; i++) {
            NetworkNode n = nodes[i];
            RichNode rN = new RichNode(n);
            richNodes[i] = rN;
            richIndex.put(rN, i);
            extRichGraph.addVertex(rN);
        }
        // Add edges, but include additional dummy node.
        int j = 0;
        for (RichEdge e : richGraph.edgeSet()) {
            RichNode rSN = richGraph.getEdgeSource(e);
            RichNode rTN = richGraph.getEdgeTarget(e);

            RichNode dN = new RichNode(null);
            extRichGraph.addVertex(dN);
            e.subNode = dN;
            richNodes[nodes.length + j] = dN;
            richIndex.put(dN, nodes.length + j);

            RichEdge sE = extRichGraph.addEdge(rSN, dN);
            sE.core = e.core;
            RichEdge tE = extRichGraph.addEdge(dN, rTN);
            tE.core = e.core;

            double hW = 0.5 * richGraph.getEdgeWeight(e);
            extRichGraph.setEdgeWeight(sE, hW);
            extRichGraph.setEdgeWeight(tE, hW);

            j++;
        }
    }

    // Dimensions of drawn node label.
    public static Point2D labelDimensions(NetworkNode node, boolean padding) {
        final Bounds bounds = new Text(node.getName()).getBoundsInLocal();
        return new Point2D(
                bounds.getWidth() + (padding ? bounds.getHeight() : 0),
                bounds.getHeight() + NODE_OUTLINE
        );

        /*double height = textHeight();
        
        StaticGraphics.textFont(graphics.draw.Parameters.labelFont);
        return new Point2D(textWidth(node.toString()) + (padding ? height : 0),
                 height + NODE_OUTLINE);*/
    }

    public static Point2D labelSpacedDimensions(NetworkNode node) {
        return labelDimensions(node, true).add(NODE_OUTLINE + NODE_SPACE, NODE_OUTLINE + NODE_SPACE);
    }

    // Set membership discrepancy between two nodes.
    private int membershipDiscrepancy(NetworkNode n1, NetworkNode n2) {
        int discr = 0;

        List<NetworkAnnotation> sets1 = nodeMemberships.get(n1);
        List<NetworkAnnotation> sets2 = nodeMemberships.get(n2);
        for (NetworkAnnotation s : sets1)
            if (!s.getNodes().contains(n2))
                discr++;
        for (NetworkAnnotation s : sets2)
            if (!s.getNodes().contains(n1))
                discr++;

        return discr;
    }

    private class BoundProjection {
        private final Variable[] xVariables, yVariables;
        private final double[] radii;
        private final double[][] distances;

        public BoundProjection(double[] radii, double[][] distances) {
            this.radii = radii;
            this.distances = distances;

            xVariables = new Variable[radii.length];
            yVariables = new Variable[radii.length];
            for (int i = 0; i < radii.length; i++) {
                xVariables[i] = new Variable(0, 1, 1);
                yVariables[i] = new Variable(0, 1, 1);
            }
        }

        public Descent.Projection[] projectFunctions() {
            return new Descent.Projection[]{
                    new Descent.Projection() {
                        @Override
                        public void apply(double[] x0, double[] y0, double[] r) {
                            xProject(x0, y0, r);
                        }
                    },
                    new Descent.Projection() {
                        @Override
                        public void apply(double[] x0, double[] y0, double[] r) {
                            yProject(x0, y0, r);
                        }
                    }
            };
        }

        private void xProject(double[] x0, double[] y0, double[] x) {
            solve(xVariables, createConstraints(x0, y0, true), x0, x);
        }

        private void yProject(double[] x0, double[] y0, double[] y) {
            solve(yVariables, createConstraints(x0, y0, false), y0, y);
        }

        private Constraint[] createConstraints(double[] x0, double[] y0, boolean xAxis) {
            final List<Constraint> cs = new ArrayList<>();

            // Pair wise constraints, only when within distance bounds.
            // Limit to plain nodes, for now.
            for (int i = 0; i < nodes.length; i++) {
                Point2D iP = new Point2D(x0[i], y0[i]);

                for (int j = 0; j < nodes.length; j++) {
                    Point2D jP = new Point2D(x0[j], y0[j]);

                    double ijDD = this.distances[i][j];  // Desired distance.
                    if (ijDD > Math.abs(y0[i] - y0[j]) || // Rough distance cut optimization.
                            ijDD > Math.abs(x0[i] - x0[j])) {
                        double iR = this.radii[i];
                        double jR = this.radii[j];

                        Point2D xM = new Point2D(0.5 * (iP.getX() + jP.getX() + (iP.getX() < jP.getX() ? this.radii[i] - this.radii[j] :
                                this.radii[j] - this.radii[i])),
                                0.5 * (iP.getY() + jP.getY())); // Point between two vertex lines.
                        Point2D iM = new Point2D(Math.min(iP.getX() + iR, Math.max(iP.getX() - iR, xM.getX())), iP.getY());
                        Point2D jM = new Point2D(Math.min(jP.getX() + jR, Math.max(jP.getX() - jR, xM.getX())), jP.getY());
                        Point2D ijV = jM.subtract(iM);  // Minimum distance vector between vertex lines.
                        double ijAD = ijV.magnitude();      // Actual distance between vertex lines.

                        // Create constraint when distance is violated.
                        if (ijDD > ijAD) {
                            Variable lV;
                            Variable rV;
                            double gap;

                            // Use ij vector angle to determine axis of constraint.
                            if (xAxis && iM.getX() != jM.getX()) {
                                lV = iP.getX() < jP.getX() ? xVariables[i] : xVariables[j];
                                rV = iP.getX() < jP.getX() ? xVariables[j] : xVariables[i];
                                gap = this.radii[i] + this.radii[j] + ijV.normalize().multiply(ijDD).getX();

                                cs.add(new Constraint(lV, rV, gap, false));
                            }

                            if (!xAxis /*&& Math.abs(ijV[0]) < Math.abs(ijV[1])*/) {
                                lV = iP.getY() < jP.getY() ? this.yVariables[i] : this.yVariables[j];
                                rV = iP.getY() < jP.getY() ? this.yVariables[j] : this.yVariables[i];
                                gap = ijV.normalize().multiply(ijDD).getY();

                                cs.add(new Constraint(lV, rV, gap, false));
                            }
                        }
                    }
                }
            }

            return cs.toArray(new Constraint[]{});
        }

        private void solve(Variable[] vs,
                           Constraint[] cs,
                           double[] starting,
                           double[] desired) {
            Solver solver = new Solver(vs, cs);
            solver.setStartingPositions(starting);
            solver.setDesiredPositions(desired);
            solver.solve();

            // Push solution as result.
            for (int i = 0; i < vs.length; i++) {
                desired[i] = vs[i].position();
            }
        }
    }

    public static class RichNode {
        public NetworkNode element;
        public List<NetworkAnnotation> memberships;

        public RichNode(NetworkNode element) {
            this.element = element;
            this.memberships = new ArrayList<>();
        }

        @Override
        public int hashCode() {
            return element == null ? super.hashCode() : this.element.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            final RichNode other = (RichNode) obj;
            return element == null ? super.equals(obj) : element.equals(other.element);
        }
    }

    public static class RichEdge extends DefaultWeightedEdge {
        public DefaultEdge edge;
        public boolean core;            // Whether edge is part of original graph.
        public List<NetworkAnnotation> memberships;  // Set memberships.
        public RichNode subNode;        // Optional dummy node that divides edge in extended graph.

        public RichEdge() {
            memberships = new ArrayList<>();
        }
    }
}
