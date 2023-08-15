package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.composite;

import java.awt.*;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public class MaskColoringComposite implements Composite {
    @Override
    public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
        return new MaskColoringCompositeContext();
    }
}

class MaskColoringCompositeContext implements CompositeContext {
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

                int alphaSrc = srcPixel[3];
                int alphaMask = dstPixel[3];

                // Combine colors based on the mask's alpha
                int resultRed = (srcPixel[0] * alphaMask) / 255;
                int resultGreen = (srcPixel[1] * alphaMask) / 255;
                int resultBlue = (srcPixel[2] * alphaMask) / 255;
                int resultAlpha = (alphaSrc * alphaMask) / 255;

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