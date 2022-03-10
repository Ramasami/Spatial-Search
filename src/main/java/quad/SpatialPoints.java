package quad;

import java.util.Set;

public class SpatialPoints<T> {

    private final QuadTree<T> quadTree;

    public SpatialPoints() {
        this.quadTree = new QuadTree<T>(new Point(100,0), new Point(0, 100), 0);
    }

    public void insert(double latitude, double longitude, T data) {
        quadTree.insert(new Point(latitude, longitude), data);
    }

    public QuadTree<T> search(double latitude, int longitude) {
        return quadTree.search(new Point(latitude, longitude));
    }

    public Set<QuadNode<T>> rangeSearch(double latitude, int longitude, double range) {
        return quadTree.rangeSearch(new Point(latitude, longitude),  range);
    }
}
