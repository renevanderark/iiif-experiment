package nl.kb.image;

class MatrixRotate {
    static int rotate(int offset, int width, int height, int deg) {
        switch (deg) {
            case 90:
                return rot90(offset, width, height);
            case 180:
                return rot180(offset, width, height);
            case 270:
                return rot270(offset, width, height);
            default:
                return offset;
        }
    }

    /* = 270 counter clockwise (height becomes width) */
    private static int rot90(int offset, int width, int height) {
        int y = (int) Math.floor(offset / width);
        int x = offset - (y * width);
        int transX = -y + (height - 1);
        return x * height + transX;
    }

    private static int rot180(int offset, int width, int height) {
        int y = (int) Math.floor(offset / width);
        int x = offset - (y * width);
        int transY = -y + (height - 1);
        int transX = -x + (width - 1);
        return transY * width + transX;
    }

    /* = 90 counter clockwise (height becomes width) */
    private static int rot270(int offset, int width, int height) {
        int y = (int) Math.floor(offset / width);
        int x = offset - (y * width);
        int transY = -x + (width - 1);
        return transY * height + y;
    }
}
