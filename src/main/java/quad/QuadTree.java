package quad;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class QuadTree<P> {
    private final Point topLeft;
    private final Point bottomRight;
    private final Set<QuadNode<P>> nodes;
    private QuadTree<P> topLeftTree;
    private QuadTree<P> topRightTree;
    private QuadTree<P> bottomLeftTree;
    private QuadTree<P> bottomRightTree;
    private final int currentLength;
    private static final int maxLength = 20;

    boolean isLeaf = true;

    public QuadTree(Point topLeft, Point bottomRight, int currentLength) {
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
        this.currentLength = currentLength;
        this.nodes = new HashSet<>();
    }

    private boolean shouldSubDivide() {
        return currentLength < maxLength;
    }

    public void insert(Point point, P data) {
        QuadTree<P> curr = this;
        QuadNode<P> quadNode = new QuadNode<>(point, data);
        while (!curr.isLeaf) {
            curr.nodes.add(quadNode);
            if (point.getLongitude() < (curr.topLeft.getLongitude() + curr.bottomRight.getLongitude()) / 2) {
                if (point.getLatitude() < (curr.topLeft.getLatitude() + curr.bottomRight.getLatitude()) / 2) {
                    curr = curr.bottomLeftTree;
                } else {
                    curr = curr.topLeftTree;
                }

            } else {
                if (point.getLatitude() < (curr.topLeft.getLatitude() + curr.bottomRight.getLatitude()) / 2) {
                    curr = curr.bottomRightTree;

                } else {
                    curr = curr.topRightTree;
                }
            }
        }

        curr.nodes.add(quadNode);
        if (curr.shouldSubDivide()) {
            curr.subDivide();
        }
    }

    private void subDivide() {
        double midX = (topLeft.getLongitude() + bottomRight.getLongitude()) / 2;
        double midY = (topLeft.getLatitude() + bottomRight.getLatitude()) / 2;
        isLeaf = false;
        topLeftTree = new QuadTree<>(topLeft, new Point(midY, midX), currentLength + 1);
        bottomRightTree = new QuadTree<>(new Point(midY, midX), bottomRight, currentLength + 1);
        topRightTree = new QuadTree<>(new Point(topLeft.getLatitude(), midX), new Point(midY, bottomRight.getLongitude()), currentLength + 1);
        bottomLeftTree = new QuadTree<>(new Point(midY, topLeft.getLongitude()), new Point(bottomRight.getLatitude(), midX), currentLength + 1);
        QuadTree<P> curr = this;
        for (QuadNode<P> node : nodes) {
            Point point = node.getPoint();
            if (point.getLongitude() < (curr.topLeft.getLongitude() + curr.bottomRight.getLongitude()) / 2) {
                if (point.getLatitude() < (curr.topLeft.getLatitude() + curr.bottomRight.getLatitude()) / 2) {
                    curr.bottomLeftTree.addToNode(node);
                } else {
                    curr.topLeftTree.addToNode(node);
                }

            } else {
                if (point.getLatitude() < (curr.topLeft.getLatitude() + curr.bottomRight.getLatitude()) / 2) {
                    curr.bottomRightTree.addToNode(node);
                } else {
                    curr.topRightTree.addToNode(node);
                }
            }
        }
    }

    private void addToNode(QuadNode<P> data) {
        nodes.add(data);
    }


    public QuadTree<P> search(Point point) {
        QuadTree<P> curr = this;
        while (!curr.isLeaf) {

            if (point.getLongitude() < (curr.topLeft.getLongitude() + curr.bottomRight.getLongitude()) / 2) {
                if (point.getLatitude() < (curr.topLeft.getLatitude() + curr.bottomRight.getLatitude()) / 2) {
                    curr = curr.bottomLeftTree;
                } else {
                    curr = curr.topLeftTree;
                }

            } else {
                if (point.getLatitude() < (curr.topLeft.getLatitude() + curr.bottomRight.getLatitude()) / 2) {
                    curr = curr.bottomRightTree;

                } else {
                    curr = curr.topRightTree;
                }
            }
        }
        return curr;
    }

    public Set<QuadNode<P>> rangeSearch(Point point, double range) {
        if (getSize() == 0)
            return Collections.emptySet();
        Set<QuadNode<P>> nearByNode = new HashSet<>();

        if (isLeaf) {
            return nodes;
        }

        double midLatitude = (topLeft.getLatitude() + bottomRight.getLatitude()) / 2;
        double midLongitude = (topLeft.getLongitude() + bottomRight.getLongitude()) / 2;
        if (point.getLongitude() < midLongitude) {
            if (point.getLatitude() < midLatitude) {
                nearByNode.addAll(bottomLeftTree.rangeSearch(point, range));
                if (midLatitude - point.getLatitude() < range && topLeftTree.getSize() > 0)
                    nearByNode.addAll(topLeftTree.rangeSearch(point, range));
                if (midLongitude - point.getLongitude() < range && bottomRightTree.getSize() > 0)
                    nearByNode.addAll(bottomRightTree.rangeSearch(point, range));
                if (midLatitude - point.getLatitude() < range && midLongitude - point.getLongitude() < range && topRightTree.getSize() > 0)
                    nearByNode.addAll(topRightTree.rangeSearch(point, range));

            } else {
                nearByNode.addAll(topLeftTree.rangeSearch(point, range));
                if (point.getLatitude() - midLatitude < range && bottomLeftTree.getSize() > 0)
                    nearByNode.addAll(bottomLeftTree.rangeSearch(point, range));
                if (midLongitude - point.getLongitude() < range && topRightTree.getSize() > 0)
                    nearByNode.addAll(topRightTree.rangeSearch(point, range));
                if (point.getLatitude() - midLatitude < range && midLongitude - point.getLongitude() < range && bottomRightTree.getSize() > 0)
                    nearByNode.addAll(bottomRightTree.rangeSearch(point, range));
            }

        } else {
            if (point.getLatitude() < midLatitude) {
                nearByNode.addAll(bottomRightTree.rangeSearch(point, range));
                if (midLatitude - point.getLatitude() < range && topRightTree.getSize() > 0)
                    nearByNode.addAll(topRightTree.rangeSearch(point, range));
                if (point.getLongitude() - midLongitude < range && bottomLeftTree.getSize() > 0)
                    nearByNode.addAll(bottomLeftTree.rangeSearch(point, range));
                if (midLatitude - point.getLatitude() < range && point.getLongitude() - midLongitude < range && topLeftTree.getSize() > 0)
                    nearByNode.addAll(topLeftTree.rangeSearch(point, range));

            } else {
                nearByNode.addAll(topRightTree.rangeSearch(point, range));
                if (point.getLatitude() - midLatitude < range && bottomRightTree.getSize() > 0)
                    nearByNode.addAll(bottomRightTree.rangeSearch(point, range));
                if (point.getLongitude() - midLongitude < range && topLeftTree.getSize() > 0)
                    nearByNode.addAll(topLeftTree.rangeSearch(point, range));
                if (point.getLatitude() - midLatitude < range && point.getLongitude() - midLongitude < range && bottomLeftTree.getSize() > 0)
                    nearByNode.addAll(bottomLeftTree.rangeSearch(point, range));
            }
        }
        return nearByNode;
    }

    public int getSize() {
        return nodes.size();
    }

    public Point getTopLeft() {
        return topLeft;
    }

    public Point getBottomRight() {
        return bottomRight;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public QuadTree<P> getTopLeftTree() {
        return topLeftTree;
    }

    public QuadTree<P> getTopRightTree() {
        return topRightTree;
    }

    public QuadTree<P> getBottomLeftTree() {
        return bottomLeftTree;
    }

    public QuadTree<P> getBottomRightTree() {
        return bottomRightTree;
    }

    public Set<QuadNode<P>> getNodes() {
        return nodes;
    }

    @Override
    public String toString() {
        return "QuadTree{" +
                "topLeft=" + topLeft +
                ", bottomRight=" + bottomRight +
                ", currentLength=" + currentLength +
                ", isLeaf=" + isLeaf +
                ", size=" + getSize() +
                '}';
    }
}