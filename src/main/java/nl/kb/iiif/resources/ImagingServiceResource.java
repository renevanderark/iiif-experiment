package nl.kb.iiif.resources;

import nl.kb.iiif.api.Region;
import nl.kb.iiif.api.ScaleDims;
import nl.kb.iiif.core.ImageFetcher;
import nl.kb.jp2.Jp2Header;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
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
    public Response get(
            @QueryParam("id") String identifier,
            @QueryParam("x") Double xParam,
            @QueryParam("y") Double yParam,
            @QueryParam("w") Double wParam,
            @QueryParam("h") Double hParam,
            @QueryParam("s") Double sParam,
            @QueryParam("r") Double rParam,
            @QueryParam("c") String cParam,
            @QueryParam("f") String fParam
    ) {
        if (cParam != null && cParam.equals("imghead")) {
            // TODO
            return Response.noContent().build();
        }

        try {
            final File cached = imageFetcher.fetch(identifier);
            final Jp2Header jp2Header = Jp2Header.read(cached);
            final ScaleDims scaleDims = new ScaleDims(jp2Header);
            final Region region = new Region(jp2Header);

            interpretParams(scaleDims, region,
                    xParam == null ? null : (int) Math.round(xParam),
                    yParam == null ? null : (int) Math.round(yParam),
                    wParam == null ? null : (int) Math.round(wParam),
                    hParam == null ? null : (int) Math.round(hParam),
                    sParam,
                    rParam == null ? null : rParam.intValue(),
                    jp2Header);

            return getJpegResponse(jp2Header, region, scaleDims, rParam == null ? 0 : rParam.intValue());

        } catch (IOException e) {
            imageFetcher.clear(identifier);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (UnsupportedOperationException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private void interpretParams(ScaleDims scaleDims, Region region,
                                 Integer xParam, Integer yParam, Integer wParam, Integer hParam, Double sParam,
                                 Integer rParam, Jp2Header jp2Header) {

        if (xParam == null && yParam == null && sParam == null) {
            interpretFullRegionParams(scaleDims, wParam, hParam, rParam, jp2Header);
        } else {
            rParam = rParam == null ? 0 : rParam;
            sParam = sParam == null ? 1.0 : sParam;
            final int requestedX = xParam == null ? 0 : xParam;
            final int requestedY = yParam == null ? 0 : yParam;
            final int requestedW = wParam == null ? (int) Math.round(jp2Header.getX1() * sParam) : wParam;
            final int requestedH = hParam == null ? (int) Math.round(jp2Header.getY1() * sParam) : hParam;

            final Region rotatedRegion = new Region(
                    (int) Math.round(requestedX / sParam), (int) Math.round(requestedY / sParam),
                    (int) Math.round(requestedW / sParam), (int) Math.round(requestedH / sParam))
                    .rotatedForRequest(jp2Header, rParam);

            final int scaledRequestedW = rotatedRegion.getW();
            final int scaledRequestedH = rotatedRegion.getH();
            final int requestedRegionX = rotatedRegion.getX();
            final int requestedRegionY = rotatedRegion.getY();

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

    private void interpretFullRegionParams(ScaleDims scaleDims, Integer wParam, Integer hParam, Integer rParam, Jp2Header jp2Header) {
        if (hParam == null && wParam == null) {
            return;
        }
        if (hParam == null) {
            if (rParam == 90 || rParam == 270) {
                setScaleFromHeightParam(scaleDims, wParam, jp2Header);
            } else {
                setScaleFromWidthParam(scaleDims, wParam, jp2Header);
            }
        } else if (wParam == null) {
            if (rParam == 90 || rParam == 270) {
                setScaleFromWidthParam(scaleDims, hParam, jp2Header);
            } else {
                setScaleFromHeightParam(scaleDims, hParam, jp2Header);
            }
        } else {
            if (hParam < wParam) {
                if (rParam == 90 || rParam == 270) {
                    setScaleFromWidthParam(scaleDims, hParam, jp2Header);
                } else {
                    setScaleFromHeightParam(scaleDims, hParam, jp2Header);
                }
            } else {
                if (rParam == 90 || rParam == 270) {
                    setScaleFromHeightParam(scaleDims, wParam, jp2Header);
                } else {
                    setScaleFromWidthParam(scaleDims, wParam, jp2Header);
                }
            }
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
