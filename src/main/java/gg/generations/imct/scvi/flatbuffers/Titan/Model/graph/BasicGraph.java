package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph;

import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.ChannelSplitterNode;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.TextureNode;

import java.nio.file.Path;

public class BasicGraph {
    public static void main(String[] args) {
        var texture = new TextureNode().setImage(Path.of("C:\\Users\\water\\Downloads\\pm0108_00_00\\pm0108_00_00_eye_lym.png"));

        var channelSplitter = new ChannelSplitterNode().setInput(texture);

        channelSplitter.getRedChannel().getInputData().display("Red");
        channelSplitter.getGreenChannel().getInputData().display("Green");
        channelSplitter.getBlueChannel().getInputData().display("Blue");
        channelSplitter.getAlphaChannel().getInputData().display("Alpha");
    }
}
