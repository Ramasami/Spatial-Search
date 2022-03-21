import processing.Graph;
import processing.core.PApplet;
import quad.SpatialPoints;

import static quad.SpatialPoints.searchType.*;

public class Application {
    public static void main(String[] args) {
        SpatialPoints<String> spatialPoints = new SpatialPoints<>();
        long time = System.currentTimeMillis();
        for (int i = 0; i < 1_000; i++) {
            spatialPoints.insert(Math.random() * 100, Math.random() * 100, String.valueOf(i));
        }
        System.out.println("InsertionTime:" + (System.currentTimeMillis() - time));

        Graph<String> quadTree = new Graph<>(spatialPoints, 75, 75, 20, CIRCLE, String::valueOf);
        PApplet.runSketch(new String[]{"QuadTree"}, quadTree);
    }
}
