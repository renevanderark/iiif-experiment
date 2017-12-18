package nl.kb.iiif.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.kb.iiif.api.ImageInfo;
import nl.kb.iiif.api.Region;
import nl.kb.iiif.api.ScaleDims;
import nl.kb.iiif.core.ShardFetcher;
import nl.kb.jp2.Jp2Header;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;

@Path("/iiif-sharded/{identifier}")
public class IIIFShardedResource extends ImageResource {

    private final ShardFetcher shardFetcher;

    public IIIFShardedResource(ShardFetcher shardFetcher) {

        this.shardFetcher = shardFetcher;
    }


    @GET
    @Path("/")
    public Response redirectToInfo(@PathParam("identifier") String identifier) {
        final URI uri = UriBuilder.fromPath(String.format("/iiif-sharded/%s/info.json", identifier)).build();
        return Response.seeOther(uri).build();
    }

    @GET
    @Path("/info.json")
    @Produces(MediaType.APPLICATION_JSON)
    public Response info(@Context UriInfo uriInfo, @PathParam("identifier") String identifier) throws IOException {
        try {

            final Jp2Header jp2Header = new ObjectMapper()
                    .readValue(shardFetcher.fetchMetadata(identifier), Jp2Header.class);

            return Response
                    .ok(new ImageInfo(jp2Header, uriInfo))
                    .header("Access-Control-Allow-Origin", "*")
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
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
            final Jp2Header jp2Header = new ObjectMapper()
                    .readValue(shardFetcher.fetchMetadata(identifier), Jp2Header.class);



            final Region region = Region.parseAndDetermine(regionParam, jp2Header.getX1(), jp2Header.getY1());
            final ScaleDims scaleDims = ScaleDims.parseAndDetermine(sizeParam, region);
            final int deg = rotation.matches("^(90|180|270)$") ? Integer.parseInt(rotation) : 0;

            return getJpegResponse(jp2Header, region, scaleDims, deg, true);
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}
