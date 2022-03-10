import quad.SpatialPoints;

public class Application {
    public static void main(String[] args) {
        SpatialPoints<String> spatialPoints = new SpatialPoints<>();
        long time = System.currentTimeMillis();
        for (int i = 0; i < 600; i++) {
            spatialPoints.insert(Math.random() * 100, Math.random() * 100, String.valueOf(i));
        }
        System.out.println(System.currentTimeMillis() - time);
        time = System.currentTimeMillis();
        spatialPoints.rangeSearch(95.0001, 95, 0.1);
        System.out.println(System.currentTimeMillis() - time);
        time = System.currentTimeMillis();
        spatialPoints.rangeSearch(95, 95, 10);
        System.out.println(System.currentTimeMillis() - time);
    }
}
