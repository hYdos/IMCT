package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph;

import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.BaseNode;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.InputNode;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.TextureNode;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class MirrorNode extends BaseNode {
    private InputNode input = InputNode.DEFAULT;

    private boolean mirrorLeft = false;

    @Override
    protected void process() {
        var inputImage = input.getInputData().get();
        var mirrorImage = horizontalFlip(inputImage);

        image = new BufferedImage(inputImage.getWidth() * 2, inputImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        image.getGraphics().drawImage(mirrorLeft ? mirrorImage : inputImage, 0, 0, null);
        image.getGraphics().drawImage(mirrorLeft ? inputImage : mirrorImage, inputImage.getWidth(), 0, null);
    }

    public static BufferedImage horizontalFlip(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // Create an affine transformation for horizontal flip
        AffineTransform flip = AffineTransform.getScaleInstance(-1, 1);
        flip.translate(-width, 0);

        // Create a copy of the original image with the transformation applied
        BufferedImage flippedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = flippedImage.createGraphics();
        g.drawImage(image, flip, null);
        g.dispose();

        return flippedImage;
    }

    public MirrorNode setInput(InputNode input) {
        this.input = input;
        input.addChangeListener(this);
        update();
        return this;
    }

    public MirrorNode setMirrrLeft(boolean mirrorLeft) {
        this.mirrorLeft = mirrorLeft;
        update();
        return this;
    }
}
