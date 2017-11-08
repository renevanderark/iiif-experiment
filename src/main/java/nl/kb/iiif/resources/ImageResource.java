package nl.kb.iiif.resources;

import nl.kb.iiif.api.Region;
import nl.kb.iiif.api.ScaleDims;
import nl.kb.image.BufferedImageWriter;
import nl.kb.jp2.DecodedImage;
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

        final DecodedImage decodedImage = Jp2Decode.decodeArea(jp2Header, region.getX(), region.getY(), region.getW(), region.getH(), cp_reduce);
        final BufferedImage image = BufferedImageWriter.fromRaw(
                decodedImage.getColorBands(),
                decodedImage.getDecodedImageDims().getWidth(), decodedImage.getDecodedImageDims().getHeight(),
                scaleDims.getW(), scaleDims.getH(), deg
        );

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
}
