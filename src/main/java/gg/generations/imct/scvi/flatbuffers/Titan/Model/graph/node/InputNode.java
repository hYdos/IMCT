package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node;

import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.Nodes;

import java.awt.image.BufferedImage;

public interface InputNode {
    InputNode DEFAULT = new InputNode() {
        private InputData data = new InputData() {
            @Override
            public BufferedImage get() {
                return Nodes.DEFAULT_IMAGE;
            }

            @Override
            public int getColor(float x, float y) {
                return 0;
            }
        };

        @Override
        public InputData getInputData() {
            return data;
        }

        @Override
        public void addChangeListener(ChangeListener listener) {

        }
    };

    InputData getInputData();

    void addChangeListener(ChangeListener listener);
}
