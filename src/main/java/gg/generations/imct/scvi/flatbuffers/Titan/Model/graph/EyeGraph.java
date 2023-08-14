package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph;

import gg.generations.imct.intermediate.Model;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.composite.Composites;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Path;

public class EyeGraph {
    private final CompositeNode output;
    private ColorNode baseColor1 = new ColorNode().setSize(256, 256);
    private ColorNode baseColor2 = new ColorNode().setSize(256, 256);
    private ColorNode baseColor3 = new ColorNode().setSize(256, 256);
    private ColorNode baseColor4 = new ColorNode().setSize(256, 256);

    private ColorNode emissionColor1 = new ColorNode().setSize(256, 256);
    private ColorNode emissionColor2 = new ColorNode().setSize(256, 256);
    private ColorNode emissionColor3 = new ColorNode().setSize(256, 256);
    private ColorNode emissionColor4 = new ColorNode().setSize(256, 256);
    private ColorNode emissionColor5 = new ColorNode().setSize(256, 256);

    private TextureNode lym = new TextureNode();

    private TextureNode alb = new TextureNode();
    private TextureNode mask = new TextureNode();

    public EyeGraph(int scale) {
        var lymSplit = new ChannelSplitterNode().setInput(lym);
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

    public BufferedImage update(Model.Material material, Path modelDir) {
        alb.setImage(modelDir.resolve(modelDir.getFileName().toString() + "_eye_alb.png"));
        lym.setImage(modelDir.resolve(modelDir.getFileName().toString() + "_eye_lym.png"));
        mask.setImage(modelDir.resolve(modelDir.getFileName().toString() + "_eye_msk.png"));

        baseColor1.setColor(material.colors().get("BaseColorLayer1"));
        baseColor2.setColor(material.colors().get("BaseColorLayer2"));
        baseColor3.setColor(material.colors().get("BaseColorLayer3"));
        baseColor4.setColor(material.colors().get("BaseColorLayer4"));

        emissionColor1.setColor(material.colors().get("EmissionColorLayer1"));
        emissionColor2.setColor(material.colors().get("EmissionColorLayer2"));
        emissionColor3.setColor(material.colors().get("EmissionColorLayer3"));
        emissionColor4.setColor(material.colors().get("EmissionColorLayer4"));
        emissionColor5.setColor(material.colors().get("EmissionColorLayer5"));

        output.display("output");

        return null;
    }

    public static class LayersNode extends BaseNode {
        private final InputNode[] layers;

        public LayersNode(InputNode... layers) {
            this.layers = layers;
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
            if(input instanceof ChangeListener listener) input.addChangeListener(listener);
            update();
            return this;
        }
    }
}
