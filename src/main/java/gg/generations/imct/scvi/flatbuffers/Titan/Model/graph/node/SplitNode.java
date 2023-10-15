package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node;

import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.Nodes;

import java.awt.image.BufferedImage;

public class SplitNode implements InputNode, ChangeListener {
    private InputNode input;
    private final ChannelNode right = new ChannelNode();
    private final ChannelNode left = new ChannelNode();

    public SplitNode() {
        updateImage(null);
    }

    private void updateImage(BufferedImage image) {
        if (image != null && image.getWidth() > 1) {
            right.setImage(image.getSubimage(0, 0, image.getWidth() / 2, image.getHeight()));
            left.setImage(image.getSubimage(image.getWidth() / 2, 0, image.getWidth() / 2, image.getHeight()));
        } else {
            right.setImage(Nodes.DEFAULT_IMAGE);
            left.setImage(Nodes.DEFAULT_IMAGE);
        }
    }

    public SplitNode setInput(InputNode input) {
        this.input = input;
        updateImage(input.getInputData().get());
        input.addChangeListener(this);

        right.onChange();
        left.onChange();

        return this;
    }

    @Override
    public InputData getInputData() {
        return null;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
    }

    @Override
    public void onChange() {
        updateImage(input.getInputData().get());
        right.onChange();
        left.onChange();
    }

    public InputNode getRight() {
        return right;
    }

    public InputNode getLeft() {
        return left;
    }

    private static class ChannelNode extends BaseNode {

        public ChannelNode() {
        }

        @Override
        protected void process() {}

        public void setImage(BufferedImage image) {
            this.image = image;
            update();
        }
    }
}
