package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph;

import gg.generations.imct.api.ApiMaterial;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.EyeGraph.LayersNode;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.composite.Composites;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.*;
import org.joml.Vector4f;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

public class FIreGraph {
    private final CompositeNode1 output;
    private final LayersNode base;
    private final LayersNode emission;
    private final ScaleNode lymScale;
    private final ScaleNode baseLayer;
    private MaskNode baseColor1;
    private MaskNode baseColor2;
    private MaskNode baseColor3;
    private MaskNode baseColor4;

    private MaskNode emissionColor1;
    private MaskNode emissionColor2;
    private MaskNode emissionColor3;
    private MaskNode emissionColor4;

    private TextureNode lym = new TextureNode();

    private TextureNode alb = new TextureNode();

    private ChannelSplitterNode lymSplit = new ChannelSplitterNode();

    public FIreGraph(int scale) {
        baseColor1 = new MaskNode();
        baseColor2 = new MaskNode();
        baseColor3 = new MaskNode();
        baseColor4 = new MaskNode();
        emissionColor1 = new MaskNode();
        emissionColor2 = new MaskNode();
        emissionColor3 = new MaskNode();
        emissionColor4 = new MaskNode();

        lymScale = new ScaleNode().setScale(scale).setInput(lym);

        lymSplit.setInput(lymScale);


        baseLayer = new ScaleNode().setScale(scale).setInput(alb);

        base = new LayersNode(
                baseLayer,
                baseColor1.setMask(lymSplit.getRedChannel()),
                baseColor2.setMask(lymSplit.getGreenChannel()),
                baseColor3.setMask(lymSplit.getBlueChannel()),
                baseColor4.setMask(lymSplit.getAlphaChannel())
        );

        emission = new gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.EyeGraph.LayersNode(
                emissionColor1.setMask(lymSplit.getRedChannel()),
                emissionColor2.setMask(lymSplit.getGreenChannel()),
                emissionColor3.setMask(lymSplit.getBlueChannel()),
                emissionColor4.setMask(lymSplit.getAlphaChannel())
        );

        output = new CompositeNode1().setComposite(Composites.SCREEN).setBottom(base).setTop(emission);
    }

    public BufferedImage update(ApiMaterial material, Path modelDir) {
        alb.setImage(Path.of(material.getTexture("BaseColorMap").filePath()));

        var lymImage = material.getTexture("LayerMaskMap");

        var image = alb.get();

        if(lymImage == null) return image;

        lym.setImage(Path.of(lymImage.filePath()));
        image = lym.get();
        int width = image.getWidth();
        int height = image.getHeight();

        lymScale.setScale(width, height);
        baseLayer.setScale(width, height);


        if (material.properties().get("BaseColorLayer1") instanceof Vector4f vec) {
            baseColor1.setColor(vec.x, vec.y, vec.z);
        }
        else baseColor1.resetColor();
        if (material.properties().get("BaseColorLayer2") instanceof Vector4f vec) {
            baseColor2.setColor(vec.x, vec.y, vec.z);
        }
        else baseColor2.resetColor();
        if (material.properties().get("BaseColorLayer3") instanceof Vector4f vec)
            baseColor3.setColor(vec.x, vec.y, vec.z);
        else baseColor3.resetColor();
        if (material.properties().get("BaseColorLayer4") instanceof Vector4f vec)
            baseColor4.setColor(vec.x, vec.y, vec.z);
        else baseColor4.resetColor();

//        emissionColor1.resetColor().resetIntensity();

        if (material.properties().get("EmissionColorLayer1") instanceof Vector4f vec)
            emissionColor1.setColor(vec.x, vec.y, vec.z);
        else emissionColor1.resetColor();
        if (material.properties().get("EmissionColorLayer2") instanceof Vector4f vec)
            emissionColor2.setColor(vec.x, vec.y, vec.z);
        else emissionColor2.resetColor();
        if (material.properties().get("EmissionColorLayer3") instanceof Vector4f vec)
            emissionColor3.setColor(vec.x, vec.y, vec.z);
        else emissionColor3.resetColor();
        if (material.properties().get("EmissionColorLayer4") instanceof Vector4f vec)
            emissionColor4.setColor(vec.x, vec.y, vec.z);
        else emissionColor4.resetColor();

//        if (material.properties().get("EmissionIntensityLayer1") instanceof Float vec)
//            emissionColor1.setIntensity(vec);
//        else emissionColor1.resetIntensity();
//        if (material.properties().get("EmissionIntensityLayer2") instanceof Float vec)
//            emissionColor2.setIntensity(vec);
//        else emissionColor2.resetIntensity();
//        if (material.properties().get("EmissionIntensityLayer3") instanceof Float vec)
//            emissionColor3.setIntensity(vec);
//        else emissionColor3.resetIntensity();
//        if (material.properties().get("EmissionIntensityLayer4") instanceof Float vec)
//            emissionColor4.setIntensity(vec);
//        else emissionColor4.resetIntensity();

        display();

        return output.get();

//        return emission.get();
    }

    public void display() {
        EyeTextureGenerator.displayImage(output.get(), "output");
        EyeTextureGenerator.displayImage(base.get(), "base");
        EyeTextureGenerator.displayImage(emission.get(), "emission");
        EyeTextureGenerator.displayImage(baseColor1.get(), "baseColor1");
        EyeTextureGenerator.displayImage(baseColor2.get(), "baseColor2");
        EyeTextureGenerator.displayImage(baseColor3.get(), "baseColor3");
        EyeTextureGenerator.displayImage(baseColor4.get(), "baseColor4");
        EyeTextureGenerator.displayImage(emissionColor1.get(), "emissionColor1");
        EyeTextureGenerator.displayImage(emissionColor2.get(), "emissionColor2");
        EyeTextureGenerator.displayImage(emissionColor3.get(), "emissionColor3");
        EyeTextureGenerator.displayImage(emissionColor4.get(), "emissionColor4");}
}