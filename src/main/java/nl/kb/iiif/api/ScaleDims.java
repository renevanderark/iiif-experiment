package nl.kb.iiif.api;

public class ScaleDims {
    @Override
    public String toString() {
        return "ScaleDims{" +
                "h=" + h +
                ", w=" + w +
                '}';
    }

    private final int h;
    private final int w;

    private ScaleDims(int w, int h) {
        this.w = w;
        this.h = h;
    }

    public static ScaleDims parseAndDetermine(String sizeParam, Region region) {
        if (sizeParam.matches("^pct:[0-9.]+$")) {
            final double s = Double.parseDouble(sizeParam.replaceAll("^pct:", "")) / 100.0;
            return new ScaleDims((int)Math.round(s * region.getW()), (int) Math.round(s * region.getH()));

        } else /* sizeParam = full or max*/ {
          return new ScaleDims(region.getW(), region.getH());
        }
    }

    public int getH() {
        return h;
    }

    public int getW() {
        return w;
    }
}
