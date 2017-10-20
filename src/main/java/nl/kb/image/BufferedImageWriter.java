package nl.kb.image;

import java.awt.image.BufferedImage;

public class BufferedImageWriter {
    private BufferedImageWriter() { }

    
    public static BufferedImage fromRaw(int[][] colorBands, int width, int height, int newWidth, int newHeight) {
/*
        int[] rawInput = new int[height * width];
        original.getRGB(0, 0, width, height, rawInput, 0, width);
*/

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

                rawOutput[outOffset++]= p;
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

        final BufferedImage result = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        result.setRGB(0, 0, newWidth, newHeight, rawOutput, 0, newWidth);

        return result;
    }
}
