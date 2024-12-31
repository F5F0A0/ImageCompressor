package imagecompressor;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

public class ColorQuantizer implements ColorQuantizer_Inter {

    private Pixel[][] pixelArray;
    private ColorMapGenerator_Inter gen;

    // Utils
    // convertBitmapToPixelMatrix
    // savePixelMatrixToFile
    // savePixelMatrixToBitmap

    // This class must have two constructors.

    // The first constructor must accept a two-dimensional Pixel array which represents the pixels from
    // a .bmp file and an object that is a subtype of ColorMapGenerator_Inter. Similarly to how you specify
    // which distance metric to use in your ColorMapGenerator, here you can specify which type of ColorMapGenerator
    // to use in your color quantizer class.
    public ColorQuantizer(Pixel[][] pixelArray, ColorMapGenerator_Inter gen) {
        if (pixelArray == null || gen == null) {
            throw new IllegalArgumentException("pixelArray and gen must not be null");
        }

        // 2 types of ColorMapGenerator:
        // - BucketingMapGenerator
        // - ClusteringMapGenerator

        // store the pixel data and the ColorMapGenerator_Inter instance for later use by method quantizeTo2DArray
        this.pixelArray = pixelArray;
        this.gen = gen;
    }

    // The second constructor must accept the name of .bmp file to read (as a String) and a ColorMapGenerator_Inter
    // as above. 
    public ColorQuantizer(String bmpFilename, ColorMapGenerator_Inter gen) {
        if (gen == null) {
            throw new IllegalArgumentException("ColorMapGenerator instance (gen) must not be null");
        }
        try {
            File bmpFile = new File(bmpFilename);
            BufferedImage image = ImageIO.read(bmpFile);
            if (image == null) {
                throw new IOException("Invalid BMP file or unsupported format: " + bmpFilename);
            }
            this.pixelArray = Util.convertBitmapToPixelMatrix(image);
            this.gen = gen;
        } 
        catch (IOException e) {
            throw new IllegalArgumentException("Error reading BMP file: " + bmpFilename, e);
        }
    }
    
    /**
     * Performs color quantization using the color map generator specified when
     * this quantizer was constructed.
     *
     * @param numColors number of colors to use for color quantization
     * @return A two dimensional array where each index represents the pixel
     * from the original bitmap image and contains a Pixel representing its
     * color after quantization
     */
    @Override
    public Pixel[][] quantizeTo2DArray(int numColors) {
        // 2 types of ColorMapGenerator:
        // - BucketingMapGenerator
        // - ClusteringMapGenerator
        Pixel[][] quantized2DArray = new Pixel[pixelArray.length][pixelArray[0].length];
        Pixel[] palette = gen.generateColorPalette(pixelArray, numColors);
        Map<Pixel, Pixel> map = gen.generateColorMap(pixelArray, palette);
        for (int i = 0; i < pixelArray.length; i++) {
            for (int j = 0; j < pixelArray[0].length; j++) {
                quantized2DArray[i][j] = map.get(pixelArray[i][j]);
            }
        }
        return quantized2DArray;
    }

    /**
     * Performs color quantization using the color map generator specified when
     * this quantizer was constructed. Rather than returning the pixel array,
     * this method writes the resulting image in bmp format to the specified
     * file.
     *
     * @param numColors number of colors to use for color quantization
     * @param fileName File to write resulting image to
     */
    @Override
    public void quantizeToBMP(String fileName, int numColors) {
        try {
            Pixel[][] quantized2DArray = quantizeTo2DArray(numColors);
            Util.savePixelMatrixToBitmap(fileName, quantized2DArray);
        }
        catch (Exception e) {
            //
        }
    }
    
}