package nl.kb.iiif;

import nl.kb.image.BufferedImageWriter;
import nl.kb.jp2.Jp2Decode;
import nl.kb.jp2.Jp2Header;
import nl.kb.utils.NativeUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static nl.kb.jp2.DimReducer.reduce;

public class SampleTestRunner {
    static {
        try {
            NativeUtils.loadLibraryFromJar("/native/libjp2j.so");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String... args) {
        final File[] aFiles = new File("/home/rar010/research/iiif-experiment/samples/").listFiles();
        final List<File> files = Arrays.asList(aFiles != null ? aFiles : new File[0]);
        files.sort(Comparator.comparing(File::getAbsolutePath));

        for (File file : files) {
            if (file.isFile()) {
                try {
                    testRun(file);
                } catch (IOException e) {
                    System.err.println("Failed to decode file " + file.getAbsolutePath());
                    e.printStackTrace();
                }
            }

        }
    }

    private static void testRun(File file) throws IOException {
        long before = System.currentTimeMillis();
        final Jp2Header jp2Header = Jp2Header.read(file);



        System.out.println(file + ": " + (System.currentTimeMillis() - before) + "ms: " + jp2Header);


        for (int cp_reduce = 0; cp_reduce < jp2Header.getNumRes(); cp_reduce++) {
            before = System.currentTimeMillis();

            final int w = jp2Header.getX1();
            final int h = jp2Header.getY1();
            final int[][] bands = Jp2Decode.decodeArea(jp2Header, 0, 0, w, h, cp_reduce);
            System.out.println(file + ": " + (System.currentTimeMillis() - before) + "ms: decode");

            before = System.currentTimeMillis();

            final int reducedW = reduce(w, cp_reduce);
            final int reducedH = reduce(h, cp_reduce);
            BufferedImage img = BufferedImageWriter
                    .fromRaw(bands, reducedW, reducedH, (int)(reducedW * 0.1), (int)(reducedH * 0.1), 0);

            System.out.println(file + ": " + (System.currentTimeMillis() - before) + "ms: to buffered image");
            ImageIO.write(img, "jpg", new File("output/" +
                    file.getName()
                            .replaceAll("\\..*", "") + "-" + cp_reduce +".jpg"));

        }
    }
}
