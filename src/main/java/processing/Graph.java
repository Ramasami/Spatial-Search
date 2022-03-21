package processing;

import processing.core.PApplet;
import quad.QuadNode;
import quad.QuadTree;
import quad.SpatialPoints;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class Graph<T> extends PApplet {

    private final SpatialPoints<T> spatialPoints;
    private Double latitude;
    private Double longitude;
    private Double range;
    private SpatialPoints.searchType searchType;
    private Set<QuadNode<T>> searchedNodes;
    private QuadTree<T> searchTree;

    public Graph(SpatialPoints<T> spatialPoints, double latitude, double longitude, double range, SpatialPoints.searchType searchType) {
        this.spatialPoints = spatialPoints;
        this.latitude = latitude;
        this.longitude = longitude;
        this.range = range;
        this.searchType = searchType;
    }

    public Graph(SpatialPoints<T> spatialPoints, double latitude, double longitude) {
        this.spatialPoints = spatialPoints;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Graph(SpatialPoints<T> spatialPoints) {
        this.spatialPoints = spatialPoints;
    }

    public void settings() {
        size(900, 900);
        if (latitude != null && longitude != null) {
            if (range != null) {
                long startTime = System.currentTimeMillis();
                searchedNodes = spatialPoints.rangeSearch(latitude, longitude, range, searchType);
                System.out.println("RangeSearchTime: " + (System.currentTimeMillis() - startTime));
            }
            long startTime = System.currentTimeMillis();
            searchTree = spatialPoints.search(latitude, longitude);
            System.out.println("SearchTime: " + (System.currentTimeMillis() - startTime));
        }
    }

    private double map(double x, double oldL, double oldR, double newL, double newR) {
        return newR - ((oldR - x) / (oldR - oldL)) * (newR - newL);
    }

    private double resize(double x, double oldL, double oldR, double newL, double newR) {
        return (x / (oldR - oldL)) * (newR - newL);
    }

    public void draw() {
        background(255);
        QuadTree<T> tree = spatialPoints.getQuadTree();
        Queue<QuadTree<T>> queue = new LinkedList<>();
        queue.add(tree);

        while (!queue.isEmpty()) {
            tree = queue.poll();
            printRect(tree.getTopLeft().getLatitude(), tree.getTopLeft().getLongitude(),
                    tree.getBottomRight().getLatitude(), tree.getBottomRight().getLongitude(),
                    1, 0, 0, 0, 255);
            if (tree.isLeaf()) {
                tree.getNodes().forEach(node -> {
                    printPoint(node.getPoint().getLatitude(), node.getPoint().getLongitude(), 5, 255, 0, 0);
                });
                continue;
            }
            queue.add(tree.getBottomLeftTree());
            queue.add(tree.getBottomRightTree());
            queue.add(tree.getTopLeftTree());
            queue.add(tree.getTopRightTree());
        }

        if (latitude != null && longitude != null) {

            if (range != null) {
                searchedNodes.forEach(node -> {
                    stroke(0, 255, 0);
                    strokeWeight(5);
                    printPoint(node.getPoint().getLatitude(), node.getPoint().getLongitude(), 7, 0, 255, 0);
                    stroke(0);
                    strokeWeight(1);
                });
                switch (searchType) {
                    case BOX:
                    case STRICT_BOX:
                        printRect(latitude, longitude, range, 1, 0, 0, 255, null);
                        break;
                    case CIRCLE:
                        printCircle(latitude, longitude, range, 1, 0, 0, 255, null);
                }
                printRect(searchTree.getTopLeft().getLatitude(), searchTree.getTopLeft().getLongitude(),
                        searchTree.getBottomRight().getLatitude(), searchTree.getBottomRight().getLongitude(),
                        1, 0, 0, 0, 200);
            }
            printPoint(latitude, longitude, 10, 0, 0, 255);
        }
    }

    void printCircle(double lat1, double long1, double radius, int strokeWeight, int r, int g, int b, Integer fill) {
        ellipseMode(RADIUS);
        stroke(r, g, b);
        if (fill == null)
            noFill();
        else
            fill(fill);
        strokeWeight(strokeWeight);
        lat1 = map(lat1, spatialPoints.getQuadTree().getTopLeft().getLatitude(), spatialPoints.getQuadTree().getBottomRight().getLatitude(), 0, height);
        long1 = map(long1, spatialPoints.getQuadTree().getTopLeft().getLongitude(), spatialPoints.getQuadTree().getBottomRight().getLongitude(), 0, width);
        double rangeLat = resize(radius, spatialPoints.getQuadTree().getTopLeft().getLatitude(), spatialPoints.getQuadTree().getBottomRight().getLatitude(), 0, height);
        double rangeLong = resize(radius, spatialPoints.getQuadTree().getTopLeft().getLongitude(), spatialPoints.getQuadTree().getBottomRight().getLongitude(), 0, width);
        ellipse((int) long1, (int) lat1, (int) rangeLong, (int) rangeLat);
    }

    void printRect(double lat1, double long1, double range, int strokeWeight, int r, int g, int b, Integer fill) {
        rectMode(RADIUS);
        stroke(r, g, b);
        if (fill == null)
            noFill();
        else
            fill(fill);
        strokeWeight(strokeWeight);
        lat1 = map(lat1, spatialPoints.getQuadTree().getTopLeft().getLatitude(), spatialPoints.getQuadTree().getBottomRight().getLatitude(), 0, height);
        long1 = map(long1, spatialPoints.getQuadTree().getTopLeft().getLongitude(), spatialPoints.getQuadTree().getBottomRight().getLongitude(), 0, width);
        double rangeLat = resize(range, spatialPoints.getQuadTree().getTopLeft().getLatitude(), spatialPoints.getQuadTree().getBottomRight().getLatitude(), 0, height);
        double rangeLong = resize(range, spatialPoints.getQuadTree().getTopLeft().getLongitude(), spatialPoints.getQuadTree().getBottomRight().getLongitude(), 0, width);
        rect((int) long1, (int) lat1, (int) rangeLong, (int) rangeLat);
    }

    void printRect(double lat1, double long1, double lat2, double long2, int strokeWeight, int r, int g, int b, Integer fill) {
        rectMode(CORNERS);
        stroke(r, g, b);
        if (fill == null)
            noFill();
        else
            fill(fill);
        strokeWeight(strokeWeight);
        lat1 = map(lat1, spatialPoints.getQuadTree().getTopLeft().getLatitude(), spatialPoints.getQuadTree().getBottomRight().getLatitude(), 0, height);
        lat2 = map(lat2, spatialPoints.getQuadTree().getTopLeft().getLatitude(), spatialPoints.getQuadTree().getBottomRight().getLatitude(), 0, height);
        long1 = map(long1, spatialPoints.getQuadTree().getTopLeft().getLongitude(), spatialPoints.getQuadTree().getBottomRight().getLongitude(), 0, width);
        long2 = map(long2, spatialPoints.getQuadTree().getTopLeft().getLongitude(), spatialPoints.getQuadTree().getBottomRight().getLongitude(), 0, width);
        rect((int) long1, (int) lat1, (int) long2, (int) lat2);
    }

    void printPoint(double latitude, double longitude, int strokeWeight, int r, int g, int b) {
        stroke(r, g, b);
        strokeWeight(strokeWeight);
        latitude = map(latitude, spatialPoints.getQuadTree().getTopLeft().getLatitude(), spatialPoints.getQuadTree().getBottomRight().getLatitude(), 0, height);
        longitude = map(longitude, spatialPoints.getQuadTree().getTopLeft().getLongitude(), spatialPoints.getQuadTree().getBottomRight().getLongitude(), 0, width);
        point((float) longitude, (float) latitude);
    }

}