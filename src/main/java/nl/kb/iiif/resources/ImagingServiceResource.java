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
        } else if (sParam != null) {
            final int requestedX = xParam == null ? 0 : xParam;
            final int requestedY = yParam == null ? 0 : yParam;
            final int requestedW = wParam == null ? (int) Math.round(jp2Header.getX1() * sParam) : wParam;
            final int requestedH = hParam == null ? (int) Math.round(jp2Header.getY1() * sParam) : hParam;
            final int derivedRegionW = Math.min((int) Math.round(requestedW / sParam), jp2Header.getX1());
            final int derivedRegionH = Math.min((int) Math.round(requestedH / sParam), jp2Header.getY1());
            final int requestedRegionX = (int) Math.round(requestedX / sParam);
            final int requestedRegionY = (int) Math.round(requestedY / sParam);
            final int derivedRegionX = requestedRegionX + derivedRegionW > jp2Header.getX1() ? 0 : requestedRegionX;
            final int derivedRegionY = requestedRegionY + derivedRegionH > jp2Header.getY1() ? 0 : requestedRegionY;

            scaleDims.setW((int) Math.round(derivedRegionW * sParam));
            scaleDims.setH((int) Math.round(derivedRegionH * sParam));
            region.setW(derivedRegionW);
            region.setH(derivedRegionH);
            region.setX(derivedRegionX);
            region.setY(derivedRegionY);
        } else {
            throw new UnsupportedOperationException();
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


    /*
  "url": "https://imageviewer.kb.nl/ImagingService/imagingService?id=ddd%3A010691737%3Ampeg21%3Ap001%3Aimage&w=134",
  "url": "https://imageviewer.kb.nl/ImagingService/imagingService?id=ddd%3A010691737%3Ampeg21%3Ap001%3Aimage&s=0.4",
  "url": "https://imageviewer.kb.nl/ImagingService/imagingService?r=0&s=0.02732919254658385&id=ddd%3A010691737%3Ampeg21%3Ap001%3Aimage",
  "url": "https://imageviewer.kb.nl/ImagingService/imagingService?id=ddd%3A010691737%3Ampeg21%3Ap001%3Aimage&r=0&s=0.03603192702394527&x=0&y=0&w=1416&h=237",
  "url": "https://imageviewer.kb.nl/ImagingService/imagingService?id=ddd%3A010691737%3Ampeg21%3Ap001%3Aimage&s=0.4",
  "url": "https://imageviewer.kb.nl/ImagingService/imagingService?id=ddd%3A010691737%3Ampeg21%3Ap001%3Aimage&r=0&s=0.03603192702394527&x=0&y=0&w=1416&h=237",
  "url": "https://imageviewer.kb.nl/ImagingService/imagingService?id=ddd%3A010691737%3Ampeg21%3Ap001%3Aimage&r=0&s=0.0396351197263398&x=0&y=11&w=1416&h=237",
  "url": "https://imageviewer.kb.nl/ImagingService/imagingService?id=ddd%3A010691737%3Ampeg21%3Ap001%3Aimage&r=0&s=0.04795849486887116&x=0&y=39&w=1416&h=237",
  "url": "https://imageviewer.kb.nl/ImagingService/imagingService?id=ddd%3A010691737%3Ampeg21%3Ap001%3Aimage&r=0&s=0.2666452659504655&x=0&y=758&w=1416&h=237",
  "url": "https://imageviewer.kb.nl/ImagingService/imagingService?id=ddd%3A010691737%3Ampeg21%3Ap001%3Aimage&s=0.4&r=90",
  "url": "https://imageviewer.kb.nl/ImagingService/imagingService?r=90&s=0.020068415051311288&id=ddd%3A010691737%3Ampeg21%3Ap001%3Aimage",
  "url": "https://imageviewer.kb.nl/ImagingService/imagingService?id=ddd%3A010691737%3Ampeg21%3Ap001%3Aimage&r=90&s=0.2666452659504655&x=0&y=758&w=1416&h=237",
  "url": "https://imageviewer.kb.nl/ImagingService/imagingService?id=ddd%3A010691737%3Ampeg21%3Ap001%3Aimage&r=90&s=0.2666452659504655&x=0&y=758&w=1416&h=237",
  "url": "https://imageviewer.kb.nl/ImagingService/imagingService?id=ddd%3A010691737%3Ampeg21%3Ap001%3Aimage&s=0.4&r=90",
  "url": "https://imageviewer.kb.nl/ImagingService/imagingService?id=ddd%3A010691737%3Ampeg21%3Ap001%3Aimage&r=90&s=0.2666452659504655&x=0&y=758&w=1416&h=237",
  "url": "https://imageviewer.kb.nl/ImagingService/imagingService?id=ddd%3A010691737%3Ampeg21%3Ap001%3Aimage&r=90&s=0.2666452659504655&x=247&y=693&w=1416&h=237",
  "url": "https://imageviewer.kb.nl/ImagingService/imagingService?id=ddd%3A010691737%3Ampeg21%3Ap001%3Aimage&s=0.4&r=0",
  "url": "https://imageviewer.kb.nl/ImagingService/imagingService?r=0&s=0.02732919254658385&id=ddd%3A010691737%3Ampeg21%3Ap001%3Aimage",
  "url": "https://imageviewer.kb.nl/ImagingService/imagingService?id=ddd%3A010691737%3Ampeg21%3Ap001%3Aimage&r=0&s=0.2666452659504655&x=0&y=693&w=1416&h=237",
  "url": "https://imageviewer.kb.nl/ImagingService/imagingService?id=ddd%3A010691737%3Ampeg21%3Ap001%3Aimage&r=0&s=0.2666452659504655&x=0&y=693&w=1416&h=237",
  "url": "https://imageviewer.kb.nl/ImagingService/imagingService?id=ddd%3A010691737%3Ampeg21%3Ap001%3Aimage&s=0.4&r=0",
  "url": "https://imageviewer.kb.nl/ImagingService/imagingService?id=ddd%3A010691737%3Ampeg21%3Ap001%3Aimage&r=0&s=0.2666452659504655&x=0&y=693&w=1416&h=237",
                      */
}
