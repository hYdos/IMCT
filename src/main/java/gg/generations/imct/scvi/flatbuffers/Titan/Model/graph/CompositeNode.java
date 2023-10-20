package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph;

import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.BaseNode;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.InputNode;

import java.awt.image.BufferedImage;

public class CompositeNode extends BaseNode {
    private final BlendFunction function;
    private final InputNode top;
    private final InputNode bottom;

    private final FacFunction facFunction;

    public CompositeNode(BlendFunction function, InputNode top, InputNode bottom, FacFunction facFunction) {
        this.function = function;
        this.top = top;
        top.addChangeListener(this);
        this.bottom = bottom;
        bottom.addChangeListener(this);
        this.facFunction = facFunction;
        update();
    }

    @Override
    protected void process() {
        int width = Math.max(top.getInputData().get().getWidth(), bottom.getInputData().get().getWidth());
        int height = Math.max(top.getInputData().get().getHeight(), bottom.getInputData().get().getHeight());

        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        System.out.println("(%s,%s) (%s,%s) (%s,%s)".formatted(top.getInputData().get().getWidth(), top.getInputData().get().getHeight(), bottom.getInputData().get().getWidth(), bottom.getInputData().get().getHeight(), width, height));

        mixImagesPerPixel();
    }

    public void mixImagesPerPixel() {
        var widthIncrement = 1f / image.getWidth();
        var heightIncrement = 1f / image.getHeight();

        for (float x = 0; x < 1f; x += widthIncrement) {
            for (float y = 0; y < 1f; y += heightIncrement) {

                float factor = facFunction.fac(x, y);

//                System.out.println(factor);

                int rgbA = top.getInputData().getColor(x, y);
                int rgbB = bottom.getInputData().getColor(x, y);

                int alphaA = ((rgbA >> 24) & 0xFF);
                int alphaB = ((rgbB >> 24) & 0xFF);

                int redA = ((rgbA >> 16) & 0xFF);
                int redB = ((rgbB >> 16) & 0xFF);

                int greenA = ((rgbA >> 8) & 0xFF);
                int greenB = ((rgbB >> 8) & 0xFF);

                int blueA = (rgbA & 0xFF);
                int blueB = (rgbB & 0xFF);

                int alphaMixed = 255; //lerp(alphaA, alphaB, factor);
                int redMixed = lerp(redA, redB, factor);
                int greenMixed = lerp(greenA, greenB, factor);
                int blueMixed = lerp(blueA, blueB, factor);

                int mixedRGB = (alphaMixed << 24) | (redMixed << 16) | (greenMixed << 8) | blueMixed;

                image.setRGB((int) (x * image.getWidth()), (int) (y * image.getHeight()), mixedRGB);
            }
        }
    }


    private int lerp(int a, int b, float factor) {
        var blend = function.blend(a, b);

        return (int) ((1 - factor) * a + factor * blend);
    }
}

