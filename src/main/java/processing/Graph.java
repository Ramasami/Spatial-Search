package processing;

import processing.core.PApplet;
import quad.QuadNode;
import quad.QuadTree;
import quad.SpatialPoints;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Graph<T> extends PApplet {

    private SpatialPoints<T> spatialPoints;
    private Double latitude;
    private Double longitude;
    private Double range;
    private final SpatialPoints.searchType searchType;
    private int mouseRange = 20;
    private boolean isShowingHoveredNodes = true;
    private Set<QuadNode<T>> searchedNodes;
    private QuadTree<T> searchTree;
    private Integer size;
    private final Function<Integer, T> dataFunction;
    private boolean showSearchResult = true;

    public Graph(SpatialPoints<T> spatialPoints, double latitude, double longitude, double range, SpatialPoints.searchType searchType, Function<Integer, T> dataFunction) {
        this.spatialPoints = spatialPoints;
        this.size = spatialPoints.getQuadTree().getSize();
        this.latitude = latitude;
        this.longitude = longitude;
        this.range = range;
        this.searchType = searchType;
        assert searchType != null;
        this.dataFunction = dataFunction;
    }

    public Graph(SpatialPoints<T> spatialPoints, double latitude, double longitude, Function<Integer, T> dataFunction) {
        this.spatialPoints = spatialPoints;
        this.size = spatialPoints.getQuadTree().getSize();
        this.latitude = latitude;
        this.longitude = longitude;
        this.range = null;
        this.searchType = null;
        this.dataFunction = dataFunction;
    }

    public Graph(SpatialPoints<T> spatialPoints, Function<Integer, T> dataFunction) {
        this.spatialPoints = spatialPoints;
        this.size = spatialPoints.getQuadTree().getSize();
        this.latitude = null;
        this.longitude = null;
        this.range = null;
        this.searchType = null;
        this.dataFunction = dataFunction;
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

    private double map(double value, double oldL, double oldR, double newL, double newR) {
        return newR - ((oldR - value) / (oldR - oldL)) * (newR - newL);
    }

    private double resize(double x, double oldL, double oldR, double newL, double newR) {
        return Math.abs((x / (oldR - oldL)) * (newR - newL));
    }

    public void draw() {
        background(255);
        printQuadTree();
        printSearchResult();
        showHoveredNodes();
    }

    public void keyPressed() {
        switch (key) {
            case 't':
                isShowingHoveredNodes = !isShowingHoveredNodes;
                break;
            case 'q':
                System.exit(0);
            case '+':
                if (isShowingHoveredNodes)
                    mouseRange = Math.min(mouseRange + 1, height);
                break;
            case '-':
                if (isShowingHoveredNodes)
                    mouseRange = Math.max(mouseRange - 1, 10);
                break;
            case 'r': {
                int size = spatialPoints.getQuadTree().getNodes().size();
                spatialPoints = new SpatialPoints<>();
                for (int i = 0; i < size; i++) {
                    spatialPoints.insert(Math.random() * 100, Math.random() * 100, dataFunction.apply(i));
                }
                break;
            }
            case ']':
                if (range != null)
                    range = Math.min(range + 1, spatialPoints.getQuadTree().getTopLeft().getLatitude() - spatialPoints.getQuadTree().getTopLeft().getLongitude());
                else
                    range = 1.0;
                break;
            case '[':
                if (range != null)
                    range = Math.max(range - 1, 1);
                else
                    range = 1.0;
                break;
            case 'p':
                latitude = map(mouseY, 0, height, spatialPoints.getQuadTree().getTopLeft().getLatitude(), spatialPoints.getQuadTree().getBottomRight().getLatitude());
                longitude = map(mouseX, 0, width, spatialPoints.getQuadTree().getTopLeft().getLongitude(), spatialPoints.getQuadTree().getBottomRight().getLongitude());
                break;
            case '\'': {
                spatialPoints.insert(Math.random() * 100, Math.random() * 100, dataFunction.apply(size++));
                break;
            }
            case ';': {
                Set<QuadNode<T>> nodesToRemove = spatialPoints.getQuadTree()
                        .getNodes()
                        .stream()
                        .filter(node -> node.getData().equals(dataFunction.apply(size - 1)))
                        .collect(Collectors.toSet());
                for (QuadNode<T> node : nodesToRemove)
                    spatialPoints.remove(node);
                size = spatialPoints.getQuadTree().getSize();
                break;
            }
            case 'h':
                showSearchResult = !showSearchResult;
                break;
        }
        settings();
    }

    private void showHoveredNodes() {
        if (!isShowingHoveredNodes) {
            cursor();
            return;
        } else {
            noCursor();
        }
        double mouseLatitude = map(mouseY, 0, height, spatialPoints.getQuadTree().getTopLeft().getLatitude(), spatialPoints.getQuadTree().getBottomRight().getLatitude());
        double mouseLongitude = map(mouseX, 0, width, spatialPoints.getQuadTree().getTopLeft().getLongitude(), spatialPoints.getQuadTree().getBottomRight().getLongitude());
        double range = resize(mouseRange, 0, height, spatialPoints.getQuadTree().getTopLeft().getLatitude(), spatialPoints.getQuadTree().getBottomRight().getLatitude());
        Set<String> textSet = spatialPoints.rangeSearch(mouseLatitude, mouseLongitude, range, SpatialPoints.searchType.CIRCLE)
                .stream()
                .peek(node -> printPoint(node.getPoint().getLatitude(), node.getPoint().getLongitude(), 10, 0, 0, 255))
                .map(node -> String.format("(%f,%f): %s", node.getPoint().getLatitude(), node.getPoint().getLongitude(), node.getData()))
                .collect(Collectors.toSet());
        String text = String.join("\n", textSet);
        fill(0, 0, 0);
        textSize(20);
        int h = 15;
        if (mouseX >= width / 2) {
            textAlign(RIGHT);
            text(text, mouseX - 20 - mouseRange, mouseY + h + 7.5f - textSet.size() * h);
        } else {
            textAlign(LEFT);
            text(text, mouseX + 20 + mouseRange, mouseY + h + 7.5f - textSet.size() * h);
        }
        printCircle(mouseLatitude, mouseLongitude, range, 1, 0, 0, 0, null);
    }

    private void printSearchResult() {
        if (showSearchResult && latitude != null && longitude != null) {
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
                        break;
                }
            }
            printRect(searchTree.getTopLeft().getLatitude(), searchTree.getTopLeft().getLongitude(),
                    searchTree.getBottomRight().getLatitude(), searchTree.getBottomRight().getLongitude(),
                    2, 255, 0, 0, null);
            printPoint(latitude, longitude, 10, 0, 0, 255);
        }
    }

    private void printQuadTree() {
        QuadTree<T> tree = spatialPoints.getQuadTree();
        Queue<QuadTree<T>> queue = new LinkedList<>();
        queue.add(tree);
        while (!queue.isEmpty()) {
            tree = queue.poll();
            printRect(tree.getTopLeft().getLatitude(), tree.getTopLeft().getLongitude(),
                    tree.getBottomRight().getLatitude(), tree.getBottomRight().getLongitude(),
                    1, 200, 200, 200, 255);
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