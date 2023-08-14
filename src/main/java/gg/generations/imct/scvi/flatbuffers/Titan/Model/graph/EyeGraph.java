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

import java.awt.AlphaComposite;
import java.awt.image.BufferedImage;
import java.nio.file.Path;

public class EyeGraph {
    private final CompositeNode output;
    private ColorNode baseColor1;
    private ColorNode baseColor2;
    private ColorNode baseColor3;
    private ColorNode baseColor4;

    private ColorNode emissionColor1;
    private ColorNode emissionColor2;
    private ColorNode emissionColor3;
    private ColorNode emissionColor4;
    private ColorNode emissionColor5;

    private TextureNode lym = new TextureNode();

    private TextureNode alb = new TextureNode();
    private TextureNode mask = new TextureNode();

    private ChannelSplitterNode lymSplit = new ChannelSplitterNode();

    public EyeGraph(int scale) {
        baseColor1 = new ColorNode().setSize(256, 256);
        baseColor2 = new ColorNode().setSize(256, 256);
        baseColor3 = new ColorNode().setSize(256, 256);
        baseColor4 = new ColorNode().setSize(256, 256);
        emissionColor1 = new ColorNode().setSize(256, 256);
        emissionColor2 = new ColorNode().setSize(256, 256);
        emissionColor3 = new ColorNode().setSize(256, 256);
        emissionColor4 = new ColorNode().setSize(256, 256);
        emissionColor5 = new ColorNode().setSize(256, 256);

        lymSplit.setInput(lym);


        var baseLayer = new ScaleNode().setScale(scale).setInput(alb);

        var base = new LayersNode(
                baseLayer,
                maskColor(lymSplit.getRedChannel(), baseColor1),
                maskColor(lymSplit.getGreenChannel(), baseColor2),
                maskColor(lymSplit.getBlueChannel(), baseColor3),
                maskColor(lymSplit.getAlphaChannel(), baseColor4));

        var emission = new LayersNode(
                baseLayer,
                maskColor(lymSplit.getRedChannel(), emissionColor1),
                maskColor(lymSplit.getRedChannel(), emissionColor2),
                maskColor(lymSplit.getRedChannel(), emissionColor3),
                maskColor(lymSplit.getRedChannel(), emissionColor4),
                maskColor(new GrayScaleNode().setInput(mask), emissionColor5));

        output = new CompositeNode().setComposite(Composites.ADD).setBottom(base).setTop(emission);
    }

    public BufferedImage update(ApiMaterial material, Path modelDir) {
        alb.setImage(modelDir.resolve(modelDir.getFileName().toString() + "_eye_alb.png"));
        lym.setImage(modelDir.resolve(modelDir.getFileName().toString() + "_eye_lym.png"));
        mask.setImage(modelDir.resolve(modelDir.getFileName().toString() + "_eye_msk.png"));

        lymSplit.getRedChannel().getInputData().display("Red");
        lymSplit.getGreenChannel().getInputData().display("Green");
        lymSplit.getBlueChannel().getInputData().display("Blue");
        lymSplit.getAlphaChannel().getInputData().display("Alpha");

        if(material.properties().get("BaseColorLayer1") instanceof Vector4f vec) baseColor1.setColor(vec.x, vec.y, vec.z, vec.w);
        if(material.properties().get("BaseColorLayer2") instanceof Vector4f vec) baseColor2.setColor(vec.x, vec.y, vec.z, vec.w);
        if(material.properties().get("BaseColorLayer3") instanceof Vector4f vec) baseColor3.setColor(vec.x, vec.y, vec.z, vec.w);
        if(material.properties().get("BaseColorLayer4") instanceof Vector4f vec) baseColor4.setColor(vec.x, vec.y, vec.z, vec.w);

        if(material.properties().get("EmissionColorLayer1") instanceof Vector4f vec) emissionColor1.setColor(vec.x, vec.y, vec.z, vec.w);
        if(material.properties().get("EmissionColorLayer2") instanceof Vector4f vec) emissionColor2.setColor(vec.x, vec.y, vec.z, vec.w);
        if(material.properties().get("EmissionColorLayer3") instanceof Vector4f vec) emissionColor3.setColor(vec.x, vec.y, vec.z, vec.w);
        if(material.properties().get("EmissionColorLayer4") instanceof Vector4f vec) emissionColor4.setColor(vec.x, vec.y, vec.z, vec.w);
        if(material.properties().get("EmissionColorLayer5") instanceof Vector4f vec) emissionColor5.setColor(vec.x, vec.y, vec.z, vec.w);

//        output.display("output");

        return output.get();
    }

    public static class LayersNode extends BaseNode {
        private final InputNode[] layers;

        public LayersNode(InputNode... layers) {
            this.layers = layers;
            for (int i = 0; i < layers.length; i++) {
                var layer = layers[i];
                layer.addChangeListener(this);
            }
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

    public static CompositeNode maskColor(InputNode mask, InputNode color) {
        return new CompositeNode().setTop(color).setBottom(mask).setComposite(Composites.MULTIPLY);
    }

    private class GrayScaleNode extends BaseNode {
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
