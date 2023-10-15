package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node;

import java.awt.image.BufferedImage;

public class UniteNode extends BaseNode {
    InputNode left = DEFAULT;

    InputNode right = DEFAULT;
    @Override
    protected void process() {
        var left = this.left.getInputData().get();
        var right = this.right.getInputData().get();

        image = new BufferedImage(left.getWidth() * 2, left.getHeight(), BufferedImage.TYPE_INT_ARGB);

        image.getGraphics().drawImage(left, 0, 0, left.getWidth(), left.getHeight(), null);
        image.getGraphics().drawImage(right, left.getWidth(), 0, right.getWidth(), right.getHeight(), null);
    }

    public UniteNode setRight(InputNode input) {
        this.right = input;
        input.addChangeListener(this);
        update();
        return this;
    }

    public UniteNode setLeft(InputNode input) {
        this.left = input;
        input.addChangeListener(this);
        update();
        return this;
    }
}
