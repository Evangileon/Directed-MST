import java.util.ArrayList;

/**
 * @author Jun Yu
 * Created by Jun Yu on 10/31/14.
 */
public class DirectedMST {
    ArrayList<Vertex> vertexes;
    int source;

    public DirectedMST(int num, int source) {
        if (num < 0) {
            vertexes = new ArrayList<>();
            vertexes.add(new Vertex());
            return;
        }

        vertexes = new ArrayList<>(num);
        vertexes.add(new Vertex()); // ignore 0 index
        this.source = source;

        for (int i = 0; i < num; i++) {
            vertexes.add(new Vertex());
        }
    }

    /**
     * Transform weights so that every node except s has an incoming edge
     * of weight 0
     * @return total reduction
     */
    public long transformWeight() {
        long sum = 0;

        // for each u in V-{s}
        for (int u = 1; u < vertexes.size(); u++) {
            if (u == source) {
                continue;
            }
            Vertex vertex = vertexes.get(u);
            int d_u = vertex.maxIncomingWeight;
            // for each p in V
            for (int p = 0; p < vertexes.size(); p++) {
                vertexes.get(p).changeOutgoingWeight(u, -d_u);
            }
            sum += d_u;
        }

        return sum;
    }
}
