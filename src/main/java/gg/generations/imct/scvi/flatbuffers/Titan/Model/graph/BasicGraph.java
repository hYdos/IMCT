package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph;

import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.composite.Composites;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.ChannelSplitterNode;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.CompositeNode;
import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node.TextureNode;

import java.awt.*;
import java.nio.file.Path;

public class BasicGraph {
    public static void main(String[] args) {
        var texture = new TextureNode().setImage(Path.of("C:\\Users\\water\\Downloads\\pm0108_00_00\\pm0108_00_00_eye_lym.png"));
//        texture.display("rawr");
//        var lymScale = new ScaleNode().setInput(texture).setScale(128);

        var color = new ColorNode().setSize(128, 128).setColor(1.0f, 0, 0, 1);

//        color.display("RawreRawr");

        var channelSplitter = new ChannelSplitterNode().setInput(new ScaleNode().setScale(128).setInput(texture));

        channelSplitter.getRedChannel().getInputData().display("RedChannel");
        channelSplitter.getGreenChannel().getInputData().display("GreenChannel");
        channelSplitter.getBlueChannel().getInputData().display("BlueChannel");
        channelSplitter.getAlphaChannel().getInputData().display("AlphaChannel");

        channelSplitter.getBlueChannel().getInputData().display("rawr");

        new MaskNode().setMask(channelSplitter.getRedChannel()).setColor(3, 3, 3).getInputData().display("Red");
//        new CompositeNode().setComposite(BlendComposite.Multiply).setTop(color).setBottom(channelSplitter.getGreenChannel()).getInputData().display("Green");
//        new CompositeNode().setComposite(BlendComposite.Difference).setTop(color).setBottom(channelSplitter.getBlueChannel()).getInputData().display("Blue");
//        new CompositeNode().setComposite(Composites.SCREEN).setTop(color).setBottom(channelSplitter.getAlphaChannel()).getInputData().display("Alpha");
    }
}
