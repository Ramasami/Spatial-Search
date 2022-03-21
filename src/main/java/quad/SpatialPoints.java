package quad;

import java.util.Set;
import java.util.stream.Collectors;

public class SpatialPoints<T> {

    private final QuadTree<T> quadTree;

    public SpatialPoints() {
        this.quadTree = new QuadTree<T>(new Point(100, 0), new Point(0, 100), 0);
    }

    public void insert(double latitude, double longitude, T data) {
        quadTree.insert(new Point(latitude, longitude), data);
    }

    public QuadTree<T> search(double latitude, double longitude) {
        return quadTree.search(new Point(latitude, longitude));
    }

    private boolean isInside(Point nodePoint, double minLat, double maxLat, double minLong, double maxLong) {
        return nodePoint.getLongitude() >= minLong
                && nodePoint.getLongitude() <= maxLong
                && nodePoint.getLatitude() >= minLat
                && nodePoint.getLatitude() <= maxLat;
    }

    private boolean isInside(Point nodePoint, double latitude, double longitude, double radiusSquared) {
        return (Math.pow(nodePoint.getLatitude() - latitude, 2) + Math.pow(nodePoint.getLongitude() - longitude, 2)) <= radiusSquared;
    }

    public Set<QuadNode<T>> rangeSearch(double latitude, double longitude, double range, searchType searchType) {
        Set<QuadNode<T>> rangeAnswer = quadTree.rangeSearch(new Point(latitude, longitude), range);
        switch (searchType) {
            case STRICT_BOX: {
                double minLong = Math.min(longitude + range, longitude - range);
                double maxLong = Math.max(longitude + range, longitude - range);
                double minLat = Math.min(latitude + range, latitude - range);
                double maxLat = Math.max(latitude + range, latitude - range);
                return rangeAnswer.stream().filter(node -> isInside(node.getPoint(), minLat, maxLat, minLong, maxLong)).collect(Collectors.toSet());
            }
            case BOX:
                return rangeAnswer;
            case CIRCLE:
                double radiusSquared = range * range;
                return rangeAnswer.stream().filter(node -> isInside(node.getPoint(), latitude, longitude, radiusSquared)).collect(Collectors.toSet());
        }
        return rangeAnswer;
    }

    public QuadTree<T> getQuadTree() {
        return quadTree;
    }

    public void remove(QuadNode<T> node) {
        quadTree.remove(node);
    }

    public enum searchType {
        CIRCLE, BOX, STRICT_BOX
    }
}
