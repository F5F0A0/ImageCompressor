package imagecompressor;

public class CircularHueMetric implements DistanceMetric_Inter {

    // implements the colorDistance method from DistanceMetric_Inter using circular hue distance
    // note that the provided Pixel class has a getHue method which returns the hue of the color
    // the hue of a color ignores saturation and darkness and focuses on where the color falls along the
    // "red, orange, yellow, green, blue, violet" spectrum
    // the hue is returned as an integer between 0 and 359
    // note that 0 is pure red, 120 is pure green, and 240 is pure blue

    // because hue is a circular spectrum (e.g., 359 is visually between 358 and 0), the distance
    // should be calculated with this in mind
    // as an example, the distance between hues 50 and 90 is 40
    // the distance between 20 and 340 is also 40
    // rather than measuring the distance from 20 upward to 340, it's shorter
    // to go from 340 upward past 360, looping around to 20
    // thus, the distance from this metric should never be higher than 180

    @Override
    public double colorDistance(Pixel p1, Pixel p2) {
        int h1 = p1.getHue();
        int h2 = p2.getHue();

        int diff = Math.abs(h1 - h2);

        return Math.min(diff, 360 - diff);
    }
    
}
