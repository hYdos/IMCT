package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph;

import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.BaseNode;
import org.joml.Vector4i;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ColorNode extends BaseNode {
    private int width = 1, height = 1;
    private int red = 255, green = 255, blue = 255, alpha = 255;

    public ColorNode() {
        process();
    }

    @Override
    public String toString() {
        return "(%s, %s, %s, %s)".formatted(red, green, blue, alpha);
    }

    protected void process() {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        var graphic = image.createGraphics();
        graphic.setColor(new Color(red, green, blue, alpha));
        graphic.fillRect(0, 0, width, height);
        graphic.dispose();
    }

    public ColorNode setSize(int width, int height) {
        this.width = width;
        this.height = height;
        update();
        return this;
    }

    public ColorNode setColor(float red, float green, float blue, float alpha) {
        this.red = (int) (red * 255);
        this.green = (int) (green * 255);
        this.blue = (int) (blue * 255);
        this.alpha = (int) (alpha * 255);
        update();

        return this;
    }
}
