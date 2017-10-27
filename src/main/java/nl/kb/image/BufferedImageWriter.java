package nl.kb.image;

import com.mortennobel.imagescaling.ResampleFilters;
import com.mortennobel.imagescaling.ResampleOp;

import java.awt.image.BufferedImage;

public class BufferedImageWriter {
    private BufferedImageWriter() { }

    
    public static BufferedImage fromRaw(int[][] colorBands, int width, int height, int newWidth, int newHeight, int deg) {
        // fancy downsampling
        int inOffset = 0;
        int[] remapped = new int[colorBands[0].length];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = colorBands[0][inOffset];
                int g = colorBands[colorBands.length < 3 ? 0 : 1][inOffset];
                int b = colorBands[colorBands.length < 3 ? 0 : 2][inOffset];
                int p = (r << 16) | (g << 8) | b; //pixel
                remapped[MatrixRotate.rotate(inOffset++, width, height, deg)] = p;
            }
        }


        if (deg == 0 || deg == 180) {
            final BufferedImage inImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            final ResampleOp resizeOp = new ResampleOp(newWidth, newHeight);
            inImage.setRGB(0, 0, width, height, remapped, 0, width);
            resizeOp.setFilter(ResampleFilters.getLanczos3Filter());
            return resizeOp.filter(inImage, null);
        } else {
            final BufferedImage inImage = new BufferedImage(height, width, BufferedImage.TYPE_INT_RGB);
            final ResampleOp resizeOp = new ResampleOp(newHeight, newWidth);
            inImage.setRGB(0, 0, height, width, remapped, 0, height);
            resizeOp.setFilter(ResampleFilters.getLanczos3Filter());
            return resizeOp.filter(inImage, null);
        }
/* non-fancy downsampling
        int[] rawOutput = new int[newWidth*newHeight];

        // YD compensates for the x loop by subtracting the width back out
        int YD = (height / newHeight) * width - width;
        int YR = height % newHeight;
        int XD = width / newWidth;
        int XR = width % newWidth;
        int outOffset= 0;
        int inOffset=  0;

        for (int y= newHeight, YE= 0; y > 0; y--) {
            for (int x= newWidth, XE= 0; x > 0; x--) {
                int r = colorBands[0][inOffset];
                int g = colorBands[colorBands.length < 3 ? 0 : 1][inOffset];
                int b = colorBands[colorBands.length < 3 ? 0 : 2][inOffset];
                int p = (r << 16) | (g << 8) | b; //pixel
                rawOutput[MatrixRotate.rotate(outOffset++, newWidth, newHeight, deg)]= p;
                inOffset+=XD;
                XE+=XR;
                if (XE >= newWidth) {
                    XE-= newWidth;
                    inOffset++;
                }
            }
            inOffset+= YD;
            YE+= YR;
            if (YE >= newHeight) {
                YE -= newHeight;
                inOffset+=width;
            }
        }

        if (deg == 0 || deg == 180) {
            final BufferedImage result = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            result.setRGB(0, 0, newWidth, newHeight, rawOutput, 0, newWidth);
            return result;
        } else {
            final BufferedImage result = new BufferedImage(newHeight, newWidth, BufferedImage.TYPE_INT_RGB);
            result.setRGB(0, 0, newHeight, newWidth, rawOutput, 0, newHeight);
            return result;
        }
*/
    }

}
