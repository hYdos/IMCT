package gg.generations.imct.scvi.flatbuffers.Titan.Model;

import java.awt.*;
import java.awt.image.*;

public class ScreenComposite implements Composite {
    @Override
    public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
        return new ScreenCompositeContext();
    }

    private static class ScreenCompositeContext implements CompositeContext {
        @Override
        public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
            int width = Math.min(src.getWidth(), dstIn.getWidth());
            int height = Math.min(src.getHeight(), dstIn.getHeight());

            int[] srcPixels = new int[width * height];
            int[] dstPixels = new int[width * height];
            int[] resultPixels = new int[width * height];

            src.getDataElements(0, 0, width, height, srcPixels);
            dstIn.getDataElements(0, 0, width, height, dstPixels);

            for (int i = 0; i < srcPixels.length; i++) {
                int srcColor = srcPixels[i];
                int dstColor = dstPixels[i];

                int srcAlpha = (srcColor >> 24) & 0xFF;
                int dstAlpha = (dstColor >> 24) & 0xFF;

                int srcRed = (srcColor >> 16) & 0xFF;
                int srcGreen = (srcColor >> 8) & 0xFF;
                int srcBlue = srcColor & 0xFF;

                int dstRed = (dstColor >> 16) & 0xFF;
                int dstGreen = (dstColor >> 8) & 0xFF;
                int dstBlue = dstColor & 0xFF;

                int resultAlpha = Math.min(255, srcAlpha + dstAlpha - ((srcAlpha * dstAlpha) >> 8));
                int resultRed = Math.min(255, srcRed + dstRed - ((srcRed * dstRed) >> 8));
                int resultGreen = Math.min(255, srcGreen + dstGreen - ((srcGreen * dstGreen) >> 8));
                int resultBlue = Math.min(255, srcBlue + dstBlue - ((srcBlue * dstBlue) >> 8));

                resultPixels[i] = (resultAlpha << 24) | (resultRed << 16) | (resultGreen << 8) | resultBlue;
            }

            dstOut.setDataElements(0, 0, width, height, resultPixels);
        }

        @Override
        public void dispose() {
        }
    }

    public static Composite getInstance() {
        return new ScreenComposite();
    }
}