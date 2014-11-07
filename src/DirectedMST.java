import java.util.*;

/**
 * @author Jun Yu
 *         Created by Jun Yu on 10/31/14.
 */
public class DirectedMST {
    ArrayList<Vertex> vertices;
    int numVertices;
    int source;

    long weightReduction;

    HashMap<Integer, Pair<Integer>> minIncomingEdgesToCycle = new HashMap<>();
    HashMap<Integer, Pair<Integer>> minOutgoingEdgesFromCycle = new HashMap<>();

    public DirectedMST(int num, int source) {
        if (num < 0) {
            vertices = new ArrayList<>();
            vertices.add(new Vertex(0));
            return;
        }

        vertices = new ArrayList<>(num + num / 2);
        vertices.add(new Vertex(0)); // ignore 0 index
        this.source = source;

        for (int i = 0; i < num; i++) {
            vertices.add(new Vertex(i + 1));
        }
        numVertices = vertices.size();
    }

    private DirectedMST(ArrayList<Vertex> vertices, int originalNum, int source) {
        this.vertices = vertices;
        this.numVertices = originalNum;
        this.source = source;
        this.weightReduction = 0;

        // clear MST for recursion
        for (Vertex v : vertices) {
            v.known = false;
            v.pred = -1;
            v.pathMST.clear();
            v.reachableFromS = false;
        }
    }

    public long procedure() {
        weightReduction = transformWeight();
        bfsMSTUsingZeroWeight(source);

        // if all vertices reachable from s
        if(verifyMST(source) >= 0) {
            return weightReduction;
        }

        List<Integer> cycle = walkBackward();

        int x_index = shrinkCycle(cycle);

        // recursion for for MST in smaller graph
        DirectedMST smallerGraph = new DirectedMST(this.vertices, this.numVertices, this.source);
        weightReduction += smallerGraph.procedure();

        recoverCycle(cycle, x_index);

        return weightReduction;
    }

    /**
     * Traverse all vertices, if all are reachable from s, then return weight of MST.
     * Otherwise dive into further procedure: shrink recursion and expand,
     * and eventually return weight of MST.
     *
     * @param source index
     * @return weight of MST
     */
    public long verifyMST(int source) {
        if (bfsMSTReachableFromS(source)) {
            return weightReduction;
        }

        return -1;
    }

    /**
     * Add directed edge with weight for both source and destination vertices
     * @param src index of source vertex
     * @param dst index of destination vertex
     * @param weight int
     */
    public void addEdge(int src, int dst, int weight) {
        Vertex srcV = vertices.get(src);
        srcV.addOutAdj(dst, weight);

        Vertex dstV = vertices.get(dst);
        dstV.addInAdj(src, weight);
    }

    /**
     * Transform weights so that every node except s has an incoming edge
     * of weight 0
     *
     * @return total reduction
     */
    public long transformWeight() {
        long sum = 0;

        // for each u in V-{s}
        for (int u = 1; u < vertices.size(); u++) {
            if (u == source) {
                continue;
            }
            Vertex vertex = vertices.get(u);
            // TODO should d_u be a function or a variable?
            int d_u = vertex.maxIncomingWeight();
            if (d_u == 0) {
                // no need to reduce
                continue;
            }

            // for each p in V
            ListIterator<Integer> inItor = vertex.inAdj.listIterator();
            ListIterator<Integer> inWeightItor = vertex.inAdjWeight.listIterator();
            while (inItor.hasNext()) {
                int p = inItor.next();
                int weight = inWeightItor.next();
                // set for incoming edge of u
                inWeightItor.set(weight - d_u);
                // set for outgoing edge of p, actually they are the same edge
                vertices.get(p).changeOutgoingWeight(u, -d_u);
            }
            sum += d_u;
        }

        return sum;
    }

    /**
     * BST of graph, which source is source
     * @param source index
     * @return whether all vertices are reachable from s
     */
    public boolean bfsMSTReachableFromS(int source) {
        for (Vertex v : vertices) {
            v.known = false;
        }

        Vertex s = vertices.get(source);
        s.known = true;

        LinkedList<Integer> queue = new LinkedList<>();
        queue.add(source);

        while (!queue.isEmpty()) {
            int u_index = queue.remove();
            Vertex u = vertices.get(u_index);

            for (Integer v_index : u.outAdj) {
                Vertex v = vertices.get(v_index);

                if (!v.reachableFromS) {
                    return false;
                }

                if (!v.known) {
                    v.known = true;
                    queue.add(v_index);
                }
            }
        }
        return true;
    }

