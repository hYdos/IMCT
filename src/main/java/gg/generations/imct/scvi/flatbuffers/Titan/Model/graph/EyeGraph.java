package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph;

import gg.generations.imct.api.ApiMaterial;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.BaseNode;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.ChannelSplitterNode;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.InputNode;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.TextureNode;
import org.joml.Vector4f;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class EyeGraph {
    private CompositeNode output;
    private CompositeNode base;
    private InputNode maskGray;

    private MixNode baseMix1;
    private MixNode baseMix2;
    private MixNode baseMix3;
    private MixNode baseMix4;
    private MixNode emMix1;
    private MixNode emMix2;
    private MixNode emMix3;
    private MixNode emMix4;

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
    private ConstantFacFunction emissionIntensity1 = new ConstantFacFunction();
    private ConstantFacFunction emissionIntensity2 = new ConstantFacFunction();
    private ConstantFacFunction emissionIntensity3 = new ConstantFacFunction();
    private ConstantFacFunction emissionIntensity4 = new ConstantFacFunction();

    public EyeGraph() {
        lymSplit.setInput(lym);

        var lymRed = new ImageFacFunction(lymSplit.getRedChannel());
        var lymGreen = new ImageFacFunction(lymSplit.getGreenChannel());
        var lymBlue = new ImageFacFunction(lymSplit.getBlueChannel());
        var lymAlpha = new ImageFacFunction(lymSplit.getAlphaChannel());

        baseColor1 = new ColorNode();
        baseColor2 = new ColorNode();
        baseColor3 = new ColorNode();
        baseColor4 = new ColorNode();
        emissionColor1 = new ColorNode();
        emissionColor2 = new ColorNode();
        emissionColor3 = new ColorNode();
        emissionColor4 = new ColorNode();
        emissionColor5 = new ColorNode();

        var scaleAlb = new ScaleNode().setInput(alb).setScale(lymSplit);


        baseMix1 = new MixNode(scaleAlb, baseColor1, lymRed);
        baseMix2 = new MixNode(baseMix1, baseColor2, lymGreen);
        baseMix3 = new MixNode(baseMix2, baseColor3, lymBlue);
        baseMix4 = new MixNode(baseMix3, baseColor4, lymAlpha);


        emMix1 = new MixNode(scaleAlb, baseColor1, emissionIntensity1);
        emMix2 = new MixNode(emMix1, baseColor2, emissionIntensity2);
        emMix3 = new MixNode(emMix2, baseColor3, emissionIntensity3);
        emMix4 = new MixNode(emMix3, baseColor4, emissionIntensity4);
        base = new MixNode(baseMix4, emMix4, new ConstantFacFunction());

        maskGray = new ChannelSplitterNode().setInput(mask).getRedChannel();

        output = new MixNode(base, emissionColor5, new ImageFacFunction(maskGray));
    }

    public BufferedImage update(ApiMaterial material, Path modelDir) {
        return update(material, modelDir, null);
    }

    public BufferedImage update(ApiMaterial material, Path modelDir, Path targetPath) {
        alb.setImage(Path.of(material.getTexture("BaseColorMap").filePath()));
        if(material.getTexture("LayerMaskMap") == null) return alb.get();
        lym.setImage(Path.of(material.getTexture("LayerMaskMap").filePath()));


        var path = modelDir.resolve(material.getTexture("BaseColorMap").filePath().replace("_alb.png", "").toString() + "_msk.png");

        if (Files.notExists(path)) {
            if (material.getTexture("HighLightMaskMap") != null) {
                path = Path.of(material.getTexture("HighLightMaskMap").filePath());
            } else {
                path = null;
            }
        }

        mask.setImage(path);

        if(material.properties().get("BaseColorLayer1") instanceof Vector4f vec) baseColor1.setColor(vec.x, vec.y, vec.z);
        else baseColor1.resetColor();
        if(material.properties().get("BaseColorLayer2") instanceof Vector4f vec) baseColor2.setColor(vec.x, vec.y, vec.z);
        else baseColor2.resetColor();
        if(material.properties().get("BaseColorLayer3") instanceof Vector4f vec) baseColor3.setColor(vec.x, vec.y, vec.z);
        else baseColor3.resetColor();
        if(material.properties().get("BaseColorLayer4") instanceof Vector4f vec) baseColor4.setColor(vec.x, vec.y, vec.z);
        else baseColor4.resetColor();

        if(material.properties().get("EmissionColorLayer1") instanceof Vector4f vec) emissionColor1.setColor(vec.x, vec.y, vec.z);
        else emissionColor1.resetColor();
        if(material.properties().get("EmissionColorLayer2") instanceof Vector4f vec) emissionColor2.setColor(vec.x, vec.y, vec.z);
        else emissionColor2.resetColor();
        if(material.properties().get("EmissionColorLayer3") instanceof Vector4f vec) emissionColor3.setColor(vec.x, vec.y, vec.z);
        else emissionColor3.resetColor();
        if(material.properties().get("EmissionColorLayer4") instanceof Vector4f vec) emissionColor4.setColor(vec.x, vec.y, vec.z);
        else emissionColor4.resetColor();
        if(material.properties().get("EmissionColorLayer5") instanceof Vector4f vec) emissionColor5.setColor(vec.x, vec.y, vec.z);
        else emissionColor5.resetColor();

        if(material.properties().get("EmissionIntensityLayer1") instanceof Float intensity) emissionIntensity1.setValue(intensity);
        else emissionIntensity1.resetValue();
        if(material.properties().get("EmissionIntensityLayer2") instanceof Float intensity) emissionIntensity2.setValue(intensity);
        else emissionIntensity2.resetValue();
        if(material.properties().get("EmissionIntensityLayer3") instanceof Float intensity) emissionIntensity3.setValue(intensity);
        else emissionIntensity3.resetValue();
        if(material.properties().get("EmissionIntensityLayer4") instanceof Float intensity) emissionIntensity4.setValue(intensity);
        else emissionIntensity4.resetValue();

        if(targetPath != null) display(material, targetPath);

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
        return new MaskNode().setMask(mask).setColor(color.getRed(), color.getGreen(), color.getBlue());
    }

    public void display(ApiMaterial material, Path targetPath) {
        EyeTextureGenerator.generate(output.get(), targetPath.resolve(Path.of("debug", material.name(), "output.png")));
        EyeTextureGenerator.generate(baseMix1.get(), targetPath.resolve(Path.of("debug", material.name(), "baseMix1.png")));
        EyeTextureGenerator.generate(baseMix2.get(), targetPath.resolve(Path.of("debug", material.name(), "baseMix2.png")));
        EyeTextureGenerator.generate(baseMix3.get(), targetPath.resolve(Path.of("debug", material.name(), "baseMix3.png")));
        EyeTextureGenerator.generate(baseMix4.get(), targetPath.resolve(Path.of("debug", material.name(), "baseMix4.png")));
        EyeTextureGenerator.generate(emMix1.get(), targetPath.resolve(Path.of("debug", material.name(), "emMix1.png")));
        EyeTextureGenerator.generate(emMix2.get(), targetPath.resolve(Path.of("debug", material.name(), "emMix2.png")));
        EyeTextureGenerator.generate(emMix3.get(), targetPath.resolve(Path.of("debug", material.name(), "emMix3.png")));
        EyeTextureGenerator.generate(emMix4.get(), targetPath.resolve(Path.of("debug", material.name(), "emMix4.png")));

        EyeTextureGenerator.generate(base.get(), targetPath.resolve(Path.of("debug", material.name(), "base.png")));
        EyeTextureGenerator.generate(mask.get(), targetPath.resolve(Path.of("debug", material.name(), "mask.png")));
        EyeTextureGenerator.generate(maskGray.getInputData().get(), targetPath.resolve(Path.of("debug", material.name(), "maskGray.png")));

        EyeTextureGenerator.generate(baseColor1.get(), targetPath.resolve(Path.of("debug", material.name(), "baseColor1.png")));
        EyeTextureGenerator.generate(baseColor2.get(), targetPath.resolve(Path.of("debug", material.name(), "baseColor2.png")));
        EyeTextureGenerator.generate(baseColor3.get(), targetPath.resolve(Path.of("debug", material.name(), "baseColor3.png")));
        EyeTextureGenerator.generate(baseColor4.get(), targetPath.resolve(Path.of("debug", material.name(), "baseColor4.png")));
        EyeTextureGenerator.generate(emissionColor1.get(), targetPath.resolve(Path.of("debug", material.name(), "emissionColor1.png")));
        EyeTextureGenerator.generate(emissionColor2.get(), targetPath.resolve(Path.of("debug", material.name(), "emissionColor2.png")));
        EyeTextureGenerator.generate(emissionColor3.get(), targetPath.resolve(Path.of("debug", material.name(), "emissionColor3.png")));
        EyeTextureGenerator.generate(emissionColor4.get(), targetPath.resolve(Path.of("debug", material.name(), "emissionColor4.png")));
        EyeTextureGenerator.generate(emissionColor5.get(), targetPath.resolve(Path.of("debug", material.name(), "emissionColor5.png")));
        EyeTextureGenerator.generate(lymSplit.getRedChannel().getInputData().get(), targetPath.resolve(Path.of("debug", material.name(), "lymRed.png")));
        EyeTextureGenerator.generate(lymSplit.getGreenChannel().getInputData().get(), targetPath.resolve(Path.of("debug", material.name(), "lymGreen.png")));
        EyeTextureGenerator.generate(lymSplit.getBlueChannel().getInputData().get(), targetPath.resolve(Path.of("debug", material.name(), "lymBlue.png")));
        EyeTextureGenerator.generate(lymSplit.getAlphaChannel().getInputData().get(), targetPath.resolve(Path.of("debug", material.name(), "lymAlpha.png")));
    }

    class ConstantFacFunction implements FacFunction {
        private float value = 0;

        public ConstantFacFunction setValue(float value) {
            this.value = value;
            return this;
        }

        public ConstantFacFunction resetValue() {
            setValue(0);
            return this;
        }

        @Override
        public float fac(float x, float y) {
            return value;
        }
    }
}
