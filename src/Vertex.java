/**
 * @author Jun Yu
 */

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

public class Vertex implements Comparable<Vertex> {

    boolean reachableFromS;
    int index;

    // incoming edges
    LinkedList<Integer> inAdj = new LinkedList<>();
    LinkedList<Integer> inAdjWeight = new LinkedList<>();
    // outgoing edges
    LinkedList<Integer> outAdj = new LinkedList<>();
    LinkedList<Integer> outAdjWeight = new LinkedList<>();

    LinkedList<Integer> pathMST = new LinkedList<>();

    int dis;
    boolean known;
    int pred;

    public int maxIncomingWeight() {
        Iterator<Integer> inItor = inAdj.iterator();
        Iterator<Integer> inWeightItor = inAdjWeight.iterator();
        int max = Integer.MIN_VALUE;

        while (inItor.hasNext()) {
            inItor.next();
            int weight = inWeightItor.next();
            max = Math.max(max, weight);
        }
        return max;
    }

    int maxIncomingWeight;
    int maxOutgoingWeight;

    public Vertex(int index) {
        this.index = index;
        known = false;
        reachableFromS = false;
        dis = Integer.MAX_VALUE;
        pred = -1;
        maxIncomingWeight = Integer.MIN_VALUE;
        maxOutgoingWeight = Integer.MIN_VALUE;
    }

    public void addInAdj(int adjIndex, int weight) {

        inAdj.add(adjIndex);
        inAdjWeight.add(weight);

        if (weight > maxIncomingWeight) {
            maxIncomingWeight = weight;
        }
    }

    public void removeInAdj(int adjIndex) {
        Iterator<Integer> adjItor = this.inAdj.iterator();
        Iterator<Integer> adjWeightItor = this.inAdjWeight.iterator();

        while (adjItor.hasNext()) {
            int index = adjItor.next();
            adjWeightItor.next();

            if (index == adjIndex) {
                adjItor.remove();
                adjWeightItor.remove();
                return;
            }
        }
    }

    public void addOutAdj(int adjIndex, int weight) {
        outAdj.add(adjIndex);
        outAdjWeight.add(weight);

        if (weight > maxOutgoingWeight) {
            maxOutgoingWeight = weight;
        }
    }

    public void removeOutAdj(int adjIndex) {
        Iterator<Integer> adjItor = this.outAdj.iterator();
        Iterator<Integer> adjWeightItor = this.outAdjWeight.iterator();

        while (adjItor.hasNext()) {
            int index = adjItor.next();
            adjWeightItor.next();

            if (index == adjIndex) {
                adjItor.remove();
                adjWeightItor.remove();
                return;
            }
        }
    }

    @Override
    public int compareTo(Vertex o) {
        if (o == null) {
            return 0;
        }
        return this.dis - o.dis;
    }

    /**
     * Change the weight of outgoing edge
     * @param adjIndex the index of outgoing edge pointer to
     * @param change can be positive or negative.
     */
    public void changeOutgoingWeight(int adjIndex, int change) {
        ListIterator<Integer> itorV = outAdj.listIterator();
        ListIterator<Integer> itorW = inAdj.listIterator();

        while (itorV.hasNext()) {
            int index = itorV.next();
            int weight = itorW.next();
            if (adjIndex == index) {
                itorW.set(weight + change);
                break;
            }
        }
    }

    /**
     * The the vertex has a zero-weight edge come into this
     */
    public int getOneIncomingZeroWeightEdgeVertex() {
        Iterator<Integer> inItor = inAdj.iterator();
        Iterator<Integer> inWeightItor = inAdjWeight.iterator();

        while (inItor.hasNext()) {
            int v_index = inItor.next();
            int weight = inWeightItor.next();
            if (weight == 0) {
                return v_index;
            }
        }
        return -1;
    }
}


