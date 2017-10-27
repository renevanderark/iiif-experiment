package nl.kb.iiif.resources;

import nl.kb.iiif.api.Region;
import nl.kb.iiif.api.ScaleDims;
import nl.kb.iiif.core.ImageFetcher;
import nl.kb.jp2.Jp2Header;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;

@Path("/imagingService")
public class ImagingServiceResource extends ImageResource {
    private final ImageFetcher imageFetcher;

    public ImagingServiceResource(ImageFetcher imageFetcher) {
        this.imageFetcher = imageFetcher;
    }

    @GET
    @Produces("image/jpeg")
    public Response get(
            @QueryParam("id") String identifier,
            @QueryParam("x") Integer xParam,
            @QueryParam("y") Integer yParam,
            @QueryParam("w") Integer wParam,
            @QueryParam("h") Integer hParam,
            @QueryParam("s") Double sParam,
            @QueryParam("r") Integer rParam
    ) {
        System.out.println(identifier);
        System.out.println(xParam);
        System.out.println(yParam);
        System.out.println(wParam);
        System.out.println(hParam);
        System.out.println(sParam);
        System.out.println(rParam);

        try {
            final File cached = imageFetcher.fetch(identifier);
            final Jp2Header jp2Header = Jp2Header.read(cached);
            final ScaleDims scaleDims = new ScaleDims(jp2Header);
            final Region region = new Region(jp2Header);

            // TODO apply rotation using MatrixRotate on all dimension params ...

            interpretParams(scaleDims, region, xParam, yParam, wParam, hParam, sParam, jp2Header);

            return getJpegResponse(jp2Header, region, scaleDims, 0);

        } catch (IOException e) {
            imageFetcher.clear(identifier);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (UnsupportedOperationException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private void interpretParams(ScaleDims scaleDims, Region region,
                                 Integer xParam, Integer yParam, Integer wParam, Integer hParam, Double sParam,
                                 Jp2Header jp2Header) {

        // TODO: integrate current if-blocks
        // TODO: ...then apply apply rotation using MatrixRotate on the requested[XYWH] params...
        // TODO: ...but before deriving any scale from them !!
        // TODO: ...and DEFINITELY before checking any bounding-box restrictions!

        // ORDER OF OPERERATIONS is:
        // 1) rotate the region to be decoded using MatrixRotate
        // 2) the w and h properties should be swapped (if rot=90|270), because they are 'rotated back' afterwards.
        // 3) derive the scale based on the rotated region and the unrotated source image
        // 4) decode this region
        // 5) rotate this decoded region again
        if (xParam == null && yParam == null && sParam == null) {
            if (hParam == null && wParam == null) {
                return;
            } if (hParam == null) {
                setScaleFromWidthParam(scaleDims, wParam, jp2Header);
            } else if (wParam == null) {
                setScaleFromHeightParam(scaleDims, hParam, jp2Header);
            } else {
                if (hParam < wParam) {
                    setScaleFromHeightParam(scaleDims, hParam, jp2Header);
                } else {
                    setScaleFromWidthParam(scaleDims, wParam, jp2Header);
                }
            }
        } else {
            sParam = sParam == null ? 1.0 : sParam;
            final int requestedX = xParam == null ? 0 : xParam;
            final int requestedY = yParam == null ? 0 : yParam;
            final int requestedW = wParam == null ? (int) Math.round(jp2Header.getX1() * sParam) : wParam;
            final int requestedH = hParam == null ? (int) Math.round(jp2Header.getY1() * sParam) : hParam;

            final int scaledRequestedW = (int) Math.round(requestedW / sParam);
            final int scaledRequestedH = (int) Math.round(requestedH / sParam);
            final int requestedRegionX = (int) Math.round(requestedX / sParam);
            final int requestedRegionY = (int) Math.round(requestedY / sParam);

            int derivedRegionW = Math.min(scaledRequestedW, jp2Header.getX1());
            int derivedRegionH = Math.min(scaledRequestedH, jp2Header.getY1());
            int derivedRegionX;
            int derivedRegionY;

            if (requestedRegionX + derivedRegionW > jp2Header.getX1()) {
                if (jp2Header.getX1() - scaledRequestedW >= 0) {
                    derivedRegionX = jp2Header.getX1() - scaledRequestedW;
                    derivedRegionW = scaledRequestedW;
                } else {
                    derivedRegionX = 0;
                }
            } else {
                derivedRegionX = requestedRegionX;
            }

            if (requestedRegionY + derivedRegionH > jp2Header.getY1()) {
                if (jp2Header.getY1() - scaledRequestedH >= 0) {
                    derivedRegionY = jp2Header.getY1() - scaledRequestedH;
                    derivedRegionH = scaledRequestedH;
                } else {
                    derivedRegionY = 0;
                }
            } else {
                derivedRegionY = requestedRegionY;
            }

            scaleDims.setW((int) Math.round(derivedRegionW * sParam));
            scaleDims.setH((int) Math.round(derivedRegionH * sParam));
            region.setW(derivedRegionW);
            region.setH(derivedRegionH);
            region.setX(derivedRegionX);
            region.setY(derivedRegionY);
        }
    }

    private void setScaleFromHeightParam(ScaleDims scaleDims, Integer hParam, Jp2Header jp2Header) {
        scaleDims.setH(hParam);
        scaleDims.setW((int) Math.round(jp2Header.getX1() * ((double) hParam / (double) jp2Header.getY1())));
    }

    private void setScaleFromWidthParam(ScaleDims scaleDims, Integer wParam, Jp2Header jp2Header) {
        scaleDims.setW(wParam);
        scaleDims.setH((int) Math.round(jp2Header.getY1() * ((double) wParam / (double) jp2Header.getX1())));
    }
}
