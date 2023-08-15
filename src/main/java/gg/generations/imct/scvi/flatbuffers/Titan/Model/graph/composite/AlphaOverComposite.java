package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.composite;

import java.awt.*;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public class AlphaOverComposite implements Composite {
    @Override
    public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
        return new AlphaOverCompositeContext();
    }
}

class AlphaOverCompositeContext implements CompositeContext {
    @Override
    public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
        int width = Math.min(src.getWidth(), dstIn.getWidth());
        int height = Math.min(src.getHeight(), dstIn.getHeight());

        int[] srcPixel = new int[4];
        int[] dstPixel = new int[4];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                src.getPixel(x, y, srcPixel);
                dstIn.getPixel(x, y, dstPixel);

                // Combine the colors while considering alpha
                int alphaSrc = srcPixel[3];
                int alphaDst = dstPixel[3];

                int resultRed = (srcPixel[0] * alphaSrc + dstPixel[0] * (255 - alphaSrc)) / 255;
                int resultGreen = (srcPixel[1] * alphaSrc + dstPixel[1] * (255 - alphaSrc)) / 255;
                int resultBlue = (srcPixel[2] * alphaSrc + dstPixel[2] * (255 - alphaSrc)) / 255;
                int resultAlpha = alphaSrc + (alphaDst * (255 - alphaSrc)) / 255;

                dstPixel[0] = resultRed;
                dstPixel[1] = resultGreen;
                dstPixel[2] = resultBlue;
                dstPixel[3] = resultAlpha;

                dstOut.setPixel(x, y, dstPixel);
            }
        }
    }

    @Override
    public void dispose() {
        // Nothing to dispose
    }
}