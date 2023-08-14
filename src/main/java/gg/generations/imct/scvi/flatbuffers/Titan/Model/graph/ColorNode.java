package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph;

import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.BaseNode;
import org.joml.Vector4i;

import java.awt.*;
import java.awt.image.BufferedImage;

class ColorNode extends BaseNode {
    private int width = 1, height = 1;
    private int red = 255, green = 255, blue = 255, alpha = 255;

    public ColorNode() {
        process();
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

    public ColorNode setColor(Vector4i color) {
        this.red = color.x;
        this.green = color.y;
        this.blue = color.z;
        this.alpha = color.w;
        update();

        return this;
    }
}
