import processing.Graph;
import processing.core.PApplet;
import quad.SpatialPoints;

public class Application {
    public static void main(String[] args) {
        SpatialPoints<String> spatialPoints = new SpatialPoints<>();
        long time = System.currentTimeMillis();
        for (int i = 0; i < 1_000; i++) {
            spatialPoints.insert(Math.random() * 100, Math.random() * 100, String.valueOf(i));
        }
        System.out.println("InsertionTime:" + (System.currentTimeMillis() - time));

        Graph<String> mySketch = new Graph<>(spatialPoints, 75, 75, 20, SpatialPoints.searchType.CIRCLE);
        PApplet.runSketch(new String[]{"MySketch"}, mySketch);
    }
}
