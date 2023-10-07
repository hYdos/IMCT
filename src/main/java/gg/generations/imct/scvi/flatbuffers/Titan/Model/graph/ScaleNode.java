package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph;

import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.BaseNode;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.ChangeListener;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.InputNode;

public class ScaleNode extends BaseNode {
    private int width, height = 1;
    private InputNode input = InputNode.DEFAULT;
    @Override
    protected void process() {
        image = input.getInputData().get();

        if(image.getWidth() == width && image.getHeight() == height) return;
        image = EyeTextureGenerator.resizeImage(input.getInputData().get(), width, height);
    }

    public ScaleNode setInput(InputNode input) {
        this.input = input;
        input.addChangeListener(this);
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
