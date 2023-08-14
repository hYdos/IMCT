package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node;

import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.Nodes;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseNode implements InputNode, InputData, ChangeListener {
    protected BufferedImage image = Nodes.DEFAULT_IMAGE;

    private final List<ChangeListener> listeners = new ArrayList<>();

    protected abstract void process();

    @Override
    public BufferedImage get() {
        return image;
    }

    @Override
    public InputData getInputData() {
        return this;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void onChange() {
        listeners.forEach(ChangeListener::onChange);
    }

    protected void update() {
        process();
        listeners.forEach(ChangeListener::onChange);
    }

}
