package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph;

import gg.generations.imct.api.ApiMaterial;
import gg.generations.imct.api.Model;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.composite.Composites;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.BaseNode;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.ChangeListener;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.ChannelSplitterNode;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.CompositeNode;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.InputNode;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.TextureNode;
import org.joml.Vector4f;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Arrays;

public class EyeGraph {
    private final CompositeNode output;
    private final LayersNode base;
    private final LayersNode emission;
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
    private TextureNode mask = new TextureNode();

    private ChannelSplitterNode lymSplit = new ChannelSplitterNode();

    public EyeGraph(int scale) {
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

        lymScale.display("derp");

        lymSplit.setInput(lymScale);


        var baseLayer = new ScaleNode().setScale(scale).setInput(alb);

        base = new LayersNode(
                baseLayer,
                baseColor1.setMask(lymSplit.getRedChannel()),
                baseColor2.setMask(lymSplit.getGreenChannel()),
                baseColor3.setMask(lymSplit.getBlueChannel()),
                baseColor4.setMask(lymSplit.getAlphaChannel()),
                emissionColor5.setMask(new GrayScaleNode().setInput(mask))
        );

        emission = new LayersNode(
                baseLayer,
                emissionColor1.setMask(lymSplit.getRedChannel()),
                emissionColor2.setMask(lymSplit.getGreenChannel()),
                emissionColor3.setMask(lymSplit.getBlueChannel()),
                emissionColor4.setMask(lymSplit.getAlphaChannel())
        );

        output = new CompositeNode().setComposite(Composites.SCREEN).setBottom(base).setTop(emission);
    }

    public BufferedImage update(ApiMaterial material, Path modelDir) {
        alb.setImage(modelDir.resolve(modelDir.getFileName().toString() + "_eye_alb.png"));
        lym.setImage(modelDir.resolve(modelDir.getFileName().toString() + "_eye_lym.png"));
        lym.display("LYm");
        mask.setImage(modelDir.resolve(modelDir.getFileName().toString() + "_eye_msk.png"));

        lymSplit.getRedChannel().getInputData().display("RedChannel");
        lymSplit.getGreenChannel().getInputData().display("GreenChannel");
        lymSplit.getBlueChannel().getInputData().display("BlueChannel");
        lymSplit.getAlphaChannel().getInputData().display("AlphaChannel");

        if(material.properties().get("BaseColorLayer1") instanceof Vector4f vec) baseColor1.setColor(vec.x, vec.y, vec.z, vec.w).getInputData();
        if(material.properties().get("BaseColorLayer2") instanceof Vector4f vec) baseColor2.setColor(vec.x, vec.y, vec.z, vec.w).getInputData();
        if(material.properties().get("BaseColorLayer3") instanceof Vector4f vec) baseColor3.setColor(vec.x, vec.y, vec.z, vec.w).getInputData();
        if(material.properties().get("BaseColorLayer4") instanceof Vector4f vec) baseColor4.setColor(vec.x, vec.y, vec.z, vec.w).getInputData();

        if(material.properties().get("EmissionColorLayer1") instanceof Vector4f vec) emissionColor1.setColor(vec.x, vec.y, vec.z, vec.w).getInputData();
        if(material.properties().get("EmissionColorLayer2") instanceof Vector4f vec) emissionColor2.setColor(vec.x, vec.y, vec.z, vec.w).getInputData();
        if(material.properties().get("EmissionColorLayer3") instanceof Vector4f vec) emissionColor3.setColor(vec.x, vec.y, vec.z, vec.w).getInputData();
        if(material.properties().get("EmissionColorLayer4") instanceof Vector4f vec) emissionColor4.setColor(vec.x, vec.y, vec.z, vec.w).getInputData();
        if(material.properties().get("EmissionColorLayer5") instanceof Vector4f vec) emissionColor5.setColor(vec.x, vec.y, vec.z, vec.w).getInputData();

//        base.display("Basic");
//        emission.display("emission");
//        mask.display("Mask");
        return output.get();
    }

    public static class LayersNode extends BaseNode {
        private final InputNode[] layers;

        public LayersNode(InputNode... layers) {
            this.layers = layers;
            Arrays.stream(layers).forEach(layer -> layer.addChangeListener(this));
        }

        @Override
        protected void process() {
            var base = layers[0].getInputData().get();

            image = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_ARGB);

            var graphics = image.createGraphics();
            graphics.drawImage(base, 0, 0, null);

            graphics.setComposite(AlphaComposite.SrcOver);
            for (int i = 1; i < layers.length; i++) {
                var layer = layers[i];
                graphics.drawImage(layer.getInputData().get(), 0, 0, null);
            }
            graphics.dispose();
        }
    }

    public static MaskNode maskColor(InputNode mask, Color color) {
        return new MaskNode().setMask(mask).setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    private static class GrayScaleNode extends BaseNode {
        private InputNode input = DEFAULT;

        @Override
        protected void process() {
            image = Nodes.grayScaleToColor(input.getInputData().get());
        }

        public GrayScaleNode setInput(InputNode input) {
            this.input = input;
            input.addChangeListener(this);
            update();
            return this;
        }
    }
}
