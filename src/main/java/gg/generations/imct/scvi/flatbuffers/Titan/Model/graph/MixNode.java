package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph;

import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.CompositeNode1;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.InputNode;

import java.awt.image.BufferedImage;

public class MixNode extends CompositeNode {
    public MixNode(InputNode top, InputNode bottom, FacFunction facFunction) {
        super(BlendFunctions.NORMAL, top, bottom, facFunction);
    }
}

class ScreenNode extends CompositeNode {
    public ScreenNode(InputNode top, InputNode bottom, FacFunction facFunction) {
        super(BlendFunctions.SCREEN, top, bottom, facFunction);
    }
}


interface BlendFunction {
    int blend(int a, int b);
}

class BlendFunctions {
    public static BlendFunction NORMAL = (a, b) -> b;
    public static BlendFunction SCREEN = (a, b) -> a + b - (a * b);
}
