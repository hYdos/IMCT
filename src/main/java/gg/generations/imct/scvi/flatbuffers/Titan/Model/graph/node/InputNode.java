package gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.node;

import gg.generations.imct.scvi.flatbuffers.Titan.Model.graph.Nodes;

public interface InputNode {
    InputNode DEFAULT = new InputNode() {
        private InputData data = () -> Nodes.DEFAULT_IMAGE;

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
