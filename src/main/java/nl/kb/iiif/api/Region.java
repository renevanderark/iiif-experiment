package nl.kb.iiif.api;

public class Region {
    private final int x;
    private final int y;
    private final int w;
    private final int h;

    @Override
    public String toString() {
        return "Region{" +
                "x=" + x +
                ", y=" + y +
                ", w=" + w +
                ", h=" + h +
                '}';
    }

    private Region(int x, int y, int w, int h) {

        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public static Region parseAndDetermine(String raw, int imgW, int imgH) {
        if (raw.equals("square")) {
            return getSquare(imgW, imgH);
        } else if (raw.startsWith("pct:") && raw.matches("^pct:[0-9.]+,[0-9.]+,[0-9.]+,[0-9.]+$")) {
            final String[] dims = raw.replaceAll("^pct:","").split(",");
            return new Region(
                    (int) ((Double.parseDouble(dims[0]) / 100.0) * imgW),
                    (int) ((Double.parseDouble(dims[1]) / 100.0)  * imgH),
                    (int) ((Double.parseDouble(dims[2]) / 100.0)  * imgW),
                    (int) ((Double.parseDouble(dims[3]) / 100.0)  * imgH));

        } else if (raw.matches("^[0-9]+,[0-9]+,[0-9]+,[0-9]+$")) {
            final String[] dims = raw.split(",");
            return new Region(Integer.parseInt(dims[0]),
                    Integer.parseInt(dims[1]),
                    Integer.parseInt(dims[2]),
                    Integer.parseInt(dims[3]));

        } else /* if raw.equals("full") */{
            return new Region(0,0, imgW, imgH);
        }
    }

    private static Region getSquare(int imgW, int imgH) {
        return imgH < imgW
               ? new Region((int) ((imgW - imgH) * 0.5), 0, imgH, imgH)
               : new Region(0, (int) ((imgH - imgW) * 0.5), imgW, imgW);

    }

    public int getW() {
        return w;
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    public int getH() {
        return h;
    }
}
