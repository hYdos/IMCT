package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class CompositeNode extends BaseNode {
    private InputNode top = InputNode.DEFAULT;
    private InputNode bottom = InputNode.DEFAULT;

    private Composite composite = null;

    public CompositeNode() {
    }

    private final List<ChangeListener> listeners = new ArrayList<>();

    protected void process() {
        if (composite == null) {
            image = top.getInputData().get();
        } else {
            var bottomImage = bottom.getInputData().get();
            var topImage = bottom.getInputData().get();

            image = new BufferedImage(bottomImage.getWidth(), bottomImage.getHeight(), BufferedImage.TYPE_INT_ARGB);

            var graphics = image.createGraphics();
            graphics.drawImage(bottomImage, 0, 0, null);

            graphics.setComposite(composite);
            graphics.drawImage(topImage, 0, 0, null);
            graphics.dispose();
        }
    }

    public CompositeNode setTop(InputNode top) {
        this.top = top;
        top.addChangeListener(this);
        process();
        return this;
    }

    public CompositeNode setBottom(InputNode bottom) {
        this.bottom = bottom;
        bottom.addChangeListener(this);
        process();
        return this;
    }

    public CompositeNode setComposite(Composite composite) {
        this.composite = composite;
        process();
        return this;
    }
}