    /**
     * BST of graph using only 0-weight edge, which source is source
     *
     * @param source index
     */
    public void bfsMSTUsingZeroWeight(int source) {
        for (Vertex v : vertices) {
            v.known = false;
        }

        Vertex s = vertices.get(source);
        s.known = true;
        s.pred = source;
        s.reachableFromS = true;

        LinkedList<Integer> queue = new LinkedList<>();
        queue.add(source);

        while (!queue.isEmpty()) {
            int u_index = queue.remove();
            Vertex u = vertices.get(u_index);
            Iterator<Integer> adjItor = u.outAdj.iterator();
            Iterator<Integer> adjWeightItor = u.outAdjWeight.iterator();

            // every vertex in queue is reachable from s using only 0-weight edges
            while (adjItor.hasNext()) {
                int v_index = adjItor.next();
                Vertex v = vertices.get(v_index);
                int weight = adjWeightItor.next();

                // v is reachable from u using only 0-weight edges iff weight = 0,
                // that means v is reachable from s
                if (weight == 0) {
                    v.reachableFromS = true;
                    // add vertex, to which o-weight pointer, and unknown to queue
                    if (!v.known) {
                        v.known = true;
                        v.pred = u_index;
                        u.pathMST.add(v_index);
                        queue.add(v_index);
                    }
                }
            }
        }
    }

    /**
     * Walk backward from one of the vertices that are not reachable from s
     * @return cycle
     */
    public List<Integer> walkBackward() {
        Vertex z = null;
        // find z that is not reachable from s
        for (int i = 1; i < vertices.size(); i++) {
            Vertex v = vertices.get(i);
            if (!v.reachableFromS) {
                z = v;
                break;
            }
        }

        assert z != null;

        List<Integer>  tempList = new LinkedList<>();
        Vertex v = z;

        // find a node that repeats in backward search
        while (true) {
            int zeroEdgeVertex = v.getOneIncomingZeroWeightEdgeVertex();
            assert zeroEdgeVertex != -1;

            if (tempList.contains(zeroEdgeVertex)) {
                break; // v repeated
            }

            tempList.add(zeroEdgeVertex);
            v = vertices.get(zeroEdgeVertex);
        }

        // find the cycle begins at v
        // what need to do is truncate temp list begin at v;
        int v_in_tempList = tempList.indexOf(v.index);
        assert v_in_tempList != -1;

        List<Integer> cycle = new LinkedList<>();
        cycle.addAll(v_in_tempList, tempList);

        return cycle;
    }

    class Pair<T> {
        T from, to;

        public Pair(T from, T to) {
            this.from = from;
            this.to = to;
        }
    }

