package imagecompressor;

public class SquaredEuclideanMetric implements DistanceMetric_Inter {

    // Uses the squared Euclidean distance
    // that is, for two pixels (r1, g1, b1) and (r2, g2, b2), the distance would be
    // (r1-r2)^2 + (g1-g2)^2 + (b1-b2)^2
    
    @Override
    public double colorDistance(Pixel p1, Pixel p2) {
        int r1 = p1.getRed();
        int r2 = p2.getRed();

        int g1 = p1.getGreen();
        int g2 = p2.getGreen();

        int b1 = p1.getBlue();
        int b2 = p2.getBlue();

        return Math.pow(r1 - r2, 2) + Math.pow(g1 - g2, 2) + Math.pow(b1 - b2, 2);
    }
}