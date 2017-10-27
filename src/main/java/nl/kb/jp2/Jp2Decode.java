package nl.kb.jp2;

import java.io.IOException;

public class Jp2Decode {
    private Jp2Decode() { }

    public static int[][] decodeArea(Jp2Header jp2Header, int x, int y, int w, int h, int cp_reduce /* TODO: params */) throws IOException {
        int size = DimReducer.reduce(w * h, cp_reduce);
        int[][] colorBands = new int[jp2Header.getNumComps()][];
        for (int i = 0; i < jp2Header.getNumComps(); i++) {
            colorBands[i] = new int[size];
        }
        decodeJp2Area(jp2Header.getFileName(), x, y, w, h, cp_reduce, colorBands);

        // TODO: rewrite to return also width/height (size/width) of decoded image
        return colorBands;
    }

    // TODO: rewrite to return width of decoded image
    private static native void decodeJp2Area(String filename,
                                             int x, int y, int w, int h, int cp_reduce,
                                             int[][] colorBands) throws IOException;
}
