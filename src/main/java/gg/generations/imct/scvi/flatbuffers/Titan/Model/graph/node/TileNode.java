package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node;

import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.MirrorNode;

import java.awt.image.BufferedImage;

public class TileNode extends BaseNode {
    int width = 1, height = 1;

    private InputNode inputNode = InputNode.DEFAULT;


    @Override
    protected void process() {
        var inputImage = inputNode.getInputData().get();
        var imageWidth = inputImage.getWidth();
        var imageHeight = inputImage.getHeight();

        image = new BufferedImage(imageWidth * width, imageHeight * height, BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.getGraphics().drawImage(inputImage, x * imageWidth, y * imageHeight, null);
            }
        }
    }

    public TileNode setInput(InputNode input) {
        this.inputNode = input;
        input.addChangeListener(this);
        update();
        return this;
    }

    public TileNode setTiling(int width, int height) {
        this.width = width;
        this.height = height;
        inputNode.addChangeListener(this);
        update();
        return this;
    }
}
