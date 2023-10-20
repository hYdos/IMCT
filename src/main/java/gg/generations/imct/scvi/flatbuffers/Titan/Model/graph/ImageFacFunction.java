package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph;

import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.InputNode;

public class ImageFacFunction implements FacFunction {
    private final InputNode source;

    public ImageFacFunction(InputNode source) {
        this.source = source;
    }

    @Override
    public float fac(float x, float y) {
        return source.getInputData().getColor(x, y) / 255f;
    }
}
