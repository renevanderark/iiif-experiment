package nl.kb.jp2;

import java.io.File;
import java.io.IOException;

public class Jp2Header {
    private Object scaleFactors;

    private Jp2Header() { }
    private int x1;
    private int y1;
    private int tw;
    private int th;
    private int tdx;
    private int tdy;
    private int numRes;
    private int numComps;
    private String fileName;

    public int getX1() {
        return x1;
    }

    public void setX1(int x1) {
        this.x1 = x1;
    }

    public int getY1() {
        return y1;
    }

    public void setY1(int y1) {
        this.y1 = y1;
    }

    public int getTw() {
        return tw;
    }

    public void setTw(int tw) {
        this.tw = tw;
    }

    public int getTh() {
        return th;
    }

    public void setTh(int th) {
        this.th = th;
    }

    public int getTdx() {
        return tdx;
    }

    public void setTdx(int tdx) {
        this.tdx = tdx;
    }

    public int getTdy() {
        return tdy;
    }

    public void setTdy(int tdy) {
        this.tdy = tdy;
    }

    public int getNumRes() {
        return numRes;
    }

    public void setNumRes(int numRes) {
        this.numRes = numRes;
    }

    public int getNumComps() {
        return numComps;
    }

    public void setNumComps(int numComps) {
        this.numComps = numComps;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public String toString() {
        return "Jp2Header{" +
                "x1=" + x1 +
                ", y1=" + y1 +
                ", tw=" + tw +
                ", th=" + th +
                ", tdx=" + tdx +
                ", tdy=" + tdy +
                ", numRes=" + numRes +
                ", numComps=" + numComps +
                '}';
    }

    public static Jp2Header read(File file) throws IOException {
        final Jp2Header jp2Header = fromFile(file.getAbsolutePath());
        jp2Header.setFileName(file.getAbsolutePath());
        return jp2Header;
    }

    private static native Jp2Header fromFile(String filename) throws IOException;
}
