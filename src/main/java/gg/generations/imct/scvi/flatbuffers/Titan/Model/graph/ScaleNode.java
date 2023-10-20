package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph;

import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.BaseNode;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.ChannelSplitterNode;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.InputNode;

public class ScaleNode extends BaseNode {
    private int width = 1, height = 1;
    private InputNode input = InputNode.DEFAULT;
    private InputNode sizeInput = InputNode.DEFAULT;

    @Override
    protected void process() {
        image = input.getInputData().get();

        int width = sizeInput.getInputData().get().getWidth();
        int height = sizeInput.getInputData().get().getHeight();

        if(image.getWidth() == width && image.getHeight() == height) return;
        image = EyeTextureGenerator.resizeImage(input.getInputData().get(), width, height);
    }

    public ScaleNode setInput(InputNode input) {
        this.input = input;
        input.addChangeListener(this);
        update();
        return this;
    }

    public ScaleNode setScale(InputNode image) {
        this.sizeInput = image;
        image.addChangeListener(this);
        update();
        return this;
    }

    public ScaleNode setScale(int width, int height) {
        this.width = width;
        this.height = height;
        update();
        return this;
    }

    public ScaleNode setScale(int scale) {
        return setScale(scale, scale);
    }
}
