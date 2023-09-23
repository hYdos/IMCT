package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph;

import gg.generations.imct.api.ApiMaterial;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.ChannelSplitterNode;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.TextureNode;
import org.joml.Vector4f;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

public class FIreGraph {
    private final MaskNode baseColor1 = new MaskNode();
    private final MaskNode baseColor2 = new MaskNode();
    private final MaskNode baseColor3 = new MaskNode();
    private final MaskNode baseColor4 = new MaskNode();

    private final TextureNode lym = new TextureNode();

    private final ChannelSplitterNode lymSplit = new ChannelSplitterNode();
    private final EyeGraph.LayersNode output;

    private TextureNode alb = new TextureNode();

    public FIreGraph(int scale) {

        var lymScale = new ScaleNode().setScale(scale).setInput(lym);


        lymSplit.setInput(lymScale);

        var baseLayer = new ScaleNode().setScale(scale).setInput(alb);


        output = new EyeGraph.LayersNode(
                baseLayer,
                baseColor1.setMask(lymSplit.getRedChannel()),
                baseColor2.setMask(lymSplit.getGreenChannel()),
                baseColor3.setMask(lymSplit.getBlueChannel()),
                baseColor4.setMask(lymSplit.getAlphaChannel())
        );
    }

    public BufferedImage update(ApiMaterial material, Path modelDir) {
        alb.setImage(modelDir.resolve(modelDir.getFileName().toString() + "_fire_alb.png"));
        lym.setImage(modelDir.resolve(modelDir.getFileName().toString() + "_fire_lym.png"));

        if(material.properties().get("BaseColorLayer1") instanceof Vector4f vec) baseColor1.setColor(vec.x, vec.y, vec.z, vec.w);
        else baseColor1.resetColor();
        if(material.properties().get("BaseColorLayer2") instanceof Vector4f vec) baseColor2.setColor(vec.x, vec.y, vec.z, vec.w);
        else baseColor2.resetColor();
        if(material.properties().get("BaseColorLayer3") instanceof Vector4f vec) baseColor3.setColor(vec.x, vec.y, vec.z, vec.w);
        else baseColor3.resetColor();
        if(material.properties().get("BaseColorLayer4") instanceof Vector4f vec) baseColor4.setColor(vec.x, vec.y, vec.z, vec.w);
        else baseColor4.resetColor();

        return output.get();
    }
}
