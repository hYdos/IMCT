package gg.generations.imct.api;

import de.javagl.jgltf.model.impl.DefaultNodeModel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Model {

    public List<DefaultNodeModel> skeleton;
    public List<DefaultNodeModel> joints;
    public final List<Mesh> meshes = new ArrayList<>();
    public final Map<String, ApiMaterial> materials = new HashMap<>();

    protected ByteBuffer read(Path path) {
        try {
            return ByteBuffer.wrap(Files.readAllBytes(path));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read " + path.getFileName(), e);
        }
    }

    protected void processEyes(Path modelDir) {

    }
}
