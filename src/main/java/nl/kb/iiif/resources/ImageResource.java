package nl.kb.iiif.resources;

import nl.kb.iiif.api.Region;
import nl.kb.iiif.api.ScaleDims;
import nl.kb.image.BufferedImageWriter;
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
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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

        final BufferedImage image = BufferedImageWriter.fromRaw(
                Jp2Decode.decodeArea(jp2Header, region.getX(), region.getY(), region.getW(), region.getH(), cp_reduce),
                DimReducer.reduce(region.getW(), cp_reduce), DimReducer.reduce(region.getH(), cp_reduce),
                scaleDims.getW(), scaleDims.getH(), deg
        );

        final JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
        final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpegParams.setCompressionQuality(1f);

        writer.setOutput(new MemoryCacheImageOutputStream(baos));
        writer.write(null, new IIOImage(image, null, null), jpegParams);

        return Response.ok(baos.toByteArray()).build();
    }
}
