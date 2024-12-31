/*
 * @Author: Liam Baird
 * Run the bitmap quantization methods all at once on an image or directory of images
 * Result images are saved to the outputs directory with a structure of
 * outputs/{id}/{filename}/{method}/n{numColors}/{id}_{filename}_{method}_n{numColors}.bmp
 * You can change this structure by modifying the getOutputPath method and the fields passed to it
 * Make sure to change the id and paths.
 */
package imagecompressor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Run {

    static final String id = "brb351";
    static final String baseOutputPath = "/Users/bridgetbrinkman/Documents/GitHub/ImageCompressor/app/src/main/java/imagecompressor/outputs";
    static final String baseInputPath = "/Users/bridgetbrinkman/Documents/GitHub/ImageCompressor/app/src/main/java/imagecompressor/inputs";

    public static void main(String[] args) {
        runOnDirectory(baseInputPath, 2, 4, 8, 16);
    }

    private static String getOutputPath(String name, String... fields) {
        String meta = "";
        String path = baseOutputPath + "/" + id + "/" + name;
        for (String field : fields) {
            meta += "_" + field;
            path += "/" + field;
        }
        try {
            Files.createDirectories(Paths.get(path));
        } catch (IOException e) {
            System.out.println("Error creating directory: " + path);
            e.printStackTrace();
            System.exit(1);
        }

        return path + "/" + id + "_" + name + "_" + meta + ".bmp";
    }

    public static void runOnImage(String filename, int numColors) {
        String filename_noextension = filename.substring(0, filename.length() - 4);// .bmp only
        String filePath = baseInputPath + "/" + filename;
        ColorMapGenerator_Inter genEuclid = new ClusteringMapGenerator(new SquaredEuclideanMetric());
        ColorQuantizer quantEuclid = new ColorQuantizer(filePath, genEuclid);
        quantEuclid.quantizeToBMP(getOutputPath(filename_noextension, "euclid", "n" + numColors), numColors);
        ColorMapGenerator_Inter genHue = new ClusteringMapGenerator(new CircularHueMetric());
        ColorQuantizer quantHue = new ColorQuantizer(filePath, genHue);
        quantHue.quantizeToBMP(getOutputPath(filename_noextension, "hue", "n" + numColors), numColors);
        ColorMapGenerator_Inter genBucket = new BucketingMapGenerator();
        ColorQuantizer quantBucket = new ColorQuantizer(filePath, genBucket);
        quantBucket.quantizeToBMP(getOutputPath(filename_noextension, "bucket", "n" + numColors), numColors);
    }

    public static void runOnDirectory(String dir, int... numColors) {
        File folder = new File(dir);
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            String filename = file.getName();
            if (filename.endsWith(".bmp")) {
                for (int n : numColors) {
                    System.out.println("Running on " + filename + " with " + n + " colors");
                    runOnImage(filename, n);
                }
            }

        }
    }

}
