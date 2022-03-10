package quad;

import java.util.Objects;

public class QuadNode<T> {
    private final T data;
    private final Point point;

    public QuadNode(Point p, T data) {
        this.data =  data;
        this.point = p;
    }

    public QuadNode(double latitude, double longitude, T data) {
        this.data = data;
        this.point = new Point(latitude, longitude);
    }

    public T getData() {
        return data;
    }

    public Point getPoint() {
        return point;
    }

    @Override
    public String toString() {
        return "data " + data + " point " + point;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuadNode<?> quadNode = (QuadNode<?>) o;
        return Objects.equals(data, quadNode.data) && Objects.equals(point, quadNode.point);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, point);
    }
}