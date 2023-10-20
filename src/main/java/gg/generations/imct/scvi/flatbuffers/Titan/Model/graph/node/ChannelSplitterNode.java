package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node;

import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.Nodes;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ChannelSplitterNode implements InputNode, ChangeListener {
    private InputNode input = InputNode.DEFAULT;
    private final ChannelNode redChannel = new ChannelNode();
    private final ChannelNode greenChannel = new ChannelNode();
    private final ChannelNode blueChannel = new ChannelNode();
    private final ChannelNode alphaChannel = new ChannelNode();

    public ChannelSplitterNode() {
        updateImage(null);
    }

    private void updateImage(BufferedImage image) {
        if (image != null) {
            var channels = Nodes.splitImageChannels(image);
            redChannel.setImage(channels.redPath());
            greenChannel.setImage(channels.greenPath());
            blueChannel.setImage(channels.bluePath());
            alphaChannel.setImage(channels.alphaPath());
        } else {
            redChannel.setImage(Nodes.DEFAULT_IMAGE);
            greenChannel.setImage(Nodes.DEFAULT_IMAGE);
            blueChannel.setImage(Nodes.DEFAULT_IMAGE);
            alphaChannel.setImage(Nodes.DEFAULT_IMAGE);
        }
    }

    public ChannelSplitterNode setInput(InputNode input) {
        this.input = input;
        updateImage(input.getInputData().get());
        input.addChangeListener(this);

        redChannel.onChange();
        greenChannel.onChange();
        redChannel.onChange();
        alphaChannel.onChange();

        return this;
    }

    @Override
    public InputData getInputData() {
        return input.getInputData();
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
    }

    @Override
    public void onChange() {
        updateImage(input.getInputData().get());
        redChannel.onChange();
        greenChannel.onChange();
        redChannel.onChange();
        alphaChannel.onChange();
    }

    public InputNode getAlphaChannel() {
        return alphaChannel;
    }

    public InputNode getBlueChannel() {
        return blueChannel;
    }

    public InputNode getGreenChannel() {
        return greenChannel;
    }

    public InputNode getRedChannel() {
        return redChannel;
    }

    private static class ChannelNode extends BaseNode {

        public ChannelNode() {
        }

        @Override
        protected void process() {}

        @Override
        public int getColor(float x, float y) {
            var val = super.getColor(x, y);
            return val & 0xff;
        }

        public void setImage(BufferedImage image) {
            this.image = image;
            update();
        }
    }
}
