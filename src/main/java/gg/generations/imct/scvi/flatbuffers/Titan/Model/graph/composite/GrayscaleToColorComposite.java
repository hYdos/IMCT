package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.composite;

import java.awt.*;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public class GrayscaleToColorComposite implements Composite {

    @Override
    public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
        return new GrayscaleToColorContext();
    }

    private class GrayscaleToColorContext implements CompositeContext {

        @Override
        public void dispose() {
            // Cleanup if needed
        }

        @Override
        public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
            int width = Math.min(src.getWidth(), dstOut.getWidth());
            int height = Math.min(src.getHeight(), dstOut.getHeight());

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = src.getSample(x, y, 0); // Grayscale pixel value
                    int newPixel = (pixel & 0xFF) << 24 | 255 << 16 | 255 << 8 | 255; // White color

                    int[] components = new int[4];
                    components[0] = newPixel;
                    dstOut.setPixel(x, y, components);
                }
            }
        }
    }
}