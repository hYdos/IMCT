package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node;

import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.Nodes;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TextureNode implements InputNode, InputData {
    private Path path = null;
    public BufferedImage color = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    public TextureNode() {
        updateImage();
    }

    private void updateImage() {
        color = Nodes.DEFAULT_IMAGE;

        if (path == null) return;

        try {
            color = ImageIO.read(path.toFile());
        } catch (IOException ignored) {
        }
    }

    public TextureNode setImage(Path path) {
        this.path = path;
        updateImage();

        listeners.forEach(ChangeListener::onChange);

        return this;
    }

    private final List<ChangeListener> listeners = new ArrayList<>();

    public InputData getInputData() {
        return this;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public BufferedImage get() {
        return color;
    }
}
