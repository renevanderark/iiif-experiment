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
            final BufferedImage inImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            inImage.setRGB(0, 0, width, height, remapped, 0, width);

            if (width == newWidth && height == newHeight) {
                return inImage;
            }

            final ResampleOp resizeOp = new ResampleOp(newWidth, newHeight);
            resizeOp.setFilter(ResampleFilters.getLanczos3Filter());
            return resizeOp.filter(inImage, null);
        } else {
            final BufferedImage inImage = new BufferedImage(height, width, BufferedImage.TYPE_3BYTE_BGR);
            inImage.setRGB(0, 0, height, width, remapped, 0, height);

            if (width == newWidth && height == newHeight) {
                return inImage;
            }

            final ResampleOp resizeOp = new ResampleOp(newHeight, newWidth);
            resizeOp.setFilter(ResampleFilters.getLanczos3Filter());
            return resizeOp.filter(inImage, null);
        }
    }

}
