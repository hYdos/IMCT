package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.composite;

import java.awt.*;

public class Composites {
    public static final Composite ADD = new AddComposite();
    public static final Composite MULTIPLY = new MultiplyComposite();
    public static final Composite SCREEN = new ScreenComposite();

    public static final Composite COLOR_MASK = new MaskColoringComposite();
}
