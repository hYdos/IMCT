package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph;

import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.BaseNode;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ColorNode extends BaseNode {
    private final int width = 256;
    private final int height = 256;
    private int rgb;

    public ColorNode() {
        process();
    }

//    @Override
//    public String toString() {
//        return "(%s, %s, %s)".formatted(red, green, blue);
//    }

    protected void process() {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        try {
            var graphic = image.createGraphics();
            graphic.setColor(new Color(rgb));
            graphic.fillRect(0, 0, width, height);
            graphic.dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ColorNode setColor(int red, int green, int blue) {
        this.rgb = (red << 16) | (green << 8) | blue;
        update();
        return this;
    }

    @Override
    public int getColor(float x, float y) {
        return rgb;
    }

    public ColorNode setColor(float red, float green, float blue) {
        return setColor((int) (rgbTosRGB(red) * 255), (int) (rgbTosRGB(green) * 255), (int) (rgbTosRGB(blue) * 255));
    }

    private float clamp(float value) {
        return Math.max((float) 0, Math.min((float) 1.0, value));
    }

    private float rgbTosRGB(float value) {
        return (float) Math.pow(clamp(value), 1/2.2);
    }

    public ColorNode resetColor() {
        setColor(255, 255, 255);
        return this;
    }
}
