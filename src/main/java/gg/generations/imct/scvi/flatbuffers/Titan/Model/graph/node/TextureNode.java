package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node;

import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.Nodes;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TextureNode extends BaseNode {
    private Path path = null;
    public TextureNode() {
    }

    protected void process() {
        image = Nodes.DEFAULT_IMAGE;

        if (path == null) return;

        try {
            image = ImageIO.read(path.toFile());
        } catch (IOException ignored) {
        }
    }

    public TextureNode setImage(Path path) {
        this.path = path;
        update();
        return this;
    }
}
