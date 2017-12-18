package nl.kb.iiif.resources;

import nl.kb.iiif.api.Region;
import nl.kb.iiif.api.ScaleDims;
import nl.kb.image.BufferedImageWriter;
import nl.kb.jp2.DecodedImage;
import nl.kb.jp2.DimReducer;
import nl.kb.jp2.Jp2Decode;
import nl.kb.jp2.Jp2Header;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

class ImageResource {
    Response getJpegResponse(Jp2Header jp2Header, Region region, ScaleDims scaleDims, int deg) throws IOException {
        return getJpegResponse(jp2Header, region, scaleDims, deg, false);
    }

    Response getJpegResponse(Jp2Header jp2Header, Region region, ScaleDims scaleDims, int deg, boolean fromShards) throws IOException {

        if (!region.isValid() || !scaleDims.isValid()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        final double scale = (double) scaleDims.getW() / (double) region.getW();
        int cp_reduce = 0;

        if (scale <= 0.5) {
            for (double s = 0.5; scale <= s && cp_reduce < jp2Header.getNumRes() - 1; cp_reduce++, s *= 0.5) {
                // Leave empty
            }
        }

        final BufferedImage image = fromShards ?
                getBufferedImageFromJp2Shards(jp2Header, region, scaleDims, deg, cp_reduce) :
                getBufferedImageFromJp2File(jp2Header, region, scaleDims, deg, cp_reduce);

        final JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
        final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();

        jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpegParams.setCompressionQuality(1f);


        final StreamingOutput stream = os -> {
            writer.setOutput(new MemoryCacheImageOutputStream(os));
            try {
                writer.write(null, new IIOImage(image, null, null), jpegParams);
            } catch (IOException ignored) {
                // ignores broken pipes when peer closes connection early
            }
        };
        return Response
                .ok(stream)
                .header("Content-type", "image/jpeg")
                .header("Cache-Control", "public, max-age=3600")
                .header("Expires", ZonedDateTime.now().plusHours(1).format(DateTimeFormatter.ofPattern("EEE, dd-MMM-yyyy HH:mm:ss zzz")))
                .build();
    }

    private BufferedImage getBufferedImageFromJp2Shards(Jp2Header jp2Header, Region region, ScaleDims scaleDims, int deg, int cp_reduce) {
        final BufferedImage inImage = new BufferedImage(DimReducer.reduce(region.getW(), cp_reduce), DimReducer.reduce(region.getH(), cp_reduce),
                BufferedImage.TYPE_3BYTE_BGR);

        final int minY = region.getY() - (region.getY() % jp2Header.getTdy());
        final int minX = region.getX() - (region.getX() % jp2Header.getTdx());

        System.out.println(region);
        for (int x = minX; x  < region.getX() + region.getW(); x += jp2Header.getTdx()) {
            for (int y = minY; y  < region.getY() + region.getH(); y += jp2Header.getTdy()) {
                System.out.println(String.format("%d-%d-%d.jpg", x, y, cp_reduce));
            }
        }

        return inImage;
    }

    private BufferedImage getBufferedImageFromJp2File(Jp2Header jp2Header, Region region, ScaleDims scaleDims, int deg, int cp_reduce) throws IOException {
        final DecodedImage decodedImage = Jp2Decode.decodeArea(jp2Header, region.getX(), region.getY(), region.getW(), region.getH(), cp_reduce);
        return BufferedImageWriter.fromRaw(
                decodedImage.getColorBands(),
                decodedImage.getDecodedImageDims().getWidth(), decodedImage.getDecodedImageDims().getHeight(),
                scaleDims.getW(), scaleDims.getH(), deg
        );
    }
}