    /**
     * Shrink cycle C into a single node
     * @param cycle cycle to be shrunk
     * @return index of new node
     */
    public int shrinkCycle(List<Integer> cycle) {
        // key node not in the cycle -> new minimum edge
        HashMap<Integer, Integer> imcomingVisitedWeight = new HashMap<>();
        HashMap<Integer, Integer> outgoingVisitedWeight = new HashMap<>();
        // record the edge corresponding to this minimum weight in the graph before shrinking
        Pair<Integer> minIncomingEdge;
        Pair<Integer> minOutgoingEdge;


        // for each vertex in cycle
        for (Integer u_index : cycle) {
            Vertex u = vertices.get(u_index);

            Iterator<Integer> adjItor = u.inAdj.iterator();
            Iterator<Integer> adjWeightItor = u.inAdjWeight.iterator();

            // for all incoming edge of u
            while (adjItor.hasNext()) {
                int v_index = adjItor.next();
                Vertex v = vertices.get(v_index);
                int weight = adjWeightItor.next();

                // skip vertex that in cycle
                if (cycle.contains(v_index)) {
                    continue;
                }

                // remove incoming edge to cycle
                adjItor.remove();
                adjWeightItor.remove();
                // remove outgoing edge from vertex not in cycle
                v.removeOutAdj(u_index);

                if (!imcomingVisitedWeight.containsKey(v_index)) {
                    minIncomingEdge = new Pair<>(v_index, u_index);
                    this.minIncomingEdgesToCycle.put(v_index, minIncomingEdge);
                    imcomingVisitedWeight.put(v_index, weight);
                }

                if (imcomingVisitedWeight.get(v_index) > weight) {
                    minIncomingEdge = new Pair<>(v_index, u_index);
                    this.minIncomingEdgesToCycle.put(v_index, minIncomingEdge);
                    imcomingVisitedWeight.put(v_index, weight);
                }
            }
        }

        // for each vertex in cycle
        for (Integer u_index : cycle) {
            Vertex u = vertices.get(u_index);

            Iterator<Integer> adjItor = u.outAdj.iterator();
            Iterator<Integer> adjWeightItor = u.outAdjWeight.iterator();

            // for all outgoing edge of u
            while (adjItor.hasNext()) {
                int v_index = adjItor.next();
                Vertex v = vertices.get(v_index);
                int weight = adjWeightItor.next();

                // skip vertex that in cycle
                if (cycle.contains(v_index)) {
                    continue;
                }

                // remove outgoings edge to cycle
                adjItor.remove();
                adjWeightItor.remove();
                // remove incoming edge in vertex not in cycle
                v.removeInAdj(u_index);

                if (!outgoingVisitedWeight.containsKey(v_index)) {
                    minOutgoingEdge = new Pair<>(u_index, v_index);
                    this.minOutgoingEdgesFromCycle.put(v_index, minOutgoingEdge);
                    outgoingVisitedWeight.put(v_index, weight);
                }

                if (outgoingVisitedWeight.get(v_index) > weight) {
                    minOutgoingEdge = new Pair<>(u_index, v_index);
                    this.minOutgoingEdgesFromCycle.put(v_index, minOutgoingEdge);
                    outgoingVisitedWeight.put(v_index, weight);
                }
            }
        }

        // insert new vertex x
        Vertex x = new Vertex(vertices.size());
        int x_index = vertices.size();
        vertices.add(x);

        // For each edge (u,a) in the graph, with u not in C and a in C, introduce the edge (u,x) of weight w(u,a)
        for (Map.Entry<Integer, Integer> pair : imcomingVisitedWeight.entrySet()) {
            int u_index = pair.getKey();
            addEdge(u_index, x_index, pair.getValue());
        }

        // For each edge (a,u) in the graph, with a in C and u not in C, introduce the edge (x,u) of weight w(a,u)
        for (Map.Entry<Integer, Integer> pair : outgoingVisitedWeight.entrySet()) {
            int u_index = pair.getKey();
            addEdge(x_index, u_index, pair.getValue());
        }


        // the direction of path is the reverse of cycle list

        return x_index;
    }

    /**
     * Include the cycle into MST
     * @param cycle the zero cycle
     * @param x_index to which cycle shrunk
     */
    public void recoverCycle(List<Integer> cycle, int x_index) {
        // include the cycle into MST
        Vertex x = vertices.get(x_index);

        // MST has exactly one edge into x
        int pred_x_index = x.pred;
        Vertex u = vertices.get(pred_x_index);
        Pair<Integer> incoming = minIncomingEdgesToCycle.get(pred_x_index);

        // first link cycle in MST
        int prev_index = cycle.get(0);
        Vertex prev = vertices.get(prev_index);
        ListIterator<Integer> revItor = cycle.listIterator(cycle.size());
        while (revItor.hasPrevious()) {
            int next_index = revItor.previous();
            Vertex next = vertices.get(next_index);
            prev.pathMST.add(next_index);
            next.pred = prev_index;
            prev_index = next_index;
            prev = next;
        }

        // second break the edge from the cycle
        int a_index = incoming.to;
        Vertex a = vertices.get(a_index);
        int prevToBeBreak_index = a.pred;
        Vertex prevToBeBreak = vertices.get(prevToBeBreak_index);
        prevToBeBreak.pathMST.remove((Object)(Integer)a_index);

        // third recover incoming edge in MST
        a.pred = pred_x_index;
        u.pathMST.remove((Object)(Integer)x_index);
        u.pathMST.add(a_index);
    }
}
