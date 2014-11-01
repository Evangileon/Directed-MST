/**
 * @author Jun Yu
 */

/**
 * A priority queue of vertices that provide the ability to decease
 * the weight of any element and reorganize the structure to ensure
 * the property of priority tree
 */
public class VertexHeap {
	private Vertex[] vertices;
	private int[] Q;
	private int[] X;
	private int heapSize = 0;

    /**
     * Constructor
     * @param verticesRef the reference to vertices
     */
	public VertexHeap(Vertex[] verticesRef) {
        if (verticesRef == null) {
            return;
        }

		int size = verticesRef.length;
		vertices = verticesRef;
		Q = new int[size + 1];
		X = new int[size];
	}

    /**
     * Must be executed after constructor
     */
	public void buildHeap() {
		heapSize = vertices.length;

		for (int i = 0; i < vertices.length; i++) {
			Q[i + 1] = i;
			X[i] = i + 1;
		}

		for (int i = heapSize / 2; i > 0; i--) {
			percolateDown(i);
		}
	}

    /**
     * Exchange the position both in queue and inverse array
     * @param i position in queue
     * @param j position in queue
     */
	private void exchange(int i, int j) {
		int tmp = Q[i];
		Q[i] = Q[j];
		Q[j] = tmp;
		X[Q[i]] = i;
		X[Q[j]] = j;
	}

    /**
     * Percolate down
     * @param hole position in queue
     */
	private void percolateDown(int hole) {
		int child;

		for (; hole * 2 <= heapSize; hole = child) {
			child = hole * 2;
			if (child != heapSize
					&& vertices[Q[child + 1]].compareTo(vertices[Q[child]]) < 0)
				child++;
			if (vertices[Q[child]].compareTo(vertices[Q[hole]]) < 0)
				exchange(hole, child);
			else
				break;
		}
	}

    /**
     * Delete minimum element of queue
     * @return the index to reference vertices array
     */
	public int deleteMin() {
		int minItem = Q[1];
		Q[1] = Q[heapSize--];
		percolateDown(1);

		return minItem;
	}

	// public int insert(Vertex newVertex) {
	// int hole = ++heapSize;
	// array[hole] = newVertex;
	// hole = percolateUp(hole);
	// return hole;
	// }
	//

    /**
     * For deleteMin, used to percolate an el
     * @param hole position in queue
     * @return the new position in queue
     */
	private int percolateUp(int hole) {
		for (Q[0] = Q[hole]; vertices[Q[hole]].compareTo(vertices[Q[hole / 2]]) < 0; hole /= 2) {
			exchange(hole, hole / 2);
		}
		return hole;
	}

	public boolean isEmpty() {
		return (heapSize == 0);
	}

    /**
     * Decrease the distance
     * @param index in queue
     * @param newDis
     */
	public void decreaseElement(int index, int newDis) {
		int a = X[index];

		if (vertices[index].dis < newDis)
			return;

		vertices[index].dis = newDis;

		percolateUp(a);
	}
}
