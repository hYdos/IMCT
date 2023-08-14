package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph;

import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.BaseNode;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.ChangeListener;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.InputNode;

public class ScaleNode extends BaseNode {
    private int scale = 1;
    private InputNode input = InputNode.DEFAULT;
    @Override
    protected void process() {
        image = EyeTextureGenerator.resizeImage(input.getInputData().get(), scale, scale);
    }

    public ScaleNode setInput(InputNode input) {
        this.input = input;
        input.addChangeListener(this);
        update();
        return this;
    }

    public ScaleNode setScale(int scale) {
        this.scale = scale;
        update();
        return this;
    }
}
