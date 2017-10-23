package nl.kb.iiif.resources;

import nl.kb.iiif.api.ImageInfo;
import nl.kb.iiif.api.Region;
import nl.kb.iiif.api.ScaleDims;
import nl.kb.iiif.core.ImageFetcher;
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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;

@Path("/iiif-service/{identifier}")
public class ImageResource {
    private final ImageFetcher imageFetcher;

    public ImageResource(ImageFetcher imageFetcher) {
        this.imageFetcher = imageFetcher;
    }

    @GET
    @Path("/")
    public Response redirectToInfo(@PathParam("identifier") String identifier) {
        final URI uri = UriBuilder.fromPath(String.format("/iiif-service/%s/info.json", identifier)).build();
        return Response.seeOther(uri).build();
    }

    @GET
    @Path("/info.json")
    @Produces(MediaType.APPLICATION_JSON)
    public Response info(@Context UriInfo uriInfo, @PathParam("identifier") String identifier) throws IOException {
        try {
            final File cached = imageFetcher.fetch(identifier);
            final Jp2Header jp2Header = Jp2Header.read(cached);

            return Response.ok(new ImageInfo(jp2Header, uriInfo)).build();
        } catch (IOException e) {
            imageFetcher.clear(identifier);
            throw e;
        }
    }

    @GET
    @Path("/{region}/{size}/{rotation}/default.jpg")
    @Produces("image/jpeg")
    public Response decode(@PathParam("identifier") String identifier,
        @PathParam("region") String regionParam,
        @PathParam("size") String sizeParam,
        @PathParam("rotation") String rotation
    ) {
        try {
            final File cached = imageFetcher.fetch(identifier);
            final Jp2Header jp2Header = Jp2Header.read(cached);
            final Region region = Region.parseAndDetermine(regionParam, jp2Header.getX1(), jp2Header.getY1());
            final ScaleDims scaleDims = ScaleDims.parseAndDetermine(sizeParam, region);

            // TODO: determine optimal cp_reduce by deriving actual scale & recalculate scaleDims accordingly
            final double scale = (double) scaleDims.getW() / (double) region.getW();
            int cp_reduce = 0;

            if (scale <= 0.5) {
              for (double s = 0.5; scale < s && cp_reduce < jp2Header.getNumRes() - 1; cp_reduce++, s *= 0.5) {
              }
            }
            System.out.println("Derived scale: " + scale + " cp_reduce: " + cp_reduce);

            final BufferedImage image = BufferedImageWriter.fromRaw(
                    Jp2Decode.decodeArea(jp2Header, region.getX(), region.getY(), region.getW(), region.getH(), cp_reduce),
                    DimReducer.reduce(region.getW(), cp_reduce), DimReducer.reduce(region.getH(), cp_reduce),
                    scaleDims.getW(), scaleDims.getH()
            );

            final JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
            final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();

            jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpegParams.setCompressionQuality(1f);

            writer.setOutput(new MemoryCacheImageOutputStream(baos));
            writer.write(null, new IIOImage(image, null, null), jpegParams);

            return Response.ok(baos.toByteArray()).build();
        } catch (IOException e) {
            imageFetcher.clear(identifier);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
