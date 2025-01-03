package imagecompressor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClusteringMapGenerator implements ColorMapGenerator_Inter {

    // this class must have a constructor that accepts an object that is a subtype of DistanceMetric_Inter
    // that is, when instantiating ClustringMapGenerator, one must specify an object that can be used (via the colorDistance)
    // method, to determine the distance between the two colors, each represented as an instance of Pixel

    // will perform color quantization via a variation of k-means clustering as follows

    private final DistanceMetric_Inter metric;

    public ClusteringMapGenerator(DistanceMetric_Inter metric) {
        this.metric = metric;
    }

    /**
     * Produces an initial palette. For bucketing implementations, the initial
     * palette will be the centers of the evenly-divided buckets. For clustering
     * implementations, the initial palette will be the initial centroids. When
     * needed, a distance metric should be specified when the color map
     * generator is constructed.
     *
     * @param pixelArray the 2D Pixel array that represents a bitmap image
     * @param numColors the number of desired colors in the palette
     * @return a Pixel array containing numColors elements
     */
    @Override
    public Pixel[] generateColorPalette(Pixel[][] pixelArray, int numColors) {
        Pixel[] palette = new Pixel[numColors];
        palette[0] = pixelArray[0][0]; // set the first centroid

        for (int i = 1; i < numColors; i++) { // Filling the buckets
            // Select the remaining numColors - 1 colors by choosing the pixel 
            // with the greatest computed distance from its closest existing centroid.
            Pixel furthestPixel = null;
            double maxDistance = -1;

            for (int row = 0; row < pixelArray.length; row++) {
                for (int col = 0; col < pixelArray[0].length; col++) {
                    Pixel candidate = pixelArray[row][col];
    
                    double minDistanceToCentroids = Double.MAX_VALUE;
                    for (int j = 0; j < i; j++) {
                        double distance = metric.colorDistance(candidate, palette[j]);
                        minDistanceToCentroids = Math.min(minDistanceToCentroids, distance);
                    }

                    if (minDistanceToCentroids > maxDistance) {
                        maxDistance = minDistanceToCentroids;
                        furthestPixel = candidate;
                    }
                    else if (minDistanceToCentroids == maxDistance) {
                        int candidateRGB = (candidate.getRed() << 16) | (candidate.getGreen() << 8) | candidate.getBlue();
                        int furthestRGB = (furthestPixel.getRed() << 16) | (furthestPixel.getGreen() << 8) | furthestPixel.getBlue();
                        if (candidateRGB > furthestRGB) {
                            furthestPixel = candidate;
                        }
                    }
                }
            }
            palette[i] = furthestPixel;
        }
        return palette;
    }

    // This method should implement the naive k-means clusering algorithm we discussed in lecture (Lloyd's algorithm).
    // The initialColorPalette parameter identifies the starting k centroids. This clustering should produce a final
    // color palette and then return a map of each distinct color in pixelArray to its value in the final color palette.
    
    /**
     * Computes the reduced color map. For bucketing implementations, this will
     * map each color to the center of its bucket. For clustering
     * implementations, this will map each color to its final centroid. When
     * needed, a distance metric should be specified when the color map
     * generator is constructed.
     *
     * @param pixelArray the pixels array that represents a bitmap image
     * @param initialColorPalette an initial color palette, such as those
     * generated by generateColorPalette, represented as an array of Pixels
     * @return A Map that maps each distinct color in pixelArray to a final
     * color
     */
    @Override
    public Map<Pixel, Pixel> generateColorMap(Pixel[][] pixelArray, Pixel[] initialColorPalette) {
        // Pick starting "means" (initial centroids) given in initialColorPalette
        // Assign all examples to a cluster based on the centroid they are closest
        // to by some distance metric (e.g., Euclidean, CircularHue).
        // For each cluster, compute the new centroid as the mean of all feature vectors in that cluster.
        // Redo assignments/centroid calculation until convergence. I.e., no examples are assigned
        // to new clusters after new centroids are computed.

        List<Pixel> centroids = new ArrayList<>(); // list of centroids
        for (Pixel p : initialColorPalette) {
            centroids.add(p);
        }

        Map<Pixel, List<Pixel>> clusters = new HashMap<>(); // centroid and its cluster
        for (Pixel centroid : centroids) {
            clusters.put(centroid, new ArrayList<>());
        }

        boolean converged = false;
        int maxIterations = 1000;
        int iterations = 0;

        while (!converged && iterations < maxIterations) {
            System.out.println("Iteration: " + iterations);
            
            // clear previous cluster assignments
            for (Pixel centroid : clusters.keySet()) {
                clusters.get(centroid).clear();
            }

            // assign each pixel to its closest centroid
            for (int i = 0; i < pixelArray.length; i++) {
                for (int j = 0; j < pixelArray[0].length; j++) {
                    Pixel pixel = pixelArray[i][j];
                    Pixel closestCentroid = null;
                    double minDistance = Double.MAX_VALUE;

                    for (Pixel centroid : centroids) {
                        double distance = metric.colorDistance(pixel, centroid);
                        if (distance < minDistance) {
                            minDistance = distance;
                            closestCentroid = centroid;
                        }
                    }
                    clusters.get(closestCentroid).add(pixel); // add the pixel to the cluster
                }
            }

            // compute new centroids for each cluster
            List<Pixel> newCentroids = new ArrayList<>();
            for (Pixel centroid : centroids) {
                List<Pixel> clusterPixels = clusters.get(centroid);

                if (!clusterPixels.isEmpty()) {
                    int redTotal = 0;
                    int greenTotal = 0;
                    int blueTotal = 0;
                    for (Pixel p : clusterPixels) {
                        redTotal += p.getRed();
                        greenTotal += p.getGreen();
                        blueTotal += p.getBlue();
                    }

                    int clusterSize = clusterPixels.size();
                    Pixel newCentroid = new Pixel(redTotal/clusterSize, greenTotal/clusterSize, blueTotal/clusterSize);
                    newCentroids.add(newCentroid);
                }
                else {
                    newCentroids.add(centroid);
                }
            }

            converged = centroids.equals(newCentroids);
            centroids = newCentroids;

            Map<Pixel, List<Pixel>> newClusters = new HashMap<>();
            for (Pixel newCentroid : centroids) {
                newClusters.put(newCentroid, clusters.getOrDefault(newCentroid, new ArrayList<>()));
            }
            clusters = newClusters;
            iterations++;
        }

        Map<Pixel, Pixel> pixelToCentroidMap = new HashMap<>();
        for (Map.Entry<Pixel, List<Pixel>> entry : clusters.entrySet()) {
            Pixel centroid = entry.getKey();
            for (Pixel pixel : entry.getValue()) {
                pixelToCentroidMap.put(pixel, centroid);
            }
        }

        return pixelToCentroidMap;
    }
}
