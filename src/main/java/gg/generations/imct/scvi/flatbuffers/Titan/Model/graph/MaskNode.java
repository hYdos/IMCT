package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph;

import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.BaseNode;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.InputNode;

import java.awt.image.BufferedImage;

public class MaskNode extends BaseNode {
    private InputNode mask = DEFAULT;
    public int red = 255, green = 255, blue = 255;
    private double alphaIntensity = 1.0;

    public MaskNode setColor(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        update();
        return this;
    }

    public MaskNode setIntensity(double alphaIntensity) {
        this.alphaIntensity = alphaIntensity;
        update();
        return this;
    }

    public MaskNode setColor(float red, float green, float blue) {
        return setColor((int) (rgbTosRGB(red) * 255), (int) (rgbTosRGB(green) * 255), (int) (rgbTosRGB(blue) * 255));
    }

    private float rgbTosRGB(float value) {
        return (float) Math.pow(value, 1/2.2);
    }

    public MaskNode resetColor() {
        this.red = 255;
        this.blue = 255;
        this.green = 255;
        return this;
    }

    public MaskNode setMask(InputNode mask) {
        this.mask = mask;
        mask.addChangeListener(this);
        update();
        return this;
    }

    @Override
    protected void process() {
        var original = mask.getInputData().get();
        image = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < original.getWidth(); x++) {
            for (int y = 0; y < original.getWidth(); y++) {
                var pixel = original.getRGB(x, y);
                var newPixel = ((alphaPercentage(pixel >> 24, alphaIntensity) & 0xff) << 24) | (red << 16) | (green << 8) | blue;
                image.setRGB(x,y, newPixel);
            }
        }
    }

    public static int alphaPercentage(int alpha, double percentage) {
        percentage = Math.max(0.0, Math.min(1.0, percentage));
        return (int) Math.round(alpha * percentage);
    }

    public void resetIntensity() {
        alphaIntensity = 0.0;
    }
}
