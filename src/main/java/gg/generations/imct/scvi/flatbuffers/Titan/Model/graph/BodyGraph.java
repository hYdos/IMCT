package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph;

import gg.generations.imct.api.ApiMaterial;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.composite.Composites;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.ChannelSplitterNode;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.CompositeNode;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.TextureNode;
import org.joml.Vector4f;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

public class BodyGraph {
    private final CompositeNode output;
    private final EyeGraph.LayersNode base;
    private final EyeGraph.LayersNode emission;
    private MaskNode baseColor1;
    private MaskNode baseColor2;
    private MaskNode baseColor3;
    private MaskNode baseColor4;

    private MaskNode emissionColor1;
    private MaskNode emissionColor2;
    private MaskNode emissionColor3;
    private MaskNode emissionColor4;
    private MaskNode emissionColor5;

    private TextureNode lym = new TextureNode();

    private TextureNode alb = new TextureNode();
    private TextureNode ao = new TextureNode();

    private ChannelSplitterNode lymSplit = new ChannelSplitterNode();

    public BodyGraph(int scale) {
        baseColor1 = new MaskNode();
        baseColor2 = new MaskNode();
        baseColor3 = new MaskNode();
        baseColor4 = new MaskNode();
        emissionColor1 = new MaskNode();
        emissionColor2 = new MaskNode();
        emissionColor3 = new MaskNode();
        emissionColor4 = new MaskNode();
        emissionColor5 = new MaskNode();

        var lymScale = new ScaleNode().setScale(scale).setInput(lym);

        lymSplit.setInput(lymScale);


        var baseLayer = new ScaleNode().setScale(scale).setInput(alb);

        base = new EyeGraph.LayersNode(
//                baseLayer,
                baseColor1.setMask(lymSplit.getRedChannel()),
                baseColor2.setMask(lymSplit.getGreenChannel()),
                baseColor3.setMask(lymSplit.getBlueChannel()),
                baseColor4.setMask(lymSplit.getAlphaChannel())
        );

        emission = new EyeGraph.LayersNode(
//                baseLayer,
                emissionColor1.setMask(lymSplit.getRedChannel()),
                emissionColor2.setMask(lymSplit.getGreenChannel()),
                emissionColor3.setMask(lymSplit.getBlueChannel()),
                emissionColor4.setMask(lymSplit.getAlphaChannel())
        );

        output = new CompositeNode().setComposite(Composites.SCREEN).setBottom(base).setTop(emission);
    }

    public BufferedImage update(ApiMaterial material) {
        alb.setImage(Path.of(material.getTexture("BaseColorMap").filePath()));
        lym.setImage(Path.of(material.getTexture("LayerMaskMap").filePath()));
        lymSplit.getRedChannel().getInputData();
        lymSplit.getGreenChannel().getInputData();
        lymSplit.getBlueChannel().getInputData();
        lymSplit.getAlphaChannel().getInputData();

        if (material.properties().get("BaseColorLayer1") instanceof Vector4f vec)
            baseColor1.setColor(vec.x, vec.y, vec.z).getInputData();
        if (material.properties().get("BaseColorLayer2") instanceof Vector4f vec)
            baseColor2.setColor(vec.x, vec.y, vec.z).getInputData();
        if (material.properties().get("BaseColorLayer3") instanceof Vector4f vec)
            baseColor3.setColor(vec.x, vec.y, vec.z).getInputData();
        if (material.properties().get("BaseColorLayer4") instanceof Vector4f vec)
            baseColor4.setColor(vec.x, vec.y, vec.z).getInputData();

        if (material.properties().get("EmissionColorLayer1") instanceof Vector4f vec)
            emissionColor1.setColor(vec.x, vec.y, vec.z).getInputData();
        if (material.properties().get("EmissionColorLayer2") instanceof Vector4f vec)
            emissionColor2.setColor(vec.x, vec.y, vec.z).getInputData();
        if (material.properties().get("EmissionColorLayer3") instanceof Vector4f vec)
            emissionColor3.setColor(vec.x, vec.y, vec.z).getInputData();
        if (material.properties().get("EmissionColorLayer4") instanceof Vector4f vec)
            emissionColor4.setColor(vec.x, vec.y, vec.z).getInputData();
        if (material.properties().get("EmissionColorLayer5") instanceof Vector4f vec)
            emissionColor5.setColor(vec.x, vec.y, vec.z).getInputData();

        return output.get();
    }
}