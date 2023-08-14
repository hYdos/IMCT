package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node;

import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.Nodes;

import java.awt.image.BufferedImage;

public interface InputData {

    BufferedImage get();

    default void display(String name) {
        Nodes.displayImage(get(), name);
    }
}
